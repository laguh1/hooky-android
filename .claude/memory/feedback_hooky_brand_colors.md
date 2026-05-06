---
name: Hooky brand color constraint
description: All Hooky UI must use only the B&W palette or brand purple — no custom colors outside the theme
type: feedback
---

All UI work on Hooky must strictly follow the brand palette. No custom or ad-hoc colors.

**Why:** User explicitly requested this — brand consistency matters for the app.

**How to apply:**
- White backgrounds, near-black text (`#111827` / `TextPrimary`)
- Slate `#1E293B` (`Slate`) for interactive elements, buttons, primary actions
- Brand purple `#8C015E` (`BrandPurple`) for accents and active/highlighted states only
- `ErrorRed` (`#EF4444`) for destructive actions only
- Never introduce blues, greens, or any other color not in `Color.kt`
- All new colors must be added to `Color.kt` and referenced by name — never hardcode hex in composables
