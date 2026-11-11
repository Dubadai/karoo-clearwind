# Developing ClearWind

For building the app yourself and publishing releases. Riders only need the [README](../README.md).

## Build and install

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
./gradlew -q assembleDebug
adb install -r app/build.nosync/outputs/apk/debug/app-debug.apk
```

Then add the field to a ride page on the Karoo (Profiles → page → ClearWind).

## Releases and update notices

The app tells riders on the Karoo when a newer release is out: `UpdateCheck.kt` reads
`releases/latest/download/manifest.json` every time the Karoo starts and once a day, never during a
ride, and puts an "Update available" card in Control Center › Notifications. Tapping it opens the
Karoo's Settings › Apps page for the app, whose UPDATE button installs the new APK. So every
release uploads `manifest.json` next to `clearwind.apk`, and is a normal release (not a pre-release):

```bash
./gradlew :app:assembleRelease
./gradlew -q :app:releaseManifest -PreleaseNotes="What changed"
cp app/build.nosync/outputs/apk/release/app-release.apk clearwind.apk
gh release create vX.Y.Z clearwind.apk app/build.nosync/manifest.json --title vX.Y.Z --notes "What changed"
```
