# Long-press to change the field

**Problem:** the whole field had a tap handler (tap to cycle the arrow colour). A clickable
RemoteViews field keeps the touch, so the Karoo's own long-press (change data field) never started.
RemoteViews has no long-press hook and can't pass a touch back to the Karoo, so tap and long-press
can't share a spot.

**Decided:** remove the tap. Nothing in the field is clickable; the arrow colour is picked in
Settings (or with the "Switch arrow colour" remote button).

## Device screenshots

- `device/01-ride-page-dark.png`: ClearWind on a ride page (bottom right).
- `device/02-long-press-select-data-dark.png`: long-press on it opens the Karoo's Select data screen.
