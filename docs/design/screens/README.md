# Screens for visuals

Clean ClearWind screens for marketing and the Karoo Artboard: the real ride page with the
ClearWind field bottom-left, the Karoo's floating back/play buttons removed (both bottom fields
redrawn), wind 5.2 m/s, arrow A2 at 45°.

- `screen-arrow-<colour>-<light|dark>.png`: full Karoo 3 screen, 480 × 800, ready for the artboard.
- `field-arrow-<colour>-<light|dark>@2x.png`: the field on its own, 476 × 284 (2×).

Colours: `speed` (by wind speed), `black-white`, `karoo-green`. Light uses
`../field/device/05-arrow-under-header-light.png`, dark uses the real dark capture
`../arrow-head/device/04-a2-mono-dark.png`.

Made from `../arrow-head/preview.html` (exported from the page's own drawing code). The six screens
are also in `Karoo/design-assets/` as `21–23-clearwind-arrow-*` so the Karoo Artboard preloads them.
