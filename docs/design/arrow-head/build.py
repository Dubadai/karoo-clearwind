"""Inline the Karoo frame, the device screenshot and karoo-device.js into mockup.html and preview.html.

Mockup pages are opened as static snapshots that can't load files beside them, so everything is
embedded. Run: python3 build.py  (from this folder)
"""
import base64, pathlib
here = pathlib.Path(__file__).parent
assets = here.parents[3] / 'design-assets'          # Karoo/design-assets
def data_url(p): return 'data:image/png;base64,' + base64.b64encode(p.read_bytes()).decode()
for name in ('mockup', 'preview'):
    html = (here / f'{name}.src.html').read_text()
    html = html.replace('/*__KAROO_DEVICE_JS__*/', (assets / 'karoo-device.js').read_text())
    html = html.replace('__FRAME__', data_url(assets / 'karoo-3-frame.png'))
    html = html.replace('__SCREEN__', data_url(here.parent / 'field' / 'device' / '05-arrow-under-header-light.png'))
    html = html.replace('__SCREEN_DARK__', data_url(here / 'device' / '04-a2-mono-dark.png'))
    (here / f'{name}.html').write_text(html)
    print('wrote', here / f'{name}.html')
