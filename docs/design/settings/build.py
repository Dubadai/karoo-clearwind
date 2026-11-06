"""Inline the Karoo frame, reference screens, karoo-device.js and field.js into mockup.html.

Mockup pages are opened as static snapshots that can't load files beside them, so everything is
embedded. Run: python3 build.py  (from this folder)
"""
import base64, pathlib
here = pathlib.Path(__file__).parent
assets = here.parents[3] / 'design-assets'          # Karoo/design-assets
def data_url(p): return 'data:image/png;base64,' + base64.b64encode(p.read_bytes()).decode()
html = (here / 'mockup.src.html').read_text()
html = html.replace('/*__KAROO_DEVICE_JS__*/', (assets / 'karoo-device.js').read_text())
html = html.replace('/*__FIELD_JS__*/', (here.parent / 'field.js').read_text())
html = html.replace('__FRAME__', data_url(assets / 'karoo-3-frame.png'))
html = html.replace('__BASE_LIGHT__', data_url(here / 'reference' / 'interval-settings-light.png'))
html = html.replace('__BASE_DARK__', data_url(here / 'reference' / 'interval-settings-dark.png'))
(here / 'mockup.html').write_text(html)
print('wrote', here / 'mockup.html')
