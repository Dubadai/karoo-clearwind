# Gusts: show or hide

We wanted a setting to show or hide the gust in brackets.

- Settings › **Gusts**: Show (default, as today) / Hide. Same segmented control as the other
  settings; help text "The strongest gust in brackets after the speed: 5.2 (9) m/s."
- Hidden: the header reads `5.2 M/S`. The arrow is laid out around the widest header for that
  setting (`8.8 M/S` instead of `8.8 (88) M/S`), so it stays steady in a tile and may get a
  little more room in narrow tiles.
- No remote action or tap for it (tap already switches the arrow colour).

| Picture | Shows |
|---|---|
| `01-show-light.png` | Today: `5.2 (9) M/S`, quarter page |
| `02-hide-light.png`, `02-hide-dark.png` | Gusts hidden: `5.2 M/S` |
| `03-settings-show.png`, `04-settings-hide.png` | The new Gusts setting |
| `05-settings-hide-example.png` | The settings example with gusts hidden |

Made with the field gallery (`nogust` variant) on the Android 12L emulator (Karoo3-api32, gesture navigation) and karooize.swift.
