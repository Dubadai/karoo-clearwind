# Empty states

Shared rule (empty states follow the Karoo's own spacing, `KarooType.emptyPaint` / `emptyBaseline`):
a tile with no value says so, centred, in the label font at 25 px caps, ink bottom 39 px above the
tile bottom. ClearWind uses it when there's no wind to show:

- **Searching…**: no wind yet (before the first fetch).
- **No connection**: the last fetch failed and there's no wind. With wind already shown, a failed
  fetch keeps the last wind.

Picked **B**: the wind icon follows the Karoo's sensor legend too: grey while searching; grey,
smaller, with a red × when there's no connection (`KarooType.drawStateCross`, as PartnerETA's flag).
A (icon unchanged) was the alternative.

**Update 2026-11-09, shared rule "× only if needed":** a disconnected icon stays full size and grey,
and the red × only goes on tiles that show no text. ClearWind's tile says "No connection" in words,
so the × is gone: both states have the full-size grey icon. Before/after: `07-noconn-before-light.png`,
`08-noconn-after-light.png`, `08-noconn-after-dark.png`.

Pictures: `01`/`02` option A, `03`/`04` option B (quarter tile, light and dark), `05` B on a Karoo
page, `06` B at half page. Every size: `../gallery/` (variants `searching`, `noconn`).
