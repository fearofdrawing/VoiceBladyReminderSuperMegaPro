# Voice Reminder v0.3

Changes: large alarm-screen acknowledge and snooze buttons (5, 10, 15, 60 minutes); notification actions; Settings for alarm ringtone, hiding test button, clearing completed or all reminders; automatic 14-day deletion of completed reminders and voice files when app opens or phone restarts.

Important: Android 14+ may restrict automatic full-screen launches. Tapping the alarm notification opens the large-button screen. For some devices the user must grant full-screen notification access.

## GitHub
Upload this project into a single folder such as `VoiceReminder_v0.3_GitHub_Ready`. Keep your existing `.github/workflows/build-apk.yml` with its auto-find Gradle project logic. If the older v0.2 folder is still present, remove it or ensure the workflow selects this new v0.3 folder. Do not upload the included .github workflow over the working one.
