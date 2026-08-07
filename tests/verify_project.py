from pathlib import Path
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
def must(path):
    p = ROOT / path
    assert p.exists(), f'missing: {path}'
    return p
manifest = must('watchface/src/main/AndroidManifest.xml')
wf = must('watchface/src/main/res/raw/watchface.xml')
comp_manifest = must('companion/src/main/AndroidManifest.xml')
mroot = ET.parse(manifest).getroot()
app = mroot.find('application')
android = '{http://schemas.android.com/apk/res/android}'
assert app.get(android+'hasCode') == 'false'
root = ET.parse(wf).getroot()
assert root.tag == 'WatchFace'
assert root.get('clipShape') == 'CIRCLE'
text = wf.read_text(encoding='utf-8')
for token in ['DigitalClock', 'hourFormat="SYNC_TO_DEVICE"', 'slotId="100"', 'slotId="101"', 'slotId="102"', 'slotId="103"', 'slotId="200"', 'slotId="201"']:
    assert token in text, token
for launch in ['target="CALENDAR"', 'target="ALARM"', 'target="com.kang77556.schoolwatch.companion"']:
    assert launch in text, launch
assert 'mode="AMBIENT"' in text
assert text.count('mode="AMBIENT"') >= 2
cm = comp_manifest.read_text(encoding='utf-8')
assert 'NextClassComplicationService' in cm
assert 'PriorityTaskComplicationService' in cm
print('project structure checks passed')
