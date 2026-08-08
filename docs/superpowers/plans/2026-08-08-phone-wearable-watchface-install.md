# Phone + Wearable Watch Face Installation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add an Android phone companion that imports school-work JSON, sends it to the paired Galaxy Watch, and leaves the installed School Work watch face selectable from Galaxy Wearable.

**Architecture:** Add a `:phone` Android application module beside the existing resource-only `:watchface` and Wear `:companion` modules. The phone validates/stores the shared JSON and publishes it over Wear OS Data Layer; the Wear companion receives and persists the same JSON for complication providers. The watch-face package remains a normal Wear OS watch face so Galaxy Wearable can list it once installed on the watch.

**Tech Stack:** Kotlin, Android SDK 33+, Wear OS, Google Play services Wearable Data Layer, Storage Access Framework, WFF 1, JUnit, GitHub Actions.

## Global Constraints
- Wear OS 4 / API 33 minimum.
- Existing WFF watch face stays resource-only.
- No cloud server or account system.
- Shared JSON schema version remains 1.
- Failed phone imports must not replace the last valid data.
- Failed watch transfers must not break time/date/battery rendering.
- Debug sideload cannot silently install the watch APK from the phone; watch package installation is a separate first-time step.

---

### Task 1: Add phone application module
**Files:** `settings.gradle.kts`, `phone/build.gradle.kts`, `phone/src/main/AndroidManifest.xml`, `phone/src/main/java/com/kang77556/schoolwatch/phone/MainActivity.kt`
- [ ] Add a failing project-structure verification for `:phone`.
- [ ] Register `:phone` and create a minimal Android application module.
- [ ] Build `:phone:assembleDebug`.
- [ ] Commit `feat: add school work phone companion`.

### Task 2: Share JSON validation behavior
**Files:** phone JSON/import classes and tests; keep schema semantics aligned with companion `SchoolWorkJson`.
- [ ] Write tests for valid schema v1, Korean text, malformed JSON, unsupported schema.
- [ ] Implement validation and last-valid local storage.
- [ ] Run phone unit tests.
- [ ] Commit `feat: import school work JSON on phone`.

### Task 3: Add phone file picker UI
**Files:** phone `MainActivity.kt` and import helper.
- [ ] Write import-result helper tests.
- [ ] Add `JSON 가져오기` via Storage Access Framework.
- [ ] Show imported teacher, class count, task count, and last update.
- [ ] Build phone APK.
- [ ] Commit `feat: add phone JSON import screen`.

### Task 4: Send JSON to paired watch
**Files:** phone `WearSync.kt`, tests for payload/path constants.
- [ ] Define Data Layer path `/school-work/data-v1` and payload key `json`.
- [ ] Add Play services wearable dependency.
- [ ] Publish validated JSON using DataClient after import and on manual `워치로 보내기`.
- [ ] Surface success/failure state in phone UI.
- [ ] Commit `feat: sync school work data to watch`.

### Task 5: Receive and persist data on Wear companion
**Files:** companion `SchoolWorkStore.kt`, `SchoolWorkDataListenerService.kt`, manifest, tests.
- [ ] Write tests for receive/validate/keep-last-valid behavior.
- [ ] Implement SharedPreferences-backed raw JSON storage.
- [ ] Add WearableListenerService for `/school-work/data-v1`.
- [ ] Validate before replacing stored data.
- [ ] Commit `feat: receive phone school data on watch`.

### Task 6: Feed stored data to complications
**Files:** `SchoolComplications.kt`, repository/store integration tests.
- [ ] Write failing tests proving imported stored data drives current/next class and tasks.
- [ ] Load stored JSON in each complication request.
- [ ] Fall back to visible `수업 일정 없음` and `할 일 없음` text.
- [ ] Keep time/date/battery independent.
- [ ] Commit `feat: show synced school data in complications`.

### Task 7: Improve watch-face readability
**Files:** `watchface/src/main/res/raw/watchface.xml`, structure verification.
- [ ] Add static assertions for safe complication bounds and minimum text sizing.
- [ ] Increase central complication area and lower-label readability without clipping circular displays.
- [ ] Ensure empty complication slots do not erase useful fallback labels.
- [ ] Build watchface APK.
- [ ] Commit `fix: improve school watch face readability`.

### Task 8: Build and verify three APKs
**Files:** `.github/workflows/android.yml` and verification scripts.
- [ ] Run structure verification.
- [ ] Run companion and phone JUnit tests.
- [ ] Build `:phone:assembleDebug`, `:companion:assembleDebug`, `:watchface:assembleDebug`.
- [ ] Upload one artifact containing all three APKs.
- [ ] Verify GitHub Actions is green.
- [ ] Commit `ci: package phone and watch apps`.

### Acceptance
1. Install phone APK on Galaxy phone.
2. Install watchface and Wear companion APKs once on Galaxy Watch for debug distribution.
3. Confirm `학교 업무` appears in Galaxy Wearable's installed watch-face list and can be selected.
4. Import `school_work_data.json` on phone.
5. Tap `워치로 보내기` and confirm the watch complication updates with current/next class and today's task.
6. Reboot/reopen and confirm the last valid data remains available.
