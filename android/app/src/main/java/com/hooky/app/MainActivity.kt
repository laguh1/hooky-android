package com.hooky.app

import android.os.Bundle
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

    override fun onCreate(savedInstanceState: Bundle?) {
        val savedMode = getSharedPreferences("hooky_settings", MODE_PRIVATE)
            .getInt("night_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        AppCompatDelegate.setDefaultNightMode(savedMode)

        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        lifecycleScope.launch { premiumManager.initialize() }

        setContent {
            CrochetManagerTheme {
                CrochetNavGraph()
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
}
