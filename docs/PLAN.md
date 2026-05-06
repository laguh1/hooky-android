# Crochet Manager — Android App Plan

**Created:** 2026-03-23
**Based on:** Desktop app at `/Users/joanasocrates/Local Documents/claude/crochet/`
**Status:** Phases 1–8 + Phase 10A complete. UI polish (icons, nav restructure) done. i18n partially wired (list screens + dashboard + search). See per-phase detail below.

---

## What the Desktop App Does

A personal crochet hobby management system tracking three entities:

1. **Pieces** — finished/in-progress crochet items (shawls, scarves, blankets, etc.)
   - Work status: in_progress → finished → ready
   - Destination: for_sale / sold / for_gift / gifted / for_self / in_use
   - Tracks: dimensions, work hours, sessions, yarns used, stitches used, photos
   - Pricing: material cost, suggested price, sale info
   - Gift tracking: recipient name

2. **Yarns** — material inventory
   - Brand, color, color code, material type, weight category
   - Ball weight/length, price paid, purchase location, quantity owned
   - Care instructions, recommended hook/needle sizes

3. **Stitches** — technique library
   - Name (standardized via hookfully.com), aliases, Spanish name
   - Category (basic/textured/lace/colorwork/specialty), difficulty
   - Tutorial links (Hookfully, YouTube, instruction pages)
   - Photos/diagrams

**Additional features:**
- Dashboard with stats (pieces by type, hours worked, revenue, in-progress items)
- Price calculator (material cost + labor at €8/hr + complexity factor + profit margin)
- Time estimation and completion date prediction
- Photo gallery per entity
- Soft archive (never hard-delete)

---

## Android App Scope

### What to include (full feature parity)
- All 3 entities with full CRUD
- Dashboard
- Photo capture with device camera
- Price calculator
- Offline-first (no server needed)

### Android-specific additions
- **Camera integration** — take photos directly from the app
- **Share** — share piece info/photo via Android share sheet
- **Search** — quick search across pieces, yarns, stitches
- **Barcode scanner** — scan yarn label barcode to look up product (future/optional)

### What changes vs desktop
- No Python backend — Room (SQLite) replaces JSON files
- No Electron IPC — direct database access from ViewModels
- Photo storage in app's internal storage (not flat file system)
- Navigation adapted for mobile (bottom nav + back stack)

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material Design 3 |
| Architecture | MVVM + Clean Architecture |
| Database | Room (SQLite) |
| Dependency Injection | Hilt |
| Navigation | Navigation Compose |
| Images | Coil (display) + CameraX (capture) |
| State management | StateFlow + ViewModel |
| Async | Coroutines + Flow |

---

## Project Structure

