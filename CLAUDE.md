# Hooky — Claude Instructions

## Project Structure

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

## Architecture Rules
- **Layer dependency:** UI → Domain → Data (one-way only)
- **Domain layer:** no Android imports
- **Data layer:** Room entities, DAOs, repositories
- **ViewModels:** business logic + StateFlow UiState
- **Use cases:** one per operation (GetPiecesUseCase, CreatePieceUseCase, etc.)

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

## Labels Must Never Break Mid-Word
ES and PT-BR labels are longer than English, and many phones are only 360dp wide. A word split across lines ("Edit-ar", "Comprime-nto") is a bug; wrapping between words is fine. This has regressed twice (Sept 2026 Yarn detail, Oct 2026 Piece detail after a third button was added), so treat it as part of every UI change:
- **Text in a narrow slot** (two or more buttons/chips/fields side by side, anything with `Modifier.weight`): use `ui/components/WordSafeText.kt`, or for rows of three buttons the tight-padding + `maxLines = 1` pattern from `YarnDetailScreen` / `PieceDetailScreen` action rows.
- **Never give a text container a fixed height** (`.height(52.dp)`) when its label can wrap; use `heightIn(min = …)`.
- **Adding a button/chip/field to an existing row** means re-checking the whole row, and the same row on the sibling screens (Piece/Yarn/Stitch/Needle detail and forms are separate code — a fix in one is not shared).
- **Before calling a UI change done:** check it in PT-BR and ES at 360dp width (`adb shell wm density 480` on the Pixel_6 AVD, which is 411dp by default and hides the problem) and once with font scale 1.3. Reset both afterwards.

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

## When Starting Work
1. Read this file and `docs/PLAN.md`
2. Check The Nag project for patterns (same stack, same conventions)
3. Check `docs/LAUNCH-STATUS.md` for current Play Store / testing status before assuming it's out of date
4. When fixing a bug found through actual testing (yours or a tester's), add it to `docs/FIXES-2026-08-31.md` — it's used as evidence of active testing when reapplying to Google for production access
5. Ask before any architectural decision not covered here
