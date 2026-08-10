# HWPX Timetable Integration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace hardcoded sample timetable data with the user's uploaded 2026 first-semester HWPX timetable data and show the current/next class automatically on the Galaxy Watch.

**Architecture:** Parse the uploaded HWPX as ZIP/XML outside the watch runtime, extract rows containing teacher 강성호/강성, normalize them into weekday-period-time-subject-room records, and commit the resulting static timetable into the companion app. The existing complication service will read this normalized timetable and calculate the next class from day/time without changing the resource-only WFF watch-face module.

**Tech Stack:** HWPX ZIP/XML, Kotlin, java.time, Wear OS complication data source, GitHub Actions.

## Global Constraints
- Wear OS 4 / API 33 minimum remains unchanged.
- Watch-face module remains resource-only WFF 1.
- No server, login, or background cloud sync in v1.5.
- Only the user's own timetable rows are imported.
- Core clock rendering must remain independent of timetable-data failures.

---

### Task 1: Extract and normalize the HWPX timetable

**Files:**
- Create: `docs/data/2026-1-hwpx-extraction.md`
- Create: `companion/src/main/java/com/kang77556/schoolwatch/ImportedTimetable.kt`

**Interfaces:**
- Produces: `fun importedClasses(): List<SchoolClassSchedule>`

- [ ] Inspect HWPX XML tables and identify weekday columns, period/time rows, and cells containing `강성호` or abbreviated `강성`.
- [ ] Record every matched class with weekday, period, subject, start/end time, and room when present.
- [ ] Add a failing test that asserts known extracted rows are present and sorted.
- [ ] Implement immutable imported timetable data.
- [ ] Run unit tests and commit.

### Task 2: Make timetable logic weekday-aware

**Files:**
- Modify: `companion/src/main/java/com/kang77556/schoolwatch/SchoolData.kt`
- Modify: `companion/src/main/java/com/kang77556/schoolwatch/SchoolDataRepository.kt`
- Test: existing/new repository unit tests.

**Interfaces:**
- Produces: `nextClass(now: LocalDateTime): SchoolClassSchedule?`

- [ ] Add failing tests for weekday filtering, before-first-class, between-classes, after-final-class, and weekend behavior.
- [ ] Replace hardcoded sample classes with imported timetable data.
- [ ] Keep task data independent from timetable import.
- [ ] Run unit tests and commit.

### Task 3: Update complication text

**Files:**
- Modify: `companion/src/main/java/com/kang77556/schoolwatch/companion/SchoolComplications.kt`

**Interfaces:**
- Consumes: weekday-aware `SchoolDataRepository.nextClass`.
- Produces: short text such as `3교시 회계실무 10:35`.

- [ ] Add failing text-format tests.
- [ ] Include period + subject + start time while keeping text short enough for the enlarged v1.4 slot.
- [ ] Preserve `수업 일정 없음` for no remaining class.
- [ ] Run tests and commit.

### Task 4: Build and package v1.5

**Files:**
- Modify: project verification tests only if needed for v1.5 data assertions.

- [ ] Run structure verification.
- [ ] Run all JUnit tests.
- [ ] Build `:watchface:assembleDebug` and `:companion:assembleDebug` in GitHub Actions.
- [ ] Download the successful APK artifact.
- [ ] Provide updated companion/watch-face APKs and installation order.
