# Building the APK without Android Studio

This is an **early prototype**, not a verified working alarm app. It does **not** yet provide the agreed one-button guided voice setup, English/Latvian selection, large lock-screen buttons, or a tested alarm experience on Android 16. Do not depend on it for important reminders.

## GitHub Actions build

1. Create a free GitHub account if needed, and a **private** repository named `VoiceReminder`.
2. Upload the **contents** of this folder to the repository (including the hidden `.github` directory). A GitHub Desktop client is optional; the website's Upload files feature can upload the project files, but hidden directories may be easier to add using GitHub's web file editor.
3. In the repository, open **Actions** > **Build Voice Reminder APK** > **Run workflow**.
4. Wait for the build to finish. If it fails, inspect the build log; no APK is available until the job succeeds.
5. Download the `VoiceReminder-v0.1-debug-apk` artifact from the completed run and unzip it to find `app-debug.apk`.
6. Copy the APK to the phone and open it to install, approving the source only if you trust the file.

The APK is **debug-signed** and suitable only for private testing. It is not a release-signed public distribution build. To preserve updates across future releases, a stable private release signing key will be required.

Android 12+ exact alarm permission may require a manual settings approval. Android 13+ notifications also require approval. Lock-screen full-screen alarms are not implemented in this prototype.
