# FF Set Alarm Clock

A minimal Flutter plugin to schedule and cancel exact alarms via Android's `AlarmManager.setAlarmClock()`.

## Features

- Schedule exact alarms that survive device doze mode
- Cancel scheduled alarms by ID
- Displays alarm clock icon in status bar
- Plays alarm sound with full-screen notification
- **NEW: Support for both reminder and alarm types**
- **NEW: Snooze functionality with customizable snooze duration**
- **NEW: Stop alarm functionality**
- **NEW: Foreground service for continuous alarm ringing**
- **NEW: Action buttons in notifications (Skip/Snooze/Stop)**
- Works seamlessly with FlutterFlow

## Usage

### Basic Alarm Scheduling

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

### Advanced Alarm with Type Support (New)

```dart
// Schedule a reminder (dismissible notification)
await FFAlarmClock.scheduleAt(
  id: 456,
  epoch: DateTime.now().add(Duration(minutes: 15)).millisecondsSinceEpoch,
  title: 'Meeting Reminder',
  text: 'Team standup in 5 minutes',
  type: 'reminder',
  snoozeMinutes: 5,
);

// Schedule an alarm (continuous ringing until stopped)
await FFAlarmClock.scheduleAt(
  id: 789,
  epoch: DateTime.now().add(Duration(hours: 8)).millisecondsSinceEpoch,
  title: 'Morning Alarm',
  text: 'Wake up!',
  type: 'alarm',
  snoozeMinutes: 10,
);

// Snooze an alarm
await FFAlarmClock.snooze(id: 789, minutes: 15);

// Stop an alarm
await FFAlarmClock.stop(id: 789);
```

## Branch Information

**Current Branch:** `feature/alarm_plus_notification`

This branch includes enhanced alarm functionality with:
- Support for both reminder and alarm notification types
- Foreground service for continuous alarm ringing
- Snooze and stop functionality
- Action buttons in notifications
- Improved notification handling

## Installation

Add this package to your FlutterFlow project via Custom Code > Packages > From Git (Public).

## Requirements

- Android API level 21+
- POST_NOTIFICATIONS permission (Android 13+)
- SCHEDULE_EXACT_ALARM permission (Android 12+)
- FOREGROUND_SERVICE permission