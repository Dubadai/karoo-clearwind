# Settings screen

Question: can riders pick the units when they install it? Yes: like karoo-interval, ClearWind gets
a settings screen in the Karoo's app list, in the Karoo's own style (segmented control as in
Karoo Settings → Display), measured on `reference/interval-settings-light|dark.png`.

`mockup.html` (from `mockup.src.html` via `build.py`, uses `../field.js` and the shared
`Karoo/design-assets/karoo-device.js`): two options, each light and dark on the device, with the
field showing the chosen settings. Tap a segment on a device to try it.

- A: Wind speed unit m/s | km/h | mph | kn, plus Arrow colour Speed | Plain | Green.
- B: Wind speed unit Auto | m/s | km/h | mph (Auto = the Karoo's own units, km/h or mph).

The remote "Switch wind units" action and the field tap for colour stay.

Decision: units knots | m/s | km/h | mph (no Auto), m/s selected on install; arrow colour
Speed | Plain | Green. Built as `SettingsActivity` (launcher entry, so it's in the Karoo's app
list). Settings, field tap and remote actions share `Settings.kt`; the running field listens for
changes. Device: `device/01-settings-light.png`, `device/02-settings-knots-light.png`, `device/03-settings-knots-dark.png`.
