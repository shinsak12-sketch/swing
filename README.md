# 스윙시뮬 (SwingSimul)

카메라 **슬로우모션**으로 골프 스윙을 촬영하고, 느린 속도·프레임 단위로 되돌려 보며 자세를 분석하는 안드로이드 앱입니다.

Kotlin + Jetpack Compose로 작성되었으며, 촬영은 **CameraX**, 슬로우 재생은 **Media3(ExoPlayer)** 를 사용합니다.

## 주요 기능

- 📹 **스윙 촬영** — CameraX 기반 후면/전면 카메라 전환, 최고 화질(FHD→HD→SD 폴백) 녹화
- 🐢 **슬로우 재생** — 0.25x / 0.5x / 1x 배속으로 스윙을 천천히 재생
- 🎞️ **프레임 단위 분석** — 이전/다음 프레임 버튼으로 임팩트 순간을 한 컷씩 확인
- 🗂️ **보관함** — 촬영한 스윙을 썸네일 그리드로 모아 보고 재생·삭제
- 🔐 카메라·마이크 권한을 앱 안에서 자연스럽게 요청

> 촬영은 기기가 지원하는 최고 화질로 저장하고, "슬로우모션" 효과는 재생 단계에서 배속·프레임 스텝으로 구현합니다. 추후 기기별 고프레임(120/240fps) 캡처 API를 붙이면 촬영 자체의 슬로우모션도 확장할 수 있습니다.

## 기술 스택

| 영역 | 사용 기술 |
|------|-----------|
| 언어 | Kotlin 2.0 |
| UI | Jetpack Compose (Material 3) |
| 촬영 | CameraX 1.4 (`camera-video`, `camera-view`) |
| 재생 | Media3 ExoPlayer 1.5 |
| 네비게이션 | Navigation Compose |
| 권한 | Accompanist Permissions |
| 썸네일 | Coil (video frame decoder) |
| 빌드 | AGP 8.7, Gradle 8.14 |

## 프로젝트 구조

```
app/src/main/java/com/swingsimul/app/
├── MainActivity.kt          # 진입점, Compose 세팅
├── SwingSimulApp.kt         # Application
├── data/
│   ├── Recording.kt         # 스윙 영상 모델
│   └── RecordingRepository.kt  # 앱 전용 저장소 파일 관리
└── ui/
    ├── SwingSimulNavHost.kt # 화면 네비게이션
    ├── theme/               # 색상·타이포·테마
    ├── camera/              # 촬영 화면 + ViewModel (CameraX)
    ├── playback/            # 슬로우 재생/분석 화면 (Media3)
    └── gallery/             # 보관함 화면 + ViewModel
```

영상은 저장소 권한이 필요 없는 앱 전용 외부 저장소
(`Android/data/com.swingsimul.app/files/Movies/swings/`)에 `.mp4`로 저장됩니다.

## 빌드 & 실행

Android Studio(Ladybug 이상 권장)에서 열거나, Android SDK가 설정된 환경에서:

```bash
./gradlew :app:assembleDebug        # 디버그 APK 빌드
./gradlew :app:installDebug         # 연결된 기기/에뮬레이터에 설치
```

- **minSdk 24 / targetSdk 35**
- 최초 실행 시 카메라·마이크 권한을 허용해야 촬영 화면이 동작합니다.
- 실기기 촬영을 권장합니다(에뮬레이터는 가상 카메라라 스윙 분석 확인이 제한적).

## 로드맵 (아이디어)

- [ ] 고프레임(슬로우모션) 하드웨어 캡처 지원
- [ ] 스윙 궤적/임팩트 자동 감지 및 마커
- [ ] 두 스윙 나란히 비교(레퍼런스 vs 내 스윙)
- [ ] 자세 추정(ML Kit Pose)으로 각도 오버레이
