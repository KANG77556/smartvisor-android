# Windows–Android–Galaxy Watch Integration Design

## 1. Goal
Build one school-work data flow that reuses the uploaded Python HWPX parser on Windows, shares normalized timetable/task data with Android, and displays the current/next class plus key tasks on the Galaxy Watch.

## 2. Existing assets
- Wear OS WFF watch face and Wear companion complication providers already exist.
- The uploaded Windows prototype contains `src/hwpx_reader.py` and tests.
- The prototype already discovers `Contents/section*.xml`, extracts date headings, timetable tables, cell coordinates, spans, and text.

## 3. Recommended architecture
Use Windows as the primary HWPX import/editor, Android as the transport/cache layer, and Galaxy Watch as the glance-only display.

### Windows
- Open `.hwpx` files.
- Reuse and extend the Python `HwpxReader` instead of rewriting parsing logic.
- Support both single-day daily-operation HWPX files and multi-day Monday–Friday timetable HWPX files.
- Extract rows for teacher `강성호` and abbreviated `강성`.
- Normalize duplicate/split period rows into one class record where appropriate.
- Allow the user to review and correct extracted rows before export.
- Allow simple task entry/editing.
- Export one versioned JSON data file.

### Android phone
- Import the same JSON file created by Windows.
- Optionally import an HWPX directly later; this is not required for the first integrated release.
- Store the latest timetable and tasks locally.
- Send or mirror the relevant data to the Wear OS companion layer.

### Galaxy Watch
- Keep WFF watch-face rendering resource-only.
- Use the Wear companion complication provider to calculate and display the next/current class.
- Display short text such as `3교시 회실 10:35`.
- Keep the current large-text v1.4 visual layout.

## 4. Shared data contract
Use UTF-8 JSON with an explicit schema version.

Example structure:

```json
{
  "schemaVersion": 1,
  "generatedAt": "2026-08-08T09:00:00+09:00",
  "source": {
    "type": "hwpx",
    "fileName": "2026.1학기(3.27.).hwpx"
  },
  "teacher": "강성호",
  "classes": [
    {
      "date": "2026-03-24",
      "weekday": "TUESDAY",
      "period": 2,
      "subject": "회실",
      "start": "09:25",
      "end": "10:35",
      "room": "공용실습실"
    }
  ],
  "tasks": [
    {
      "id": "task-1",
      "date": "2026-08-08",
      "title": "출결 확인",
      "priority": 1,
      "completed": false
    }
  ]
}
```

## 5. Parsing rules
- Primary teacher match: exact `강성호`.
- Timetable cell abbreviation match: `강성` when the cell structure clearly represents subject/teacher/room.
- Never match arbitrary text containing `강성` outside timetable cells.
- Preserve the source date heading from the HWPX.
- When a period is split into multiple adjacent time fragments for the same subject/teacher, merge it into one logical period using the earliest start and latest end.
- If extraction is ambiguous, mark the row for Windows review rather than silently guessing.

## 6. Windows UX
First release is intentionally small:
1. `HWPX 열기`
2. Extracted timetable grid
3. Highlight uncertain rows
4. Editable subject, period, start/end time, room
5. Simple today-task list
6. `워치 데이터 저장` button to write the shared JSON

Do not build login, cloud accounts, or a large school-management suite in v1.5.

## 7. Synchronization for v1.5
Use file-based transfer first because it is reliable and debuggable.
- Windows exports `school_work_data.json`.
- User sends/opens the file on the Android phone.
- Android imports and stores it.
- Wear complication services read the stored data.

Future versions may add LAN/cloud automatic synchronization without changing the JSON contract.

## 8. Error handling
- Unsupported/corrupt HWPX: show a clear Windows import error and keep previous data intact.
- No teacher rows found: show zero results and require review.
- Invalid JSON on Android: reject the import and keep previous valid data.
- No classes remaining today: show `수업 일정 없음`.
- Data failures must never stop time/date/battery rendering on the watch face.

## 9. Testing
### Python/Windows
- Existing section/date/table/cell parser tests remain.
- Add single-day and five-day fixture cases.
- Add teacher matching and split-period merge tests.
- Add JSON schema/export tests.

### Android/Wear
- Test JSON decoding and schema-version rejection.
- Test before-first-class, during-class, between-class, after-last-class, and weekend selection.
- Test task priority ordering.
- Run Android structure verification, JUnit tests, and APK builds in GitHub Actions.

## 10. Delivery units
1. Shared JSON schema and parser normalization library.
2. Windows desktop importer/editor built around the existing Python parser.
3. Android JSON import/cache.
4. Wear complication integration.
5. APK and Windows executable packaging.

## 11. Success criteria
- A school HWPX can be opened on Windows and the teacher's timetable can be reviewed without editing source code.
- Windows exports one JSON file understood by Android.
- Android accepts the file and retains the previous valid data if import fails.
- Galaxy Watch shows the correct current/next class and today's task from the imported data.
- Existing large, readable watch-face layout remains intact.
