package com.hooky.app

import android.app.Application
import com.hooky.app.notifications.DeliveryReminderWorker
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class CrochetApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        DeliveryReminderWorker.schedule(this)
    }
}
