# MIND — Personal Life OS

Native Android app (Kotlin, Jetpack Compose, Room). Arabic / English. 100% offline.

## Build an APK
**Android Studio:** open the folder → Build ▸ Build Bundle(s)/APK(s) ▸ Build APK(s).
**Command line (Gradle 9.3.1):** `gradle :app:assembleRelease` → `app/build/outputs/apk/release/`.
**GitHub:** push the project and run the "Build APK" workflow (Actions tab), then download the artifact.

Without your own keystore the release APK is signed with the debug key (fine for installing/testing).
For Google Play set `KEYSTORE_PATH`, `STORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`.

## Cloud accounts (Google / Facebook / E-mail) and sync — setup checklist
Package: `com.mind.mim` · Project: `mind-65b3e` · `app/google-services.json` is already in place.

Signing fingerprint of the fixed key in `app/mind.keystore` (debug AND release builds use it):
```
SHA-1   E1:E8:6B:E1:E8:0D:4A:4C:61:74:3A:A5:C3:6B:F2:9A:B2:A6:7D:10
SHA-256 DB:A5:B8:CD:3D:C9:0C:ED:9C:EE:8F:7E:A8:AE:CA:E4:CA:6C:02:36:39:32:9E:BD:C3:89:3A:C4:0D:AB:76:7E
```
1. Firebase Console → Project settings → your Android app (`com.mind.mim`) → **Add fingerprint**: paste the SHA-1, then the SHA-256.
2. Authentication → Sign-in method → enable **Email/Password**, **Google** (choose a support e-mail) and **Facebook** (App ID + App secret from developers.facebook.com; copy Firebase's OAuth redirect URI into the Facebook app).
3. **Download `google-services.json` again** (it must now contain an `oauth_client` entry — today it is empty, which is why Google sign-in cannot work yet) and replace `app/google-services.json`.
4. Firestore Database → Create database, then set these rules:
```
rules_version = '2';
service cloud.firestore { match /databases/{db}/documents {
  match /users/{uid} { allow read, write: if request.auth != null && request.auth.uid == uid; } } }
```
E-mail accounts must confirm their address (a verification link is sent at sign-up) before sync starts.
Facebook login uses Firebase's built-in OAuth flow, so no Facebook SDK or key hashes are needed.

## Home-screen widgets
Long-press the home screen → Widgets → MIND: **Clock**, **Water** (tap + to log a glass), **Pomodoro** (opens the timer) and **Music** (previous / play-pause / next for the active player).

## App lock while studying
Study → "App lock while studying": turn it on, grant *Usage access* and *Display over other apps*, then choose the apps to block.
During a **focus** Pomodoro phase a "back to study" panel covers those apps; during breaks they open normally.
A floating timer bubble also appears above other apps while a session runs (needs *Display over other apps*).

## Web version
`mind-web.html` is self-contained. To enable Google/Apple/Facebook/e-mail sign-in and cloud sync, fill the `FB` constant at the top of its script with your Firebase web config and host the file on your own site (the Firebase scripts are loaded from gstatic). Use *Export for app* / *Import from app* to move data between the web version and the Android app, or sign in with the same account on both.
