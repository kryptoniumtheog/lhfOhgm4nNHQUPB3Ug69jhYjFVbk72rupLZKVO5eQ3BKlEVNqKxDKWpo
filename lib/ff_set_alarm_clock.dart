import 'dart:async';
import 'package:flutter/services.dart';

class FFAlarmClock {
  static const _ch = MethodChannel('ff_set_alarm_clock');

  /// Schedule an exact alarm via native setAlarmClock(...)
  static Future<void> schedule(int id, DateTime when,
      {required String title, required String text}) async {
    await _ch.invokeMethod('schedule', {
      'id': id,
      'epoch': when.millisecondsSinceEpoch,
      'title': title,
      'text': text,
    });
  }

  /// Cancel a previously scheduled alarm.
  static Future<void> cancel(int id) async {
    await _ch.invokeMethod('cancel', {'id': id});
  }

  /// NEW: Android 12+ exact-alarms permission check.
  static Future<bool> hasExactPermission() async {
    final ok = await _ch.invokeMethod<bool>('hasExactPermission');
    return ok ?? true; // pre-Android 12 returns true
  }

  /// NEW: Open the OS page to grant "Alarms & reminders".
  static Future<void> openExactAlarmSettings() async {
    await _ch.invokeMethod('openExactAlarmSettings');
  }

  /// NEW: debug helper ù fire the alarm broadcast immediately.
  static Future<void> debugFireNow() async {
    await _ch.invokeMethod('debugFireNow');
  }
}