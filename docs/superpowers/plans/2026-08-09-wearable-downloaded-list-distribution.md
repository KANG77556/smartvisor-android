# Galaxy Wearable Downloaded List Distribution Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 학교 업무 워치의 폰/워치 데이터 연결을 완성하고 정식 Wear OS 배포 구조와 폰의 `워치에 적용` 흐름을 추가한다.

**Architecture:** `:phone`이 JSON 가져오기/저장/Data Layer 전송과 적용 진입점을 담당하고, `:companion`이 수신 JSON을 영속화하여 complication에 공급한다. `:watchface`는 Watch Face Format을 유지하며 release/AAB 배포 산출물로 패키징한다.

**Tech Stack:** Android Kotlin, Wear OS, Watch Face Format, Google Play services Wearable Data Layer, Gradle 9.1, GitHub Actions

## Global Constraints
- Data Layer 통신 phone/companion은 동일 applicationId와 서명 계열을 사용한다.
- release signing secret은 GitHub 저장소에 커밋하지 않는다.
- Galaxy Wearable `다운로드됨` 섹션은 앱 코드로 강제 등록하지 않고 정상 스토어 설치 흐름으로 노출시킨다.
- 구현은 TDD로 진행하고 CI에서 phone/companion 테스트 및 APK/AAB 산출물을 검증한다.

---

## File Structure
- `phone/src/main/.../MainActivity.kt`: 가져오기/전송/적용 UI 흐름
- `phone/src/main/.../WearSync.kt`: Data Layer 전송
- `companion/src/main/.../SchoolWorkStore.kt`: 수신 JSON 저장/복원
- `companion/src/main/.../StoredSchoolRepository.kt`: 저장 JSON을 complication repository로 변환
- `companion/src/main/.../SchoolComplications.kt`: 실제 저장 데이터 표시
- `watchface/src/main/AndroidManifest.xml`: WFF 배포 메타데이터
- `.github/workflows/android.yml`: 테스트, APK 및 AAB 빌드

## Task 1: 저장 JSON → complication 연결
- [ ] 실패 테스트로 저장 JSON에서 다음 수업/우선 업무가 계산되는 계약을 고정한다.
- [ ] 테스트가 RED인지 확인한다.
- [ ] `StoredSchoolRepository` 변환 코드를 구현한다.
- [ ] complication 서비스가 샘플 저장소 대신 저장 JSON 기반 repository를 사용하게 한다.
- [ ] companion 단위 테스트를 GREEN으로 만든다.

## Task 2: 폰 `워치에 적용` 흐름
- [ ] 적용 가능 조건 테스트를 작성한다.
- [ ] 유효 JSON이 있을 때 최신 데이터를 먼저 Data Layer로 전송한다.
- [ ] 폰 화면에 워치페이스 미리보기/데이터 상태/`워치에 적용` 버튼을 구성한다.
- [ ] 직접 적용이 불가능한 기기에서는 워치페이스 선택 안내로 안전하게 fallback한다.
- [ ] phone 단위 테스트를 GREEN으로 만든다.

## Task 3: release 배포 산출물
- [ ] phone/companion/watchface 버전과 release 구성을 점검한다.
- [ ] CI에 release bundle(AAB) 생성 작업을 추가한다.
- [ ] signing secret이 없을 때도 debug CI는 계속 동작하도록 분리한다.
- [ ] APK 3종과 배포용 AAB 산출물을 artifact로 업로드한다.

## Task 4: 최종 검증
- [ ] 전체 단위 테스트를 실행한다.
- [ ] debug APK 3종 빌드를 실행한다.
- [ ] release/AAB 빌드 가능 여부를 검증한다.
- [ ] 실제 Galaxy Watch에서 폰 전송 후 수업/업무 표시를 확인한다.
- [ ] 내부 테스트/스토어 설치 후 Galaxy Wearable의 설치된/다운로드된 워치페이스 목록 노출을 확인한다.
