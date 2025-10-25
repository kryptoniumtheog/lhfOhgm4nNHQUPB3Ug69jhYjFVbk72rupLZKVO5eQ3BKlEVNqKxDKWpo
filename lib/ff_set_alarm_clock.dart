import 'dart:async';
import 'package:flutter/services.dart';

enum FFNType { reminder, alarm }

class FFAlarmClock {
  static const MethodChannel _ch = MethodChannel('ff_set_alarm_clock');

  // Back-compat exact-alarm (same as today)
  static Future<void> schedule(
    int id,
    DateTime when, {
    String title = 'Alarm',
    String text = '',
  }) async {
    await _ch.invokeMethod('schedule', {
      'id': id,
      'epoch': when.millisecondsSinceEpoch,
      'title': title,
      'text': text,
    });
  }

  // New: schedule by type
  static Future<void> scheduleAt({
    required int id,
    required FFNType type,
    required DateTime when,
    String title = '',
    String text = '',
    int snoozeMinutes = 10,
  }) async {
    await _ch.invokeMethod('scheduleAt', {
      'id': id,
      'type': type.name, // 'reminder' | 'alarm'
      'epoch': when.millisecondsSinceEpoch,
      'title': title,
      'text': text,
      'snoozeMinutes': snoozeMinutes,
    });
  }

  static Future<void> cancel(int id) async =>
      _ch.invokeMethod('cancel', {'id': id});

  static Future<bool> hasExactPermission() async =>
      (await _ch.invokeMethod('hasExactPermission')) == true;

  static Future<void> openExactAlarmSettings() async =>
      _ch.invokeMethod('openExactAlarmSettings');

  static Future<void> debugFireNow() async =>
      _ch.invokeMethod('debugFireNow');

  // Actions from notification
  static Future<void> stop(int id) async =>
      _ch.invokeMethod('stop', {'id': id});

  static Future<void> snooze(int id, {int minutes = 10}) async =>
      _ch.invokeMethod('snooze', {'id': id, 'minutes': minutes});
}