# Settings kit (ClearWind)

ClearWind's Settings rebuilt from the shared settings kit (karoo-starter `SettingsKit.kt`, design
`karoo-starter/docs/design/settings-kit/`), on the Android 12L emulator (Karoo3-api32), light:

- Order: Wind speed unit, Tile colour, Arrow colour, Show gusts, then the shared app rows (Update
  notices, version and repo).
- Each heading has its own icon: unit = the kit's `ic_settings_speed`, Tile colour =
  `ic_settings_tiles`, Arrow colour = ClearWind's arrow, Show gusts = ClearWind's wind icon.
- Show gusts is a `settingsSwitchRow`. With a coloured tile, Arrow colour is greyed out
  (`enabled = false`) and its help says why.
- The Example card scrolls with the page.

`settings-kit-light.jpg`: the four screens in the Karoo 3 on Sky (top, unit and tile, arrow and gusts,
end). `01–04-light.png`: the same screens alone. Also checked in dark mode on the emulator and on
our test Karoo.
