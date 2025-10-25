import 'dart:async'; 
import 'package:flutter/services.dart'; 

class FFAlarmClock { 
  static const MethodChannel _ch = MethodChannel('ff_set_alarm_clock'); 

  /// Schedule an exact alarm at [when] (millisecondsSinceEpoch). 
  static Future<void> schedule( 
    int id, 
    DateTime when, { 
    String title = 'Alarm', 
    String text = '', 
  }) async { 
    await _ch.invokeMethod('schedule', { 
      'id': id, 
      'epoch': when.millisecondsSinceEpoch, // <- milliseconds! 
      'title': title, 
      'text': text, 
    }); 
  } 

  /// Cancel a previously scheduled alarm by [id]. 
  static Future<void> cancel(int id) async { 
    await _ch.invokeMethod('cancel', {'id': id}); 
  } 

  /// Android 12+ — do we have the exact-alarms app-op? 
  static Future<bool> hasExactPermission() async { 
    final ok = await _ch.invokeMethod('hasExactPermission'); 
    return (ok == true); 
  } 

  /// Opens the system "Alarms & reminders" page for this app. 
  static Future<void> openExactAlarmSettings() async { 
    await _ch.invokeMethod('openExactAlarmSettings'); 
  } 

  /// Debug helper: fire the receiver immediately (no wait). 
  static Future<void> debugFireNow() async { 
    await _ch.invokeMethod('debugFireNow'); 
  } 
}