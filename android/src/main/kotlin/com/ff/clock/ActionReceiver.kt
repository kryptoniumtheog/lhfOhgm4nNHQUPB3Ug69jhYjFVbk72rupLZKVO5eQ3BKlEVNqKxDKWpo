package com.ff.clock

import android.content.*
import android.app.*
import android.os.*
import io.flutter.Log

class ActionReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent) {
    val id = intent.getIntExtra(FFConst.EXTRA_ID, 0)
    FileLogger.log(context, "ActionReceiver ${intent.action} id=$id")
    when (intent.action) {
      FFConst.ACT_STOP -> {
        // Stop service & clear notif
        FileLogger.log(context, "ActionReceiver STOP id=$id")
        context.stopService(Intent(context, AlarmForegroundService::class.java))
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.cancel(id)
      }
      FFConst.ACT_SNOOZE -> {
        FileLogger.log(context, "ActionReceiver SNOOZE id=$id")
        context.stopService(Intent(context, AlarmForegroundService::class.java))
        val minutes = intent.getIntExtra(FFConst.EXTRA_SNOOZE_MIN, 10)
        val whenMs = System.currentTimeMillis() + minutes * 60_000L

        // Reuse plugin's AlarmManager scheduling for a future alarm
        val pluginIntent = Intent(context, AlarmFireReceiver::class.java).apply {
          putExtra("id", id)
          putExtra("title", "Snoozed Alarm")
          putExtra("text", "Ringing after $minutes min")
        }
        val pi = PendingIntent.getBroadcast(
          context, id, pluginIntent,
          PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.setAlarmClock(AlarmManager.AlarmClockInfo(whenMs, null), pi)

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.cancel(id)
      }
    }
  }
}