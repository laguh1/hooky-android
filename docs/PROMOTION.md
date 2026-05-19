# Hooky — Promotion & Launch Guide

## Landing Page

**Live URL:** https://laguh1.github.io/hooky-landing/
**Repo:** https://github.com/laguh1/hooky-landing
**Files:** `/Users/joanasocrates/Documents/claude/hooky/landing/`

To update the page: edit `landing/index.html` → `git commit` → `git push` (GitHub Pages auto-deploys in ~1 min).

---

## Landing page status (May 2026)

- ✅ Auto language detection (EN/ES/PT) + manual switcher
- ✅ Android CTA + iOS waitlist form (Google Sheets backend)
- ✅ Privacy policy page (`privacy.html`) — linked in footer
- ✅ Brand purple nav + footer
- ✅ 6 feature cards with Hooky icons
- ✅ Countries: USA, UK, Australia, Canada, Brazil, Spain, LATAM, Portugal
- ⬜ Wire form to Google Sheets (see step 1 below)
- ⬜ Add Play Store link (see step 2)
- ⬜ Record and embed tutorial video (see step 3)
- ⬜ Replace HTML phone mockup with real screenshots (see step 4)
- ⬜ Add favicon (use the app icon export from Android Studio / brand assets)

---

## Next steps before promoting

### 1. Set up the iOS waitlist form → Google Sheets (free, unlimited)

The form on the landing page needs a backend to save submissions. Use Google Apps Script (free, no limits).

**Step-by-step:**

1. Go to [sheets.google.com](https://sheets.google.com) → create a new sheet named **"Hooky iOS Waitlist"**
2. Add these headers in row 1:
   `Timestamp | Name | Country | WhatsApp | Email | Instagram | Facebook | Source | Language`
3. Go to **Extensions → Apps Script**
4. Delete the default code and paste this:

```javascript
function doPost(e) {
  const sheet = SpreadsheetApp.getActiveSpreadsheet().getActiveSheet();
  const data = JSON.parse(e.postData.contents);
  sheet.appendRow([
    data.ts || new Date().toISOString(),
    data.name || '',
    data.country || '',
    data.whatsapp || '',
    data.email || '',
    data.instagram || '',
    data.facebook || '',
    data.source || '',
    data.lang || ''
  ]);
  return ContentService
    .createTextOutput(JSON.stringify({status: 'ok'}))
    .setMimeType(ContentService.MimeType.JSON);
}
```

5. Click **Deploy → New deployment**
6. Type: **Web App** | Execute as: **Me** | Who has access: **Anyone**
7. Click **Deploy** → copy the Web App URL (looks like `https://script.google.com/macros/s/ABC.../exec`)
8. In `landing/index.html`, find `const FORM_ENDPOINT = 'GOOGLE_SCRIPT_URL'` and replace `GOOGLE_SCRIPT_URL` with your copied URL
9. `git commit -am "feat: wire waitlist form to Google Sheets" && git push`

**Result:** every iOS waitlist submission appears as a new row in your Google Sheet instantly.
You can filter by country, sort, export CSV for WhatsApp broadcasts, etc.

---

### 2. Add your Play Store link

Once the app is published on Google Play:
1. Copy the Play Store URL (format: `https://play.google.com/store/apps/details?id=com.hooky.app`)
2. In `landing/index.html`, find and replace both `PLAY_STORE_URL` occurrences
3. Commit and push

---

### 3. Add tutorial video

Once you've recorded and uploaded your tutorial to YouTube:
1. Get the video ID from the YouTube URL (the part after `?v=`)
2. In `landing/index.html`, find the comment block in the Tutorial section
3. Delete the `<div class="video-placeholder">` block
4. Uncomment the `<iframe>` line and paste your video ID
5. Commit and push

---

### 4. Add real app screenshots

Replace the HTML phone mockup with actual app screenshots:
1. Run Hooky on your Android phone or emulator
2. Take screenshots of key screens: Dashboard, Piece Detail (with counter), Yarn list, Reports
3. Save to `landing/screenshots/`
4. Update the hero section in `index.html` to use `<img>` instead of the HTML mockup

---

## Promoting the app

### Where to post (Brazil)
- Facebook groups: "Crochê Brasil", "Crochet e Tricô Brasil", regional craft groups
- Instagram: hashtags #croche #tricô #amigurumi #artesanato — use Reels for best reach
- WhatsApp communities: ask in craft groups to share

### Where to post (Spain + LATAM)
- Facebook groups: "Ganchillo España", "Crochet Hispano", country-specific craft groups
- Instagram: hashtags #ganchillo #crochet #tejido #manualidades
- Pinterest: boards with project photos linking to the landing page

### Content ideas
- "I built an app to track my crochet projects" — personal story (performs well on Facebook)
- Tutorial video (YouTube → Instagram Reel → TikTok)
- Before/after: tracking in notes app vs Hooky
- "How much does your crochet really cost?" → Cost & profit feature

### Link in bio strategy
Use the landing page URL (`laguh1.github.io/hooky-landing`) as your link in bio on Instagram/TikTok.
Android visitors → Play Store button. iPhone visitors → waitlist form. One link handles both.

---

## iOS waitlist outreach

When the iOS version is ready:
1. Export the waitlist Google Sheet as CSV
2. Filter by contact type (WhatsApp / Email / Instagram / Facebook)
3. WhatsApp: send a personal message (not broadcast unless they opted in to that)
4. Email: send a simple plain-text announcement
5. Instagram/Facebook: DM their handle

Suggested message (EN):
> Hi [Name]! You signed up for the Hooky iOS waitlist a while back. Great news — the app is now available on iPhone! Here's the link: [APP_STORE_URL]. Thanks for waiting 🎉

---

## Formspree vs Google Sheets — why we chose Google Sheets

Formspree is a third-party service that collects form submissions. The free tier is limited to 50 submissions/month, which might fill up quickly once you start promoting. Google Sheets via Apps Script is completely free, has no submission limit, and you already know how to use Sheets. The data lands directly in a spreadsheet you control — easier to filter, export, and use for outreach.

---

## Legal

### Privacy Policy ✅
- **File:** `landing/privacy.html` — linked from the landing page footer
- **Covers:** Android app (offline, no data collection), iOS waitlist form (name + contacts → Google Sheet), Google Fonts, localStorage, user rights (access/correct/delete)
- **Contact email in policy:** hooky.app.privacy@gmail.com — create this Gmail alias or use your own email and update the file
- **Required by:** Google Play Store (mandatory), GDPR (EU/Spain), LGPD (Brazil)
- **Action needed:** Update the email address in `privacy.html` if you don't use hooky.app.privacy@gmail.com

### Trademark / Brand
- **App name "Hooky"** — not registered. Before scaling promotion, search EUIPO (EU) and INPI (Brazil) for conflicting trademarks. If clear, filing costs ~€850 for EU class 42 (software).
- **Logo/wordmark** — automatically copyright-protected as original artwork. No registration needed.
- **Source code** — automatically copyright-protected under Berne Convention (EU/Brazil/US all signatories). No registration needed.
- **Apple logo** — do NOT use, even from Wikipedia/Vecteezy. Use a generic smartphone silhouette for iOS references (already done).

### Google Play Store requirements before publishing
- Privacy policy URL: add `https://laguh1.github.io/hooky-landing/privacy.html` in Play Console → App content → Privacy policy
- Data safety form: declare "no data collected" (the app is offline-only)
