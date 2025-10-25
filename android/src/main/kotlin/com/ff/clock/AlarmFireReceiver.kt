package com.ff.clock

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat

class AlarmFireReceiver : BroadcastReceiver() {

  override fun onReceive(context: Context, intent: Intent) {
    val id = intent.getIntExtra("id", 0)
    val title = intent.getStringExtra("title") ?: "Alarm"
    val text = intent.getStringExtra("text") ?: ""

    val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val channelId = "alarm_clock_channel"

    // Create channel (ALARM usage, high importance, plays alarm sound)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val channel = NotificationChannel(
        channelId,
        "Alarms",
        NotificationManager.IMPORTANCE_HIGH
      ).apply {
        description = "Alarm notifications"
        setSound(
          RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM),
          AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .build()
        )
        lockscreenVisibility = Notification.VISIBILITY_PUBLIC
      }
      nm.createNotificationChannel(channel)
    }

    // Full-screen intent to bring app to front when ringing
    val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
    val fullScreenPI = PendingIntent.getActivity(
      context, id, launchIntent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val smallIcon = try {
      // Prefer the host app icon
      context.applicationInfo.icon.takeIf { it != 0 } ?: android.R.drawable.ic_lock_idle_alarm
    } catch (_: Exception) {
      android.R.drawable.ic_lock_idle_alarm
    }

    val notif = NotificationCompat.Builder(context, channelId)
      .setSmallIcon(smallIcon)
      .setContentTitle(title)
      .setContentText(text)
      .setCategory(NotificationCompat.CATEGORY_ALARM)
      .setPriority(NotificationCompat.PRIORITY_MAX)
      .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
      .setAutoCancel(true)
      .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
      .setFullScreenIntent(fullScreenPI, true)
      .build()

    nm.notify(id, notif)
  }
}