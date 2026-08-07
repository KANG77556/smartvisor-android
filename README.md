# 학교 업무용 Galaxy Watch 워치페이스

Wear OS 5+용 Watch Face Format v2 기반 프로젝트입니다.

## 모듈
- `watchface`: 리소스 전용 WFF 워치페이스 (`android:hasCode=false`)
- `companion`: 다음 수업/오늘 할 일 SHORT_TEXT 컴플리케이션 공급자

## 화면
시간/초, 날짜·요일, 걸음 수, 배터리, 심박수, 날씨 슬롯, 다음 수업, 오늘 할 일, 캘린더/업무/알람 바로가기를 표시합니다. AOD에서는 시간·날짜·배터리만 남깁니다.

## 데이터 수정
`companion/src/main/java/com/kang77556/schoolwatch/SchoolDataRepository.kt`의 `defaultClasses()`와 `defaultTasks()`를 실제 일정으로 바꾸세요.

## Android Studio
1. Android Studio Otter 3 Feature Drop 이상에서 프로젝트를 엽니다.
2. Android SDK 36을 설치합니다.
3. 먼저 `companion`을 Galaxy Watch/Wear OS emulator에 설치합니다.
4. 다음으로 `watchface`를 실행/설치합니다.
5. 워치페이스 편집기에서 날씨 슬롯은 원하는 날씨 공급자를 선택할 수 있습니다.

## 빌드
Android SDK/Gradle이 준비된 환경에서 `./gradlew :companion:assembleDebug :watchface:assembleDebug`를 실행합니다.

설치 예: `adb install -r companion/build/outputs/apk/debug/companion-debug.apk` 후 `adb install -r watchface/build/outputs/apk/debug/watchface-debug.apk`.

> WFF 번들은 코드와 함께 배포할 수 없으므로 워치페이스와 동반 앱은 별도 APK/AAB입니다.
