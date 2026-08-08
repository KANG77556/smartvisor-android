# Windows–Android–Galaxy Watch Integration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a Windows HWPX importer/editor that exports one shared JSON file, add Android import/cache support for that JSON, and make the Wear OS complication services display the imported current/next class and tasks.

**Architecture:** Reuse the uploaded Python HWPX parser as the Windows-side source parser. Normalize timetable rows into a versioned UTF-8 JSON contract. Android imports and persists the JSON locally, and the existing Wear complication services read the same normalized model while the WFF watch-face layout remains unchanged.

**Tech Stack:** Python 3, HWPX ZIP/XML, Tkinter, JSON, Kotlin, Android SDK 33+, Wear OS complication APIs, JUnit, GitHub Actions.

## Global Constraints
- Wear OS 4 / API 33 minimum remains unchanged.
- Watch-face module remains resource-only WFF 1.
- No server, login, or cloud dependency in v1.5.
- Windows is the primary HWPX import/editor for the first integrated release.
- Android must retain the last valid imported data when a new import fails.
- Existing v1.4 large-text watch-face layout remains unchanged.
- Time/date/battery rendering must remain independent of timetable data failures.

---

### Task 1: Bring the Windows HWPX prototype into the repository

**Files:**
- Create: `windows/README.md`
- Create: `windows/src/hwpx_reader.py`
- Create: `windows/tests/test_hwpx_reader.py`

**Interfaces:**
- Produces: `HwpxReader.read(path: str) -> ParsedDocument`
- Produces parsed date headings, timetable tables, cell coordinates/spans/text.

- [ ] **Step 1: Add the prototype reader and its existing parser tests**

Copy the uploaded prototype behavior into repository-owned files without changing parsing behavior.

- [ ] **Step 2: Run the existing Python tests**

Run: `python -m unittest discover -s windows/tests -v`
Expected: all imported prototype tests pass.

- [ ] **Step 3: Commit**

Commit message: `feat: add Windows HWPX parser prototype`

### Task 2: Normalize teacher timetable rows and merge split periods

**Files:**
- Create: `windows/src/timetable_normalizer.py`
- Create: `windows/tests/test_timetable_normalizer.py`

**Interfaces:**
- Consumes: parsed tables/cells from `HwpxReader`.
- Produces: `ClassRecord(date, weekday, period, subject, start, end, room, confidence)`.
- Produces: `extract_teacher_classes(parsed, full_name="강성호", short_name="강성") -> list[ClassRecord]`.

- [ ] **Step 1: Write failing tests**

Test cases must cover:
- exact `강성호` teacher match,
- timetable abbreviation `강성`,
- rejecting unrelated text containing `강성`,
- extracting `2교시 회실`, `3교시 회실`, `5교시 회실`, `7교시 스생1` from the provided daily-operation fixture,
- merging adjacent fragments for the same period/subject/teacher using earliest start and latest end,
- preserving room when available,
- marking ambiguous records with `confidence="review"`.

- [ ] **Step 2: Run tests and verify they fail**

Run: `python -m unittest windows.tests.test_timetable_normalizer -v`
Expected: FAIL because the normalizer does not exist yet.

- [ ] **Step 3: Implement the minimal normalizer**

Use table structure, period/time labels, and neighboring subject/teacher/room cells. Do not use unrestricted substring matching across the document.

- [ ] **Step 4: Run tests**

Run: `python -m unittest windows.tests.test_timetable_normalizer -v`
Expected: PASS.

- [ ] **Step 5: Commit**

Commit message: `feat: normalize teacher timetable rows`

### Task 3: Define and export the shared JSON contract

**Files:**
- Create: `shared/schema/school_work_data.schema.json`
- Create: `windows/src/school_work_export.py`
- Create: `windows/tests/test_school_work_export.py`

**Interfaces:**
- Produces: `export_school_work_data(path, source_file, teacher, classes, tasks)`.
- JSON root fields: `schemaVersion`, `generatedAt`, `source`, `teacher`, `classes`, `tasks`.

- [ ] **Step 1: Write failing export tests**

Assert UTF-8 Korean round-trip, ISO date/time formatting, `schemaVersion == 1`, stable class/task field names, and deterministic class sorting.

- [ ] **Step 2: Run tests and verify failure**

Run: `python -m unittest windows.tests.test_school_work_export -v`
Expected: FAIL because exporter/schema do not exist.

- [ ] **Step 3: Implement schema and exporter**

Reject records missing date, period, subject, start, or end. Tasks require `id`, `date`, `title`, `priority`, `completed`.

- [ ] **Step 4: Run tests**

Run: `python -m unittest windows.tests.test_school_work_export -v`
Expected: PASS.

- [ ] **Step 5: Commit**

Commit message: `feat: add shared school-work JSON contract`

### Task 4: Build the Windows desktop importer/editor

**Files:**
- Create: `windows/src/app.py`
- Create: `windows/tests/test_app_model.py`
- Create: `windows/build_windows.bat`

**Interfaces:**
- UI actions: `HWPX 열기`, timetable grid edit, task add/delete, `워치 데이터 저장`.
- UI model exposes normalized classes/tasks independent of Tkinter widgets for testing.

- [ ] **Step 1: Write failing model tests**

Test loading parsed records into editable rows, editing subject/time/room, adding/removing tasks, and exporting current state.

- [ ] **Step 2: Run tests and verify failure**

Run: `python -m unittest windows.tests.test_app_model -v`
Expected: FAIL because app model does not exist.

- [ ] **Step 3: Implement app model and Tkinter UI**

Use standard-library Tkinter. Do not add cloud/login features. Highlight `confidence="review"` rows. Show import errors without discarding the previous valid model.

- [ ] **Step 4: Add Windows packaging script**

