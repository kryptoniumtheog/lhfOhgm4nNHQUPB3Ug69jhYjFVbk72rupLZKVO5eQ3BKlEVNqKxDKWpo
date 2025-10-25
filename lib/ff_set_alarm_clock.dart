import 'dart:async';
import 'package:flutter/services.dart';

class FFAlarmClock {
  static const MethodChannel _ch = MethodChannel('ff.alarmclock');

  /// Schedule an exact alarm that will ring at [when].
  /// [id] must be unique per alarm (use it to cancel).
  static Future<void> schedule(
    int id,
    DateTime when, {
    String title = 'Alarm',
    String text = '',
  }) async {
    await _ch.invokeMethod('scheduleAlarmClock', <String, dynamic>{
      'id': id,
      'when': when.millisecondsSinceEpoch,
      'title': title,
      'text': text,
    });
  }

  /// Cancel a previously scheduled alarm by [id].
  static Future<void> cancel(int id) async {
    await _ch.invokeMethod('cancel', <String, dynamic>{'id': id});
  }

  /// Check if the app has permission to schedule exact alarms (Android 12+)
  static Future<bool> hasExactPermission() async {
    final v = await _ch.invokeMethod('hasExactPermission');
    return v == true;
  }

  /// Open system settings to grant exact alarm permission
  static Future<void> openExactAlarmSettings() async {
    await _ch.invokeMethod('openExactAlarmSettings');
  }
}