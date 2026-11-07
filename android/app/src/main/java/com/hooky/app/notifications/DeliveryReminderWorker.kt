package com.hooky.app.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.hooky.app.MainActivity
import com.hooky.app.R
import com.hooky.app.data.db.dao.PieceDao
import com.hooky.app.util.DeliveryReminders
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit

/** Daily check (about 9:00) for delivery dates: "due in N days" and "due today" notifications. */
class DeliveryReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface Deps { fun pieceDao(): PieceDao }

    override suspend fun doWork(): Result {
        val ctx = applicationContext
        if (!canNotify(ctx)) return Result.success()
        val dao = EntryPointAccessors.fromApplication(ctx, Deps::class.java).pieceDao()
        val today = LocalDate.now()
        val prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val sent = DeliveryReminders.prune(prefs.getStringSet(KEY_SENT, emptySet()).orEmpty(), today)

        val deliveries = dao.getAllNow().map {
            DeliveryReminders.Delivery(it.id, it.name, it.dueDate, it.destination, it.archived)
        }
        val reminders = DeliveryReminders.due(deliveries, today, sent)
        if (reminders.isNotEmpty()) ensureChannel(ctx)
        reminders.forEach { show(ctx, it) }

        prefs.edit().putStringSet(KEY_SENT, sent + reminders.map { it.key }).apply()
        return Result.success()
    }

    private fun show(ctx: Context, r: DeliveryReminders.Reminder) {
        val text = when (r.kind) {
            DeliveryReminders.Kind.TODAY -> ctx.getString(R.string.notif_delivery_today)
            DeliveryReminders.Kind.SOON -> ctx.resources.getQuantityString(
                R.plurals.notif_delivery_soon, r.daysLeft.toInt(), r.daysLeft.toInt()
            )
        }
        val open = PendingIntent.getActivity(
            ctx, r.id,
            Intent(ctx, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                .putExtra(MainActivity.EXTRA_OPEN_PIECE_ID, r.id),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(ctx, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(ContextCompat.getColor(ctx, R.color.brand_purple))
            .setContentTitle(r.name)
            .setContentText(text)
            .setContentIntent(open)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        try {
            NotificationManagerCompat.from(ctx).notify(NOTIF_BASE + r.id, notification)
        } catch (_: SecurityException) { /* permission revoked meanwhile */ }
    }

    companion object {
        private const val WORK_NAME = "delivery_reminders"
        private const val CHANNEL_ID = "deliveries"
        private const val PREFS = "hooky_reminders"
        private const val KEY_SENT = "sent"
        private const val NOTIF_BASE = 10_000
        private val RUN_AT: LocalTime = LocalTime.of(9, 0)

        fun canNotify(ctx: Context): Boolean =
            (Build.VERSION.SDK_INT < 33 ||
                ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) &&
                NotificationManagerCompat.from(ctx).areNotificationsEnabled()

        /** Call on every app start; keeps the existing schedule if there is one. */
        fun schedule(ctx: Context) {
            val now = LocalDateTime.now()
            var next = now.toLocalDate().atTime(RUN_AT)
            if (!next.isAfter(now)) next = next.plusDays(1)
            val request = PeriodicWorkRequestBuilder<DeliveryReminderWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(Duration.between(now, next).toMinutes(), TimeUnit.MINUTES)
                .build()
            WorkManager.getInstance(ctx).enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
        }

        /** Debug: run the check at once; [resetSent] forgets reminders already shown. */
        fun runNow(ctx: Context, resetSent: Boolean = false) {
            if (resetSent) ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().remove(KEY_SENT).apply()
            WorkManager.getInstance(ctx).enqueue(androidx.work.OneTimeWorkRequestBuilder<DeliveryReminderWorker>().build())
        }

        private fun ensureChannel(ctx: Context) {
            if (Build.VERSION.SDK_INT < 26) return
            val channel = NotificationChannel(
                CHANNEL_ID, ctx.getString(R.string.notif_channel_deliveries), NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = ctx.getString(R.string.notif_channel_deliveries_desc) }
            ctx.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }
}
