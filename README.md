# Hop Timer (Android)

Offline Android app for scheduling up to 10 hop additions during the boil.

## Build an APK automatically with GitHub Actions

You do **not** need Android Studio on your computer.

1. Sign in to GitHub and create a new repository (for example, `HopTimer`).
2. Unzip the downloaded project archive. Open the `HopTimer` folder inside it.
3. Upload the **contents** of that `HopTimer` folder to the root of your GitHub repository. The `.github/workflows/build-apk.yml` file must be included. If uploading through the GitHub website, use **Add file → Upload files** and drag the files and folders from the project folder.
4. Commit the upload to the `main` branch.
5. Open the repository's **Actions** tab. Select **Build Hop Timer APK**. If the workflow has not started automatically, click **Run workflow**.
6. Wait for the workflow to finish with a green check mark.
7. Open the completed workflow run. At the bottom, under **Artifacts**, download **HopTimer-debug-apk**.
8. Extract the downloaded artifact ZIP. Install `app-debug.apk` on your Android phone. Android may ask you to allow installation from that browser/file manager.

The workflow uses JDK 17 and Gradle 8.2 and runs `assembleDebug`. The APK is an unsigned debug build suitable for personal testing and installation; it is not a Play Store release build.

## Manual build (optional)

Open this folder in Android Studio with Android SDK Platform 34 installed, then run **Build → Build Bundle(s) / APK(s) → Build APK(s)**.

## App behavior

- Total boil duration in minutes.
- Up to ten hop additions, entered as minutes remaining until the end, hop name and grams.
- Elapsed time from start, remaining time, pause/resume/stop.
- Foreground service and partial wake lock keep the timer running while the screen is locked.
- Alert notification, sound, vibration and Ukrainian text-to-speech announcement at each addition (availability of Ukrainian TTS voice depends on the phone).

Test on the target phone before brew day, especially notification permission, sound volume, TTS voice availability and battery-optimization settings.
