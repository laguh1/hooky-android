# Next release — running list of changes

Started 2026-10-24. Branch `fix/suggest-price-i18n`. Last release on Play: build 25 / 1.8.0.
Add each new change here as it lands; draft the Play release notes from this list at the end.

Build: versionCode **27** (26 was already used in Play Console) / versionName **1.9.0** (bumped 2026-10-25; the suggestions links are a
small new feature). Bundle: `android/app/build/outputs/bundle/release/app-release.aab`.

## Changes so far

### Visible to users (go in the release notes)
1. **Newest photo first** (pieces, yarns, stitches, needles). New photos go to
   the front of the gallery, so the first photo shown is the most recent and
   swiping goes back in time. The piece share card uses the newest photo too.
   Photos saved before this update are flipped once on upgrade (database
   migration 9 → 10), and old backups are flipped when restored (backup format
   version 2), so everything ends up newest first.
2. **Portuguese (Brazil): rows are now "carreiras"** instead of "fileiras"
   (row counter, progress, pace, help tips).
3. **More screens translated into Spanish and Portuguese**: these were still in
   English. "Suggest price →" on the piece screen, search hints ("Enter at
   least 2 characters", "Nothing matched…", section names), photo editor
   buttons (Rotate left, Flip, Rotate right, Save), the needle "Size detected"
   dialog, the delete-photo dialog, a Cancel button in the piece form, and
   "Finished <date>" on the dashboard, scan error messages, the free-version
   photo limit message, and "Work time" / "Rows" on the share card.
4. **Share card fixes**: piece type and destination now show in your language
   (they showed raw codes like "SHAWL · FOR_SALE"), and longer ES/PT labels no
   longer overlap.
5. **Photo editor fix**: rotating or flipping a photo now saves the first time.
   Before, the first save came out turned the wrong way and you had to edit
   twice. Photos on the share card and in the label scanner are upright too.
6. **Guide and suggestions links**: Settings → Help and the Help & Tips screen
   now link to the full guide with videos and to the new suggestions page on
   the website (hookyapp.netlify.app/feedback), both opening in the app's
   language.

### Internal (not in the release notes)
- Photo description for accessibility ("Photo 1"…) translated.
- Logged as fix #14 (photo editor) and fix #13 in `docs/FIXES-2026-08-31.md`.

- Tests added: `PhotoOrderTest` (unit) and `MigrationTest` (instrumented,
  Room 9 → 10). Both pass (2026-10-24).

## Before release
- [x] New/changed labels in PT-BR and ES at 360dp + font scale 1.3: Settings → Help rows and the Help & Tips card checked on the emulator 2026-10-25, wrap between words
- [x] Suggestions link opens hookyapp.netlify.app in Chrome (emulator)
- [x] Unit tests (PhotoOrderTest) and instrumented tests (MigrationTest, BitmapUtilsTest) pass
- [x] Bump versionName (1.9.0), signed release bundle built
- [x] Privacy policy updated for Play Billing / Firebase / ML Kit (live 2026-10-25)
- [ ] **On the phone, after installing from internal testing:** rotate a camera photo and a gallery photo once, save, check it stays as shown (fix #14)
- [ ] **On the phone:** add photos to a piece and a yarn (newest first); a piece made before the update still shows its newest photo first
- [ ] **On the phone:** a share card in PT (type/destination translated)
- [ ] Play Console → App content → Data safety: update per `docs/PRIVACY-UPDATE-DRAFT.md` (Device or other IDs, Purchase history, Diagnostics)
- [ ] Play Console → App content → Privacy policy URL: switch to `https://hookyapp.netlify.app/privacy` (the GitHub copy also shows the new text)

## Release notes (final, 1.9.0)

<en-US>
• New: send us ideas and suggestions from Settings → Help
• Full guide with videos, one tap away in Help
• Newest photo first: your latest photo now shows first
• Photo editor: rotating a photo now saves correctly the first time
• Share card fully in your language
• More screens translated into Spanish and Portuguese
• Small fixes and improvements
</en-US>
<pt-BR>
• Novo: envie ideias e sugestões em Configurações → Ajuda
• Guia completo com vídeos, a um toque na Ajuda
• Foto mais recente primeiro: sua última foto aparece na frente
• Editor de fotos: girar a foto agora salva certo na primeira vez
• Contador agora fala em "carreiras"
• Cartão de compartilhamento todo em português
• Mais telas traduzidas e pequenas correções
</pt-BR>
<es-ES>
• Nuevo: envíanos ideas y sugerencias desde Ajustes → Ayuda
• Guía completa con vídeos, a un toque en Ayuda
• La foto más reciente primero: tu última foto aparece delante
• Editor de fotos: girar una foto ahora se guarda bien a la primera
• La tarjeta para compartir, toda en español
• Más pantallas traducidas y pequeñas correcciones
</es-ES>

## For the release after 1.9.0 (noted 2026-10-31, not started)
- PT-BR: "gancho" is Portugal Portuguese / literal. Brazilian crocheters say "agulha": `yarn_label_hook_size` "Tamanho gancho" → "Agulha recomendada", `piece_field_hook_size_mm`, `piece_section_dimensions_hook`, and "agulha ou gancho" phrases. `yarn_field_gauge` / `yarn_label_gauge` "Tensão" → "Amostra". Spotted while making the toalhinha reel.
- ES: Help text says "vueltas" while the counter says "filas".
- PT/ES: Dashboard Search/Settings accessibility labels are still in English.

## Feature plan: "the app for crafters who sell" (agreed 2026-11-02)
Already in the app (don't rebuild): balls used per yarn on a piece, material cost from balls × price paid, Suggested Price, work timer.

**Next release**
1. Yarn amounts as fractions (2⅓ balls), not only whole balls. *In progress on `feature/yarn-usage-decimals`.*
   - Fix: piece detail showed no yarns for pieces saved with ball counts (it read the old ID-only format).
   - Yarn detail: "Used in pieces" and "Left" worked out from the pieces (owned − used), so editing a piece never double-counts. Archived pieces don't count.
2. ✅ *Done on `feature/excel-export` (2026-11-02):* Reports share button now sends one .xlsx (Pieces / Customers / Yarns tabs, app language, real numbers and dates, frozen header, SUM totals, Total spent = price × balls). Old plan: Excel-friendly export: save a real file, not text. Today the CSV is sent as text with comma separators, which Spanish/Brazilian Excel opens in one column. Prefer .xlsx with tabs Pieces / Customers / Yarns (stock, where bought, unit price, total spent, notes). Then a tutorial reel showing the file in Excel.
3. ✅ *Done on `feature/stash-value` (2026-11-02):* "Yarn in stock" card on the dashboard (balls left × price paid), free for everyone.

**After that**
- ✅ *Done on `feature/yarn-colours` (2026-11-02):* real colour saved per yarn (DB 11), swatches, tap-the-photo picker, smarter auto-detection, circles everywhere, colour-group filter + sort by colour on Yarns, pieces filtered by the colours of their yarns.
- "Do I have enough yarn?" for planned pieces + shopping list.
- Orders with due date, deposit and reminder.
- Year summary for taxes (autónomo / MEI).
- Automatic Google Drive backup; home-screen widget.
- Check Play Console store-listing conversion and retention before big builds.

## Also noted 2026-11-02
- ✅ **Done on `feature/free-extra-counter` (2026-11-02): free users get 1 extra counter, Pro for more.** Store listing text still says Pro has "extra counters": change to "more extra counters" when publishing.
- **Extra counters are invisible without Pro** (`PieceDetailScreen` shows `ExtraCountersCard` only `if (isPremium)`). A 4★ review on 1.7.0 (before Pro could be bought) said the counter is "useless" without repeat/stitch counters. Options: make 1–2 extra counters free, or at least show the card to free users with a Pro lock so people know it exists. Lace ideas: linked counters (every N rows → +1 repeat, auto-reset), voice for extra counters, keep screen on while counting.
- **Play Console "R8 optimisation" hint (1.9.0):** `proguard-rules.pro` keeps whole libraries (`androidx.room.**`, `dagger.hilt.**`), every `@Serializable` class, and has stale `com.crochet.manager` rules from the old package. Trim to what's needed (Room/Hilt ship their own rules) → smaller, faster app. Test a release build on the emulator end to end before shipping: a missing keep rule only crashes release builds.
- **Play Console "bitmap downsampling" hint:** already handled. Every decode goes through `util/BitmapUtils.kt` with `inSampleSize`, and photos on screen use Coil. Likely a false positive from the bounds-only decode or a library; no action.
- Build folder keeps getting "… 2.class" duplicates (iCloud syncs ~/Documents), which breaks `assembleDebug` until `./gradlew clean`.

## Release notes (final, 1.10.0 / build 28, prepared 2026-11-02)
Paste into Play Console → Release → Release notes. Each is under Play's 500-character limit.

<en-US>
• Yarn in fractions (2⅓ balls), with balls used and left per yarn
• Yarn colours: pick a swatch or tap the yarn in a photo
• Filter and sort yarns by colour; pieces show their colours too
• Tap any photo to see it full screen and zoom in
• One extra counter free (repeats, stitches…); more with Pro
• Yarn in stock: what your stash is worth, on the dashboard
• Pro: export your report to Excel
• Fixes: yarns on the piece page, prices in your currency
</en-US>
<pt-BR>
• Fios em frações (2⅓ novelos), com quanto usou e quanto resta
• Cores dos fios: escolha a cor ou toque no fio na foto
• Filtre e ordene fios por cor; as peças mostram suas cores
• Toque em qualquer foto para ver em tela cheia e dar zoom
• Um contador extra grátis (repetições, pontos…); mais no Pro
• Fios em estoque: quanto vale seu estoque, no painel
• Pro: exporte o relatório para o Excel
• Correções: fios na peça, preços na sua moeda, "agulha de crochê"
</pt-BR>
<es-ES>
• Lana en fracciones (2⅓ ovillos), con lo usado y lo que queda
• Colores: elige el color o toca la lana en la foto
• Filtra y ordena las lanas por color; las piezas muestran sus colores
• Toca cualquier foto para verla a pantalla completa y hacer zoom
• Un contador extra gratis (repeticiones, puntos…); más con Pro
• Lanas en stock: cuánto vale tu stock, en el inicio
• Pro: exporta el informe a Excel
• Correcciones: lanas en la pieza, precios en tu moneda
</es-ES>

### 1.10.0 checklist
- [ ] Upload `android/app/build/outputs/bundle/release/app-release.aab` (build 28)
- [ ] Store listing: Pro now has "more extra counters" (one extra counter is free), and mention Excel export and yarn colours
- [ ] DB 10 → 11 (yarns.colorHex): tested by upgrading over existing data on the emulator; check once on your phone after installing
- [ ] Check on the phone at 360dp-ish width: Yarns colour row, Pieces colour row (emulator kept hanging at 360dp)
- [ ] Update the guide on the landing page (new features)
- [ ] Reply to Kim's review once live

### Also in 1.10.0 (internal)
- PT-BR: "gancho" → "agulha de crochê" / "agulha de tricô"; "Tensão" → "Amostra". ES: "vueltas" → "filas". Screen-reader labels that were hardcoded English are now translated.

## 1.10.1 / build 29 (2026-11-03)
Fix: on phones with three-button navigation, the "Pick the colour from a photo" Confirm button was hidden behind the bar.

<en-US>
• Fix: the Confirm button in "Pick the colour from a photo" was hidden on phones with navigation buttons
</en-US>
<pt-BR>
• Correção: o botão Confirmar em "Pegar a cor da foto" ficava escondido em celulares com botões de navegação
</pt-BR>
<es-ES>
• Corrección: el botón Confirmar de "Coger el color de la foto" quedaba oculto en móviles con botones de navegación
</es-ES>

## 1.10.2 / build 30 (2026-11-03)
The 1.10.1 fix didn't work on a Samsung S22 with Android 16 and three-button navigation: inside
the full-screen colour picker the navigation bar reports zero height, so Cancelar/Confirmar were
still under the bar (found while recording the 1.10 social series). The buttons now sit at the
top of the picker, under the hint, and the bottom keeps a minimum 56dp gap. Labels use
`WordSafeText`. Checked on the Pixel_6 emulator with three-button nav at 360dp and 308dp.
**Check on the S22 from internal testing before promoting to production** (the Play-signed app
can't be replaced with a local build without uninstalling).

<en-US>
• Fix: Confirm and Cancel in "Pick the colour from a photo" are now at the top, so navigation buttons can't hide them
</en-US>
<pt-BR>
• Correção: Confirmar e Cancelar em "Pegar a cor da foto" agora ficam no alto da tela, sem ficar escondidos pelos botões de navegação
</pt-BR>
<es-ES>
• Corrección: Confirmar y Cancelar de "Coger el color de la foto" ahora están arriba y los botones de navegación ya no los tapan
</es-ES>

## 1.10.3 / build 31 (2026-11-07)
Onboarding + rating (below) **and** the bug-fix batch from `docs/IDEAS-NEXT-RELEASE.md` groups 1–2
(full list with how each was verified: `docs/FIXES-2026-08-31.md`, section "1.10.3").

Fixes: comma prices/costs lost in PT/ES · Skip on the welcome screens didn't work · backup import now
asks first + "Undo last import" · "Save to phone" for backups · report totals match the rows, gifts
not counted as revenue · gift name hidden on pieces for sale · scanned yarn material on one line ·
dashboard status bar · stash "≈ 13¼" · Excel whole-number totals and date cells · "Por cor" scrolls
to top · long names end with "…" · yarn clone clears the colour code.

Onboarding (Play stats 9 Oct–5 Nov: ~9.9k impressions, 61 installs, only 25 first opens):
- **Getting started card** on the dashboard (new installs, and older installs with no pieces): 3 steps
  (add a piece, add a yarn by scanning its label, count rows with the counter), each a shortcut to the right screen and
  ticked off from real data. Hides when all 3 are done or with ✕. `ui/dashboard/GettingStartedCard.kt`.
- **Empty "In progress"** now has a "+ New piece" button.
- **Last onboarding slide**: "Add my first piece" (opens the new-piece form) + "Look around first".
- **In-app review prompt** (Play Review API `com.google.android.play:review:2.0.2`) right after a piece
  becomes Finished, only if installed ≥3 days, ≥2 pieces and not asked in the last 90 days
  (`util/ReviewPolicy.kt` + unit tests). Play also applies its own quota, so it may not appear in testing.
- Checked on the Pixel_6 emulator: fresh install in PT-BR and ES at 360dp, ES at font 1.3, EN in dark
  mode; all three steps tick off and the card disappears.

<en-US>
• New "Getting started" steps on the home screen, and "Add my first piece" on the welcome screens
• Prices and costs typed with a comma are saved correctly
• Importing a backup now asks first, and you can undo it in Settings
• "Save to phone" for backups
• Cost & profit report: totals match the rows; gifts don't count as revenue
• Skip on the welcome screens works, plus many small fixes
</en-US>
<pt-BR>
• Novos "Primeiros passos" na tela inicial e "Adicionar minha primeira peça" nas boas-vindas
• Preços e custos digitados com vírgula agora são salvos corretamente
• Importar um backup agora pede confirmação, e dá para desfazer em Configurações
• "Salvar no celular" para backups
• Relatório de custos e lucro: totais batem com as linhas; presentes não contam como receita
• "Pular" nas boas-vindas funciona, e muitas pequenas correções
</pt-BR>
<es-ES>
• Nuevos "Primeros pasos" en el inicio y "Añadir mi primera pieza" en la bienvenida
• Los precios y costes con coma ahora se guardan bien
• Importar una copia ahora pide confirmación, y se puede deshacer en Ajustes
• "Guardar en el móvil" para las copias
• Informe de costes y beneficio: los totales cuadran con las filas; los regalos no cuentan como ingresos
• "Omitir" en la bienvenida funciona, y muchas pequeñas correcciones
</es-ES>


## 1.11.0 / build 32 (2026-11-07) — before Christmas
Branch `feature/1.11`. Target: live by ~1 Dec for the Christmas promotion.

Changes:
- **Delivery date** on pieces (any destination): card/dashboard badge "Entrega 09/11" (purple ≤3 days or
  late), detail row "em N dias", "Entregas" filter (open deliveries, soonest first), Excel columns.
  DB 11→12 adds `dueDate`, `deposit` (nullable; checked by installing over 1.10.3 data).
- **Reminders**: daily ~9:00 check (WorkManager); "Entrega em N dias" (1–3 days before) and "A entrega é
  hoje", once each; permission asked when a date is first set; tap opens the piece.
- **Deposit** ("Sinal pago") + "Falta receber" on the piece page.
- **Sua marca** (Settings): shop name/@ + logo on the share card (free); card mark is the app icon +
  "via Hooky App" (not "feito com", user 2026-11-07); Pro can switch the mark off.
- **Softer colour-filter circles** + fade on the row's right edge.
- **Revenue**: dashboard "Total vendido" = report "Vendido"; report adds "Vendido · A vender".
- **Dark mode pass**: search text, chips, tab pill, badges, status-bar icons, slate texts; purple text
  uses a lighter tint in dark mode only (`BrandPurpleText`).
- Numbers in forms/sizes use the locale comma, money keeps cents ("9,50", "25cm").
- Not in 1.11: care/materials cards (user: later); Tester-ID feedback promotion (paused, see memory).

Checked on the emulator (PT-BR; ES at 360dp + font 1.3; light + dark). The emulator showed "isn't
responding" a few times while the Mac was overloaded (GPU frames of 4.95 s); after a cold restart the
same build scrolled at 99th percentile 32 ms. **Check scrolling on the S22.**

### Test on the S22 (internal testing) before production
- [ ] Scroll the dashboard and the piece edit form fast: smooth, no "isn't responding"
- [ ] Add a delivery date 1–2 days ahead → permission prompt → badge; next morning a reminder; tap opens the piece
- [ ] Deposit "30,50" saves; "Falta receber" right
- [ ] Settings → Sua marca: name + logo from gallery; share a piece; switch off "via Hooky App" (Pro)
- [ ] Dark mode: dashboard, Fios search, report
- [ ] "Entregas" is the 4th filter chip — at 360 dp it's just off-screen; decide if it should move

### Play release notes (paste as is)
```
<en-GB>
• Delivery dates for orders: badge, "Deliveries" filter and a reminder 3 days before and on the day
• Record the deposit paid and see what's still to receive
• Your shop name or @ and logo on the cards you share
• Softer colour filters for yarns and pieces
• "Total sold" on the home screen matches the report, which now shows sold vs. still to sell
• Better dark mode, and numbers in your language's format
</en-GB>
<en-US>
• Delivery dates for orders: badge, "Deliveries" filter and a reminder 3 days before and on the day
• Record the deposit paid and see what's still to receive
• Your shop name or @ and logo on the cards you share
• Softer color filters for yarns and pieces
• "Total sold" on the home screen matches the report, which now shows sold vs. still to sell
• Better dark mode, and numbers in your language's format
</en-US>
<es-419>
• Fecha de entrega en los pedidos: etiqueta, filtro "Entregas" y recordatorio 3 días antes y el mismo día
• Registra la seña pagada y mira cuánto falta cobrar
• El nombre de tu tienda o @ y tu logo en las tarjetas que compartes
• Filtros de color más suaves para lanas y piezas
• "Total vendido" en el inicio coincide con el informe, que ahora muestra vendido y por vender
• Mejor modo oscuro y números en el formato de tu idioma
</es-419>
<es-ES>
• Fecha de entrega en los encargos: etiqueta, filtro "Entregas" y aviso 3 días antes y el mismo día
• Anota la señal pagada y mira cuánto falta cobrar
• El nombre de tu tienda o @ y tu logo en las tarjetas que compartes
• Filtros de color más suaves para lanas y piezas
• "Total vendido" en el inicio coincide con el informe, que ahora muestra vendido y por vender
• Mejor modo oscuro y números con coma
</es-ES>
<pt-BR>
• Data de entrega nas encomendas: selo, filtro "Entregas" e lembrete 3 dias antes e no dia
• Registre o sinal pago e veja quanto falta receber
• Nome da loja ou @ e seu logo nos cartões que você compartilha
• Filtros de cor mais suaves para fios e peças
• "Total vendido" na tela inicial bate com o relatório, que agora mostra vendido e a vender
• Modo escuro melhorado e números com vírgula
</pt-BR>
<pt-PT>
• Data de entrega nas encomendas: etiqueta, filtro "Entregas" e lembrete 3 dias antes e no próprio dia
• Registe o sinal pago e veja quanto falta receber
• Nome da loja ou @ e o seu logótipo nos cartões que partilha
• Filtros de cor mais suaves para fios e peças
• "Total vendido" no ecrã inicial coincide com o relatório, que agora mostra vendido e a vender
• Modo escuro melhorado e números com vírgula
</pt-PT>
```
