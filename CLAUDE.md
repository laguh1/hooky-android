# Hooky — Claude Instructions

## Project Structure
```
hooky/                        ← project root
├── android/                  ← Android app (Kotlin/Jetpack Compose)
├── ios/                      ← iOS app (future, Swift/SwiftUI)
├── shared/
│   ├── branding/             ← logos, icons (final/ and raw/)
│   ├── translations/         ← strings reference
│   ├── design/                ← design preview HTML files
│   └── promo/                 ← promo video assets, organized by language (see docs/VIDEO-SCRIPT.md)
├── docs/
│   ├── PLAN.md               ← full feature spec + AI roadmap
│   ├── PLAYSTORE.md          ← Play Store listing/publishing guide
│   ├── LAUNCH-STATUS.md      ← Play Console setup/testing status
│   ├── FIXES-2026-08-31.md   ← running log of bug fixes found via testing/feedback (evidence for Google review)
│   ├── PREMIUM-TUTORIAL.md / PREMIUM-GIFTING.md ← Firebase Remote Config premium system
│   ├── MARKETING.md
│   └── FACEBOOK-GROUPS.md
└── CLAUDE.md                 ← this file
```

**Project root:** `/Users/joanasocrates/Documents/claude/hooky/`
**Android project:** `android/` — open this folder in Android Studio
**Package name:** `com.laguh.hooky` (changed from `com.hooky.app`, which was already taken on Play Store)
**Landing page:** separate git repo at `landing/` (pushed independently to `laguh1/hooky-landing`, live on GitHub Pages)

## Reference Projects
- **The Nag** (same Android tech stack): `/Users/joanasocrates/Documents/claude/nag/`

## What This App Is
Hooky is a crochet & knitting hobby management app. Tracks 4 entities:
- **Pieces** — crochet/knit items (shawls, scarves, blankets, etc.), work status, destination, pricing, photos
- **Yarns** — material inventory with care instructions, camera label scanning
- **Stitches** — technique library with tutorial links, chart upload, inspiration/ideas tab
- **Needles** — hooks and knitting needles, camera size scanning

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
| ML | ML Kit text recognition (on-device OCR for yarn labels + hook/needle size scanning, no API key) |
| Premium | Firebase Remote Config (tester allowlist + early-adopter auto-premium promo) |

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
- **IDs:** PIECE-001, YARN-001, STITCH-001, NEEDLE-001 format — generated sequentially via a counter table in Room
- **Soft deletes only** — never hard delete; use `archived: Boolean` flag
- **JSON columns** in Room for list fields (work sessions, photos, yarns used, stitches used, care instructions) — use `kotlinx-serialization-json` with TypeConverters
- **Photos** stored in `filesDir/photos/{entity}/{ID}/` (app private storage)
- **Timestamps** stored as `Long` (Unix epoch ms)
- **Dates** stored as `String` ISO-8601 (`yyyy-MM-dd`) internally; **always displayed** to the user as `dd/MM/yyyy` via `util/DateFormatUtil.kt` — never show raw ISO in the UI

---

## Design Style — B&W Minimalist
- Pure white backgrounds (`#FFFFFF`)
- Near-black text (`#1F2937`)
- Light grey borders and muted backgrounds (`#E5E7EB`, `#F3F4F6`)
- **Dark slate (`#1E293B`) for all primary interactive actions** — buttons, FABs, primary CTAs. Keep this consistent regardless of screen mode (e.g. create vs. edit) — don't switch a primary button to brand purple based on state.
- Brand purple (`#8C015E`) is reserved for brand/accent elements (logo, focused input borders, badges) — not general button backgrounds
- Red only for destructive actions (`#EF4444`)
- Border radius: 12–16dp on cards, 8dp on chips/buttons
- No decorative colors — content is the focus
- Bottom navigation: 5 tabs (Dashboard, Pieces, Yarns, Stitches, Needles)
- **Custom `Button`/`AlertDialog` colors:** when setting a non-theme-role `containerColor` (e.g. `Slate`, `BrandPurple`) via `ButtonDefaults.buttonColors()`, always pass an explicit `contentColor` too — Compose's automatic contrast computation only works for registered `MaterialTheme.colorScheme` roles (`primary`, `error`, etc.), not custom colors, and silently falls back to a low-contrast default otherwise (caused an invisible-button bug in dark mode)

## Brand (Hooky)
- App icon: purple `#8C015E` background + white infinity "oo" symbol
- Wordmark: hand-drawn brush style, purple `#8C015E` (`shared/branding/final/hooky.svg` / `.png`)
- All brand assets in `shared/branding/final/`

---

## Key Decisions
- Standalone mobile app — offline-first (Room/SQLite)
- Min SDK: API 26+
- i18n: EN + ES (Spain) + PT-BR (Brazilian Portuguese) — when adding user-facing text, always update all 3 `strings.xml` files, including any hardcoded option/dropdown values in Kotlin (don't leave values as English literals while only the field label gets a string resource)
- AI vision: A) ML Kit OCR yarn label scanner ✅, B) TFLite stitch recognition (not yet built)
- Premium: Firebase Remote Config-driven, not a backend server — manual per-tester allowlist (`premium_ids`, supports optional expiry) plus an automatic early-adopter promo (`early_adopter_cutoff` / `early_adopter_premium_days`) for anyone installing before a given date

---

## Implementation Status
1. ✅ Foundation — project, Room, Hilt, nav, theme
2. ✅ Pieces module
3. ✅ Yarns module
4. ✅ Stitches module (+ inspiration/Ideas tab with curated tutorial links)
5. ✅ Needles module
6. ✅ Dashboard
7. ✅ Camera & Photos (CameraX) + in-app photo editing
8. ✅ Polish — search, filters, price calculator, empty/loading states
9. ✅ ML Kit OCR — yarn label scanning (brand/material/care detection) + hook/needle size scanning
10. ✅ Row Counter (−/+/voice/edit dialog) + editable Work Timer (Start/Pause/Resume/Stop, persists restarts, manually correctable)
11. ✅ Library pickers for Yarn/Needle/Stitch in piece form
12. ✅ Onboarding flow + in-app Help screen
13. ✅ Export/Import backup (JSON, shareable via FileProvider)
14. ✅ Premium tier — Firebase Remote Config allowlist + early-adopter auto-promo; gated features: unlimited photos, extra counters, Cost & Profit reports with CSV export
15. ⬜ TFLite on-device stitch recognition (AI vision Phase B)
16. ⬜ Cloud backup / pattern import / billing (full Pro tier beyond current gated features)
17. 🔵 Closed testing on Google Play — see `docs/LAUNCH-STATUS.md` for current tester count and status

---

## When Starting Work
1. Read this file and `docs/PLAN.md`
2. Check The Nag project for patterns (same stack, same conventions)
3. Check `docs/LAUNCH-STATUS.md` for current Play Store / testing status before assuming it's out of date
4. When fixing a bug found through actual testing (yours or a tester's), add it to `docs/FIXES-2026-08-31.md` — it's used as evidence of active testing when reapplying to Google for production access
5. Ask before any architectural decision not covered here
