# Payment Tracker

Payment Tracker is a student-first Android app to track expenses, manage budgets, and stay on top of recurring bills.

## Features
- Manual transactions with categories and notes
- Quick Add and receipt scan (AI parsing with user-provided keys)
- Recurring subscriptions with auto-generated transactions
- Daily budget guidance with upcoming subscription breakdown
- Goals and savings tracking
- CSV export
- Biometric lock

## Tech Stack
- Kotlin
- Jetpack Compose
- Room
- WorkManager

## Setup
1) Open the project in Android Studio.
2) Sync Gradle.
3) Run on an emulator or device.

## Build
```bash
./gradlew assembleDebug
```

## AI Setup (Optional)
Quick Add and Scan Bill use Gemini or Claude with user-provided API keys.

1) Open Settings.
2) Under AI Configuration, choose the model.
3) Paste your API key.

## Notes
- Data is stored locally on the device.
- CSV import is not available yet.
