package com.ff.clock

import android.app.*
import android.content.*
import android.media.*
import android.os.*
import androidx.core.app.NotificationCompat

class AlarmForegroundService : Service() {
  private var player: MediaPlayer? = null
  private var notifId: Int = 0
  private var title: String = "Alarm"
  private var text: String = ""
  private var snoozeMin: Int = 10

  override fun onBind(intent: Intent?) = null

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    try {
      notifId = intent?.getIntExtra(FFConst.EXTRA_ID, 0) ?: 0
      title = intent?.getStringExtra(FFConst.EXTRA_TITLE) ?: "Alarm"
      text = intent?.getStringExtra(FFConst.EXTRA_TEXT) ?: ""
      snoozeMin = intent?.getIntExtra(FFConst.EXTRA_SNOOZE_MIN, 10) ?: 10
      
      FileLogger.log(this, "Service start id=$notifId title='$title' snooze=$snoozeMin")

    // Channel
    val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val ch = NotificationChannel(
        FFConst.CH_ALARM, "Alarms",
        NotificationManager.IMPORTANCE_HIGH
      ).apply {
        description = "Alarm notifications"
        setSound(
          RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM),
          AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).build()
        )
        lockscreenVisibility = Notification.VISIBILITY_PUBLIC
      }
      nm.createNotificationChannel(ch)
    }

    val stopPI = PendingIntent.getBroadcast(
      this, notifId,
      Intent(this, ActionReceiver::class.java).apply {
        action = FFConst.ACT_STOP
        putExtra(FFConst.EXTRA_ID, notifId)
      },
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val snoozePI = PendingIntent.getBroadcast(
      this, notifId + 1,
      Intent(this, ActionReceiver::class.java).apply {
        action = FFConst.ACT_SNOOZE
        putExtra(FFConst.EXTRA_ID, notifId)
        putExtra(FFConst.EXTRA_SNOOZE_MIN, snoozeMin)
      },
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val launch = packageManager.getLaunchIntentForPackage(packageName)
    val fullScreenPI = PendingIntent.getActivity(
      this, notifId, launch,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val notif = NotificationCompat.Builder(this, FFConst.CH_ALARM)
      .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
      .setContentTitle(title)
      .setContentText(text)
      .setCategory(NotificationCompat.CATEGORY_ALARM)
      .setPriority(NotificationCompat.PRIORITY_MAX)
      .setOngoing(true)                    // < cannot swipe away
      .setAutoCancel(false)
      .setFullScreenIntent(fullScreenPI, true)
      .addAction(0, "Snooze", snoozePI)
      .addAction(0, "Stop", stopPI)
      .build()

    // Start foreground
    startForeground(notifId, notif)

    // Loop the alarm tone with error handling
    try {
      val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
      FileLogger.log(this, "MediaPlayer create with uri=$uri")
      player = MediaPlayer().apply {
        setAudioAttributes(
          AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).build()
        )
        setDataSource(this@AlarmForegroundService, uri)
        isLooping = true
        prepare()
        start()
      }
      FileLogger.log(this, "MediaPlayer started")
    } catch (e: Throwable) {
      FileLogger.log(this, "MediaPlayer error: ${e.message}\n${e.stackTraceToString()}")
    }

    return START_STICKY
    } catch (e: Throwable) {
      FileLogger.log(this, "Service fatal error: ${e.message}\n${e.stackTraceToString()}")
      stopSelf()
      return START_NOT_STICKY
    }
  }

  override fun onDestroy() {
    FileLogger.log(this, "Service onDestroy")
    try { player?.stop() } catch (_: Throwable) {}
    try { player?.release() } catch (_: Throwable) {}
    player = null
    super.onDestroy()
  }
}