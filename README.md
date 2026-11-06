# Karoo ClearWind

A small data field for the Hammerhead Karoo 3 that shows wind direction and speed, turned to the
way you are riding, built to be read at a glance.

Wind comes from Open-Meteo (free, no API key) for your current location, fetched through the
Karoo's internet connection. Heading comes from the Karoo. Debug builds show demo data; release
builds never do.

Read `../CLAUDE.md` (the Karoo playbook) first.

## Build and install

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
./gradlew -q assembleDebug
adb install -r app/build.nosync/outputs/apk/debug/app-debug.apk
```

Then add the field to a ride page on the Karoo (Profiles → page → ClearWind).

## Licence

MIT, see `LICENSE`. The included Karoo SDK in `karoo-ext/` is Apache 2.0, see `karoo-ext/LICENSE`.
