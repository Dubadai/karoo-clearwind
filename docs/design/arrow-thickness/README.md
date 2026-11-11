# Arrow thickness: the same in every tile (proposal)

We liked the arrow in a short half-width tile (about 9 px thick on the Karoo) and asked to
keep that thickness at every size.

**Now:** the arrow is one shape scaled to fit, so its lines are 1/12 of the arrow's box: about
9 px in a short half-width tile, 10–14 px in taller ones, 16 px full width, 40 px on a half page.

**Proposed:** build the arrow outline from a fixed line width (9 px) at any size: shaft and both
wings 9 px, sharp tip, same wing length and angle as now (arrow A2). The arrow keeps its current
size and position (as big as the space under the header allows); only the lines stay 9 px.

- `01-now-vs-fixed-9px-light.png`: five tile sizes, now (left) and fixed 9 px (right). Drawn from
  the ClearWind geometry (WindRenderer's sizing and arrow A2), header from real ClearWind pixels
  placed like the Karoo's own (see `../header-typography/`).

## Round 2

Feedback: 9 px everywhere is too thin when the arrow is really big; on a half page it should be
thicker but smaller, and on full width smaller too.

**Rule:** the arrow is as big as the space allows, but never longer than a maximum. Its lines are
9 px up to the size we liked (68 px long) and get thicker gently up to the maximum size.

- `02-capped-size-a-b-light.png`: now, **A** (at most 110 px long, lines 9 → 12 px) and
  **B** (at most 140 px long, lines 9 → 15 px). Half-width tiles barely change; full width and
  half page get a smaller, bolder arrow.

## Decided and built

Feedback: **A, but the half page like B.** Tiles: at most 110 px long, lines 9 → 12 px. Half page and
full page (478 px wide, 250 px tall or more): at most 140 px long, lines 9 → 15 px. Lines are 9 px
up to a 68 px arrow, and never thicker than 2.6 of the arrow's 24 units (tiny arrows stay arrows).

- `03-built-every-size-light-dark.png`: the built field from the emulator gallery at every Karoo
  size (one row, quarter, full-width row, half page, full page), light (plain arrow) and dark
  (speed colour).
