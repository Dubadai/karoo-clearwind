# Coloured tile (mock)

Feedback: keep the Karoo's own tile for the normal look, and add a coloured tile with a black / white
arrow, drawn by us since the Karoo SDK can't colour an extension's tile. It must follow the tile rules.

Tile rules (karoo-type README, SmallerFields `TileRenderer`): our card fills the field box, radius
**14**, 1 px **#7D7D7D** border on the box edge; with data field boundaries off, a plain rectangle
(no corners, no border). Only when there's wind: Searching / No connection keep the Karoo's tile.

## Round 1 (in `round1/`)

Two palettes to pick from:

- **A · Speed scale**: ClearWind's own green → red, white header and arrow.
  `round1/01-a-speed-scale-light/dark.png` (5.2 m/s).
- **B · Karoo colours**: the Karoo's zone colours (climb1..7, same light and dark) in wind bands
  < 2, 4, 6, 8, 11, 14, ≥ 14 m/s; black header and arrow on climb1–4, white on climb5–7 (as the
  Karoo's climb fields). `round1/02` calm 1.4, `03` moderate 5.2 (light, dark), `04` strong 12.6, `05` half page.
- `round1/06-corners-zoom-light.png`: corners and border next to the Karoo's tiles (gallery, 6×).

## Round 2: soft tints (Feedback: careful with colours, look at Apple; never switch text between black and white)

After iOS's tinted fills: Apple's system colours over the tile at low strength, the text keeps its
normal colour. Light: system colour at 28 % over white. Dark: its dark variant at 36 % over black.
Header and arrow always black (light) / white (dark).

| Band (m/s) | Apple colour | Light tint | Black on it | Dark tint | White on it |
|---|---|---|---|---|---|
| < 2 calm | mint | #B8EFED | 16.6 | #245351 | 8.6 |
| 2–4 light | green | #C6EFD1 | 16.7 | #114B20 | 10.2 |
| 4–6 gentle | yellow | #FFF1B8 | 18.5 | #5C4D04 | 8.3 |
| 6–8 moderate | orange | #FFE1B8 | 16.7 | #5C3904 | 10.3 |
| 8–11 strong | red | #FFC8C5 | 14.3 | #5C1915 | 13.1 |
| ≥ 11 gale | purple | #E9CFF6 | 14.7 | #452057 | 13.1 |

All far above WCAG AA (4.5 : 1 text, 3 : 1 arrow); most reach AAA (7 : 1).
`07-soft-bands-light-dark.png` (all six bands), `08-soft-page-light/dark.png` (5.2 m/s on a ride
page), `09-soft-half-gale-light.png` (half page, 12.6 m/s).

## Round 3: Apple vs Karoo colours, toned down the same way

Feedback: compare the Karoo's colours, toned down, with Apple's. Karoo zone colours climb1–4, 6, 7
(the Karoo's HR set plus purple) at the same strengths (28 % over white, 36 % over black; the Karoo
uses the same colours in light and dark). Same bands, same fixed black / white text.

| Band | Karoo colour | Light tint | Black on it | Dark tint | White on it |
|---|---|---|---|---|---|
| < 2 | climb1 #60EEB2 | #D2FAE9 | 18.6 | #235640 | 8.5 |
| 2–4 | climb2 #00B988 | #B8EBDE | 16.0 | #004331 | 11.4 |
| 4–6 | climb3 #FFF500 | #FFFCB8 | 19.9 | #5C5800 | 7.4 |
| 6–8 | climb4 #FB8C65 | #FEDFD4 | 16.7 | #5A3224 | 11.0 |
| 8–11 | climb6 #D60404 | #F4B9B9 | 12.5 | #4D0101 | 15.9 |
| ≥ 11 | climb7 #B700A2 | #EBB8E5 | 12.5 | #42003A | 16.2 |

`10-apple-vs-karoo-light.png`, `11-apple-vs-karoo-dark.png`: top Apple, bottom Karoo, all six bands.
`12-apple-vs-karoo-page-light.png`: 9.4 m/s on a ride page, Apple left, Karoo right.

## Round 4: how strong can the Karoo colours go? (Feedback: keep Karoo)

Strongest strength per colour with fixed black (light) / white (dark) text:

| Band | Colour | Light, AA 4.5 | Light, AAA 7 | Dark, AA 4.5 | Dark, AAA 7 |
|---|---|---|---|---|---|
| < 2 | climb1 | 100 % | 100 % | 55 % | 41 % |
| 2–4 | climb2 | 100 % | 100 % | 73 % | 54 % |
| 4–6 | climb3 | 100 % | 100 % | 49 % | 37 % |
| 6–8 | climb4 | 100 % | 100 % | 69 % | 52 % |
| 8–11 | climb6 | 83 % | 56 % | 100 % | 84 % |
| ≥ 11 | climb7 | 80 % | 56 % | 100 % | 89 % |

One strength for every band (so the steps stay even), limited by the weakest band:

- Row 1: 28 % light / 36 % dark (round 3).
- Row 2: **56 % / 37 %**, the strongest at AAA (7 : 1) in every band.
- Row 3: **80 % / 49 %**, the strongest at AA (4.5 : 1) in every band.

`13-karoo-strength-light.png`, `14-karoo-strength-dark.png` (rows 1–3), `15-karoo-aaa-page-light.png`
(row 2, 9.4 m/s on a ride page).

## Built (Feedback: row 2, AAA)

Settings: **Tile colour** (Off / Wind strength) comes before **Arrow colour**. With a coloured tile,
Arrow colour is greyed out (the arrow stays black / white for contrast) and its help says so.
`16-settings-tile-off.png`, `17-settings-tile-on-arrow-disabled.png`, `18-settings-tile-on-example.png`.

Still to check on the device: the live coloured tile next to the Karoo's own (needs Wi-Fi for wind).
