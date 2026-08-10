# Phone-installed Galaxy Wearable Watch-Face Workflow Design

## 1. Goal
Make the school-work watch-face project usable from a Galaxy phone first: the user installs a phone app, manages timetable/task data there, and then selects the already-installed school-work watch face from Galaxy Wearable.

## 2. Important platform constraint
A normal sideloaded Android phone APK cannot silently install a separate Wear OS APK onto a paired Galaxy Watch. Android/Wear OS package installation requires an explicit watch-side install path or store-managed delivery.

Therefore v1.6 supports two delivery modes:

### Development / direct APK mode
- Install the Android phone app on the Galaxy phone.
- Install the Wear companion/watch-face APKs on the Galaxy Watch once.
- After the watch-face APK is installed, it appears in Galaxy Wearable's watch-face selection UI.
- Day-to-day timetable/task changes are then managed entirely from the phone; no repeated watch APK installation is needed for data changes.

### Production distribution mode
- Publish phone and Wear components through Google Play using the same product/app family.
- The phone can guide the user to the Wear OS store listing or store-managed watch install.
- Once the Wear app/watch face is installed on the watch, Galaxy Wearable can select/customize it.

Galaxy Store publication can be considered later, but is not required for the first integrated release.

## 3. Module architecture
Current modules:
- `:watchface` — resource-only WFF watch face.
- `:companion` — Wear OS app containing complication data sources and local watch-side data cache.

Add:
- `:phone` — Android phone application for HWPX/JSON import, timetable/task editing, and phone-to-watch synchronization.

The watch-face module remains resource-only and references complication providers in the Wear companion module.

## 4. Phone app responsibilities
The phone app is the user's main control surface.

Functions:
1. Import `school_work_data.json` exported by Windows.
2. Later support direct HWPX import if desired, reusing the same normalized JSON contract.
3. Show teacher, source file, last updated time, classes, and tasks.
4. Allow timetable/task review and basic editing.
5. Save the latest valid dataset locally.
6. Send the dataset to the paired Wear OS device using the Wearable Data Layer.
7. Show synchronization state: `워치 연결됨`, `전송 완료`, `워치 미연결`, or error.
8. Provide a `Galaxy Wearable에서 워치페이스 설정` help/action screen explaining that the watch face must already be installed on the watch.

## 5. Data flow

```text
Windows HWPX importer
        ↓
school_work_data.json
        ↓
Galaxy phone :phone app
  - validate schemaVersion 1
  - store last valid data
  - edit/review data
        ↓ Wear OS Data Layer
Galaxy Watch :companion
  - receive JSON payload
  - validate before replacing previous cache
  - persist latest valid JSON
        ↓
SchoolDataRepository
        ↓
NextClassComplicationService / PriorityTaskComplicationService
        ↓
:watchface WFF complication slots
        ↓
Galaxy Wearable watch-face selection
```

## 6. Synchronization protocol
Use a small versioned message contract over Wear OS Data Layer.

Path:
- `/school_work/data`

Payload fields:
- `schemaVersion`: 1
- `json`: full UTF-8 school-work JSON
- `sentAt`: epoch milliseconds

Rules:
- Phone sends after successful import/save and on explicit `워치로 전송`.
- Watch validates JSON before saving.
- Invalid/newer schemas are rejected and the previous valid cache remains.
- Last-write-wins is acceptable for v1.6 because the phone is the primary editor.

## 7. Watch-side data storage
Create `SchoolWorkStore` in the Wear companion app.

Behavior:
- `saveValidJson(context, text)` decodes first, then writes only valid JSON.
- `load(context)` returns the last valid `SchoolWorkData` or null.
- Complication services load the store and construct `SchoolDataRepository(data.classes, data.tasks)`.
- Missing data returns visible fallback strings instead of an empty slot.

## 8. Watch-face visual correction
Based on the real-watch photo:
- Keep large clock/date layout.
- Ensure fallback text remains visible when no complication provider data is available.
- Do not let an EMPTY complication visually erase the fallback label/text.
- Increase central class/task readability and move bottom actions farther inside the circular safe area.
- Bottom labels remain `시간표`, `업무`, `알람` unless phone-app launch behavior requires changing one target.

## 9. Galaxy Wearable behavior
Expected user flow after one-time watch installation:
1. Open Galaxy Wearable on the phone.
2. Open `워치 페이스`.
3. Locate the installed school-work watch face under downloaded/installed faces.
4. Select it.
5. The phone app updates timetable/task content independently through Data Layer; changing data does not require choosing the face again.

The project must not claim that sideloading only the phone APK installs the watch face. That is not supported by standard Android app permissions.

## 10. Testing
### Phone
- Valid/invalid JSON import.
- Previous valid data retained on import failure.
- Data Layer payload creation.
- Sync status mapping.

### Wear companion
- Data Layer message reception.
- Schema rejection.
- Safe persistence.
- Repository loads received data.
- Current/next class and task complication formatting.

### Watch face
- WFF structure validation.
- Fallback texts not hidden by EMPTY complications.
- APK build succeeds.

### End-to-end manual acceptance
- Install phone APK.
- Install watch APKs once.
- Confirm face is visible/selectable in Galaxy Wearable.
- Import Windows JSON on phone.
- Send to watch.
- Confirm next class/task changes on watch without reinstalling APKs.

## 11. Packaging
Debug delivery:
- `SchoolWorkPhone-debug.apk`
- `SchoolWorkCompanion-debug.apk`
- `SchoolWorkWatchFace-debug.apk`
- one ZIP containing all three and a short installation-order text file.

Recommended installation order for direct APK testing:
1. Phone APK on Galaxy phone.
2. Wear companion APK on Galaxy Watch.
3. Watch-face APK on Galaxy Watch.
4. Select the face in Galaxy Wearable.
5. Import/send school-work data from phone.

## 12. Success criteria
- The phone becomes the normal daily management interface.
- After one-time Wear installation, the school-work face can be selected in Galaxy Wearable.
- Windows JSON can be imported on the phone and sent to the watch.
- Watch complications use persisted phone-supplied data rather than empty repositories.
- Missing/corrupt data never blanks the central watch-face information area.
