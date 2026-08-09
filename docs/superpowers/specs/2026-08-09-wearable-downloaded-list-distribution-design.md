# Galaxy Wearable 다운로드됨 노출 설계

## 목표
`학교 업무 워치`를 단순 사이드로드용 APK가 아니라, 스마트폰 companion 앱과 정식 Wear OS 워치페이스 배포 구조로 정리해 Galaxy Wearable의 설치된/다운로드된 워치페이스 흐름에서 선택할 수 있도록 한다.

## 권장 구조
1. `:phone`은 스마트폰용 관리 앱으로 유지한다.
2. `:watchface`는 Watch Face Format 기반 워치페이스 패키지로 유지한다.
3. `:companion`은 워치에서 Data Layer를 수신하고 complication 데이터를 제공한다.
4. 폰 앱에는 워치페이스 미리보기, 데이터 상태, `워치에 적용` 버튼을 제공한다.
5. 배포 단계에서는 Google Play의 Wear OS 멀티디바이스 배포 구조를 기준으로 서명/버전/패키지 관계를 정리한다.
6. 개발용 debug APK는 최초 설치 테스트용으로 유지하되, Galaxy Wearable `다운로드됨` 노출 보장은 정식 스토어 배포 경로에서 검증한다.

## 사용자 흐름
`학교 업무 워치` 폰 앱 설치 → JSON 가져오기 → 워치로 보내기 → `워치에 적용` → Galaxy Wearable의 설치된/다운로드된 워치페이스 목록에서 `학교 업무` 선택.

## 패키지 및 배포 원칙
- 폰/워치 Data Layer 통신 앱은 동일한 applicationId와 동일 서명 계열을 사용한다.
- 워치페이스 패키지는 Wear OS가 인식할 수 있는 Watch Face Format 메타데이터를 유지한다.
- debug와 release의 versionCode를 독립적으로 증가시킨다.
- release 서명은 로컬 비밀키를 저장소에 커밋하지 않는다.
- Google Play Console 등록 전에는 AAB 생성과 내부 테스트 트랙 배포 준비까지만 자동화한다.

## `워치에 적용` 동작
- 폰 앱에서 데이터가 유효하면 버튼을 활성화한다.
- 버튼 클릭 시 최신 JSON을 Data Layer로 먼저 전송한다.
- 이후 사용자가 워치페이스 선택 화면으로 이동할 수 있도록 안내/인텐트 기반 연결을 제공한다.
- 기기/One UI 버전에서 직접 특정 워치페이스를 강제 적용할 수 없는 경우 Galaxy Wearable/워치의 워치페이스 선택 화면으로 유도한다.

## 성공 기준
1. 폰 앱에서 학교 업무 JSON을 가져오고 워치에 전송할 수 있다.
2. 워치 companion이 데이터를 저장하고 complication이 저장 데이터를 표시한다.
3. release 빌드에서 phone/companion/watchface 산출물이 생성된다.
4. Google Play 내부 테스트용 AAB/배포 구성이 준비된다.
5. 정식 또는 내부 테스트 설치 후 Galaxy Wearable에서 `학교 업무`가 설치된/다운로드된 워치페이스로 선택 가능함을 실제 기기에서 검증한다.

## 현재 한계
Galaxy Wearable의 `다운로드됨` 분류 자체는 Samsung/Galaxy Wearable 앱이 관리하므로 APK 코드만으로 해당 섹션 강제 등록을 보장할 수 없다. 따라서 제품 목표는 스토어를 통한 정상 설치 흐름으로 그 목록에 나타나게 하는 것이다.