```
crochet-android/
├── PLAN.md                          # This file
├── CLAUDE.md                        # Claude instructions
└── CrochetManager/                  # Android Studio project
    ├── app/
    │   └── src/main/
    │       ├── AndroidManifest.xml
    │       └── java/com/crochet/manager/
    │           ├── data/
    │           │   ├── db/
    │           │   │   ├── CrochetDatabase.kt
    │           │   │   ├── dao/
    │           │   │   │   ├── PieceDao.kt
    │           │   │   │   ├── YarnDao.kt
    │           │   │   │   └── StitchDao.kt
    │           │   │   └── converters/
    │           │   │       └── Converters.kt    (List<String>, enums)
    │           │   ├── entities/
    │           │   │   ├── PieceEntity.kt
    │           │   │   ├── YarnEntity.kt
    │           │   │   ├── StitchEntity.kt
    │           │   │   └── CareInstructionsEntity.kt
    │           │   └── repository/
    │           │       ├── PieceRepository.kt
    │           │       ├── YarnRepository.kt
    │           │       └── StitchRepository.kt
    │           ├── domain/
    │           │   ├── model/
    │           │   │   ├── Piece.kt
    │           │   │   ├── Yarn.kt
    │           │   │   ├── Stitch.kt
    │           │   │   └── enums/
    │           │   │       ├── PieceType.kt
    │           │   │       ├── WorkStatus.kt
    │           │   │       ├── Destination.kt
    │           │   │       ├── Material.kt
    │           │   │       └── WeightCategory.kt
    │           │   └── usecase/
    │           │       ├── piece/
    │           │       │   ├── GetPiecesUseCase.kt
    │           │       │   ├── CreatePieceUseCase.kt
    │           │       │   ├── UpdatePieceUseCase.kt
    │           │       │   └── ArchivePieceUseCase.kt
    │           │       ├── yarn/
    │           │       └── stitch/
    │           ├── ui/
    │           │   ├── MainActivity.kt
    │           │   ├── navigation/
    │           │   │   └── CrochetNavGraph.kt
    │           │   ├── theme/
    │           │   │   ├── Theme.kt
    │           │   │   ├── Color.kt
    │           │   │   └── Type.kt
    │           │   ├── dashboard/
    │           │   │   ├── DashboardScreen.kt
    │           │   │   └── DashboardViewModel.kt
    │           │   ├── pieces/
    │           │   │   ├── list/
    │           │   │   │   ├── PieceListScreen.kt
    │           │   │   │   └── PieceListViewModel.kt
    │           │   │   ├── detail/
    │           │   │   │   ├── PieceDetailScreen.kt
    │           │   │   │   └── PieceDetailViewModel.kt
    │           │   │   └── form/
    │           │   │       ├── PieceFormScreen.kt
    │           │   │       └── PieceFormViewModel.kt
    │           │   ├── yarns/             (same structure as pieces)
    │           │   ├── stitches/          (same structure as pieces)
    │           │   └── components/
    │           │       ├── PieceCard.kt
    │           │       ├── YarnCard.kt
    │           │       ├── StitchCard.kt
    │           │       ├── PhotoGallery.kt
    │           │       ├── StatusBadge.kt
    │           │       └── ConfirmDialog.kt
    │           └── di/
    │               ├── DatabaseModule.kt
    │               └── RepositoryModule.kt
    └── build.gradle.kts
```

---

## Database Schema

### pieces table
```kotlin
@Entity(tableName = "pieces")
data class PieceEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val pieceId: String,              // "PIECE-001"
    val name: String,
    val type: String,                 // PieceType enum
    val workStatus: String,           // WorkStatus enum
    val destination: String,          // Destination enum
    val widthCm: Float? = null,
    val lengthCm: Float? = null,
    val dateStarted: String? = null,
    val dateFinished: String? = null,
    val workHours: Float? = null,
    val workSessions: String,         // JSON: List<WorkSession>
    val hookSizeMm: Float? = null,
    val photos: String,               // JSON: List<String> (file paths)
    val price: Float? = null,
    val materialCost: Float? = null,
    val giftRecipient: String? = null,
    val salePlatform: String? = null,
    val saleLink: String? = null,
    val soldDate: String? = null,
    val soldPrice: Float? = null,
    val yarnsUsed: String,            // JSON: List<String> (YARN-IDs)
    val stitchesUsed: String,         // JSON: List<String> (STITCH-IDs)
    val notes: String? = null,
    val archived: Boolean = false,
    val archivedDate: String? = null,
    val archivedReason: String? = null,
    val createdAt: Long,
    val updatedAt: Long
)
```

