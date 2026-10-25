# Next release — running list of changes

Started 2026-10-24. Branch `fix/suggest-price-i18n`. Last release on Play: build 25 / 1.8.0.
Add each new change here as it lands; draft the Play release notes from this list at the end.

Build: versionCode 26 / versionName **1.9.0** (bumped 2026-10-25; the suggestions links are a
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
