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
- **Card names cut with no "…":** piece and stitch cards use `maxLines = 1` without
  `overflow = TextOverflow.Ellipsis` (`PieceListScreen.kt:330`, `StitchListScreen.kt:685`), so
  "Xale branco final de ano" shows as "Xale branco final de". Use 2 lines + ellipsis.
  The report's piece column cuts the same way ("Centro de Mesa para").
- **Dashboard content scrolls under the status bar** with no background behind it, so text and
  the clock overlap.
- **Pieces status filter has no "Prontas"** (only Todas / Em Andamento / Concluídas), although
  "Pronto" is a status.
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
