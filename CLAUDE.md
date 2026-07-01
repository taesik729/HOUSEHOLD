# 심플 가계부 — CLAUDE.md

---

## 프로젝트 개요

- **이름**: 심플 가계부
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
- 현재: `versionCode 7`

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

## Android APK Safe Area 처리 (필수!)

Capacitor 5는 기본적으로 edge-to-edge 모드로 실행됨.
WebView가 상단 Status Bar / 하단 Navigation Bar 영역까지 침범해서 UI가 겹치는 문제 발생.

### 해결 방법 (HOUSEHOLD에서 검증 완료)

**1. `index.html`** — viewport-fit=cover 추가
```html
<meta name="viewport" content="..., viewport-fit=cover" />
```

**2. `src/assets/main.css`** — safe-area CSS 적용
```css
/* 상단 */
.topbar {
  padding: env(safe-area-inset-top, 0px) 20px 0;
  height: calc(56px + env(safe-area-inset-top, 0px));
  align-items: flex-end;
  padding-bottom: 8px;
}
/* 하단 탭바 */
.bottom-nav {
  bottom: env(safe-area-inset-bottom);
  height: 64px;
}
/* 콘텐츠 영역 */
.content { padding-bottom: calc(80px + env(safe-area-inset-bottom, 0px)); }
/* FAB 버튼 */
.btn-fab { bottom: calc(80px + env(safe-area-inset-bottom, 0px)); }
```

**3. `android/app/src/main/java/.../MainActivity.java`**
```java
import android.os.Bundle;
import androidx.core.view.WindowCompat;
public class MainActivity extends BridgeActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), true);
    }
}
```

**4. `capacitor.config.json`** — StatusBar 설정
```json
"plugins": {
  "StatusBar": {
    "overlaysWebView": false,
    "style": "DEFAULT",
    "backgroundColor": "#ffffff"
  }
}
```

> safe-area-inset-bottom 값 확인: 앱에서 JS로 CSS 변수에 담아 화면에 표시  
> 삼성 갤럭시 3버튼 네비 기준 **48px** 반환 확인됨

---

## 비밀번호 재설정 흐름

- `src/views/LoginView.vue` → `resetPasswordForEmail` 에 `redirectTo: 'https://household-taesik.vercel.app/reset-password'` 하드코딩
- Supabase 대시보드 → Authentication → URL Configuration:
  - **Site URL**: `https://household-taesik.vercel.app`
  - **Redirect URLs**: `https://household-taesik.vercel.app/reset-password` 등록 필수
- `src/supabase/client.js` → `detectSessionInUrl: true` 설정
- `src/router/index.js` → `/reset-password` 경로는 가드 최상단에서 `return true` 처리
- `src/views/ResetPasswordView.vue` → 변경 완료 후 `signOut()` → "앱으로 돌아가세요" 메시지 표시 (웹 로그인 화면으로 이동 안 함)
- **이메일 링크 클릭 흐름**: 네이버/지메일 → 웹 브라우저에서 비밀번호 변경 → 브라우저 닫고 앱에서 로그인

---

## 회원 탈퇴

- `src/views/SettingsView.vue` → `withdraw()` 함수
- 순서: 데이터 삭제(`household_ledger`, `household_categories`) → `supabase.rpc('delete_user')` → `supabase.auth.signOut()` → `localStorage.removeItem('household-auth')` → `window.location.href = '/login'`
- `router.push` 대신 `window.location.href` 사용 (APK에서 router.push 미작동 이슈)

---

## 삼성 카테고리 피커 문제

- 삼성 기기에서 `<select>` 태그 → 전체화면 네이티브 피커로 표시됨
- **해결**: `<select>` 제거 후 커스텀 버튼 그리드(`cat-grid`, `cat-btn`)로 교체
- `src/components/LedgerModal.vue` 에 적용됨

---

## Supabase 인증 문제 디버깅 순서

1. Supabase 대시보드 설정 (Site URL, Redirect URLs) 확인
2. 환경변수 확인
3. 코드 확인

---

## 프로젝트 구분 (중요!)

- **심플 가계부** (`com.taesik.household`) — Android APK, Capacitor 앱
- **골프 스코어** (`C:\work\SCORE`) — Android APK, Capacitor 앱
- **AI 병해충 진단** — Android APK, Capacitor 앱
- **태식 팜 MES** — 웹앱 (PWA), APK 아님
- 앱 관련 작업(빌드, 배포, AdMob, Play Console)은 심플 가계부·골프 스코어·병해충 진단에만 해당

---

## 코드 수정 규칙

- 코드 수정 시 반드시 **수정 전 → 수정 후** 코드블록 형식으로 대화창에 펼쳐서 보여줌
- 터미널 명령어 실행 전 코드블록으로 명령어 먼저 표시
- "실행합니다" 하고 그냥 넘어가지 않음
- 모든 세션에 적용
- **구현 전 설계 먼저**: 새 기능 구현 전 반드시 폴더 구조·상태 흐름을 먼저 설계하고 사용자 확인 후 구현
