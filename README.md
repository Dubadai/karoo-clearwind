# ClearWind for Karoo 3

Listed on the Karoo as **DDB ClearWind**, next to David's other apps.

A data field for the Hammerhead Karoo 3 that shows the wind turned to the way you ride, built to be
read at a glance.

![ClearWind in dark mode: the arrow coloured by wind speed, black/white and Karoo green](docs/images/clearwind-dark.webp)
![ClearWind in light mode](docs/images/clearwind-light.webp)

- **Arrow:** points the way the wind blows (up = tailwind, down = headwind), as big as the field allows.
- **Header like the Karoo's own fields:** wind icon left, wind speed right (`5.2 M/S`).
- **Arrow colour:** by wind speed (green → red), black/white, or Karoo green, picked in Settings.
- **Settings in the Karoo's app list:** wind speed in knots, m/s, km/h or mph, and the arrow colour.
- **Long-press the field** to swap it for another, like any Karoo field.
- **Remote button actions:** switch units and switch arrow colour.

Wind comes from Open-Meteo (free, no API key) for your location, every 10 minutes or after 5 km,
fetched through the Karoo's Wi-Fi or the Hammerhead phone app; your position is rounded to about
1 km. Heading comes from the Karoo. Debug builds show demo data; release builds never do.

**Install:** download `clearwind.apk` from [Releases](https://github.com/Dubadai/karoo-clearwind/releases),
sideload it, then add the field: Profiles → a ride page → tap a field → DDB ClearWind → ClearWind.
More in the [rider guide](docs/guide/README.md).

Developers: read `../CLAUDE.md` (the Karoo playbook) first.

## Build and install

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
./gradlew -q assembleDebug
adb install -r app/build.nosync/outputs/apk/debug/app-debug.apk
```

Then add the field to a ride page on the Karoo (Profiles → page → ClearWind).

## Releases and update notices

The app tells riders on the Karoo when a newer release is out: `UpdateCheck.kt` reads
`releases/latest/download/manifest.json` when the Karoo starts and once a day, never during a
ride, and puts an "Update available" card in Control Center › Notifications. Tapping it opens the
Karoo's Settings › Apps page for the app, whose UPDATE button installs the new APK. So every
release uploads `manifest.json` next to `clearwind.apk`, and is a normal release (not a pre-release):

```bash
./gradlew :app:assembleRelease
./gradlew -q :app:releaseManifest -PreleaseNotes="What changed"
cp app/build.nosync/outputs/apk/release/app-release.apk clearwind.apk
gh release create vX.Y.Z clearwind.apk app/build.nosync/manifest.json --title vX.Y.Z --notes "What changed"
```

## Licence

MIT, see `LICENSE`. The included Karoo SDK in `karoo-ext/` is Apache 2.0, see `karoo-ext/LICENSE`.
