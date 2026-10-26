# Call Notes — Auto Call Recorder (personal use)

A minimal Android Studio project that auto-records your own calls in the
background and lists them in-app with number, date/time, and duration.

## How to open and run
1. Install **Android Studio** (latest stable).
2. Open this folder (`CallRecorder/`) as a project.
3. Let Gradle sync (it will download dependencies automatically).
4. Connect your Android phone via USB with Developer Options + USB
   debugging enabled, then click **Run**.
5. On first launch, tap **Grant Permissions** and allow phone, call log,
   microphone, and notification permissions.

## How it works
- `CallReceiver` listens for call state changes (ringing → active → idle)
  and captures the number.
- `RecordingService` is a foreground service that starts `MediaRecorder`
  when a call becomes active and stops it when the call ends, saving an
  `.m4a` file and a database row (number, timestamp, duration, file path).
- `MainActivity` lists saved recordings; tapping one plays it back.

## Important real-world limitations (please read)
- **A notification cannot be fully hidden.** Since Android 8, any
  foreground service (required to record reliably while the screen is
  off) must show a notification — this is an OS rule, not something an
  app can turn off. This project uses the lowest possible priority
  (`IMPORTANCE_MIN`) so it's silent, makes no sound/vibration, and stays
  collapsed in the status bar — but it is not literally invisible.
- **Many phones block call-audio recording outright**, regardless of
  permissions: stock Samsung and Google Pixel devices largely prevent
  apps from capturing the other party's voice on calls, especially on
  Android 10+. Some Xiaomi/Realme/Oppo models allow it with extra
  settings (Security app → Permissions → "record calls"), others don't
  at all. Test on your specific device before relying on this.
- **One-party consent laws vary by country.** Recording your own calls
  is generally fine if you're a participant, but recording someone
  without informing them can be restricted depending on your
  jurisdiction — worth checking before using this regularly.

## Next steps you may want to add
- A settings screen to turn auto-record on/off per contact.
- Export/delete controls for old recordings.
- Cloud backup (e.g., to your own Drive) if you want recordings off-device.
