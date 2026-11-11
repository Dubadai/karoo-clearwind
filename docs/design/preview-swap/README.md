# Bug: the field switched between two sizes (three causes)

Feedback: the wind speed and arrow changed size "at certain angles". Two causes, found on the Karoo:

1. **A preview kept drawing into the live field.** The Karoo starts a preview view of the field
   (`preview=true`, 240 × 128) for the page editor and field chooser, and could keep it running.
   Its pictures (20 px capitals, small arrow) then showed up in the live 238 × 168 tile between the
   live view's (23 px capitals). Fix: a preview draws one still picture with example wind and stops.
2. **The arrow followed the text width.** Its size and place were worked out from the header text,
   so a wider number (9.8 → 12, a gust appearing) could move it from under the header to beside it.
   Fix: the arrow is laid out around the widest header the unit can show (`8.8 (88)  M/S`), so it
   keeps one size per tile.

3. **The other page's picture.** With ClearWind on two ride pages (a 238 × 168 and a 238 × 142
   tile) the Karoo now and then showed the other page's picture in the visible tile: the header
   dropped to the short-tile size for ~2 s, about every 15 s (caught in 24 of 159 screenshots;
   the Karoo's own fields never changed). Each view sends its own pictures, so the mix-up is on
   the Karoo's side. Fix: the Karoo's `ActiveRidePage` event says which page is on screen and its
   fields' grid sizes; a view draws only while its grid size is on that page, and draws at once
   when its page comes back. Checked: one size in 128 of 128 screenshots, and page swipes switch
   views within ~40 ms.

Also from this feedback: the arrow is 10 % smaller (90 % of its room; caps 100 / 126 px).

Device shots (pre-ride page, dark): `01-device-preview-size-dark.png` (preview's picture, small
header), `02-device-live-size-dark.png` (live picture).
