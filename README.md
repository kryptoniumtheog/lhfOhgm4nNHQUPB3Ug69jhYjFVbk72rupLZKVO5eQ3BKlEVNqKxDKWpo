# FF Set Alarm Clock

A comprehensive Flutter plugin for Android alarm management with exact alarm scheduling, notifications, and advanced features via Android's `AlarmManager.setAlarmClock()`.

## Features

- **Exact Alarm Scheduling**: Schedule alarms that survive device doze mode
- **Dual Notification Types**: Support for both reminder and alarm notifications
- **Continuous Alarm Ringing**: Foreground service keeps alarms active until user interaction
- **Action Buttons**: Built-in Snooze and Stop buttons in notifications
- **Permission Management**: Built-in helpers for exact alarm and notification permissions
- **Debug Tools**: Development utilities for testing alarm functionality
- **Comprehensive Logging**: Built-in logging system for debugging
- **Settings Integration**: Direct access to notification and battery optimization settings
- **FlutterFlow Compatible**: Works seamlessly with FlutterFlow projects

## Usage

### Basic Alarm Scheduling

```dart
import 'package:ff_set_alarm_clock/ff_set_alarm_clock.dart';

// Schedule a basic alarm (legacy method)
await FFAlarmClock.schedule(
  123, // unique ID
  DateTime.now().add(Duration(minutes: 30)),
  title: 'Wake up!',
  text: 'Time to get up',
);

// Cancel an alarm
await FFAlarmClock.cancel(123);
```

### Advanced Alarm Scheduling with Types

```dart
// Schedule a reminder (dismissible notification)
await FFAlarmClock.scheduleAt(
  id: 456,
  type: FFNType.reminder,
  when: DateTime.now().add(Duration(minutes: 15)),
  title: 'Meeting Reminder',
  text: 'Team standup in 5 minutes',
  snoozeMinutes: 5,
);

// Schedule an alarm (continuous ringing until stopped)
await FFAlarmClock.scheduleAt(
  id: 789,
  type: FFNType.alarm,
  when: DateTime.now().add(Duration(hours: 8)),
  title: 'Morning Alarm',
  text: 'Wake up!',
  snoozeMinutes: 10,
);
```

### Alarm Control

```dart
// Snooze an alarm
await FFAlarmClock.snooze(789, minutes: 15);

// Stop an alarm
await FFAlarmClock.stop(789);
```

### Permission Management

```dart
// Check if app has exact alarm permission (Android 12+)
bool hasPermission = await FFAlarmClock.hasExactPermission();

// Open exact alarm settings (Android 12+)
await FFAlarmClock.openExactAlarmSettings();
```

### Debug Tools

```dart
// Trigger an alarm immediately for testing
await FFAlarmClock.debugFireNow();
```

### Settings Integration

```dart
// Open notification settings for this app
await FFAlarmClock.openNotificationSettings();

// Open battery optimization settings
await FFAlarmClock.openBatteryOptimizationSettings();
```

## API Reference

### Methods

| Method | Description | Parameters |
|--------|-------------|------------|
| `schedule(id, when, title, text)` | Legacy method for basic alarm scheduling | `id`: int, `when`: DateTime, `title`: String, `text`: String |
| `scheduleAt({id, type, when, title, text, snoozeMinutes})` | Advanced scheduling with notification type | `id`: int, `type`: FFNType, `when`: DateTime, `title`: String, `text`: String, `snoozeMinutes`: int |
| `cancel(id)` | Cancel a scheduled alarm | `id`: int |
| `stop(id)` | Stop a ringing alarm | `id`: int |
| `snooze(id, {minutes})` | Snooze a ringing alarm | `id`: int, `minutes`: int (default: 10) |
| `hasExactPermission()` | Check exact alarm permission (Android 12+) | Returns: Future<bool> |
| `openExactAlarmSettings()` | Open exact alarm settings (Android 12+) | None |
| `openNotificationSettings()` | Open app notification settings | None |
| `openBatteryOptimizationSettings()` | Open battery optimization settings | None |
| `debugFireNow()` | Trigger alarm immediately for testing | None |

### Enums

```dart
enum FFNType {
  reminder,  // Dismissible notification
  alarm,     // Continuous ringing until stopped
}
```

## Installation

Add this package to your FlutterFlow project via Custom Code > Packages > From Git (Public).

## Requirements

- **Android API Level**: 21+ (Android 5.0+)
- **Permissions**:
  - `POST_NOTIFICATIONS` (Android 13+)
  - `SCHEDULE_EXACT_ALARM` (Android 12+)
  - `FOREGROUND_SERVICE` (all versions)
  - `FOREGROUND_SERVICE_MEDIA_PLAYBACK` (Android 14+)
  - `WAKE_LOCK`, `USE_FULL_SCREEN_INTENT`

## Android 14+ Support

This plugin is fully compatible with Android 14+ and includes:
- Proper foreground service type declarations
- Required media playback permissions
- Enhanced notification handling
- Battery optimization awareness

## FlutterFlow Integration

Perfect for FlutterFlow projects with:
- Custom code packages support
- Built-in permission handling
- Debug utilities for development
- Comprehensive error logging