### yarns table
```kotlin
@Entity(tableName = "yarns")
data class YarnEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val yarnId: String,               // "YARN-001"
    val name: String,
    val brand: String? = null,
    val color: String,
    val colorCode: String? = null,
    val material: String,             // Material enum
    val materialComposition: String? = null,
    val materialSpecs: String? = null,
    val weightCategory: String? = null,
    val ballWeightG: Float? = null,
    val ballLengthM: Float? = null,
    val pricePaid: Float? = null,
    val purchaseLocation: String? = null,
    val purchaseLink: String? = null,
    val purchaseDate: String? = null,
    val quantityOwned: Int? = null,
    val hookSizeMm: Float? = null,
    val needleSizeMm: String? = null,
    val gauge: String? = null,
    val careInstructions: String,     // JSON: CareInstructions object
    val photos: String,               // JSON: List<String>
    val notes: String? = null,
    val archived: Boolean = false,
    val archivedDate: String? = null,
    val archivedReason: String? = null,
    val createdAt: Long,
    val updatedAt: Long
)
```

### stitches table
```kotlin
@Entity(tableName = "stitches")
data class StitchEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val stitchId: String,             // "STITCH-001"
    val name: String,
    val nameAliases: String,          // JSON: List<String>
    val nameEs: String? = null,
    val abbreviation: String? = null,
    val category: String? = null,     // StitchCategory enum
    val difficulty: String? = null,   // Difficulty enum
    val description: String,
    val hookfullyLink: String? = null,
    val instructionLink: String? = null,
    val videoLink: String? = null,
    val photos: String,               // JSON: List<String>
    val notes: String? = null,
    val archived: Boolean = false,
    val archivedDate: String? = null,
    val archivedReason: String? = null,
    val createdAt: Long,
    val updatedAt: Long
)
```

---

## Navigation Structure

```
Bottom Navigation Bar:
├── Dashboard (home icon)
├── Pieces (yarn ball icon)
├── Yarns (palette icon)
└── Stitches (needle icon)

Each section:
  List Screen
    → Detail Screen
      → (Edit) Form Screen
    → (Create) Form Screen
```

---

## UI Screens

### Dashboard
- Stats row: total pieces / yarns / stitches / hours
- "In Progress" horizontal scroll list
- "Recently Finished" section
- Quick stats: pieces sold, total revenue

### Piece List
- Filter chips: All / In Progress / Finished / Ready
- Search bar
- Grid or list toggle
- FAB: Add piece
- Each card: photo thumbnail, name, type badge, work status, destination

### Piece Detail
- Photo gallery (full width, swipeable)
- Name + type
- Status chips (work status + destination)
- Dimensions
- Dates (started / finished)
- Work hours + sessions list (expandable)
- Yarns used (chips linking to yarn detail)
- Stitches used (chips linking to stitch detail)
- Pricing section (material cost, suggested price, sale info)
- Notes
- Actions: Edit / Archive

### Piece Form (Create/Edit)
- Sections: Basic Info / Dates & Hours / Materials / Pricing / Notes
- Photo picker (camera or gallery)
- Yarn selector (multi-select from yarn list)
- Stitch selector (multi-select from stitch list)

### Yarn List / Detail / Form — same pattern

### Stitch List / Detail / Form — same pattern

---

## Implementation Phases

### Phase 1 — Foundation ✅
- [x] Create Android Studio project (Kotlin + Compose)
- [x] Configure `build.gradle.kts` with all dependencies
- [x] Setup Hilt
- [x] Create Room database with all 3 entities + DAOs
- [x] Create repositories
- [x] Setup Navigation Compose with bottom nav
- [x] Create Material 3 theme (B&W minimalist)
- [ ] Initialize Git

### Phase 2 — Pieces Module ✅
- [x] PieceListScreen + ViewModel
- [x] PieceDetailScreen + ViewModel
- [x] PieceFormScreen + ViewModel (create + edit)
- [x] Archive functionality with confirmation dialog
- [x] Photo display (Coil)
- [x] Work sessions sub-list

### Phase 3 — Yarns Module ✅
- [x] YarnListScreen + ViewModel
- [x] YarnDetailScreen + ViewModel
- [x] YarnFormScreen + ViewModel
- [x] Care instructions display

