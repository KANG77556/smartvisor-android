from pathlib import Path
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
def must(path):
    p = ROOT / path
    assert p.exists(), f'missing: {path}'
    return p
manifest = must('watchface/src/main/AndroidManifest.xml')
wf = must('watchface/src/main/res/raw/watchface.xml')
build = must('watchface/build.gradle.kts')
comp_build = must('companion/build.gradle.kts')
comp_manifest = must('companion/src/main/AndroidManifest.xml')
mroot = ET.parse(manifest).getroot()
app = mroot.find('application')
android = '{http://schemas.android.com/apk/res/android}'
assert app.get(android+'hasCode') == 'false'
props = {p.get(android+'name'): p.get(android+'value') for p in app.findall('property')}
assert props.get('com.google.wear.watchface.format.version') == '1'
assert 'minSdk = 33' in build.read_text(encoding='utf-8')
assert 'minSdk = 33' in comp_build.read_text(encoding='utf-8')
root = ET.parse(wf).getroot()
assert root.tag == 'WatchFace'
assert root.get('clipShape') == 'CIRCLE'
text = wf.read_text(encoding='utf-8')
for token in ['DigitalClock', 'hourFormat="SYNC_TO_DEVICE"', '[STEP_COUNT]', '[BATTERY_PERCENT]', 'slotId="102"', 'slotId="103"', 'slotId="210"', 'slotId="211"']:
    assert token in text, token
assert 'slotId="100"' not in text
assert 'slotId="101"' not in text
for launch in ['target="CALENDAR"', 'target="ALARM"', 'target="com.kang77556.schoolwatch.companion"']:
    assert launch in text, launch
for label in ['다음 수업', '오늘 할 일', '걸음', '배터리']:
    assert label in text, label
assert 'primaryProvider="com.kang77556.schoolwatch.companion/com.kang77556.schoolwatch.companion.NextClassComplicationService"' in text
assert 'primaryProvider="com.kang77556.schoolwatch.companion/com.kang77556.schoolwatch.companion.PriorityTaskComplicationService"' in text
assert 'size="92"' in text
assert 'size="27"' in text
assert 'size="22"' in text
assert 'size="19"' in text
assert 'mode="AMBIENT"' in text
assert text.count('mode="AMBIENT"') >= 2
cm = comp_manifest.read_text(encoding='utf-8')
assert 'NextClassComplicationService' in cm
assert 'PriorityTaskComplicationService' in cm
print('project structure checks passed')
