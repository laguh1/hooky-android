# Ideas for the next release (1.11+)

Opened 2026-11-03. One list for everything that could go into the next versions. Add ideas as
they come (from the user, reviews, the suggestions form, social comments); move an item to
`docs/release-next.md` once it is picked for a release.

## Top of the list
1. 🟡 *In progress for 1.11 (date, deposit, badge, Entregas filter, Excel done 2026-11-07; reminder still to do).* **Delivery date ("prazo de entrega") on a piece**, with a reminder before it's due.
   - Why: sellers take Christmas orders in November; the 1.10 social series had to avoid saying
     "prazo" because the app can't do it yet (2026-11-03).
   - Show it on the piece card and detail, sort/filter "due soon", and add a "Data de entrega"
     column to the Excel export (Peças tab).
   - Pairs with: deposit/advance paid ("sinal"), and an "Encomenda" status before "Em Andamento".

## Small fixes found on the phone (2026-11-03, 1.10.1 on the S22)
- ✅ *Done in 1.10.2:* colour picker Cancelar/Confirmar still hidden under the nav bar on the S22 → moved to the top.
- ✅ *Done in 1.10.3.* **Decimal comma loses prices and costs (bug, important):** the piece form saves money fields with
  `toFloatOrNull()`, which only accepts a dot. In PT/ES, `ApplyYarnMaterialCost` fills the cost with
  `"%.2f".format(cost)` → "12,50" → saved as null, and anything the user types with a comma is
  lost the same way, silently (`PieceFormViewModel.kt:358, 441–447, 476`). Seen 2026-11-03:
  Escândalo Bege and Cacherubì keep no cost; every cost in the report is a whole number. Fix: one
  locale-aware parser for all decimal fields (pieces, yarns, calculator), format with the locale
  but parse both "," and "."; also check the yarn form's price paid. Add a unit test.
