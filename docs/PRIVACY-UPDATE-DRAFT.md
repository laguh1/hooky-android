# Privacy policy update — draft (written 2026-10-25)

Why: `landing/privacy.html` section 2 says the app makes "no network requests of its own".
Since 1.8.0 that is not true. Checked in the code:

| What | Code | What leaves the phone |
|---|---|---|
| Firebase Installations + Remote Config | `data/premium/PremiumManager.kt` | A random ID for this install of the app (shown as the tester ID in Settings → Beta) and the basic request info Firebase needs (app version, language, OS). The app downloads the Pro settings (tester list, early-adopter dates) and checks them **on the phone**. |
| Google Play Billing | `data/premium/ProBillingManager.kt` | The purchase runs in Google Play. The app only gets Google's confirmation that Pro was bought. No card details reach the app. |
| ML Kit text recognition (`play-services-mlkit-text-recognition`, a "thin" feature run by Play Services) | `data/scanner/YarnLabelScannerService.kt` | Reading the label / needle size happens **on the phone**; the photo is not uploaded. Per Google's ML Kit data disclosure (checked 2026-10-25), the SDK sends Google, for diagnostics and usage analytics: device info (manufacturer, model, OS version/build, ML hardware), app package name and version, a device identifier and per-installation identifiers, performance metrics (e.g. latency), API configuration (e.g. image format and resolution), input/output size, feature version, event types (initialisation, model download, detection) and error codes. Text recognition has no extra data on top of this. |
| Web links | Help, Settings → Help, stitch tutorials | Open in the browser; the app itself sends nothing. |

Still true: no ads, no analytics or crash-reporting SDK of our own, no servers of ours, and
pieces / yarns / stitches / photos / costs never leave the phone (except a backup file the user
chooses to save or share).

---

## New text for privacy.html

**Short version (replaces the current one):**

> **Short version:** Everything you enter in the Hooky App Android app (your pieces, yarns, stitches, photos and costs) stays on your device. We never see it. To unlock Hooky App Pro, the app contacts Google: Google Play handles purchases, and Firebase tells the app whether your install gets Pro for free. The forms on this website send us only what you choose to type, by email. We do not sell, share or monetise your data.

**Section 2 (replaces "2. The Android app"):**

> ### 2. The Android app
>
> **Your data stays on your device.** Hooky App stores everything you enter (pieces, yarns, stitches, needles, photos, costs, notes) in the app's private storage on your phone. We do not have servers and we never receive this data. If you export a backup, the file is saved or shared only where you choose.
>
> **When the app goes online.** The app works offline. It connects to the internet only for these Google services:
>
> - **Google Play Billing.** If you buy Hooky App Pro, Google Play handles the payment. The app only receives Google's confirmation that Pro was bought, so it can unlock it on your devices. We never see your card or payment details. Google's privacy policy applies.
> - **Firebase (by Google).** Firebase creates a random ID for your install of the app. This ID is not linked to your name, email or Google account. The app uses Firebase Remote Config to download the current Pro settings, such as the dates of a launch offer or the IDs of testers who get Pro for free, and checks them on your phone. Firebase receives the install ID and basic technical details of the request (app version, language, operating system). We use this only to decide whether Pro is unlocked. If you ask us for free tester access, you can send us this ID from Settings → Beta.
> - **Text recognition (Google ML Kit).** When you scan a yarn label or a needle size, the text is read on your phone. The photo and the text found in it are not uploaded. Google Play Services, which provides this feature, sends Google technical information so Google can monitor and improve it: device details (manufacturer, model, operating system version), the app's name and version, device and per-install identifiers, and how the feature performed (for example speed, image size and error codes). We do not receive this information. See [Google's ML Kit data disclosure](https://developers.google.com/ml-kit/android-data-disclosure).
>
> **Links.** Links in the app (the guide, the suggestions page, tutorial links you saved) open in your browser.
>
> The app has no advertising, and no analytics or crash-reporting tools of our own.

**Section 4, add one line under "Third-party services":**

> **Google Play Billing, Firebase and ML Kit** (Android app only) — see section 2. Google's privacy policy applies: [policies.google.com/privacy](https://policies.google.com/privacy).

**Section 6 (your rights), add:**

> The app's install ID is created on your device. Uninstalling the app, or clearing its data, removes it; a new one is made if you install again.

**Effective date:** change to the day it is published.

---

## Play Console → App content → Data safety (check, don't just copy)

What the code suggests. Compare with Google's own guidance before submitting:
Firebase: firebase.google.com/docs/android/play-data-disclosure ·
ML Kit: developers.google.com/ml-kit/android-data-disclosure

- **Does the app collect or share data?** Yes (collected, not shared).
- **Device or other IDs:** collected. Firebase install ID → purpose *App functionality*; ML Kit device and per-install identifiers → purpose *Analytics*. Not optional, not shared.
- **Purchase history:** collected (Pro purchase confirmation from Play Billing), purpose *App functionality*.
- **App info and performance → Diagnostics:** collected by ML Kit (device info, performance metrics, error codes, API configuration), purpose *Analytics*. Also consider **Other app performance data** for the latency / event data.
- Data is processed only when the scanner is used, but ML Kit ships with the app, so declare it rather than mark it optional.
- **Encrypted in transit:** yes (all of these use HTTPS).
- **Can users ask for deletion?** The data is tied to the install; uninstalling removes the ID. Purchases are managed by the user's Google account.
- Photos, files, messages, location, contacts, personal info: **not collected** (stay on the device).