### Phase 4 — Stitches Module ✅
- [x] StitchListScreen + ViewModel
- [x] StitchDetailScreen + ViewModel
- [x] StitchFormScreen + ViewModel
- [x] Clickable links (Hookfully, YouTube)

### Phase 5 — Dashboard ✅
- [x] Stats calculation from Room queries
- [x] In Progress list
- [x] Recently finished list
- [x] Summary cards

### Phase 6 — Camera & Photos ✅
- [x] CameraX integration
- [x] Photo storage in app internal storage (`filesDir/photos/{entity}/{id}/`)
- [x] Photo gallery component (swipeable HorizontalPager, shared component)
- [x] Delete photo functionality (with confirmation dialog)

### Phase 7 — Polish ✅
- [x] Search across all entities (global SearchScreen, 300ms debounce)
- [x] Filter chips on list screens
- [x] Price calculator screen (material + labor + complexity + margin)
- [x] Empty states (reusable EmptyState component)
- [x] Loading states (reusable LoadingState component)
- [x] Error handling (ErrorSnackbarEffect, all screens)

### Phase 7B — Needles Module ✅
- [x] NeedleListScreen + ViewModel
- [x] NeedleDetailScreen + ViewModel
- [x] NeedleFormScreen + ViewModel (create + edit)
- [x] ML Kit OCR needle label scanner (NeedleScanConfirmDialog)
- [x] Needle tab added to bottom navigation

### Phase 7C — UI Polish & Branding ✅ (2026-05)
- [x] Custom SVG icons converted to Android Vector Drawables: `ic_pieces`, `ic_yarns`, `ic_needles`, `ic_stitches`
- [x] Custom icons applied to bottom nav bar (Pieces, Yarns, Stitches, Needles tabs)
- [x] Custom icons applied to list screen cards (photo placeholder) and empty states
- [x] Custom icons applied to SearchScreen result rows
- [x] EmptyState component: icon type changed `ImageVector` → `Painter`
- [x] Navigation restructure: Settings button moved from bottom nav to each list screen's TopAppBar
- [x] Bottom nav: 5 tabs (Dashboard, Pieces, Yarns, Stitches, Needles) — Settings removed
- [x] Price Calculator moved from Dashboard shortcut → PieceListScreen TopAppBar button
- [x] Form button labels: "Create Needle" → "Add Needle", "Create Yarn" → "Add Yarn"
- [x] Palette API integration for dynamic color extraction from photos
- [x] Splash screen: full-screen brand purple `#8C015E` + white infinity symbol (`ic_splash_icon.xml` scaled to 55% to avoid circular clip) — `androidx.core:core-splashscreen:1.0.1`
- [x] Dashboard logo 20% larger (48dp → 58dp)
- [x] `WorkStatus.READY` removed — only `IN_PROGRESS` and `FINISHED` remain; `PieceFilter.READY` and `piece_filter_ready` strings removed from all 3 locales

### Phase 8 — Internationalisation (i18n) ✅ infrastructure / ⏳ wiring in progress
Android's built-in string resource system handles language switching automatically — no runtime logic needed.

**Approach:**
- All UI text lives in `res/values/strings.xml` (English — default)
- Spanish translation in `res/values-es/strings.xml`
- PT-BR translation in `res/values-pt-rBR/strings.xml`
- Android picks the correct file based on device locale automatically
- A `translations/` folder at project root contains a human-readable reference document for translators/future languages

**Infrastructure (done):**
- [x] `res/values/strings.xml` — 236 strings, all screens + enum display names (EN)
- [x] `res/values-es/strings.xml` — complete Spanish (Spain) translation
- [x] `res/values-pt-rBR/strings.xml` — complete Brazilian Portuguese translation
- [x] `AppCompatActivity` + `AppCompatDelegate.setApplicationLocales()` + `locale_config.xml`
- [x] `translations/strings-reference.md` — side-by-side EN/ES/PT-BR reference table

