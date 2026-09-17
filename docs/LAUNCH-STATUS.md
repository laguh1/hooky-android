# Hooky — Play Store Launch Status

**Last updated:** 29/07/2026

---

## Identity Verification
- [x] Play Console account created — $25 fee paid
- [x] Identity documents uploaded
- [x] Address document uploaded (bank statement) — 12/07/2026
- [x] Google approval received ✅

---

## App Build
- [x] Signing keystore generated (`hooky-release.jks`)
- [x] `applicationId` = `com.laguh.hooky` (changed from `com.hooky.app` — was taken on Play Store)
- [x] `targetSdk = 35`, `versionCode = 3`, `versionName = 1.1`
- [x] FileProvider authority = `com.laguh.hooky.fileprovider` (in AndroidManifest + ShareCardGenerator)
- [x] Release AAB built — `android/app/build/outputs/bundle/release/app-release.aab`

---

## Store Assets
- [x] App icon 512×512 — `shared/branding/final/logo.png`
- [x] Feature graphic 1024×500 — `shared/branding/final/feature_graphic.png`
- [x] Screenshots uploaded — EN + phone
- [x] Store listing filled in — EN (main), ES, PT-BR
- [x] Privacy policy URL added
- [x] App content declarations complete (content rating, target audience, data safety, ads)

---

## Play Console Setup
- [x] App created in Play Console
- [x] Store listing complete
- [x] AAB uploaded to Internal Testing track ✅ — Released 29 Jul 2026

---

## ← YOU ARE HERE: Internal Testing

### Step 1 — Add testers
1. Go to **Testing > Internal testing > Testers tab**
2. Click **Create email list** → name it "Friends & Family"
3. Add Gmail addresses of people you want to test
4. Save — you'll get an **opt-in URL**

### Step 2 — Share the opt-in link
- Send the opt-in URL to testers (WhatsApp, Instagram, etc.)
- They open it on their Android phone → tap **"Become a tester"** → find Hooky on Play Store → install normally
- **Not publicly visible** — only invited emails can see it

### Step 3 — Fix the app name display
- The app currently shows as `com.laguh.hooky (unreviewed)` to testers
- To fix: go to **Grow > Store presence > Main store listing** and confirm everything is saved
- Google will do a minimal review and update the name to "Hooky"

---

## Closed Testing (required before Production)

Google requires **12 testers for 14 days** on a Closed Testing track before you can apply for Production access.

1. Go to **Testing > Closed testing > Create track**
2. Add at least 12 testers (can be the same people as Internal + more)
3. Promote your Internal Testing release to the Closed track (or create a new release there)
4. Wait 14 days with active testers
5. Then apply for **Production access**

---

## Production (public launch)

- [ ] Apply for Production access (after 14-day closed test)
- [ ] Submit for Google review (1–7 days for new apps)
- [ ] App live on Play Store publicly 🎉
- [ ] Copy Play Store URL and update landing page (`landing/index.html`)
- [ ] Post on r/crochet and r/androidapps
- [ ] Share on Instagram/social media

---

## Production Access Reapplication — Q&A

First application was rejected for insufficient tester engagement (not headcount) — see `docs/FIXES-2026-08-31.md` for the evidence log of active iteration kept for reapplying.

**Q: How did you recruit users for your closed test? For example, did you ask friends and family or use a paid testing provider?**

> No paid provider. I personally recruited friends, family, and crochet/knitting community members via WhatsApp and social media. I also asked developer colleagues to test specific features, and knitting/couture-industry acquaintances to review vocabulary and translations (EN/ES/PT-BR).

*(284 characters, plain text — no markdown, ready to paste directly into the Play Console field's character limit.)*

**Q: Describe the engagement you received from testers during your closed test. Include whether or not testers utilised all of the features in your app and whether tester usage was consistent with how you would expect a real user to use your app. If not, describe the differences that you would expect to see.**

> Testers added real pieces, yarns and stitches, scanned yarn labels, and used the row counter and timer. They reported bugs (e.g. dark-mode contrast, button inconsistency), fixed in later updates. Engagement matched expected real-user behaviour across most core features.

*(270 characters, plain text.)*

**Q: Provide a summary of the feedback that you received from testers. Include how you collected the feedback.**

> Collected via WhatsApp and direct conversations. Testers reported dark-mode contrast bugs, inconsistent save-button styling, and requested onboarding, translation corrections, and a shorter piece form with clearer save button. All fixed/implemented in subsequent updates.

*(271 characters, plain text.)*

**Q: Who is the intended audience of your app?**

> Crochet and knitting enthusiasts of any gender, background or skill level who want to organise their projects, yarn stash and stitch library, including hobbyists, sellers, and social media crafters who share and spread craft knowledge. Supports EN, ES and PT-BR.

*(262 characters, plain text.)*

**Q: Describe how your app provides value to users. (Ref: [App quality guidelines](https://developer.android.com/quality))**

> Hooky saves crafters time and guesswork: track project progress, yarn stash and stitches in one place, and price work fairly with built-in cost tracking. Clean, intuitive design makes it easy to navigate. Fully offline, no account: data stays on the user's device.

*(265 characters, plain text.)*

**Q: What changes did you make to your app based on what you learned during your closed test?**

> Added onboarding, improved translations, shortened piece form, clarified save button, fixed critical photo storage bug, and enhanced yarn-label and hook-size scanner accuracy. All reported issues resolved; core workflows validated across multiple devices before release.

*(270 characters, plain text.)*

**Q: How did you decide that your app is ready for production?**

> As both a software engineer and daily crochet crafter, I use Hooky myself for my own projects. All tester-reported issues were fixed, Play Console warnings resolved, and core workflows validated across multiple devices. The app runs fully offline with no crashes after the final round of fixes.

*(294 characters, plain text.)*

---

## Firebase — Premium Gifting
- See `docs/PREMIUM-GIFTING.md` for how to grant premium manually
- `premium_ids` parameter live in Remote Config (default value: `[]`)
- **Early-adopter promo live (31 Aug 2026, extended to 31 Oct on 4 Sept 2026):**
  `early_adopter_cutoff = 2026-10-31`, `early_adopter_premium_days = 365` — anyone
  whose first app launch is on or before 31 Oct 2026 gets 1 year of premium
  automatically, no ID needed. See `docs/PREMIUM-TUTORIAL.md` for how this works.

---

## Notes
- Internal Testing: up to 100 testers, no review needed, instant
- Closed Testing: needs 12+ testers for 14+ days — this is the gate to Production
- Production: requires Google review, app becomes publicly searchable
