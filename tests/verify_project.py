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
for token in ['DigitalClock', 'hourFormat="SYNC_TO_DEVICE"', '[STEP_COUNT]', '[BATTERY_PERCENT]', 'slotId="210"', 'slotId="211"']:
    assert token in text, token
for launch in ['target="CALENDAR"', 'target="ALARM"', 'target="com.kang77556.schoolwatch"']:
    assert launch in text, launch
for provider in [
    'com.kang77556.schoolwatch/com.kang77556.schoolwatch.companion.NextClassComplicationService',
    'com.kang77556.schoolwatch/com.kang77556.schoolwatch.companion.PriorityTaskComplicationService',
]:
    assert provider in text, provider
for label in ['다음 수업', '오늘 할 일', '걸음', '배터리', '시간표', '업무', '알람', '폰에서 동기화 필요']:
    assert label in text, label
for size in ['size="104"', 'size="38"', 'size="33"', 'size="30"', 'size="28"', 'size="27"']:
    assert size in text, size
assert 'mode="AMBIENT"' in text
assert text.count('mode="AMBIENT"') >= 2
cm = comp_manifest.read_text(encoding='utf-8')
assert 'NextClassComplicationService' in cm
assert 'PriorityTaskComplicationService' in cm
print('project structure checks passed')