**Screens wired to stringResource() (done):**
- [x] `ui/navigation/CrochetNavGraph.kt` — nav bar labels
- [x] `ui/dashboard/DashboardScreen.kt`
- [x] `ui/pieces/list/PieceListScreen.kt`
- [x] `ui/yarns/list/YarnListScreen.kt`
- [x] `ui/stitches/list/StitchListScreen.kt`
- [x] `ui/search/SearchScreen.kt`
- [x] `ui/calculator/PriceCalculatorScreen.kt`
- [x] `ui/camera/CameraScreen.kt`

**Screens still hardcoded (next i18n session):**
- [ ] `ui/pieces/detail/PieceDetailScreen.kt`
- [ ] `ui/pieces/form/PieceFormScreen.kt`
- [ ] `ui/yarns/detail/YarnDetailScreen.kt`
- [ ] `ui/yarns/form/YarnFormScreen.kt`
- [ ] `ui/stitches/detail/StitchDetailScreen.kt`
- [ ] `ui/stitches/form/StitchFormScreen.kt`
- [ ] `ui/needles/list/NeedleListScreen.kt` (add `needle_*` keys to all 3 strings.xml first)
- [ ] `ui/needles/detail/NeedleDetailScreen.kt`
- [ ] `ui/needles/form/NeedleFormScreen.kt`
- [ ] `ui/components/ConfirmDialog.kt`
- [ ] `ui/components/StatusBadge.kt`
- [ ] `ui/yarns/form/YarnLabelScanConfirmDialog.kt`
- [ ] `ui/components/NeedleScanConfirmDialog.kt`

**Missing string keys to add before wiring needles:**
`needle_empty_title`, `needle_empty_subtitle`, `needle_search_hint`, `needles_title` — add to all 3 strings.xml files.

**Future: In-app language picker (backlog)**
Currently follows device system locale. A future Settings option could override it via `AppCompatDelegate.setApplicationLocales()` + DataStore. No architectural changes needed.

### Phase 9 — Data Migration (optional, future)
- [ ] Import from desktop JSON files (one-time migration tool)
- [ ] Export to JSON (backup)

### Phase 10 — AI Vision Features (3-phase roadmap)

**Key differentiator:** automatic stitch and yarn information extraction from photos — no manual data entry.
**Principle:** each phase only proceeds if the previous one gets traction. No wasted effort.

```
Phase A → gets users
Phase B → gets reviews & retention
Phase C → gets revenue
```

---

#### Phase A — Yarn Label Scanner (ship with v1, ML Kit OCR)
**Goal:** photo of a yarn label → auto-fill yarn form fields
**Stack:** ML Kit Text Recognition (on-device, free, offline, no API key)
**What it extracts:**
- Brand name
- Yarn weight category (lace / DK / worsted / bulky)
- Fiber content (100% cotton, 80% wool 20% acrylic, etc.)
- Color name and color code
- Ball weight (g) and length (m)
- Recommended hook/needle size
- Care symbols (via barcode or text)

**Implementation:**
- [ ] Add `com.google.mlkit:text-recognition` dependency
- [ ] Create `YarnLabelScannerService` — takes `Bitmap`, returns `YarnLabelScanResult` data class
- [ ] Structured regex/NLP parsing layer to extract fields from raw OCR text
- [ ] "Scan Label" button on YarnFormScreen → CameraX → preview → confirm extracted fields
- [ ] All suggestions shown for user review — never auto-saved without confirmation
- [ ] Barcode scanning as bonus: `com.google.mlkit:barcode-scanning` → look up product if barcode found

**Estimated effort:** 1–2 weeks

---

#### Phase B — Stitch & Project Recognition (after traction, TFLite custom model)
**Goal:** photo of crochet work → identify stitch type, gauge, complexity
**Stack:** TensorFlow Lite custom model (on-device, free, offline)
**What it extracts:**
- Stitch pattern type (granny square, ripple, single/double/treble crochet, etc.)
- Approximate gauge / stitch density
- Pattern complexity estimate
- Piece type suggestion (shawl, blanket, bag, etc.)

