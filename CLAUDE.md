# Hooky — Claude Instructions

## Project Structure
```
hooky/                        ← project root
├── android/                  ← Android app (Kotlin/Jetpack Compose)
├── ios/                      ← iOS app (future, Swift/SwiftUI)
├── shared/
│   ├── branding/             ← logos, icons (final/ and raw/)
│   ├── translations/         ← strings reference
│   └── design/               ← design preview HTML files
├── docs/
│   ├── PLAN.md               ← full feature spec + AI roadmap
│   ├── MARKETING.md
│   └── FACEBOOK-GROUPS.md
└── CLAUDE.md                 ← this file
```

**Project root:** `/Users/joanasocrates/Documents/claude/hooky/`
**Android project:** `android/` — open this folder in Android Studio
**Package name:** `com.hooky.app`

## Reference Projects
- **The Nag** (same Android tech stack): `/Users/joanasocrates/Documents/claude/nag/`

## What This App Is
Hooky is a crochet & knitting hobby management app. Tracks 3 entities:
- **Pieces** — crochet/knit items (shawls, scarves, blankets, etc.), work status, destination, pricing, photos
- **Yarns** — material inventory with care instructions
- **Stitches** — technique library with tutorial links

Full plan in `docs/PLAN.md`.

---

## Tech Stack
| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material Design 3 |
| Architecture | MVVM + Clean Architecture (data / domain / ui) |
| Database | Room (SQLite) |
| DI | Hilt |
| Navigation | Navigation Compose |
| Images | Coil (display) + CameraX (capture) |
| State | StateFlow + ViewModel |
| Async | Coroutines + Flow |
| ML (Phase 10) | ML Kit + TensorFlow Lite (on-device, no API key) |

---

## Architecture Rules
- **Layer dependency:** UI → Domain → Data (one-way only)
- **Domain layer:** no Android imports
- **Data layer:** Room entities, DAOs, repositories
- **ViewModels:** business logic + StateFlow UiState
- **Use cases:** one per operation (GetPiecesUseCase, CreatePieceUseCase, etc.)

## Code Conventions
- Follow Kotlin coding conventions
- Prefer `val` over `var`
- Composables: PascalCase
- ViewModels: `<Screen>ViewModel`
- Constants: UPPER_SNAKE_CASE
- UiState pattern: `data class <Screen>UiState(val isLoading, val error, val data)`

---

## Data Model Rules
- **IDs:** PIECE-001, YARN-001, STITCH-001 format — generated sequentially via a counter table in Room
- **Soft deletes only** — never hard delete; use `archived: Boolean` flag
- **JSON columns** in Room for list fields (work sessions, photos, yarns used, stitches used, care instructions) — use `kotlinx-serialization-json` with TypeConverters
- **Photos** stored in `filesDir/photos/{entity}/{ID}/` (app private storage)
- **Timestamps** stored as `Long` (Unix epoch ms)
- **Dates** stored as `String` ISO-8601 (yyyy-MM-dd)

---

## Design Style — B&W Minimalist
- Pure white backgrounds (`#FFFFFF`)
- Near-black text (`#1F2937`)
- Light grey borders and muted backgrounds (`#E5E7EB`, `#F3F4F6`)
- Dark slate for interactive elements / primary actions (`#1E293B`)
- Red only for destructive actions (`#EF4444`)
- Border radius: 12–16dp on cards, 8dp on chips/buttons
- No decorative colors — content is the focus
- Bottom navigation: 4 tabs (Dashboard, Pieces, Yarns, Stitches)

## Brand (Hooky)
- App icon: purple `#8C015E` background + white infinity "oo" symbol
- Wordmark: hand-drawn brush style, purple `#8C015E`
- All brand assets in `shared/branding/final/`

---

## Key Decisions
- Standalone mobile app — offline-first (Room/SQLite)
- Min SDK: API 26+
- i18n: EN + ES (Spain) + PT-BR (Brazilian Portuguese)
- Phase 10 AI vision: A) ML Kit OCR yarn label scanner ✅, B) TFLite stitch recognition, C) backend freemium

---

## Implementation Status
1. ✅ Foundation — project, Room, Hilt, nav, theme
2. ✅ Pieces module
3. ✅ Yarns module
4. ✅ Stitches module
5. ✅ Dashboard
6. ✅ Camera & Photos (CameraX)
7. ✅ Polish — search, filters, price calculator, empty/loading states
8. ⬜ Data Migration (optional)
9. ⬜ AI Vision Phase 10B — TFLite stitch recognition
10. ✅ Phase 10A — ML Kit OCR yarn label + needle scanner, Palette API
11. ✅ Phase 11 — Row Counter (−/+/voice/edit dialog) + Work Timer (Start/Pause/Resume/Stop, persists restarts) + Library pickers for Yarn/Needle/Stitch in piece form + splash screen purple background
12. ✅ UX fixes — hook size field removed from piece form; nav bug fixed (dashboard shortcuts use bottom-nav save/restore pattern)

---

## When Starting Work
1. Read this file and `docs/PLAN.md`
2. Check The Nag project for patterns (same stack, same conventions)
3. Ask before any architectural decision not covered here
