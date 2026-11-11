# Header like the Karoo's own (bug, 7 Nov 2026 ride)

**Bug:** on the ride, ClearWind's header (wind icon and "3.7 M/S") was smaller than the Karoo's
own headers and sat higher and further in. The Karoo sizes a field from its page layout
(e.g. 238×168 px), but when the route bar or workout bar is on screen the tile is shorter
(238×142) and the app isn't told. ClearWind drew one picture at the old size and the Karoo shrank
the whole picture (about 85%) to fit, text included. The Karoo's own fields lay out again instead.

Measured on the device (light mode, tiles with boundaries), the Karoo's own header in a half-width
tile:

| Tile | Caps top (from the white tile) | Cap height | Icon |
| --- | --- | --- | --- |
| Short (≈110–130 px white) | 15 px | 20 px | 23 px, 12 px from the left, centred on the caps |
| Taller (≈136–156 px white) | 20 px | 23 px | 20–23 px, 12 px from the left, centred on the caps |

Right edge of the text: 10 px from the view's edge.

**Fix (proposed):** the header is never scaled: drawn at the Karoo's size and place for the tile
it is actually in; only the arrow shrinks or grows to fit the space under it.

- `01-now-vs-fix-light.png`: left, crops from the 7 Nov ride; right, the same tiles with the header
  where the Karoo draws its own (made from real ClearWind pixels; the speed reads 2.5 M/S because
  it was taken from the test build).

## Built

The Karoo picks its header size from the tile size it reported (ViewConfig), even when a bar makes
the tile shorter: capitals 20 px in tiles up to 142 px, 23 px in 168 px tiles; they start 21 px
from the top at 142 px and 26 px at 168 px. ClearWind now does the same, and draws the header as
its own picture that the field shows at 1:1 (`field_under.xml` / `field_beside.xml`); only the
arrow picture may shrink.

Device checks (our test Karoo, dark mode, 3× zoom, test build):

- `02-device-168px-tile-vs-3s-speed-dark.png`: same top, baseline, size and right edge as 3S SPEED.
- `03-device-142px-tile-vs-eta-dark.png`: same top and baseline as ETA, 20 px capitals.

Not checked on a device yet: a full-width or half-page ClearWind tile, and a tile shortened by the
route bar during a ride.

## Device vs gallery (2026-11-09)

- `04-device-small-tile-vs-3s-speed-light.png`: our test Karoo after the header outline (0.002 em,
  PR 44): ClearWind "– M/S" beside the native 3S SPEED, same weight by eye. "M" stroke 3.27 px vs
  native 3.18 at 23 px caps (ours / native 1.03).
- `05-device-vs-gallery-light.png`: top that device row, bottom the gallery (workspace 04736c4, no
  thickening, stand-ins slimmed 0.3 px): "M" 2.76 vs stand-in 2.69 (1.03, as on the device).
- `06-device-vs-gallery-api32-light.png`: the gallery on the Android 12L emulator (Karoo3-api32,
  workspace b5088cc, stand-in outline 0.1 px), same quarter tile as the device row. The "–" is
  identical (15.2 × 3.0 px, ink 45.6); ClearWind "M" 3.34 vs device 3.27; stand-in "M" 2.91.
  The Android 17 emulator drew small text ~10–15 % lighter and the dash 2 px tall; the API 32 one
  closes that gap.
