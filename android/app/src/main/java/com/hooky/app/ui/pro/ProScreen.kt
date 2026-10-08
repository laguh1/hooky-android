package com.hooky.app.ui.pro

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hooky.app.R
import com.hooky.app.data.premium.ProPurchaseEvent
import com.hooky.app.data.premium.ProStatus
import com.hooky.app.ui.theme.BrandPurple
import com.hooky.app.ui.theme.Slate
import com.hooky.app.ui.theme.TextSecondary
import com.hooky.app.ui.theme.White
import com.hooky.app.util.toDisplayDate

@Composable
fun ProScreen(
    onNavigateBack: () -> Unit,
    viewModel: ProViewModel = hiltViewModel()
) {
    val status by viewModel.status.collectAsState()
    val offer by viewModel.offer.collectAsState()
    val isLoadingOffer by viewModel.isLoadingOffer.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val purchasedMessage = stringResource(R.string.pro_event_purchased)
    val pendingMessage = stringResource(R.string.pro_event_pending)
    val failedMessage = stringResource(R.string.pro_event_failed)
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                ProPurchaseEvent.PURCHASED -> snackbarHostState.showSnackbar(purchasedMessage)
                ProPurchaseEvent.PENDING -> snackbarHostState.showSnackbar(pendingMessage)
                ProPurchaseEvent.FAILED -> snackbarHostState.showSnackbar(failedMessage)
                ProPurchaseEvent.CANCELLED -> Unit
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.action_back),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = stringResource(R.string.pro_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.pro_tagline),
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary
                )

                statusText(status)?.let { StatusCard(it) }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FeatureRow(stringResource(R.string.pro_feature_reports))
                        FeatureRow(stringResource(R.string.pro_feature_customers))
                        FeatureRow(stringResource(R.string.pro_feature_counters))
                        FeatureRow(stringResource(R.string.pro_feature_photos))
                    }
                }

                val currentOffer = offer
                when {
                    currentOffer != null -> {
                        val label = if (status == ProStatus.Purchased) {
                            stringResource(R.string.pro_extend_button, currentOffer.formattedPrice)
                        } else {
                            stringResource(R.string.pro_buy_button, currentOffer.formattedPrice)
                        }
                        Button(
                            onClick = { context.findActivity()?.let(viewModel::buy) },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Slate,
                                contentColor = White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 52.dp)
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center
                            )
                        }
                        Text(
                            text = stringResource(R.string.pro_payment_note),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    isLoadingOffer -> {
                        CircularProgressIndicator(
                            color = Slate,
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .size(28.dp)
                        )
                    }
                    else -> {
                        Text(
                            text = stringResource(R.string.pro_unavailable),
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedButton(
                            onClick = viewModel::refresh,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.pro_retry))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun statusText(status: ProStatus): String? = when (status) {
    ProStatus.None -> null
    ProStatus.Purchased -> stringResource(R.string.pro_status_purchased)
    ProStatus.Gifted -> stringResource(R.string.pro_status_gifted)
    is ProStatus.Trial ->
        stringResource(R.string.pro_status_trial, status.lastDay.toString().toDisplayDate())
    is ProStatus.EarlyAdopter -> status.lastDay
        ?.let { stringResource(R.string.pro_status_early, it.toString().toDisplayDate()) }
        ?: stringResource(R.string.pro_status_early_lifetime)
}

@Composable
private fun StatusCard(text: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, BrandPurple),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = BrandPurple,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun FeatureRow(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.Check,
            contentDescription = null,
            tint = BrandPurple,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun Context.findActivity(): Activity? {
    var current = this
    while (current is ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return null
}
