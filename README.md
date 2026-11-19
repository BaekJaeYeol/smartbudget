# SmartBudget

SmartBudget은 거래 기록, 월간 예산, 금융 데이터 연동 및 소비 분석 기능을 제공하는 Android 개인 재무 관리 앱입니다. Kotlin과 Jetpack Compose로 개발합니다.

## 개발 환경

> [!IMPORTANT]
> 프로젝트를 열기 전에 Android Studio를 **Rabbit 2 이상**으로 업그레이드해야 합니다. 이전 버전에서는 Android Gradle Plugin 9.5.1 또는 Android SDK 37 기반 프로젝트의 동기화와 빌드가 정상적으로 동작하지 않을 수 있습니다.

- Android Studio Rabbit 2 이상
- JDK 21(Gradle 실행 환경)
- Android SDK 37
- Gradle 9.6(저장소의 Gradle Wrapper 사용)
- Kotlin 2.2.10
- 최소 지원 Android 버전: API 29(Android 10)

## Android Studio에서 개발하기

1. 저장소를 복제하고 Android Studio에서 저장소 루트를 엽니다.
2. `Settings > Build, Execution, Deployment > Build Tools > Gradle`에서 Gradle JDK를 JDK 21 또는 Android Studio에 포함된 호환 JBR로 설정합니다.
3. SDK Manager에서 Android SDK 37을 설치합니다.
4. Gradle 동기화가 완료될 때까지 기다립니다. 프로젝트별 SDK 경로는 Android Studio가 생성하는 `local.properties`에 저장됩니다.
5. API 29 이상의 AVD 또는 Android 실 기기를 선택한 뒤 `app` 실행 구성을 실행합니다.

## 명령줄에서 개발하기

Windows PowerShell에서는 저장소 루트에서 체크인된 Gradle Wrapper를 사용합니다.

```powershell
# 포맷, 로컬 테스트, ktlint 검사, detekt 분석
.\gradlew.bat verify

# 디버그 APK 빌드
.\gradlew.bat assembleDebug

# 연결된 기기 또는 AVD에 디버그 앱 설치
.\gradlew.bat installDebug
```

macOS와 Linux에서는 같은 작업을 `./gradlew verify`, `./gradlew assembleDebug`, `./gradlew installDebug`로 실행할 수 있습니다.

생성된 디버그 APK는 `app/build/outputs/apk/debug/`에서 확인할 수 있습니다. 기능을 수정한 뒤에는 관련 테스트를 추가하거나 갱신하고, 커밋 전에 반드시 `verify`를 실행합니다.

## 개발 시 주의사항

- `verify`는 검사 전에 ktlint 포맷을 실행하므로 소스 파일이 변경될 수 있습니다. 실행 후 `git diff`로 변경 내용을 확인합니다.
- JDK 21이 아닌 JVM이 Gradle에 선택되어 있으면 IDE와 명령줄의 빌드 결과가 달라지거나 동기화가 실패할 수 있습니다.
- 앱은 개발 서버 및 RSS 피드 등 외부 네트워크 리소스를 사용합니다. 오프라인이거나 서버 기능이 아직 구현되지 않은 경우 일부 기능이 실패할 수 있으므로 로딩·빈 상태·오류 상태를 함께 확인합니다.
- 앱 빌드는 `arm64-v8a`와 `armeabi-v7a` ABI를 대상으로 합니다. 배포 전 검증에는 ARM 기반 AVD 또는 실 기기 사용을 권장합니다.
- `local.properties`, 키 저장소, 비밀번호, API 키, JWT와 실제 금융 데이터는 커밋하지 않습니다.
- 공식 릴리즈 빌드에는 Android 서명 키와 `ANDROID_KEYSTORE_PATH`, `ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_ALIAS`, `ANDROID_KEY_PASSWORD` 설정이 필요합니다. 서명 정보는 저장소 파일에 직접 작성하지 않습니다.
- 백엔드 계약을 변경하거나 API 연동을 수정할 때는 `knowledges/api/openapi.json`과 관련 설계 문서를 먼저 확인합니다.
