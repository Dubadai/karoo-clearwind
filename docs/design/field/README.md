# ClearWind field

`mockup.html`: current sketch (round 10). `mockup-round2.html` … `mockup-round9.html`: earlier rounds. `inspiration/`: the
arrow references the user shared.

Sketches of the data field, drawn at real Karoo pixel sizes (¼ page 239×180,
⅛ page 239×120, wide row 478×120), light and dark. Has an m/s ↔ km/h preview toggle.

Round 1 (rejected): arrow + speed + HEAD/CROSS/TAIL word, or a headwind number.
Feedback: no headwind/tailwind words, the arrow says it; the arrow must be the biggest thing;
units switchable between m/s and km/h.

Round 2:
- Option A: arrow fills the height, speed beside it.
- Option B: arrow fills the field, speed small in the top-right corner.
- Arrow coloured by effect (head/cross/tail) or plain black/white.
- Units: tap the field to switch, plus a remote button action; choice remembered.

Round 3: arrow redrawn after the references: thick rounded line, open chevron head.
- Style 1: whole field coloured by effect (head/cross/tail), white arrow.
- Style 2: style 1 plus a soft ring with a dot marking straight ahead.
- Style 3: plain background, coloured arrow, ring and dot.

Round 3 feedback: style 2 liked most but a bit busy. The dot should show where we are relative
to the wind, with a line only between the front dot and the wind. Speed centred. Also try the arrow
always in the middle.

Round 4: colour tile, no full ring. Front dot = you (straight ahead); thin arc the short way round
to a small dot where the arrow points.
- Layout 1: arrow left, speed and unit centred in the space beside it.
- Layout 2: arrow always centred; speed below it (¼ page) or centred to the right (short fields).

Round 4 feedback: better, but the speed should always be centred.

Round 5: speed centred left-to-right in the field, every size.
- Layout A: one column, arrow centred, speed centred under it (unit hangs to the right).
- Layout B: speed in the middle of the dial, arrowhead on the dial edge, arc from the front dot.

Round 5 feedback: "centred" meant the arrow is always centred in the card.

Round 6: arrow dead centre in every size; speed to its right, centred up-down against the card and
left-right in the space beside the arrow, unit under the number.

Decision (final): colour tile by effect (red head, amber cross, green tail), rounded line arrow
as big as the card allows and dead centre, front dot (you) with a thin arc to where the wind blows,
speed in the top-right corner (top ~20 px left free) with no unit text. m/s or km/h, switched by
tapping the field or a remote button action.

Device: `device/01-demo-light.png` (debug demo data, ⅛-page field 238×142, light).

Round 7 (built, on the device) feedback: still a bit busy. Arrow coloured by wind speed; dots and
line black and tonal; speed black.

Round 8: plain white/black field. Arrow colour = wind speed. Dots and arc in soft black (white in
dark mode), speed black (white).
- Option A: four steps: under 3, 3–6, 6–9, 9+ m/s (green, yellow, orange, red).
- Option B: smooth green → red from 0 to 12 m/s.

Round 8 feedback: smooth scale; dots filled so the line doesn't show through; speed in the corner
the same distance from the top and the right edge.

Round 9: smooth scale only; dots and arc solid greys (dots on top of the arc); speed digits 12 px
from the top and 12 px from the right edge.

Decision: round 9, built. Device: `device/02-speed-colour-light.png` (debug demo data, 238×142, light).
`device/01-demo-light.png` is the earlier colour-tile build (round 7).

Round 9 additions: wind icon top-left, the size of the Karoo's own field icons (23 px, measured).
Then: use the Karoo's typography: icon left, speed with unit ("5.2 M/S") on the right as the header.
Measured on the device (`device/03-karoo-native-fields-light.png`): header font IBM Plex Sans
Condensed Medium (system family `ibm-sans-cond`), caps 20 px tall, text ends ~10 px from the right
edge, centred on the 23 px icon, icon 12 px in from the left.

Round 10:
- Option A: arrow keeps the whole field, header in the top corners.
- Option B: header row first, arrow fills the space under it (smaller).

Decision: option A (arrow uses the whole field, header in the top corners), with the dots and arc
removed for now.

After building round 10 A: the font didn't match and the arrow was too close to the speed.
- Font: the Karoo's `dataHeaderTextStyle` (from its ride app) is `ibm-plex-sans-condensed`, 17 sp,
  regular, black/white, capitals. Matched exactly (compared ink weight against "LL SPEED" on the
  device).
- Arrow: the Karoo's own wind-arrow style (straight shaft, sharp angled head, filled), a bit larger.
- Rule: the arrow never touches the header. It stays centred and is sized so a circle round it at
  any angle keeps 6 px clear of the icon and the speed label (digits measured as 8, so the size
  only changes when the number format does).

Device: `device/04-native-arrow-header-light.png` (debug demo data, 238×142, light).

Then: arrow sits lower, centred in the space under the header, so it can be bigger (about 80 px
across instead of 45 in a 238×142 field). In fields wide enough that it would be bigger centred in
the whole field beside the header, it stays centred there. Device:
`device/05-arrow-under-header-light.png`.
