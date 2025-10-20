<!-- src/views/SettingsPage.vue -->
<template>
  <main class="settings-wrap">
    <!-- 상단 헤더 (스크린샷처럼 좌측 뒤로가기 + 프로필/부제목) -->
    <header class="settings-header sticky-top">
      <div class="d-flex align-items-center gap-2">
        <button class="btn btn-link p-0 me-1" @click="$router.back()" aria-label="뒤로">
          <i class="bi bi-arrow-left fs-4"></i>
        </button>
        <div class="d-flex align-items-center gap-3">
          <div class="avatar bg-light border rounded-circle"></div>
          <div>
            <div class="fw-semibold">정성범</div>
            <div class="text-secondary small">내 정보 · 주소 관리</div>
          </div>
        </div>
      </div>
    </header>

    <!-- 본문: 스크롤은 브라우저 기본 스크롤 -->
    <section class="container-fluid px-0">

      <!-- 1. 일반 섹션 -->
      <div class="section-block">
        <ul class="list-group list-group-flush">
          <li class="list-group-item d-flex justify-content-between align-items-center">
            <span>로그인</span>
            <span @click="goToLogin" class="text-primary" style="cursor: pointer;">로그인하기</span>
          </li>
          <li class="list-group-item d-flex justify-content-between align-items-center">
            <span>내아이정보</span>
            <i @click="goTokid" class="bi bi-chevron-right text-secondary"></i>
          </li>
          <li class="list-group-item d-flex justify-content-between align-items-center">
            <span>화면 테마 · 진동</span>
            <i class="bi bi-chevron-right text-secondary"></i>
          </li>
          <li class="list-group-item d-flex justify-content-between align-items-center">
            <span>연락처 관리</span>
            <i class="bi bi-chevron-right text-secondary"></i>
          </li>
        </ul>
      </div>

      <!-- 2. 인증 및 보안 -->
      <div class="section-title">인증 및 보안</div>
      <div class="section-block">
        <ul class="list-group list-group-flush">
          <li class="list-group-item d-flex justify-content-between align-items-center">
            <span>인증서</span>
            <i class="bi bi-chevron-right text-secondary"></i>
          </li>
          <li class="list-group-item d-flex justify-content-between align-items-center">
            <span>비밀번호 · 보안</span>
            <i class="bi bi-chevron-right text-secondary"></i>
          </li>
          <li class="list-group-item d-flex justify-content-between align-items-center">
            <span>토스로 로그인한 서비스</span>
            <i class="bi bi-chevron-right text-secondary"></i>
          </li>
        </ul>
      </div>

      <!-- 3. 자산 및 증명서 -->
      <div class="section-title">자산 및 증명서</div>
      <div class="section-block">
        <ul class="list-group list-group-flush">
          <li class="list-group-item d-flex justify-content-between align-items-center">
            <span>불러온 자산 (마이데이터) 관리</span>
            <i class="bi bi-chevron-right text-secondary"></i>
          </li>
          <li class="list-group-item d-flex justify-content-between align-items-center">
            <span>증명서 발급</span>
            <i class="bi bi-chevron-right text-secondary"></i>
          </li>
        </ul>
      </div>

      <!-- 4. 송금 및 결제 (스크린샷 하단 영역 시작) -->
     

      <div class="pb-4"></div>
    </section>
  </main>
</template>

<script>
export default {
  name: 'SettingsPage',
  methods: {
  goToLogin() {
      this.$router.push('/login');
    },
  goTokid(){
    this.$router.push('/MyKids');
  }  
  } 
};
</script>

<style scoped>
/* Pretendard 사용을 권장 (index.html에 링크 추가 안내 아래 참고) */
.settings-wrap {
  min-height: 100vh;
  background: #f8f9fb;
  font-family: Pretendard, system-ui, -apple-system, Segoe UI, Roboto, 'Helvetica Neue',
    Arial, 'Noto Sans', 'Apple SD Gothic Neo', 'Malgun Gothic', sans-serif;
  color: #111827;
}

/* 헤더(스크린샷 느낌으로 라이트 톤) */
.settings-header {
  background: #ffffff;
  border-bottom: 1px solid #eceef1;
  padding: 12px 16px;
}
.avatar { width: 40px; height: 40px; }

/* 섹션 타이틀 */
.section-title {
  padding: 14px 16px 6px;
  font-size: 0.92rem;
  color: #6b7280;
  font-weight: 700;
}

/* 섹션 박스 (리스트 묶음) */
.section-block {
  background: #fff;
  margin-bottom: 10px;
  border-top: 1px solid #f1f3f5;
  border-bottom: 1px solid #f1f3f5;
}

/* 리스트 아이템 간격/경계 */
.list-group-item {
  padding: 14px 16px;
  border-color: #f1f3f5;
}

.bi { font-size: 1.1rem; }
</style>
