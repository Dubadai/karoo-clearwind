# Arrow head

Feedback on the built native-style arrow: the chevron wings are too long, so on diagonals it's hard
to tell the wings from the shaft.

`mockup.html` (built by `build.py` from `mockup.src.html`): each option drawn on the real device
screenshot `../field/device/05-arrow-under-header-light.png`, inside the Karoo 3 frame, at three
angles (45°, 135°, -100°), with the field at 2× under each device. Uses the shared helper
`Karoo/design-assets/karoo-device.js`.

- Now: long wings (built).
- A: short wings.
- B: short wings, bolder.
- C: solid triangle head, thin shaft.
- D: smaller solid head, thicker shaft.

Seen in context: in this field position the Karoo's own back button covers the lower-left part of
the arrow.

Round 2: user liked A but wanted the wings less short: A2 = built arrow with wings 25% shorter
(same thickness), added to the mockup.

Decision: A2 looks great.

`preview.html` (from `preview.src.html`, same `build.py`): A2 alone, light and dark devices
centred on a dark grey page. Tap the wind field to switch between the coloured arrow and a plain
black/white arrow. Dark mode is simulated from the light screenshot (greys flipped, colours kept);
a real dark screenshot needs the Karoo switched to dark mode.

Arrow colour: three options, shown in `preview.html` (tap the field to cycle): coloured by wind
speed, black/white, or Karoo green (#129A5E, sampled from the Karoo's own field icons). How the
rider picks one in the app: pending.

Decision: tap the field to cycle the colour (speed → black/white → Karoo green); units moved to
the remote action only. Built. Device: `device/01-a2-mono-light.png`, `02-a2-green-light.png`,
`03-a2-speed-light.png` (one real tap between each).

Bug found on the way: field taps never arrived. The tap PendingIntent was an action-only intent
with `setPackage`, but `ActionReceiver` has no intent filter, so Android never delivered it. Fixed
by naming the receiver class (`Intent(context, ActionReceiver::class.java)`), as karoo-interval
does. Same fix applied to `karoo-starter`.
