# Crochet Manager Android — Claude Instructions

## Project Location
`/Users/joanasocrates/Local Documents/claude/crochet-android/`

## Reference Projects
- **Desktop app** (source of truth for data model): `/Users/joanasocrates/Local Documents/claude/crochet/`
- **The Nag** (same Android tech stack): `/Users/joanasocrates/Local Documents/claude/nag/`

## What This App Is
Android mobile version of the crochet hobby management desktop app. Tracks 3 entities:
- **Pieces** — crochet items (shawls, scarves, blankets, etc.), work status, destination, pricing, photos
- **Yarns** — material inventory with care instructions
- **Stitches** — technique library with tutorial links

Full plan in `PLAN.md`.

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
| ML (Phase 9) | ML Kit + TensorFlow Lite (on-device, no API key) |

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

## Design Style
- **Black & white minimalist** — matches the desktop app aesthetic
- Pure white backgrounds (`#FFFFFF`)
- Near-black text (`#1F2937`)
- Light grey borders and muted backgrounds (`#E5E7EB`, `#F3F4F6`)
- Dark slate for interactive elements / primary actions (`#1E293B`)
- Red only for destructive actions (`#EF4444`)
- Border radius: 12–16dp on cards, 8dp on chips/buttons
- No decorative colors — content is the focus
- Bottom navigation: 4 tabs (Dashboard, Pieces, Yarns, Stitches)

---

## Key Decisions
- Standalone mobile app — no sync with desktop (same data model, independent storage)
- Offline-first — Room replaces Python JSON backend
- Min SDK: API 26+
- Phase 9 AI features (piece classification, stitch error detection) use on-device ML Kit + TFLite — no API key required, publishable for all users

---

## Implementation Phases (see PLAN.md for detail)
1. Foundation — project, Room, Hilt, nav, theme
2. Pieces module
3. Yarns module
4. Stitches module
5. Dashboard
6. Camera & Photos (CameraX)
7. Polish — search, filters, price calculator, empty/loading states
8. Data Migration (optional)
9. AI Vision — ML Kit piece classification + stitch error detection (optional)

---

## When Starting Work
1. Read this file and `PLAN.md`
2. Check The Nag project for patterns (same stack, same conventions)
3. Follow the phase order — don't skip ahead
4. Ask before any architectural decision not covered here
