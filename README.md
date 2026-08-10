# 학교 업무용 Galaxy Watch 워치페이스

Wear OS 4+용 Watch Face Format v1 기반 프로젝트입니다.

## 모듈
- `watchface`: 리소스 전용 WFF 워치페이스 (`android:hasCode=false`)
- `companion`: 다음 수업/오늘 할 일 SHORT_TEXT 컴플리케이션 공급자

## 화면
시간/초, 날짜·요일, 걸음 수, 배터리, 심박수, 날씨 슬롯, 다음 수업, 오늘 할 일, 캘린더/업무/알람 바로가기를 표시합니다. AOD에서는 시간·날짜·배터리만 남깁니다.

v1.2부터 걸음 수와 배터리는 WFF의 `[STEP_COUNT]`, `[BATTERY_PERCENT]` 데이터 소스를 직접 사용하므로 별도 컴플리케이션 공급자 선택이 필요 없습니다. 다음 수업/오늘 할 일은 companion 공급자를 사용합니다.

## 데이터 수정
`companion/src/main/java/com/kang77556/schoolwatch/SchoolDataRepository.kt`의 `defaultClasses()`와 `defaultTasks()`를 실제 일정으로 바꾸세요.

## 설치
1. `companion` APK와 `watchface` APK를 모두 Galaxy Watch 자체에 설치합니다.
2. 기존 버전 위에 업데이트 설치할 수 있습니다.
3. 워치페이스를 다시 적용합니다.
4. 다음 수업/오늘 할 일이 연결되지 않으면 워치페이스 편집 화면에서 해당 슬롯에 `학교 업무` 공급자를 선택합니다.

## Android Studio
1. Android Studio에서 프로젝트를 엽니다.
2. Android SDK 36을 설치합니다.
3. 먼저 `companion`을 Galaxy Watch/Wear OS emulator에 설치합니다.
4. 다음으로 `watchface`를 설치합니다.

## 빌드
`./gradlew :companion:assembleDebug :watchface:assembleDebug`

> WFF 번들은 코드와 함께 배포할 수 없으므로 워치페이스와 동반 앱은 별도 APK/AAB입니다.

v1.1은 실제 원형 워치 사진을 기준으로 레이아웃을 개선했고, v1.2는 배터리/걸음 직접 데이터 연결과 학교 슬롯 재초기화를 적용했습니다.
