# Gusts in brackets

Feedback: the wind felt stronger than the number. ClearWind showed only Open-Meteo's average wind
(`wind_speed_10m`). It now also fetches `wind_gusts_10m` (the past hour's strongest gust, same request)
and shows it in brackets after the average, like yr.no: **5.2 (9) M/S**.

- Average as before (m/s with one decimal under 10); the gust always whole; no brackets without a
  gust or when it isn't above the average.
- Speed and gust close together (0.17 of the text size), a wider gap before the unit (0.38).
- The arrow colour still follows the average.
- Fits every tile, also the widest case `43 (72)  KM/H` in the smallest.
- Considered and dropped: one "felt" number (average + (gust − average) / 3). Brackets keep the
  numbers you can check against a weather app.

Pictures (mock, first spacing): `01-small-light.png`, `02-small-dark.png`, `03-half-light.png`.
Final look: `../gallery/`.
