# Development Guide

## Setup

1. **Clone the repo**
   ```bash
   git clone https://github.com/oogleyskr/symptom-tracker
   cd symptom-tracker
   ```

2. **Open in Android Studio** (Hedgehog or newer)

3. **Select build variant**
   - `freeDebug` — free tier, no billing
   - `proDebug` — pro tier (billing sandbox)

4. **Run on device or emulator** (API 26+ required)

## Project Structure

```
app/src/main/java/com/symptomtracker/
├── data/
│   ├── db/
│   │   ├── entity/          # Room entities
│   │   ├── dao/             # Room DAOs
│   │   └── AppDatabase.kt   # Room database
│   ├── notification/        # BroadcastReceivers
│   └── repository/          # Data repositories
├── di/
│   └── DatabaseModule.kt    # Hilt DI
├── ai/
│   └── GeminiInsightsEngine.kt  # AI analysis
├── ui/
│   ├── log/                 # Quick Log screen
│   ├── timeline/            # Timeline + Charts + Meds
│   ├── insights/            # AI Insights screen
│   ├── report/              # Doctor Report screen
│   ├── onboarding/          # FTUE onboarding
│   ├── billing/             # Paywall + BillingManager
│   └── theme/               # Material 3 dark theme
├── MainActivity.kt
└── SymptomTrackerApp.kt     # Hilt entry point
```

## Architecture

- **Pattern:** MVVM + Repository
- **DI:** Hilt
- **DB:** Room (SQLite) with Flow-based reactive queries
- **UI:** Jetpack Compose + Material 3
- **Navigation:** Navigation Compose
- **Charts:** Vico
- **Billing:** Google Play Billing v6

## Milestones

| # | Name | Status |
|---|------|--------|
| M1 | Foundation (DB, DI, scaffold, Quick Log) | ✅ Done |
| M2 | Charts, Timeline, Medication management | ✅ Done |
| M3 | AI Insights (Gemini pattern detection) | ✅ Done |
| M4 | Pro subscription (Play Billing v6) | ✅ Done |
| M5 | Onboarding FTUE, Play Store listing | ✅ Done |

## Next Steps

- [ ] Gemini Nano (MediaPipe LLM) on-device integration
- [ ] PDF generation (replace text export with iText/PdfDocument)
- [ ] Medication reminder scheduling (AlarmManager)
- [ ] Encrypted Google Drive backup
- [ ] Widget for quick symptom logging
- [ ] Wear OS companion app
- [ ] App icon design
- [ ] Privacy policy page

## Monetization

| Plan | Price | Features |
|------|-------|----------|
| Free | $0 | Unlimited logging, basic insights, 30-day history |
| Pro Monthly | $7.99/mo | Full AI, PDF reports, cloud backup, unlimited history |
| Pro Annual | $59.99/yr | Same as monthly, ~37% savings |

7-day free trial on all Pro plans.
