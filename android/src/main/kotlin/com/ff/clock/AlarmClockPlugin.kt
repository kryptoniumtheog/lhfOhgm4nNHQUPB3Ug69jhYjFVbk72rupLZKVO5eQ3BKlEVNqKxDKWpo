package com.ff.clock

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.SystemClock
import android.provider.Settings
import androidx.annotation.NonNull
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.Result as FlutterResult

class AlarmClockPlugin : FlutterPlugin, MethodChannel.MethodCallHandler {
  private lateinit var channel: MethodChannel
  private lateinit var appContext: Context

  override fun onAttachedToEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    appContext = binding.applicationContext
    channel = MethodChannel(binding.binaryMessenger, "ff_set_alarm_clock")
    channel.setMethodCallHandler(this)
  }

  override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: FlutterResult) {
    FileLogger.log(appContext, "onMethodCall ${call.method}")
    when (call.method) {
      "schedule" -> schedule(call, result) // back-compat
      "scheduleAt" -> scheduleAt(call, result)
      "cancel" -> cancel(call, result)
      "snooze" -> snooze(call, result)
      "stop" -> stop(call, result)
      "hasExactPermission" -> {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
          val am = appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
          result.success(am.canScheduleExactAlarms())
        } else {
          result.success(true)
        }
      }
      "openExactAlarmSettings" -> {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
          try {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
              .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            appContext.startActivity(intent)
            result.success(true)
          } catch (e: Exception) {
            result.error("OPEN_SETTINGS", e.message, null)
          }
        } else {
          result.success(true)
        }
      }
      "debugFireNow" -> {
        try {
          val intent = Intent("com.ff.clock.ALARM")
          intent.setPackage(appContext.packageName)
          appContext.sendBroadcast(intent)
          result.success(true)
        } catch (e: Exception) {
          result.error("DEBUG_FIRE", e.message, null)
        }
      }
      "readLog" -> result.success(FileLogger.read(appContext))
      "clearLog" -> {
        FileLogger.clear(appContext)
        result.success(true)
      }
      "openNotificationSettings" -> openNotificationSettings(result)
      "openBatteryOptimizationSettings" -> openBatteryOptimizationSettings(result)
      else -> result.notImplemented()
    }
  }

  // keep your existing helpers:
  private fun schedule(call: MethodCall, result: FlutterResult) {
    val id = call.argument<Int>("id") ?: 0
    val epoch = call.argument<Long>("epoch") ?: System.currentTimeMillis() + 60_000L
    val title = call.argument<String>("title") ?: "Alarm"
    val text = call.argument<String>("text") ?: ""
    
    val am = appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    // Broadcast that will fire at the alarm time - shows our alarm notification
    val fireIntent = Intent(appContext, AlarmFireReceiver::class.java).apply {
      putExtra("id", id)
      putExtra("title", title)
      putExtra("text", text)
    }
    val firePI = PendingIntent.getBroadcast(
      appContext, id, fireIntent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    // Intent shown to user when tapping the small clock icon in the status bar
    val showIntent = appContext.packageManager.getLaunchIntentForPackage(appContext.packageName)
    val showPI = PendingIntent.getActivity(
      appContext, id, showIntent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    // Exact alarm with special alarm-clock treatment (survives doze)
    val info = AlarmManager.AlarmClockInfo(epoch, showPI)
    am.setAlarmClock(info, firePI)
    
    result.success(null)
  }
  
  private fun cancel(call: MethodCall, result: FlutterResult) {
    val id = call.argument<Int>("id") ?: 0
    val am = appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val fireIntent = Intent(appContext, AlarmFireReceiver::class.java)
    val firePI = PendingIntent.getBroadcast(
      appContext, id, fireIntent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    am.cancel(firePI)
    result.success(null)
  }

  private fun scheduleAt(call: MethodCall, result: FlutterResult) {
    val id = call.argument<Int>("id") ?: 0
    val epoch = call.argument<Long>("epoch") ?: (System.currentTimeMillis() + 60_000L)
    val title = call.argument<String>("title") ?: ""
    val text = call.argument<String>("text") ?: ""
    val type = call.argument<String>("type") ?: "alarm"
    val snoozeMin = call.argument<Int>("snoozeMinutes") ?: 10
    
    FileLogger.log(appContext, "scheduleAt() called with id=$id type=$type epoch=$epoch title=$title")

    val intent = Intent(appContext, AlarmFireReceiver::class.java).apply {
      putExtra("id", id)
      putExtra("title", title)
      putExtra("text", text)
      putExtra("type", type)
      putExtra(FFConst.EXTRA_SNOOZE_MIN, snoozeMin)
    }
    val pi = PendingIntent.getBroadcast(
      appContext, id, intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val showIntent = appContext.packageManager.getLaunchIntentForPackage(appContext.packageName)
    val showPI = PendingIntent.getActivity(
      appContext, id, showIntent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val am = appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    am.setAlarmClock(AlarmManager.AlarmClockInfo(epoch, showPI), pi)
    result.success(true)
  }

  private fun snooze(call: MethodCall, result: FlutterResult) {
    try {
      val id = call.argument<Int>("id") ?: 0
      val minutes = call.argument<Int>("minutes") ?: 10
      FileLogger.log(appContext, "snooze() id=$id minutes=$minutes")
      appContext.stopService(Intent(appContext, AlarmForegroundService::class.java))
      val nm = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
      nm.cancel(id)
      result.success(true)
    } catch (e: Throwable) {
      FileLogger.log(appContext, "snooze() error: ${e.message}\n${e.stackTraceToString()}")
      result.error("SNOOZE", e.message, null)
    }
  }

  private fun stop(call: MethodCall, result: FlutterResult) {
    try {
      val id = call.argument<Int>("id") ?: 0
      FileLogger.log(appContext, "stop() id=$id")
      appContext.stopService(Intent(appContext, AlarmForegroundService::class.java))
      val nm = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
      nm.cancel(id)
      result.success(true)
    } catch (e: Throwable) {
      FileLogger.log(appContext, "stop() error: ${e.message}\n${e.stackTraceToString()}")
      result.error("STOP", e.message, null)
    }
  }

  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
  }

  // Helper function to check exact alarm permission
  private fun hasExactPermission(ctx: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      val am = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
      am.canScheduleExactAlarms()
    } else true
  }

  private fun openNotificationSettings(result: FlutterResult) {
    try {
      val ctx = appContext ?: return result.error("NOCTX", "No context", null)

      // Try the per-app notification settings screen
      val i = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
        putExtra(Settings.EXTRA_APP_PACKAGE, ctx.packageName)
        putExtra("app_package", ctx.packageName)                 // some OEMs
        putExtra("app_uid", ctx.applicationInfo?.uid ?: 0)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      ctx.startActivity(i)
      result.success(true)
    } catch (t: Throwable) {
      // Fallback to app details if OEM blocks the above
      try {
        val ctx = appContext
        val i2 = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
          data = Uri.parse("package:${ctx.packageName}")
          addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        ctx.startActivity(i2)
        result.success(true)
      } catch (tt: Throwable) {
        FileLogger.log(appContext, "openNotificationSettings failed ${tt.message}")
        result.error("INTENT", tt.message, null)
      }
    }
  }

  private fun openBatteryOptimizationSettings(result: FlutterResult) {
    try {
      val ctx = appContext ?: return result.error("NOCTX", "No context", null)
      val i = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      ctx.startActivity(i)
      result.success(true)
    } catch (t: Throwable) {
      FileLogger.log(appContext, "openBatteryOptimizationSettings failed ${t.message}")
      result.error("INTENT", t.message, null)
    }
  }
}