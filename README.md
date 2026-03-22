# 💊 SymptomTracker AI

> Log symptoms & meds. AI spots the patterns. You own your health data.

## What It Is

A clean Android app for people who want to track their health without complexity. Log symptoms, medications, side effects, and moods. The AI surfaces patterns and generates plain-english summaries you can share with your doctor.

**No medical advice. Just organized data.**

## Core Features

- 📝 **Quick Log** — Log symptoms, meds, side effects in seconds
- 🧠 **AI Insights** — Pattern detection across your logs
- 📊 **Visual Timeline** — See how symptoms correlate with meds
- 📤 **Doctor Report** — One-tap PDF summary for appointments
- 🔒 **Private by Default** — Local-first, optional encrypted backup

## Tech Stack

- **Platform:** Android (Kotlin + Jetpack Compose)
- **Local DB:** Room (SQLite)
- **AI:** On-device (Gemini Nano) + optional cloud (Gemini API)
- **Charts:** Vico
- **Backup:** Encrypted Google Drive sync (optional)

## Monetization

- **Free:** Unlimited logging, basic insights, 30-day history
- **Pro ($7.99/mo or $59.99/yr):** Full history, AI summaries, doctor reports, cloud backup

## Project Structure

```
app/
├── data/           # Room DB, DAOs, models
├── domain/         # Use cases, business logic
├── ui/             # Jetpack Compose screens
│   ├── log/        # Quick log entry
│   ├── timeline/   # History & charts
│   ├── insights/   # AI pattern screen
│   └── report/     # Doctor report generator
├── ai/             # Gemini integration
└── sync/           # Encrypted backup
```

## Roadmap

See [Issues](../../issues) for the full milestone breakdown.

## License

MIT
