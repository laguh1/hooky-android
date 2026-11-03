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
