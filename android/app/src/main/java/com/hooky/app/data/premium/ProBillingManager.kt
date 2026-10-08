package com.hooky.app.data.premium

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

// Play Console → Monetise with Play → Subscriptions. Pro is a subscription product with a
// single PREPAID base plan (12 months, no auto-renewal). Buying it again while active is a
// top-up that extends the end date — Play handles that, no special flow needed here.
const val PRO_PRODUCT_ID = "hooky_pro"
private const val PRO_BASE_PLAN_ID = "yearly"

private const val PREFS_NAME = "hooky_pro_billing"
private const val KEY_HAS_PRO = "has_pro"

/** What the user can buy right now, with the price already formatted in their currency. */
class ProOffer internal constructor(
    val formattedPrice: String,
    internal val productDetails: ProductDetails,
    internal val offerToken: String
)

enum class ProPurchaseEvent { PURCHASED, PENDING, CANCELLED, FAILED }

@Singleton
class ProBillingManager @Inject constructor(
    @ApplicationContext context: Context
) : PurchasesUpdatedListener {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // Cached so Pro keeps working offline; only a successful purchases query changes it.
    private val _hasPro = MutableStateFlow(prefs.getBoolean(KEY_HAS_PRO, false))
    val hasProFlow: StateFlow<Boolean> = _hasPro.asStateFlow()

    private val _offer = MutableStateFlow<ProOffer?>(null)
    val offerFlow: StateFlow<ProOffer?> = _offer.asStateFlow()

    private val _events = MutableSharedFlow<ProPurchaseEvent>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val events: SharedFlow<ProPurchaseEvent> = _events.asSharedFlow()

    private val connectMutex = Mutex()

    private val client: BillingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .enablePrepaidPlans()
                .build()
        )
        .enableAutoServiceReconnection()
        .build()

    /** Re-reads what the user owns and what is on sale. Safe to call often; no-op offline. */
    suspend fun refresh() {
        if (!ensureConnected()) return
        queryPurchases()
        queryOffer()
    }

    /** Opens Google Play's purchase sheet. Returns false if there is nothing to buy yet. */
    fun launchPurchase(activity: Activity): Boolean {
        val offer = _offer.value ?: return false
        val params = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(offer.productDetails)
                        .setOfferToken(offer.offerToken)
                        .build()
                )
            )
            .build()
        return client.launchBillingFlow(activity, params).isOk()
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        when (result.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                val pro = purchases.orEmpty().filter { PRO_PRODUCT_ID in it.products }
                when {
                    pro.any { it.purchaseState == Purchase.PurchaseState.PURCHASED } -> {
                        setHasPro(true)
                        acknowledge(pro)
                        _events.tryEmit(ProPurchaseEvent.PURCHASED)
                    }
                    pro.any { it.purchaseState == Purchase.PurchaseState.PENDING } ->
                        _events.tryEmit(ProPurchaseEvent.PENDING)
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED ->
                _events.tryEmit(ProPurchaseEvent.CANCELLED)
            else -> _events.tryEmit(ProPurchaseEvent.FAILED)
        }
    }

    private suspend fun ensureConnected(): Boolean = connectMutex.withLock {
        if (client.isReady) return@withLock true
        suspendCancellableCoroutine { cont ->
            client.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(result: BillingResult) {
                    if (cont.isActive) cont.resume(result.isOk())
                }

                override fun onBillingServiceDisconnected() {
                    if (cont.isActive) cont.resume(false)
                }
            })
        }
    }

    private suspend fun queryPurchases() {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()
        val purchases: List<Purchase>? = suspendCancellableCoroutine { cont ->
            client.queryPurchasesAsync(params) { result, list ->
                if (cont.isActive) cont.resume(if (result.isOk()) list else null)
            }
        }
        // null = the query itself failed; keep the cached entitlement rather than revoke it.
        purchases ?: return
        val active = purchases.filter {
            PRO_PRODUCT_ID in it.products && it.purchaseState == Purchase.PurchaseState.PURCHASED
        }
        setHasPro(active.isNotEmpty())
        acknowledge(active)
    }

    private suspend fun queryOffer() {
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(PRO_PRODUCT_ID)
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build()
                )
            )
            .build()
        val details: ProductDetails? = suspendCancellableCoroutine { cont ->
            client.queryProductDetailsAsync(params) { result, response ->
                val found = if (result.isOk()) response.productDetailsList.firstOrNull() else null
                if (cont.isActive) cont.resume(found)
            }
        }
        val plans = details?.subscriptionOfferDetails.orEmpty()
        val plan = plans.firstOrNull { it.basePlanId == PRO_BASE_PLAN_ID } ?: plans.firstOrNull()
        val price = plan?.pricingPhases?.pricingPhaseList?.lastOrNull()?.formattedPrice
        _offer.value = if (details != null && plan != null && price != null) {
            ProOffer(price, details, plan.offerToken)
        } else {
            null
        }
    }

    // Play refunds and revokes a purchase that isn't acknowledged within 3 days.
    private fun acknowledge(purchases: List<Purchase>) {
        purchases
            .filter { it.purchaseState == Purchase.PurchaseState.PURCHASED && !it.isAcknowledged }
            .forEach { purchase ->
                val params = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                client.acknowledgePurchase(params) { /* retried on the next refresh() if it failed */ }
            }
    }

    private fun setHasPro(value: Boolean) {
        _hasPro.value = value
        prefs.edit().putBoolean(KEY_HAS_PRO, value).apply()
    }
}

private fun BillingResult.isOk() = responseCode == BillingClient.BillingResponseCode.OK
