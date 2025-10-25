# FF Set Alarm Clock

A minimal Flutter plugin to schedule and cancel exact alarms via Android's `AlarmManager.setAlarmClock()`.

## Features

- Schedule exact alarms that survive device doze mode
- Cancel scheduled alarms by ID
- Displays alarm clock icon in status bar
- Plays alarm sound with full-screen notification
- Works seamlessly with FlutterFlow

## Usage

```dart
import 'package:ff_set_alarm_clock/ff_set_alarm_clock.dart';

// Schedule an alarm
await FFAlarmClock.schedule(
  123, // unique ID
  DateTime.now().add(Duration(minutes: 30)),
  title: 'Wake up!',
  text: 'Time to get up',
);

// Cancel an alarm
await FFAlarmClock.cancel(123);
```

## Installation

Add this package to your FlutterFlow project via Custom Code > Packages > From Git (Public).

## Requirements

- Android API level 21+
- POST_NOTIFICATIONS permission (Android 13+)