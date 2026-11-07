# Field gallery

The ClearWind field in each arrow colour (speed, plain, green) at every Karoo 3 field size, light
and dark, drawn by the field's own code on the Karoo 3 emulator (no Karoo needed):

- `fields/<colour>-<size>-<light|dark>.png`: the field alone (small examples)
- `screens/…`: on a Karoo page with other fields (480 × 800)
- `artboard/…` (not in git): in the Karoo 3 frame on the Artboard's Sand (light) or Night (dark)
  gradient, 1920 × 1080

Sizes: `full` 478×721, `half` 478×336, `row` 478×141, `quarter` 238×168, `small` 236×141.
Make them again (about a minute):

```bash
bash ../design-assets/tools/field-gallery/gallery.sh karoo-clearwind
```

What's drawn: `app/src/debug/kotlin/.../GalleryFields.kt`.
