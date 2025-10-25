package com.ff.clock

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.SystemClock
import android.provider.Settings
import androidx.annotation.NonNull
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel

class AlarmClockPlugin : FlutterPlugin, MethodChannel.MethodCallHandler {
  private lateinit var channel: MethodChannel
  private lateinit var appContext: Context

  override fun onAttachedToEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    appContext = binding.applicationContext
    channel = MethodChannel(binding.binaryMessenger, "ff_set_alarm_clock")
    channel.setMethodCallHandler(this)
  }

  override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: MethodChannel.Result) {
    when (call.method) {
      "schedule" -> {
        schedule(call, result)
      }
      "cancel" -> {
        cancel(call, result)
      }
      // NEW: check exact-alarms permission (Android 12+)
      "hasExactPermission" -> {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
          val am = appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
          result.success(am.canScheduleExactAlarms())
        } else {
          result.success(true)
        }
      }
      // NEW: open OS Alarms & reminders settings
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
      // NEW: debug - fire alarm broadcast immediately
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
      else -> result.notImplemented()
    }
  }

  // keep your existing helpers:
  private fun schedule(call: MethodCall, result: MethodChannel.Result) {
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
  
  private fun cancel(call: MethodCall, result: MethodChannel.Result) {
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
}