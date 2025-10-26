package com.ff.clock

import android.content.Context
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FileLogger {
  private const val LOG_NAME = "ff_alarm_log.txt"
  private val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)

  @Synchronized fun log(ctx: Context, msg: String) {
    try {
      val dir = ctx.getExternalFilesDir(null) ?: ctx.filesDir
      val f = File(dir, LOG_NAME)
      val line = "[${fmt.format(Date())}] $msg\n"
      f.appendText(line)
    } catch (_: Throwable) { /* ignore */ }
  }

  @Synchronized fun read(ctx: Context): String {
    return try {
      val dir = ctx.getExternalFilesDir(null) ?: ctx.filesDir
      val f = File(dir, LOG_NAME)
      if (f.exists()) f.readText() else "(no log yet)"
    } catch (_: Throwable) { "(failed to read log)" }
  }

  @Synchronized fun clear(ctx: Context) {
    try {
      val dir = ctx.getExternalFilesDir(null) ?: ctx.filesDir
      val f = File(dir, LOG_NAME)
      if (f.exists()) f.writeText("")
    } catch (_: Throwable) { /* ignore */ }
  }
}