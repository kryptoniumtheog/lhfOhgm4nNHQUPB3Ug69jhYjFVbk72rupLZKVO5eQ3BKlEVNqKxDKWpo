import 'package:flutter/services.dart';

class FFAlarmClock {
  static const MethodChannel _c = MethodChannel('ff.alarmclock');

  /// Schedule an exact alarm that will ring at [when].
  /// [id] must be unique per alarm (use it to cancel).
  static Future<void> schedule(
    int id,
    DateTime when, {
    String title = 'Alarm',
    String text = '',
  }) async {
    await _c.invokeMethod('scheduleAlarmClock', <String, dynamic>{
      'id': id,
      'when': when.millisecondsSinceEpoch,
      'title': title,
      'text': text,
    });
  }

  /// Cancel a previously scheduled alarm by [id].
  static Future<void> cancel(int id) async {
    await _c.invokeMethod('cancel', <String, dynamic>{'id': id});
  }
}