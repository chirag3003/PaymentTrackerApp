# Payment Tracker

Payment Tracker is a modern Android app for tracking personal finances, managing recurring payments, and staying within budget. It combines local-first data storage with optional AI-assisted entry to make expense tracking fast and practical.

## Features

- Guided first-run onboarding (name, monthly budget, preferred payment method)
- Manual income/expense entry with category, notes, date, and payment method
- Quick Add from natural language text (AI + fallback parser)
- Receipt scan flow with camera capture and AI extraction
- Recurring subscriptions with automatic transaction generation
- Home dashboard with:
  - Balance insights
  - Daily safe-to-spend guidance
  - Category budget usage
  - Weekly spending trends
  - Recent transactions and alerts
- Goals and savings tracking with contribution updates
- CSV export for transactions
- Optional biometric app lock
- Settings for profile, categories/budgets, notifications, and AI configuration

## App Flow

1. On first launch, onboarding is shown.
2. After onboarding, users land on Home.
3. Bottom navigation provides Home, Transactions, Goals, and Settings.
4. A global speed-dial action button supports:
   - Manual transaction add
   - Quick Add
   - Scan Bill

## Tech Stack

- Kotlin
- Jetpack Compose + Material 3
- Navigation Compose (typed routes)
- Room (local persistence)
- WorkManager (background recurring processing)
- OkHttp (network calls)
- kotlinx.serialization
- AndroidX Security Crypto (encrypted preference storage)
- Android Biometric APIs

## Architecture

Feature-first and layered:

- `feature/*`: UI screens and view models for Home, Transactions, Goals, Settings, Onboarding
- `navigation/*`: app graphs and routes
- `core/data/repository/*`: data access/business operations
- `core/database/*`: Room entities, DAOs, mappers, database setup
- `core/worker/*`: background recurring subscription engine
- `core/utils/*`: formatting, dates, categories, image and notification helpers

Primary persisted models:

- Transaction
- Subscription
- Goal
- UserProfile

## Requirements

- Android Studio (latest stable recommended)
- JDK 11
- Android SDK levels:
  - `minSdk = 34`
  - `targetSdk = 36`
  - `compileSdk = 36`

## Build and Run

```bash
./gradlew assembleDebug
```

Then run from Android Studio on an emulator or physical device.

## AI Setup (Optional)

Quick Add and Scan Bill can use Gemini or Claude via user-provided API keys.

1. Open **Settings**
2. Go to **AI Configuration**
3. Choose model/provider
4. Enter API key

Notes:

- API keys are stored with encrypted shared preferences.
- If AI parsing fails, manual entry and fallback parsing are still available.

## Permissions

- `INTERNET`
- `POST_NOTIFICATIONS`

Camera receipt flow uses image capture intent + FileProvider.

## Privacy and Security

- Financial data is stored locally in Room.
- Biometric lock can be enabled from Settings.
- API keys are encrypted at rest.
- CSV export is available for sharing/backups.

## Known Limitations

- CSV import is not implemented yet.
- Notification toggles exist in settings, but worker notification behavior is not fully governed by all toggles.
- Some date values are stored as formatted strings, which can affect strict chronological ordering in edge cases.
- High minimum SDK (`34`) limits support for older devices.

## Contributing

See `CONTRIBUTING.md` and `CODE_OF_CONDUCT.md`.