- ✅ *Done in 1.10.3.* **Dashboard "Fios em estoque" shows "13,17 novelos"** when the total isn't a simple fraction (13⅙); earlier it showed "12½". Round to the nearest supported fraction or show "≈ 13".
- ✅ *Done in 1.10.3.* **Excel export: Clientes "Peças" total shows "14.00"** (and Quantidade-type counts may too); whole-number columns should use an integer format.
- ✅ *Done in 1.10.3.* **Yarns "Por cor" keeps the old first item in view:** after sorting, the list stays on the yarn that was at the top (e.g. Viking, now among the blues), so reds/pinks/yellows are above the screen. Scroll to the top when the sort changes.
- ✅ *Done in 1.10.3 (display + report; data kept).* **Hidden saved fields (bug, reported by the user) — batched for the next release, no 1.10.2 (decided 2026-11-03):**
  - Piece: changing destination from gift to sale hides the "Presente para" field but saving keeps
    the name, and `PieceDetailScreen.kt:525` shows it for any destination ("Marcia" on a
    Feirinha de Natal piece). Same the other way: a gifted piece keeps its price and the report
    counts it (Cacherubì R$210). Fix: in `PieceFormViewModel` save, clear gift fields unless
    FOR_GIFT/GIFTED and sale fields unless FOR_SALE/SOLD; detail shows gift row only for gifts.
  - Yarn: label scanner stores `materialComposition` with line breaks ("100% Merino Wool\n
    Superwash\nLndeobky"); the form field is `singleLine` so only line 1 shows and the rest can't
    be deleted. Fix: replace line breaks with spaces on scan and when loading the form, allow up to
    3 lines; check other scanned fields too.
  - Workarounds: switch destination back to show the field, clear, switch again; for the yarn,
    long-press → Selecionar tudo → retype.
- ✅ *Done in 1.10.3.* **Card names cut with no "…":** piece and stitch cards use `maxLines = 1` without
  `overflow = TextOverflow.Ellipsis` (`PieceListScreen.kt:330`, `StitchListScreen.kt:685`), so
  "Xale branco final de ano" shows as "Xale branco final de". Use 2 lines + ellipsis.
  The report's piece column cuts the same way ("Centro de Mesa para").
- ✅ *Done in 1.10.3.* **Dashboard content scrolls under the status bar** with no background behind it, so text and
  the clock overlap.
- ❌ *Not a bug (no Ready status exists).* **Pieces status filter has no "Prontas"** (only Todas / Em Andamento / Concluídas), although
  "Pronto" is a status.
- ✅ *Done in 1.10.3.* **Report totals don't match the rows (bug):** "Por cliente" showed total profit R$1007,11
  but the rows add up to R$598,50: "Não atribuído" (pieces with a price but no cost) shows profit
  R$0,00 while the total counts its R$408,61 as profit. Make both use the same rule.
- ✅ *Done in 1.11 (split line under the report totals).* **"Receita" counts unsold quantity** (Toalhinha 0/3 sold counted as 3 × R$74). Consider
  splitting "Receita" (sold) and "Receita prevista" (still to sell), on screen and in the Excel.
- **Report shows pieces with a price but no linked yarn as "—" cost/profit**: consider a hint
  ("Adicione os fios para ver o custo").

## Found while recording the EN series (2026-11-04, 1.10.2 on the S22)
- ✅ *Done in 1.10.3 (confirm dialog + undo).* **Backup import replaces everything with no confirmation (important):** picking a file in
  Settings → Importar backup wipes all pieces, yarns, stitches, needles and counters at once
  (`SettingsScreen.kt:129` → `BackupService.import`, which calls `clearAll()` on every table). One wrong
  tap in the file picker loses real data. Add a dialog ("Isto substitui todos os seus dados por N
  peças e M fios do backup de dd/MM. Exportar um backup atual antes?") and ideally an automatic
  backup of the current data just before importing.
- ✅ *Done in 1.10.3 ("Save to phone").* **Backup export doesn't save to the phone:** the share uses `type = "*/*"`
  (`SettingsScreen.kt:136`), and choosing "Files by Google · Download" closes the menu but saves nothing on
  the S22. Use `application/json`, and add a "Salvar no celular" option with the system "create
  document" picker (`ActivityResultContracts.CreateDocument`) so people can choose Downloads or Drive
  without the share menu.
- ✅ *Done in 1.11: dashboard 'Total vendido' = report 'Vendido'; report adds 'Vendido · A vender' (SaleRevenue).* **Dashboard "Receita Total" doesn't match the report:** the dashboard showed R$212,00 / $212.00 while
  the report showed R$1296,75 for the same data. Decide what each one means (sold only vs. all prices)
  and label it, or use the same number (related to the report items below).
- ✅ *Done in 1.10.3.* **Clone keeps the colour code:** cloning Shine "Pérola de amêndoa (D-17)" to make the white one kept
  "D-17". Clear the colour code (and maybe the colour) on clone, since cloning is mostly for "same
  yarn, other colour".
- ✅ *Done in 1.10.3.* **Excel dates can be read the wrong way round:** "Data da venda" goes out as text like 12/10/2026; Google
  Sheets set to US English reads it as 10 Dec. Write real date cells (or ISO yyyy-MM-dd) so the
  spreadsheet app formats them by its own locale.

## Found while testing 1.10.3 (2026-11-07, emulator)
- ❌ *Not a bug: labels work on the S22 and the Tab S6 Lite (user checked 2026-11-07); emulator-only.* **Bottom tab labels may not respond to taps on three-button navigation.** On the Pixel_6 emulator
  the system navigation bar overlaps the lower part of the tab labels (Painel, Peças…), so a tap
  right on the word goes to the system bar; the icon works. Android's own file picker has the same
  overlap on this emulator (SAVE button), so it may be emulator-only. **Check on the S22 / tablet**
  before changing anything.
- ✅ *Done in 1.11 (also form fields: prices, sizes, yarn weights).* **Piece dimensions use a dot in PT/ES** ("25.0cm de largura") — format with the locale and drop ".0".
- Unused string `work_status_ready` ("Pronto") in PT only; there is no "Ready" status, so the
  "Prontas" filter item above was a mistake (removed from the list 2026-11-07).

## Found while starting 1.11 (2026-11-07, emulator, dark mode)
- ✅ *Done in 1.11 (plus status-bar icons, report chips, badges, purple text — BrandPurpleText).* Yarns search field stays white in dark mode; the selected "All" chip and the selected bottom-tab
  pill are dark slate on a dark background, so the selection is hard to see. Use theme roles.

## Requested by the user (2026-11-04)
### ✅ *Done in 1.11 (swatches only).* Softer colour-filter palette
- Today the filter circles use saturated "primary" swatches (`ColorFamily` in `domain/model/YarnColor.kt`:
  RED D32F2F, PINK E91E63, ORANGE FB8C00, YELLOW FDD835, GREEN 43A047, BLUE 1E88E5, PURPLE 8E24AA,
  BROWN 795548, NEUTRAL BDBDBD). Make them muted, yarn-like tones, as on lindehobby's site (screenshot
  2026-11-04: "Blancos y crudos · Beiges y marrones · Amarillos y naranjas · Rojos y rosas · Morados ·
  Azules · Verdes · Grises y negros", soft dots + label + count).
- Only the swatch colour changes; the grouping logic (`ColorFamily.of`) stays. Suggested swatches to try:
  red C75B5B, pink D98A9F, orange D9894A, yellow E3C065, green 7FA58A, blue 6F8FBF, purple 9A82B8,
  brown A88468, neutral E9E4DA (with a thin border so the off-white shows on white).
- Optional later: split NEUTRAL into "Brancos e crus" and "Cinzas e pretos" (a white piece and a black
  piece are not the same answer), and show the family name + count when the filter is opened, like the
  screenshot. Check PT/ES labels on 360 dp (no mid-word breaks).

### Share card: the maker's brand first
- Now (`ui/share/ShareCardGenerator.kt`): photo, piece name, hours/rows, Hooky wordmark bottom right.
- **Maker's name or logo on the card** (set once in Settings: shop name, @handle, optional logo image).
  Free.
- **Card types (Pro)** — *care + materials cards postponed past 1.11 (user, 2026-11-07: "not so impactful")*:
  1. *Showcase* (today's card, with the maker's name/logo).
  2. *Care card* for customers and gift recipients: piece name, yarn composition, care symbols and
     temperature from the yarn (hand wash, 30 °C, dry flat…), maker name/contact. Square + a
     printable tag size.
  3. *Materials card* for social media: yarns (brand · colour · balls), hook/needle size, stitches,
     tutorial link (QR code or short link), hours.
- **Hooky logo:** free cards keep a small, quiet "made with Hooky App" mark (free word of mouth); Pro can
  switch it off. User's view (2026-11-04): people won't share cards with a company logo on them, so
  the maker's brand must be the hero either way.

## Selling (from the "app for crafters who sell" plan, 2026-11-02)
- Orders with due date, deposit and reminder (item 1 above is the first step).
- Year summary for taxes (autónomo / MEI).
- "Do I have enough yarn?" for planned pieces + shopping list.

## Counters (from the 4★ review and lace ideas)
- Linked counters: every N rows → +1 repeat, auto-reset.
- Voice for extra counters; keep the screen on while counting.

## General
- Automatic Google Drive backup.
- Home-screen widget.

## Housekeeping (not features)
- PT-BR "gancho" → "agulha", "Tensão" → "Amostra"; ES "vueltas" vs "filas"; PT/ES Dashboard
  accessibility labels still in English (details in `docs/release-next.md`).
- Trim `proguard-rules.pro` (Play Console R8 hint).
- Store listing: "extra counters" → "more extra counters" for Pro.
- Check Play Console store-listing conversion and retention before big builds.
