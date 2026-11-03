# Ideas for the next release (1.11+)

Opened 2026-11-03. One list for everything that could go into the next versions. Add ideas as
they come (from the user, reviews, the suggestions form, social comments); move an item to
`docs/release-next.md` once it is picked for a release.

## Top of the list
1. **Delivery date ("prazo de entrega") on a piece**, with a reminder before it's due.
   - Why: sellers take Christmas orders in November; the 1.10 social series had to avoid saying
     "prazo" because the app can't do it yet (2026-11-03).
   - Show it on the piece card and detail, sort/filter "due soon", and add a "Data de entrega"
     column to the Excel export (Peças tab).
   - Pairs with: deposit/advance paid ("sinal"), and an "Encomenda" status before "Em Andamento".

## Small fixes found on the phone (2026-11-03, 1.10.1 on the S22)
- **Decimal comma loses prices and costs (bug, important):** the piece form saves money fields with
  `toFloatOrNull()`, which only accepts a dot. In PT/ES, `ApplyYarnMaterialCost` fills the cost with
  `"%.2f".format(cost)` → "12,50" → saved as null, and anything the user types with a comma is
  lost the same way, silently (`PieceFormViewModel.kt:358, 441–447, 476`). Seen 2026-11-03:
  Escândalo Bege and Cacherubì keep no cost; every cost in the report is a whole number. Fix: one
  locale-aware parser for all decimal fields (pieces, yarns, calculator), format with the locale
  but parse both "," and "."; also check the yarn form's price paid. Add a unit test.
- **Hidden saved fields (bug, reported by the user) — batched for the next release, no 1.10.2 (decided 2026-11-03):**
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
- **Card names cut with no "…":** piece and stitch cards use `maxLines = 1` without
  `overflow = TextOverflow.Ellipsis` (`PieceListScreen.kt:330`, `StitchListScreen.kt:685`), so
  "Xale branco final de ano" shows as "Xale branco final de". Use 2 lines + ellipsis.
  The report's piece column cuts the same way ("Centro de Mesa para").
- **Dashboard content scrolls under the status bar** with no background behind it, so text and
  the clock overlap.
- **Pieces status filter has no "Prontas"** (only Todas / Em Andamento / Concluídas), although
  "Pronto" is a status.
- **Report totals don't match the rows (bug):** "Por cliente" showed total profit R$1007,11
  but the rows add up to R$598,50: "Não atribuído" (pieces with a price but no cost) shows profit
  R$0,00 while the total counts its R$408,61 as profit. Make both use the same rule.
- **"Receita" counts unsold quantity** (Toalhinha 0/3 sold counted as 3 × R$74). Consider
  splitting "Receita" (sold) and "Receita prevista" (still to sell), on screen and in the Excel.
- **Report shows pieces with a price but no linked yarn as "—" cost/profit**: consider a hint
  ("Adicione os fios para ver o custo").

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
