package com.hooky.app

import android.content.Intent
import android.os.Bundle
import androidx.compose.runtime.mutableStateOf
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.hooky.app.data.premium.PremiumManager
import com.hooky.app.ui.navigation.CrochetNavGraph
import com.hooky.app.ui.theme.CrochetManagerTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject lateinit var premiumManager: PremiumManager

    // Piece to open when the app is started from a delivery reminder
    private val openPieceId = mutableStateOf<Int?>(null)

    private fun readOpenPiece(intent: Intent?) {
        intent?.getIntExtra(EXTRA_OPEN_PIECE_ID, -1)?.takeIf { it > 0 }?.let { openPieceId.value = it }
        intent?.removeExtra(EXTRA_OPEN_PIECE_ID)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        readOpenPiece(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val savedMode = getSharedPreferences("hooky_settings", MODE_PRIVATE)
            .getInt("night_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        AppCompatDelegate.setDefaultNightMode(savedMode)

        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        lifecycleScope.launch { premiumManager.initialize() }
        readOpenPiece(intent)   // the extra is removed once read, so rotation does not reopen it

        setContent {
            CrochetManagerTheme {
                CrochetNavGraph(
                    openPieceId = openPieceId.value,
                    onOpenPieceHandled = { openPieceId.value = null }
                )
            }
        }
    }

    private var resumedOnce = false

    override fun onResume() {
        super.onResume()
        // onCreate already checks purchases via initialize(); this covers coming back to
        // the app later (purchase finished in Play, or the paid year ran out meanwhile).
        if (resumedOnce) lifecycleScope.launch { premiumManager.refreshPurchases() }
        resumedOnce = true
    }

    companion object {
        const val EXTRA_OPEN_PIECE_ID = "open_piece_id"
    }
}