**Implementation:**
- [ ] Collect and label training dataset of crochet images (minimum ~500 images per stitch type)
- [ ] Fine-tune MobileNet or EfficientNet-Lite on labeled dataset
- [ ] Export as `.tflite` model, bundle in `assets/`
- [ ] Create `StitchRecognitionService` — takes `Bitmap`, returns top predictions with confidence scores
- [ ] ML Kit Image Labeling for general piece type detection (zero training needed)
- [ ] Pre-fill StitchesUsed and PieceType fields in form
- [ ] Show confidence score — suggestion only, never auto-saved
- [ ] Dependency: `org.tensorflow:tensorflow-lite` + `com.google.mlkit:image-labeling`

**Note:** building the labeled training dataset is the hardest part — crowdsource from users if possible.
**Estimated effort:** 2–3 months (model training dominates)

---

#### Phase C — Full AI Analysis via Backend (if it pays off, freemium)
**Goal:** rich intelligent analysis without on-device model limitations
**Stack:** your own lightweight backend (proxies Claude/OpenAI Vision API) — user never needs an account
**What it adds over Phase B:**
- Yarn weight and fiber texture hints from project photos
- Detailed pattern analysis
- Error detection (dropped stitches, tension inconsistencies)
- Multi-language pattern description

**Business model:**
- Free tier: Phase A (ML Kit OCR) + Phase B (TFLite on-device)
- Pro tier: Phase C (backend AI analysis) — subscription or per-scan credits

**Implementation:**
- [ ] Simple backend (Node.js or Python FastAPI) — single `/analyze-image` endpoint
- [ ] You hold the API key — proxied securely, users just use the app
- [ ] Android: `ImageAnalysisRepository` with `analyzeWithBackend(bitmap)` method
- [ ] Graceful degradation — if offline or free tier, falls back to Phase A/B

**Estimated effort:** 2–4 weeks once decided

---

#### Existing features (carry forward)
- [ ] Stitch error detection (compare WIP photo vs reference stitch pattern — Phase B model)
- [ ] Piece auto-categorisation overlay with confidence score

---

### Phase 11 — Row Counter & Work Timer ✅

**Goal:** let users track progress row-by-row and log time worked per piece, with both values feeding the price calculator.

**Competitor reference:** `docs/competitor-research/row_counter_myrowapp.jpeg` — MyRowApp uses a large circular counter with − / + buttons, a running timer in the header, and a manual "Change Counter Value" dialog. Hooky's version follows the same UX concept in the B&W minimalist style.

**Design constraint:** all UI strictly follows Hooky's brand palette — white backgrounds, near-black text, slate (`#1E293B`) for interactive elements, brand purple (`#8C015E`) for accents and active/running states only. No blue, no custom colors outside the defined theme.

---

#### 11A — Data model additions (PieceEntity + Room migration)

New fields on `PieceEntity`:

```kotlin
val rowCount: Int = 0,                  // current completed row count
val targetRowCount: Int? = null,         // optional target (enables progress bar)
val timerTotalSeconds: Long = 0,         // accumulated work time in seconds across all sessions
val timerIsRunning: Boolean = false,     // persisted so timer survives app restarts
val timerSessionStartAt: Long? = null,   // epoch ms when current session started; null if not running
```

- `workHours` (already on entity) stays as the **canonical labor value** for the price calculator
- When a timer session ends ("Stop"), `timerTotalSeconds` is converted to hours and written to `workHours`
- Both `rowCount` and `timerTotalSeconds`/`workHours` are stored on **all pieces regardless of status**

**Room migration:** increment DB version, add the 5 new columns with defaults.

---

#### 11B — New piece form: starting row prompt

