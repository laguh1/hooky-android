# Next release — running list of changes

Started 2026-10-24. Branch `fix/suggest-price-i18n`. Last release on Play: build 25 / 1.8.0.
Add each new change here as it lands; draft the Play release notes from this list at the end.

Build: versionCode 26. versionName is still "1.8.0" and must be bumped
(1.8.1 if it stays small fixes, 1.9.0 if a new feature lands).

## Changes so far

### Visible to users (go in the release notes)
1. **Newest photo first** (pieces, yarns, stitches, needles). New photos go to
   the front of the gallery, so the first photo shown is the most recent and
   swiping goes back in time. The piece share card uses the newest photo too.
   Photos saved before this update keep their old order.
2. **Portuguese (Brazil): rows are now "carreiras"** instead of "fileiras"
   (row counter, progress, pace, help tips).
3. **More screens translated into Spanish and Portuguese**: these were still in
   English. "Suggest price →" on the piece screen, search hints ("Enter at
   least 2 characters", "Nothing matched…", section names), photo editor
   buttons (Rotate left, Flip, Rotate right, Save), the needle "Size detected"
   dialog, the delete-photo dialog, a Cancel button in the piece form, and
   "Finished <date>" on the dashboard.

### Internal (not in the release notes)
- Photo description for accessibility ("Photo 1"…) translated.
- Logged as fix #13 in `docs/FIXES-2026-08-31.md`.

## Known leftovers (could be added to this release)
- Error messages still in English in ES/PT: scan failed, couldn't read
  label/size, free-tier photo limit.
- "Work time" / "Rows" on the share card still in English.
- Spanish versions of fix #13 not yet seen on a device.
- Optional: one-time reorder of photos saved before the update, so they are
  also newest first.

## Before release
- [ ] Check new/changed labels in PT-BR and ES at 360dp + font scale 1.3 (CLAUDE.md rule)
- [ ] Try adding photos on a piece and a yarn on the emulator (newest first)
- [ ] Bump versionName
- [ ] Write release notes in EN / PT-BR / ES (max 500 characters each)

## Draft release notes (update as the list grows)

<en-US>
• Newest photo first: your most recent photo now shows first
• More screens translated into Spanish and Portuguese
• Small fixes and improvements
</en-US>
<pt-BR>
• Foto mais recente primeiro: sua última foto agora aparece na frente
• Contador agora fala em "carreiras"
• Mais telas traduzidas para o português
• Pequenas correções e melhorias
</pt-BR>
<es-ES>
• La foto más reciente primero: tu última foto ahora aparece delante
• Más pantallas traducidas al español
• Pequeñas correcciones y mejoras
</es-ES>
