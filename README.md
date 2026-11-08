# DiaLog 🩺

DiaLog is an Android app that helps dialysis patients log and review their sessions in one place.

## Features

- **Daily check-in** for each session:
  - Blood pressure and pulse (before and after)
  - Weight before and after, with gain over dry weight
  - Target vs. achieved ultrafiltration (UF) with a progress bar
  - Current medication checklist
  - Symptoms during the session (cold, vomiting, cramps, etc.) with severity
  - Remarks for extra injections and notes
- **Home screen** with a monthly calendar (green dot for completed sessions) and recent sessions
- **Dark / light mode** switch
- Bottom navigation between screens

## Tech stack

- Kotlin
- Jetpack Compose + Material 3
- Android Studio
- UI designed in Google Stitch

## Project status

🚧 In development. The UI is built; the app currently uses sample data.

- [x] Home screen and calendar
- [x] Daily check-in form
- [x] Dark / light theme toggle
- [ ] Backend integration (Retrofit)
- [ ] Load and save real sessions
- [ ] Vitals & UF history screen
- [ ] Care team / medications screen
- [ ] Save theme preference

## Getting started

1. Clone the repo:
   ```bash
   git clone https://github.com/<your-username>/DiaLog.git
   ```
2. Open it in **Android Studio**.
3. Let Gradle sync finish.
4. Run on an emulator or a physical device (USB debugging enabled).

## Project structure

```
app/src/main/java/com/example/dialysisdiary/
├── MainActivity.kt
└── ui/
    ├── screens/    # HomeScreen, DailyLogScreen, navigation
    └── theme/
```

## Disclaimer

DiaLog is a personal tracking tool. It does not give medical advice, diagnosis or treatment. Always follow the guidance of your nephrologist and dialysis care team.

## License

Add a license of your choice (e.g. MIT).
