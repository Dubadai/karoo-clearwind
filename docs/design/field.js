// The ClearWind field, drawn like WindRenderer.kt. Shared by the design mockups (inlined by build.py).

// Arrow A2: native shape, wings 25% shorter. 24-unit box, centre (12,12), pointing up.
const ARROW = new Path2D('M12,4 L18,10 L16.58,11.42 L13,7.83 L13,20 L11,20 L11,7.83 L7.42,11.42 L6,10 Z');
const ARROW_RADIUS = 34;                                  // design units (box 100), any angle
const ICON_PX = 23, ICON_LEFT = 12, ICON_TOP = 12, TEXT_RIGHT = 10, GAP = 6;
const HEADER_FONT = '500 29px "IBM Plex Sans Condensed"';   // Karoo header: capitals 20 px tall (measured)
const KAROO_GREEN = '#129A5E';
const COLOURS = ['speed', 'mono', 'green'];

const STOPS = [[0,'#2F8A3E'],[4,'#C99A00'],[8,'#E06A10'],[12,'#C62828']];
function speedColor(ms){
  const hex = c => [1,3,5].map(i=>parseInt(c.substr(i,2),16)), v = Math.min(ms, 12);
  for (let i=1;i<STOPS.length;i++) if (v <= STOPS[i][0]) {
    const [a0,c0]=STOPS[i-1],[a1,c1]=STOPS[i], f=(v-a0)/(a1-a0), A=hex(c0), B=hex(c1);
    return `rgb(${A.map((x,j)=>Math.round(x+(B[j]-x)*f)).join(',')})`;
  }
  return STOPS.at(-1)[1];
}
const UNITS = {
  ms:  { label: 'm/s',  head: 'M/S',  k: 1,        decimals: true },
  kmh: { label: 'km/h', head: 'KM/H', k: 3.6 },
  mph: { label: 'mph',  head: 'MPH',  k: 2.23694 },
  kn:  { label: 'kn',   head: 'KN',   k: 1.94384 },
};
function speedText(ms, unit = 'ms') {
  const u = UNITS[unit], v = ms * u.k;
  return (u.decimals && v < 9.95 ? v.toFixed(1) : String(Math.round(v))) + ' ' + u.head;
}

function windIcon(ctx, color) {
  const s = ICON_PX / 18;
  ctx.save();
  ctx.translate(ICON_LEFT - 2 * s, ICON_TOP - 3.5 * s);
  ctx.scale(s, s);
  ctx.strokeStyle = color; ctx.lineWidth = 2.2; ctx.lineCap = 'round';
  ctx.beginPath();
  ctx.moveTo(3, 9); ctx.lineTo(13, 9); ctx.arc(13, 6.5, 2.5, Math.PI / 2, -Math.PI, true);
  ctx.moveTo(3, 12.5); ctx.lineTo(16.5, 12.5); ctx.arc(16.5, 10, 2.5, Math.PI / 2, -Math.PI, true);
  ctx.moveTo(3, 16); ctx.lineTo(14, 16); ctx.arc(14, 18.5, 2.5, -Math.PI / 2, Math.PI, false);
  ctx.stroke();
  ctx.restore();
}

function distance(x, y, r) {
  const dx = Math.max(r.l - x, 0, x - r.r), dy = Math.max(r.t - y, 0, y - r.b);
  return Math.hypot(dx, dy);
}

/** One field: w×h px at (0,0). opt: { dark, ms, deg, colour, unit } */
function drawField(ctx, w, h, { dark, ms, deg, colour, unit = 'ms' }) {
  const ink = dark ? '#FFFFFF' : '#000000';
  ctx.fillStyle = dark ? '#000000' : '#FFFFFF';
  ctx.fillRect(0, 0, w, h);

  // Header: icon left, speed right, centred on one line.
  windIcon(ctx, ink);
  ctx.font = HEADER_FONT; ctx.fillStyle = ink; ctx.textAlign = 'right'; ctx.textBaseline = 'alphabetic';
  const caps = ctx.measureText('M').actualBoundingBoxAscent;
  const baseline = ICON_TOP + ICON_PX / 2 + caps / 2;
  const label = speedText(ms, unit);
  ctx.fillText(label, w - TEXT_RIGHT, baseline);

  // Arrow: as big as possible without coming within GAP px of the header.
  const m = ctx.measureText(label.replace(/[0-9]/g, '8'));
  const text = { l: w - TEXT_RIGHT - m.width - GAP, r: w - TEXT_RIGHT + GAP,
                 t: baseline - m.actualBoundingBoxAscent - GAP, b: baseline + m.actualBoundingBoxDescent + GAP };
  const icon = { l: ICON_LEFT - GAP, r: ICON_LEFT + ICON_PX + GAP, t: ICON_TOP - GAP, b: ICON_TOP + ICON_PX + GAP };
  const edge = 4, top = Math.max(text.b, icon.b);
  const rUnder = Math.min((h - edge - top) / 2, w / 2 - edge);
  const rCentre = Math.min(Math.min(w, h) / 2 - edge, distance(w / 2, h / 2, text), distance(w / 2, h / 2, icon));
  const under = rUnder >= rCentre;
  const size = Math.max(rUnder, rCentre) * 100 / ARROW_RADIUS;
  ctx.save();
  ctx.translate(w / 2, under ? (top + h - edge) / 2 : h / 2);
  ctx.rotate(deg * Math.PI / 180);
  ctx.scale(size / 24, size / 24);
  ctx.translate(-12, -12);
  ctx.fillStyle = colour === 'speed' ? speedColor(ms) : colour === 'mono' ? ink : KAROO_GREEN;
  ctx.fill(ARROW);
  ctx.restore();
}