`build_windows.bat` installs/uses PyInstaller and creates one executable from `windows/src/app.py`.

- [ ] **Step 5: Run Python tests**

Run: `python -m unittest discover -s windows/tests -v`
Expected: PASS.

- [ ] **Step 6: Commit**

Commit message: `feat: add Windows timetable importer editor`

### Task 5: Add Android JSON decode and persistent cache

**Files:**
- Create: `companion/src/main/java/com/kang77556/schoolwatch/SchoolWorkJson.kt`
- Create: `companion/src/main/java/com/kang77556/schoolwatch/SchoolWorkStore.kt`
- Modify: `companion/src/main/java/com/kang77556/schoolwatch/SchoolData.kt`
- Modify: `companion/src/main/java/com/kang77556/schoolwatch/SchoolDataRepository.kt`
- Test: `companion/src/test/kotlin/com/kang77556/schoolwatch/SchoolWorkJsonTest.kt`
- Test: `companion/src/test/kotlin/com/kang77556/schoolwatch/SchoolDataRepositoryTest.kt`

**Interfaces:**
- `SchoolWorkJson.decode(text: String): SchoolWorkData`.
- `SchoolWorkStore.saveValid(context, data)` and `load(context): SchoolWorkData?`.
- `SchoolDataRepository(data: SchoolWorkData)` selects current/next class by date/time.

- [ ] **Step 1: Write failing JSON tests**

Cover valid schema v1, unknown schema rejection, malformed JSON rejection, Korean text, and required fields.

- [ ] **Step 2: Write failing repository tests**

Cover before-first-class, during-class, between-classes, after-final-class, date filtering, weekend/no-data, and task priority ordering.

- [ ] **Step 3: Run tests and verify failure**

Run: `gradle :companion:testDebugUnitTest --stacktrace`
Expected: FAIL because JSON/store/model changes do not exist.

- [ ] **Step 4: Implement decoder, store, and repository changes**

Use Android platform JSON classes; avoid adding a serialization dependency. Persist only after successful decode/validation.

- [ ] **Step 5: Run tests**

Run: `gradle :companion:testDebugUnitTest --stacktrace`
Expected: PASS.

- [ ] **Step 6: Commit**

Commit message: `feat: import and cache school-work JSON on Android`

### Task 6: Add Android file-import UI

**Files:**
- Modify: `companion/src/main/java/com/kang77556/schoolwatch/companion/MainActivity.kt`
- Modify: `companion/src/main/AndroidManifest.xml` only if required.

**Interfaces:**
- Android Storage Access Framework opens `school_work_data.json`.
- Successful import replaces stored valid data; failed import leaves previous valid data intact.

- [ ] **Step 1: Add import-result unit-testable helper**

Create a helper that accepts JSON text and returns success/error while calling store only on success.

- [ ] **Step 2: Implement simple Wear/Android activity UI**

Show current data source/updated time and an `JSON 가져오기` button. Use `ACTION_OPEN_DOCUMENT` with `application/json` and `text/plain` fallback.

- [ ] **Step 3: Run unit tests and build companion APK**

Run: `gradle :companion:testDebugUnitTest :companion:assembleDebug --stacktrace`
Expected: PASS and APK generated.

- [ ] **Step 4: Commit**

Commit message: `feat: add JSON import screen`

### Task 7: Connect Wear complications to imported data

**Files:**
- Modify: `companion/src/main/java/com/kang77556/schoolwatch/companion/SchoolComplications.kt`
- Test: `companion/src/test/kotlin/com/kang77556/schoolwatch/SchoolTextTest.kt`

**Interfaces:**
- Next-class complication reads `SchoolWorkStore` and shows current class when now is within class time; otherwise next class.
- Text format: `N교시 과목 HH:mm` within slot constraints.
- Task complication shows top one or two incomplete tasks for today.

- [ ] **Step 1: Write failing formatting/selection tests**

Cover current class, next class, no remaining class, and long-subject truncation strategy.

- [ ] **Step 2: Run tests and verify failure**

Run: `gradle :companion:testDebugUnitTest --stacktrace`
Expected: FAIL until complication-facing functions are updated.

- [ ] **Step 3: Implement complication integration**

Load stored data per request. If no stored data exists, return `수업 일정 없음` / `할 일 없음` rather than crashing.

- [ ] **Step 4: Run tests**

Run: `gradle :companion:testDebugUnitTest --stacktrace`
Expected: PASS.

- [ ] **Step 5: Commit**

Commit message: `feat: show imported timetable on Wear complications`

### Task 8: CI, packaging, and end-to-end verification

**Files:**
- Modify: `.github/workflows/android.yml`
- Create: `.github/workflows/windows.yml`
- Modify: `tests/verify_project.py` if necessary.

**Interfaces:**
- Android CI artifact contains watch-face and companion APKs.
- Windows CI artifact contains the desktop executable/ZIP.

- [ ] **Step 1: Extend Android CI**

Run structure verification, companion unit tests, watch-face build, companion build, and artifact upload.

- [ ] **Step 2: Add Windows CI**

On `windows-latest`, run Python tests and build the PyInstaller executable, then upload it as an artifact.

- [ ] **Step 3: Verify Android CI**

Expected: all checks green and APK artifact uploaded.

- [ ] **Step 4: Verify Windows CI**

Expected: Python tests green and Windows artifact uploaded.

- [ ] **Step 5: Manual end-to-end acceptance**

On Windows: open the provided HWPX, confirm extracted teacher rows, export `school_work_data.json`.
On Android: import that JSON and confirm successful status.
On Galaxy Watch: confirm current/next class and tasks appear while time/date/battery remain functional.

- [ ] **Step 6: Commit**

Commit message: `ci: verify Windows Android Watch integration`