When creating a new piece:
- Add **"Starting row"** field (integer, default 0) — with helper text: "0 for a brand new piece, or enter rows already completed"
- Add **"Target rows"** field (integer, optional) — enables a progress bar on the detail screen
- The field is visible regardless of status so pieces added retrospectively can have a row count set

---

#### 11C — Row Counter widget (Piece Detail screen)

A new card section on `PieceDetailScreen`, always visible:

```
┌─────────────────────────────────┐
│  Rows                      ✎   │
│                                 │
│     [ − ]    47    [ + ]        │
│                                 │
│  ████████████░░░░  47 / 80 rows │  ← only if targetRowCount is set
└─────────────────────────────────┘
```

- **− button**: decrement by 1 (disabled at 0)
- **+ button**: increment by 1
- **🎤 mic button**: voice input — say a number, counter increments by that amount (see voice spec below)
- **✎ (edit) icon**: opens "Set row count" dialog — manual number entry + optional "Set target rows" — same as competitor's "Change Counter Value"
- **Progress bar**: shown only when `targetRowCount` is set
- Editing is enabled for all statuses (a finished piece can still have its final row count recorded)
- Each tap saves immediately to DB via ViewModel action (no pending state — row counter must be reliable)

**Voice control spec:**
- Interaction: tap mic → speak a number → counter increments by that amount → mic closes
- Most common use: say **"one"** after finishing each row (hands-free +1)
- Saying "3" means add 3 rows, not jump to row 3 — absolute row setting is for the edit dialog
- Uses Android `SpeechRecognizer` with device locale — works in English, Spanish, and Portuguese with no extra localization work; the recognizer transcribes numbers natively in whatever language the device is set to
- No keyword commands ("next row", "siguiente fila") — numbers only, so there is nothing language-specific to translate
- Parsing: strip non-numeric text from the transcript, parse the first integer found; if no number detected, show a brief "didn't catch that" snackbar

---

#### 11D — Work Timer widget (Piece Detail screen)

A new card section on `PieceDetailScreen`, always visible, below the row counter:

```
┌─────────────────────────────────┐
│  Work Timer                     │
│                                 │
│         01 : 23 : 45            │  ← HH:MM:SS, live update every second
│                                 │
│   [ Start ]                     │  ← when stopped
│   [ Pause ]  [ Stop ]           │  ← when running
│   [ Resume ] [ Stop ]           │  ← when paused
└─────────────────────────────────┘
```

**States:**
- **Idle** (never started, or after Stop): shows total accumulated time, "Start" button
- **Running**: live countdown ticking, "Pause" + "Stop" buttons
- **Paused**: frozen display, "Resume" + "Stop" buttons

**"Stop" behaviour:**
1. Calculates session duration: `now - timerSessionStartAt` (if was running) or uses paused accumulation
2. Adds to `timerTotalSeconds`
3. Converts total to hours: `timerTotalSeconds / 3600f` → writes to `workHours` on the piece
4. Resets `timerIsRunning = false`, `timerSessionStartAt = null`

**Persistence across restarts:** on app launch, if `timerIsRunning == true`, the ViewModel re-derives elapsed time from `timerSessionStartAt` and resumes the live display.

**Implementation notes:**
- Use a `CoroutineScope` + `ticker` (or `flow { while(true) { emit(Unit); delay(1000) } }`) inside the ViewModel for the live tick
- Timer state is held in ViewModel `StateFlow`; DB is only written on Pause/Stop, not every second

---

#### 11E — Price calculator integration

**From piece detail:** add a **"Calculate price"** button that navigates to the price calculator screen pre-populated with:
- `materialCost` from the piece's `materialCost` field
- `workHours` from the piece's `workHours` field (= accumulated timer hours)

The standalone calculator screen keeps its manual entry as-is — pre-population is additive.

**Formula is unchanged:** `laborCost = workHours × laborRate`

---

#### Checklist

