<template>
  <div class="privacy-wrap">
    <div class="privacy-header">
      <button class="back-btn" @click="$router.back()">← 뒤로</button>
      <h1>개인정보처리방침</h1>
    </div>

    <div class="privacy-body">
      <p class="updated">최종 업데이트: 2025년 6월 8일</p>

      <section>
        <h2>1. 수집하는 개인정보</h2>
        <p>우리집 가계부(이하 "앱")는 서비스 제공을 위해 다음과 같은 정보를 수집합니다.</p>
        <ul>
          <li><strong>이메일 주소</strong> — 회원가입 및 로그인 인증 목적</li>
          <li><strong>가계부 데이터</strong> — 수입·지출 내역, 카테고리 등 사용자가 직접 입력한 데이터</li>
        </ul>
      </section>

      <section>
        <h2>2. 개인정보 이용 목적</h2>
        <ul>
          <li>서비스 로그인 및 계정 관리</li>
          <li>가계부 데이터 저장 및 조회</li>
          <li>비밀번호 재설정 이메일 발송</li>
        </ul>
      </section>

      <section>
        <h2>3. 개인정보 보관 기간</h2>
        <p>회원 탈퇴 시 이메일 주소 및 모든 가계부 데이터를 즉시 삭제합니다. 탈퇴 전까지는 서비스 제공을 위해 보관합니다.</p>
      </section>

      <section>
        <h2>4. 제3자 제공</h2>
        <p>수집한 개인정보는 외부에 제공하지 않습니다. 단, 다음 서비스를 통해 데이터가 처리됩니다.</p>
        <ul>
          <li><strong>Supabase</strong> — 데이터베이스 및 인증 서비스 (미국 소재)</li>
        </ul>
        <p>Supabase 개인정보처리방침: <a href="https://supabase.com/privacy" target="_blank">supabase.com/privacy</a></p>
        <li><strong>Google AdMob</strong> — 광고 서비스 (미국 소재). 광고 제공을 위해 기기 식별자 및 광고 ID를 수집할 수 있습니다.</li>
        <p>Google 개인정보처리방침: <a href="https://policies.google.com/privacy" target="_blank">policies.google.com/privacy</a></p>
      </section>

      <section>
        <h2>5. 사용자 권리</h2>
        <ul>
          <li><strong>데이터 열람</strong> — 앱 내에서 직접 확인 가능</li>
          <li><strong>데이터 삭제</strong> — 설정 → 회원 탈퇴로 즉시 삭제 가능</li>
          <li><strong>계정 탈퇴</strong> — 언제든지 자유롭게 탈퇴 가능</li>
        </ul>
      </section>

      <section>
        <h2>6. 보안</h2>
        <p>비밀번호는 암호화되어 저장되며, 앱 운영자도 확인할 수 없습니다. 모든 데이터는 HTTPS로 암호화하여 전송됩니다.</p>
      </section>

      <section>
        <h2>7. 아동 개인정보</h2>
        <p>본 앱은 만 14세 미만 아동을 대상으로 하지 않습니다. 만 14세 미만은 보호자의 동의 하에 이용하시기 바랍니다.</p>
      </section>

      <section>
        <h2>8. 문의</h2>
        <p>개인정보 관련 문의사항은 아래 이메일로 연락해 주세요.</p>
        <p><strong>taesik729@gmail.com</strong></p>
      </section>
    </div>
  </div>
</template>

<script setup>
</script>

<style scoped>
.privacy-wrap {
  max-width: 600px;
  margin: 0 auto;
  min-height: 100dvh;
  background: #fff;
}
.privacy-header {
  position: sticky;
  top: 0;
  background: #fff;
  border-bottom: 1px solid #e0e0e0;
  padding: 14px 20px;
  display: flex;
  align-items: center;
  gap: 12px;
  z-index: 10;
}
.privacy-header h1 { font-size: 16px; font-weight: 700; }
.back-btn {
  background: none;
  border: none;
  font-size: 14px;
  color: var(--primary-bright, #4f46e5);
  cursor: pointer;
  white-space: nowrap;
}
.privacy-body {
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 20px;
  font-size: 14px;
  line-height: 1.8;
  color: #333;
}
.updated { font-size: 12px; color: #999; }
section h2 {
  font-size: 15px;
  font-weight: 700;
  margin-bottom: 8px;
  color: #111;
}
ul { padding-left: 18px; }
li { margin-bottom: 4px; }
a  { color: var(--primary-bright, #4f46e5); }
</style>
