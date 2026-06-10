# 우리집 가계부 — CLAUDE.md

---

## 프로젝트 개요

- **이름**: 우리집 가계부
- **패키지**: `com.taesik.household`
- **배포**: Vercel (`https://household-taesik.vercel.app`)
- **GitHub**: https://github.com/taesik729/HOUSEHOLD

---

## 기술 스택

| 항목 | 내용 |
|------|------|
| 프레임워크 | Vue 3 (`<script setup>`) |
| 빌드 | Vite |
| DB/Auth | Supabase |
| 광고 | Google AdMob (`@capacitor-community/admob@5.0.0`) |
| Android | Capacitor v5 |

---

## AdMob 설정

- App ID: `ca-app-pub-4592571865246292~3538251911`
- Banner ID: `ca-app-pub-4592571865246292/8487662975`
- `src/App.vue` 에서 `showAds: true/false` 로 ON/OFF 제어

---

## Android 빌드 설정 (중요!)

### Gradle 버전 조합 (검증 완료)
- **Gradle**: `8.7` (`gradle-wrapper.properties`)
- **AGP**: `8.3.0` (`build.gradle`)
- **targetSdkVersion**: `35` (`variables.gradle`)
- **Gradle JDK**: Android Studio Embedded JDK (jbr)

### 버전 코드
- `android/app/build.gradle` 의 `versionCode` 업로드마다 +1 증가 필요
- 현재: `versionCode 3`

### 키스토어
- 경로: `C:\work\FARM\HOUSEHOLD\household-key.jks`
- Key alias: `household`

---

## Android 빌드 트러블슈팅

### 문제 1: Android resource linking failed (RES_TABLE_TYPE_TYPE)
- **원인**: Gradle 8.0.2 + AGP 8.0.0 이 SDK 35 와 호환 안 됨
- **해결**: Gradle 8.7 + AGP 8.3.0 으로 업그레이드

### 문제 2: Incompatible Gradle JVM version
- **원인**: Gradle JDK가 JVM 21로 설정됨 (Gradle 8.x는 JDK 17 필요)
- **해결**: Android Studio → Settings → Build Tools → Gradle → Gradle JDK → Embedded JDK 선택

### 문제 3: Could not move temporary workspace
- **원인**: Gradle 캐시 손상
- **해결**: `C:\Users\{user}\.gradle\caches` 삭제 후 Android Studio 재시작

### 문제 4: 버전 코드 중복
- **원인**: 이전에 업로드한 AAB와 동일한 versionCode
- **해결**: `build.gradle` 에서 `versionCode` +1 증가 후 재빌드

---

## Google Play 등록 정보

- 개발자 계정: taesik729@gmail.com
- 계정 ID: 759144036656683947
- 앱 상태: 비공개 테스트 심사 중 (2026-06-09)

---

## Supabase 테이블

- `household_ledger` — 수입/지출 내역
- `household_categories` — 사용자 카테고리 (기본 14개 자동 생성)

---

## 개인정보처리방침

- URL: `https://household-taesik.vercel.app/privacy`
- 파일: `src/views/PrivacyView.vue`

---

## 코드 수정 규칙

- 코드 수정 시 반드시 **변경 전/후 코드를 대화창에 펼쳐서** 보여줌 (코드블록 형식)
- 터미널 명령어 실행 전 코드블록으로 명령어 먼저 표시
- "실행합니다" 하고 그냥 넘어가지 않음
- 모든 세션에 적용
