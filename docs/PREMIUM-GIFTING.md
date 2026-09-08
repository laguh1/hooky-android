# Hooky — Manual Premium Gifting

How to grant lifetime or time-limited (e.g. 1-year) premium to a user via Firebase Remote Config.

---

## User instructions (tell the user)

1. Open Hooky → Settings
2. Scroll down to **"Tester ID"** — tap the copy icon
3. Send you the ID (Instagram DM, email, etc.)

---

## Your side (Firebase Console)

1. Go to [console.firebase.google.com](https://console.firebase.google.com) → Hooky project
2. **Remote Config** → find the `premium_ids` parameter
3. Edit the value — it is a JSON array of objects. Add the new entry:
   ```json
   [
     {"id": "existing_id_1", "expires": null},
     {"id": "new_user_id_here", "expires": "2027-08-31"}
   ]
   ```
   - `"expires": null` → lifetime, never expires
   - `"expires": "yyyy-mm-dd"` → premium through the end of that day, then expires
     automatically on its own — you don't need to come back and remove it
4. Click **Publish changes**

The user gets premium automatically the next time they open the app (no update needed).
The same automatic check applies on expiry — no action needed from you on the day it lapses.

---

## First-time Firebase setup

If the `premium_ids` parameter does not exist yet:

1. Remote Config → **Add parameter**
2. Parameter key: `premium_ids`
3. Default value: `[]`
4. Click **Save** → **Publish changes**

---

## Notes

- No limit on how many IDs you can add
- Premium is checked on every app launch (Firebase fetch)
- If you want to revoke premium early, remove the entry from the array and republish
- With an `expires` date set, you don't need to do anything on the expiry day — it lapses on its own
- The installation ID is tied to the device/app install — if the user reinstalls, they get a new ID and you would need to re-add them

## Migrating an existing `premium_ids` value

If your Remote Config still has the old flat-string format
(`["fh7k2mXXXX", "a9bQqXXXX"]`), it will stop being parsed once this update ships —
wrap each existing ID as an object before publishing:
```json
[{"id": "fh7k2mXXXX", "expires": null}, {"id": "a9bQqXXXX", "expires": null}]
```
This keeps everyone currently on the list as lifetime premium; add `expires` dates
only where you actually want a 1-year-style cutoff (e.g. for early testers).
