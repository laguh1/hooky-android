# Hooky — Premium Tester Access: How It Works & How to Manage It

## How it works (overview)

Each Hooky install has a unique **Tester ID** (a Firebase Installation ID — anonymous, no login, auto-generated). You maintain a list of allowed IDs in Firebase Remote Config. On app start, the app fetches the list, checks if the device's ID is in it, and marks it as premium. Adding or removing someone requires no new build or Play Store release.

---

## One-time setup: Create a Firebase project

You only do this once.

### 1. Create the project

1. Go to [console.firebase.google.com](https://console.firebase.google.com)
2. Click **Add project**
3. Name it `Hooky` → Continue → disable Google Analytics (not needed) → **Create project**

### 2. Register the Android app

1. In the Firebase console, click the **Android** icon (➕ Add app)
2. Enter package name: `com.hooky.app`
3. App nickname: `Hooky Android` (optional)
4. Click **Register app**
5. **Download `google-services.json`**
6. Place it at `android/app/google-services.json` (same folder as `build.gradle.kts`)
7. Skip the remaining console steps (the SDK is already in the code)

### 3. Enable Remote Config

1. In the Firebase console left menu → **Remote Config**
2. Click **Create configuration** (first time) or **Add parameter**
3. Add a parameter:
   - **Parameter key:** `premium_ids`
   - **Default value:** `[]`
4. Click **Save** → then **Publish changes** (blue button, top right)

---

## Granting premium automatically to anyone who installs in a time window

If you want to reward **everyone** who installs during a promo period (e.g. "anyone
who installs this month gets a free year") without collecting a single Tester ID,
use these two Remote Config parameters instead:

- **`early_adopter_cutoff`** — a date (`yyyy-MM-dd`). Anyone whose first app launch
  is on or before this date automatically qualifies. Leave it blank to turn this off.
- **`early_adopter_premium_days`** — how many days of premium they get, counted from
  *their own* install date (so everyone gets a full window, not a shared deadline).
  Set to `365` for "1 year." Leave blank or `0` for lifetime.

Example: to give a free year to anyone installing before 31 October 2026:
1. Remote Config → **Add parameter** → key `early_adopter_cutoff`, value `2026-10-31`
2. **Add parameter** → key `early_adopter_premium_days`, value `365`
3. **Publish changes**

No tester ever has to find or send you anything — this checks automatically on
their first app launch, entirely on-device. It runs alongside the manual
`premium_ids` list below, so you can still hand-gift lifetime access to specific
people (e.g. someone who installs after the cutoff) on top of this.

---

## Granting premium to a specific tester manually

### Step 1 — Get the tester's ID

Ask the tester to:
1. Open Hooky → tap the **gear icon** (Settings) from any list screen
2. Scroll to the **BETA** section at the bottom
3. Tap the copy icon next to **Tester ID**
4. Send you the copied ID (it looks like: `fh7k2m...`)

### Step 2 — Add the ID to Firebase

1. Open [Firebase Console](https://console.firebase.google.com) → your Hooky project → **Remote Config**
2. Click the pencil icon on the `premium_ids` parameter
3. Edit the value — it's a JSON array of objects, each with an `id` and an optional `expires` date:
   ```json
   [{"id": "fh7k2mXXXXXXXXXXXXXXXX", "expires": null}]
   ```
   - `"expires": null` (or omit the field entirely) → **lifetime** access
   - `"expires": "2027-08-31"` → premium through the end of that day, then expires automatically — no need to remember to revoke it
4. For multiple testers, mix lifetime and time-limited as needed:
   ```json
   [
     {"id": "fh7k2mXXXXXXXXXX", "expires": null},
     {"id": "a9bQqXXXXXXXXXXX", "expires": "2027-08-31"},
     {"id": "zT4rNXXXXXXXXXXX", "expires": "2027-08-31"}
   ]
   ```
5. Click **Save** → **Publish changes**

A date format tip: if you type an invalid date by mistake, that tester gets treated as
lifetime rather than being locked out — the app fails open on unparseable dates.

### Step 3 — Tester receives premium

The app checks Remote Config on every fresh launch (max once per hour). The tester will be premium on their **next app start** after publishing. If they're impatient, they can force-close and reopen the app.

---

## Revoking premium

Remove the tester's ID from the JSON array in Firebase Remote Config → Save → Publish. They lose premium on next app start.

---

## How to gate a feature with premium in code

Inject `PremiumManager` into any ViewModel:

```kotlin
@HiltViewModel
class SomeViewModel @Inject constructor(
    private val premiumManager: PremiumManager,
    // ...
) : ViewModel() {

    // Reactive: updates automatically if premium status changes
    val isPremium: StateFlow<Boolean> = premiumManager.isPremiumFlow
}
```

In your Composable:
```kotlin
val isPremium by viewModel.isPremium.collectAsState()

if (isPremium) {
    PremiumFeatureCard()
} else {
    UpgradePrompt()
}
```

Or for a one-off synchronous check (non-UI logic):
```kotlin
if (premiumManager.isPremium) {
    // do premium thing
}
```

---

## Fetch interval

Remote Config is fetched at most **once per hour** in production (set in `PremiumModule.kt`). This is a Firebase requirement to avoid throttling. For development testing, you can temporarily lower it:

```kotlin
// In PremiumModule.kt — change for testing, revert before release
minimumFetchIntervalInSeconds = 0 // immediate fetch every time
```

---

## Offline behaviour

If the device has no internet on launch:
- The **cached** premium status from the last successful fetch is used
- The Installation ID is also cached after first fetch
- No crash, no false lockout

---

## Files added by this feature

| File | Purpose |
|------|---------|
| `data/premium/PremiumManager.kt` | Core logic: fetch, check, cache |
| `di/PremiumModule.kt` | Hilt providers for Firebase |
| `ui/settings/SettingsViewModel.kt` | Exposes premium state to Settings UI |
| `android/app/google-services.json` | **YOU must add this** — not in git |

> `google-services.json` contains API keys — never commit it to git. It should be in `.gitignore` already (Android Studio adds it by default).

---

## Troubleshooting

| Symptom | Cause | Fix |
|---------|-------|-----|
| Build fails: "File google-services.json is missing" | `google-services.json` not added | Download from Firebase console, place in `android/app/` |
| Tester ID shows blank | Firebase Installations hasn't responded yet | Wait a few seconds, or check internet connection |
| Premium not updating after publishing | Fetch interval (1 hour) not elapsed | Force-close and reopen app, OR temporarily set interval to 0 |
| Multiple people with same ID | Impossible — IDs are unique per app install | — |
