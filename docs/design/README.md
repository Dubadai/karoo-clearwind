# Design

One folder per feature: the HTML mockup(s) shown before building, a short README with what was
decided, and `device/` screenshots from the Karoo (`NN-what-light|dark.png`).

| Folder | What |
| --- | --- |
| `gallery/` | The field at every Karoo size, light and dark, made on the emulator (`gallery.sh`) |
| `settings-example/` | "Example" card on the Settings page (live wind field) |
| `field/` | The ClearWind data field: layout options A/B |
| `arrow-head/` | Arrow head options (wings too long), shown on the device |
| `screens/` | Clean screens and field images for visuals and the Karoo Artboard |
| `settings/` | Settings screen: wind unit and arrow colour (options A/B) |
| `long-press/` | No tap handler, so the Karoo's long-press (change field) works |
| `transparent-field/` | Bug fix: no field background, so the Karoo's rounded tile shows (before/after) |
