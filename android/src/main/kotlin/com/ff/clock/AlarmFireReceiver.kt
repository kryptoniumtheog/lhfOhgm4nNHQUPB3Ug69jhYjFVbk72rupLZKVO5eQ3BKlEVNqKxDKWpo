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
    val text  = intent.getStringExtra("text") ?: ""
    val type  = intent.getStringExtra("type") ?: "alarm" // default
    val snoozeMin = intent.getIntExtra(FFConst.EXTRA_SNOOZE_MIN, 10)

    if (type == "reminder") {
      // Dismissible, short sound, actions
      val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val ch = NotificationChannel(
          FFConst.CH_REMINDER, "Reminders", NotificationManager.IMPORTANCE_HIGH
        ).apply {
          description = "Reminder notifications"
          setSound(
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
            AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION).build()
          )
        }
        nm.createNotificationChannel(ch)
      }

      val skipPI = PendingIntent.getBroadcast(
        context, id, Intent(context, ActionReceiver::class.java).apply {
          action = FFConst.ACT_STOP; putExtra(FFConst.EXTRA_ID, id)
        }, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
      )
      val snoozePI = PendingIntent.getBroadcast(
        context, id+1, Intent(context, ActionReceiver::class.java).apply {
          action = FFConst.ACT_SNOOZE
          putExtra(FFConst.EXTRA_ID, id)
          putExtra(FFConst.EXTRA_SNOOZE_MIN, snoozeMin)
        }, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
      )

      val notif = NotificationCompat.Builder(context, FFConst.CH_REMINDER)
        .setSmallIcon(android.R.drawable.ic_popup_reminder)
        .setContentTitle(title)
        .setContentText(text)
        .setAutoCancel(true)                 // <— dismissible, short
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .addAction(0, "Skip", skipPI)
        .addAction(0, "Snooze", snoozePI)
        .build()

      nm.notify(id, notif)
      return
    }

    // === Alarm mode: start foreground service (keeps ringing until user presses a button)
    val svc = Intent(context, AlarmForegroundService::class.java).apply {
      putExtra(FFConst.EXTRA_ID, id)
      putExtra(FFConst.EXTRA_TITLE, title)
      putExtra(FFConst.EXTRA_TEXT, text)
      putExtra(FFConst.EXTRA_SNOOZE_MIN, snoozeMin)
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      context.startForegroundService(svc)
    } else {
      context.startService(svc)
    }
  }
}