- [x] DB migration (version 3→4: rowCount, targetRowCount in v3; timerTotalSeconds, timerIsRunning, timerSessionStartAt, needlesUsed in v4)
- [x] Update `PieceEntity` with all new fields
- [x] `PieceFormScreen` — starting row + target rows fields
- [x] `PieceDetailViewModel` — `IncrementRow`, `DecrementRow`, `IncrementRowBy`, `SetRowCount`, `StartTimer`, `PauseTimer`, `ResumeTimer`, `StopTimer` actions
- [x] `RowCounterCard` composable (− / + / mic / edit dialog / progress bar)
- [x] `RECORD_AUDIO` permission request for voice input (SpeechRecognizer, numbers only)
- [x] `WorkTimerCard` composable (HH:MM:SS live tick, Start/Pause/Resume/Stop, BrandPurple accent when running)
- [x] Timer persistence on app restart (restores from DB timerIsRunning + timerSessionStartAt)
- [x] Stop writes `workHours = timerTotalSeconds / 3600f` to piece
- [ ] "Calculate price" deep-link from piece detail → calculator with pre-filled values
- [ ] Update price calculator to accept optional pre-filled `materialCost` + `workHours` nav args
- [ ] i18n: add new strings to `strings.xml`, `values-es`, `values-pt-rBR`

#### Phase 11 Extension — Library Pickers ✅ (2026-05)
- [x] `needlesUsed` field on `PieceEntity` (JSON list of NEEDLE-IDs)
- [x] `LibraryMultiPicker` composable — removable chips + dialog with checkboxes
- [x] "Materials & Stitches" form section: Yarn / Needle / Stitch pickers from their libraries
- [x] PieceDetailScreen resolves IDs to names (yarnNames, stitchNames, needleNames maps)
- [x] "Needles Used" chip section added to detail view

---

## Key Dependencies (build.gradle.kts)

```kotlin
// Compose BOM
implementation(platform("androidx.compose:compose-bom:2024.xx.xx"))
implementation("androidx.compose.ui:ui")
implementation("androidx.compose.material3:material3")
implementation("androidx.compose.ui:ui-tooling-preview")

// Navigation
implementation("androidx.navigation:navigation-compose:2.7.x")

// Room
implementation("androidx.room:room-runtime:2.6.x")
implementation("androidx.room:room-ktx:2.6.x")
ksp("androidx.room:room-compiler:2.6.x")

// Hilt
implementation("com.google.dagger:hilt-android:2.51.x")
ksp("com.google.dagger:hilt-android-compiler:2.51.x")
implementation("androidx.hilt:hilt-navigation-compose:1.2.x")

// Coil (images)
implementation("io.coil-kt:coil-compose:2.6.x")

// CameraX
implementation("androidx.camera:camera-camera2:1.3.x")
implementation("androidx.camera:camera-lifecycle:1.3.x")
implementation("androidx.camera:camera-view:1.3.x")

// Kotlinx Serialization (for JSON lists in Room)
implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.x")

// Lifecycle
implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.x")
implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.x")
```

---

## Decisions & Notes

- **No sync with desktop** initially — mobile is standalone, same data model
- **ID format preserved**: PIECE-001 etc, generated sequentially in Room via counter table
- **Soft deletes only** — same policy as desktop (archived flag, never hard delete)
- **Photos stored locally** in app's private storage (`filesDir/photos/pieces/PIECE-001/`)
- **Color palette**: Black & white minimalist — matches desktop app. White backgrounds, near-black text, light grey borders/muted, dark slate (#1E293B) for primary actions, red (#EF4444) for destructive only. No decorative colors.
- **Min SDK**: API 26+ (same as The Nag project)

---

## Reference

- Desktop app: `/Users/joanasocrates/Local Documents/claude/crochet/`
- The Nag Android project (same tech stack): `/Users/joanasocrates/Local Documents/claude/nag/`
- CLAUDE.md (desktop): full data model documentation
