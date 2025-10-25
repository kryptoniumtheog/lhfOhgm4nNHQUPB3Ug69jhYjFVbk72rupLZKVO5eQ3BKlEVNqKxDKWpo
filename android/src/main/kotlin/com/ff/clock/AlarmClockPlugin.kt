package com.ff.clock

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import androidx.annotation.NonNull
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel

class AlarmClockPlugin : FlutterPlugin, MethodChannel.MethodCallHandler {
  private lateinit var channel: MethodChannel
  private lateinit var appContext: Context

  override fun onAttachedToEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    appContext = binding.applicationContext
    channel = MethodChannel(binding.binaryMessenger, "ff.alarmclock")
    channel.setMethodCallHandler(this)
  }

  override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: MethodChannel.Result) {
    when (call.method) {
      "scheduleAlarmClock" -> {
        val id = call.argument<Int>("id") ?: 0
        val whenMs = call.argument<Long>("when") ?: System.currentTimeMillis() + 60_000L
        val title = call.argument<String>("title") ?: "Alarm"
        val text = call.argument<String>("text") ?: ""
        scheduleAlarmClock(appContext, id, whenMs, title, text)
        result.success(null)
      }
      "cancel" -> {
        val id = call.argument<Int>("id") ?: 0
        cancel(appContext, id)
        result.success(null)
      }
      else -> result.notImplemented()
    }
  }

  private fun scheduleAlarmClock(context: Context, id: Int, triggerAtMillis: Long, title: String, text: String) {
    val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    // Broadcast that will fire at the alarm time – shows our alarm notification
    val fireIntent = Intent(context, AlarmFireReceiver::class.java).apply {
      putExtra("id", id)
      putExtra("title", title)
      putExtra("text", text)
    }
    val firePI = PendingIntent.getBroadcast(
      context, id, fireIntent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    // Intent shown to user when tapping the small clock icon in the status bar
    val showIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
    val showPI = PendingIntent.getActivity(
      context, id, showIntent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    // Exact alarm with special alarm-clock treatment (survives doze)
    val info = AlarmManager.AlarmClockInfo(triggerAtMillis, showPI)
    am.setAlarmClock(info, firePI)
  }

  private fun cancel(context: Context, id: Int) {
    val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val fireIntent = Intent(context, AlarmFireReceiver::class.java)
    val firePI = PendingIntent.getBroadcast(
      context, id, fireIntent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    am.cancel(firePI)
  }

  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
  }
}