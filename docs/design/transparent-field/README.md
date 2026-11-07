# Transparent field (bug fix)

**Bug:** the field painted a solid white (black at night) rectangle over its whole box, so on the
Karoo's page editor its square corners stuck out past the Karoo's rounded tile border and covered
the grey gap between tiles.

**Fix:** no background at all. The root view and the bitmap stay transparent, so the Karoo's own
tile (rounded corners, border, white or black) shows through, like its built-in fields. Text and
arrow colours still follow light/dark mode.

Screenshots (Karoo 3, light mode, page editor):

- `01-before-page-editor-light.png`, `02-before-corners-light.png`: white corners outside the border
- `03-after-page-editor-light.png`, `04-after-corners-light.png`: the field follows the tile
