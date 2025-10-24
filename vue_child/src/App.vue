<template>
  <div id="app" class="d-flex flex-column min-vh-100">
    <!-- ===============================
         📌 Header (상단 고정 네비게이션)
         =============================== -->
    <header class="navbar navbar-light bg-white shadow-sm fixed-top">
      <div class="container d-flex align-items-center justify-content-between">
        <!-- 좌측: 로고 + 홈 버튼 -->
        <div class="d-flex align-items-center">
          <button @click="goToHome" class="btn btn-link p-0 border-0">
            <img src="@/assets/kid_logo.png" alt="logo"
                 class="me-2"
                 style="width:50px;height:50px;vertical-align:middle;" />
          </button>
          <span class="fw-bold fs-5 text-dark">아이119</span>
        </div>

        <!-- 우측: 마이페이지 버튼 (햄버거 메뉴 아이콘) -->
        <button
          @click="mypage"
          class="navbar-toggler"
          type="button"
          data-bs-toggle="collapse"
          data-bs-target="#navbarToggleExternalContent"
          aria-controls="navbarToggleExternalContent"
          aria-expanded="false"
          aria-label="Toggle navigation">
          <span class="navbar-toggler-icon"></span>
        </button>
      </div>
    </header>

    <!-- ===============================
         🧭 Main (헤더/푸터 사이 컨텐츠 영역)
         =============================== -->
    <main id="main" class="flex-grow-1">
      <!-- RouterView는 각 페이지(View) 컴포넌트를 표시 -->
      <div class="container py-3">
        <RouterView />
      </div>
    </main>

    <!-- ===============================
         🦶 Footer (하단 고정 네비게이션)
         =============================== -->
    <nav class="navbar bg-white border-top fixed-bottom">
      <ul class="nav nav-fill w-100 align-items-stretch m-0 flex-nowrap">
        <!-- 각 버튼은 라우터 경로에 따라 아이콘/페이지 전환 -->
        <li class="nav-item">
          <button
            @click="goToHome"
            class="nav-link d-flex flex-column align-items-center justify-content-center py-2 text-dark"
            :class="{ 'bg-light': $route.path === '/' }">
            <i class="bi bi-truck-front fs-5"></i>
            <small class="mt-1 text-nowrap">응급실</small>
          </button>
        </li>

        <li class="nav-item border-start">
          <button
            @click="hospitalinformation"
            class="nav-link d-flex flex-column align-items-center justify-content-center py-2 text-dark"
            :class="{ 'bg-light': $route.path === '/hospitalinformation' }">
            <i class="bi bi-hospital fs-5"></i>
            <small class="mt-1 text-nowrap">병의원</small>
          </button>
        </li>

        <li class="nav-item border-start">
          <button
            @click="aichat"
            class="nav-link d-flex flex-column align-items-center justify-content-center py-2 text-dark"
            :class="{ 'bg-light': $route.path === '/aichat' }">
            <i class="bi bi-robot fs-5"></i>
            <small class="mt-1 text-nowrap">AI 챗봇</small>
          </button>
        </li>

        <li class="nav-item border-start">
          <button
            @click="emergency"
            class="nav-link d-flex flex-column align-items-center justify-content-center py-2 text-dark"
            :class="{ 'bg-light': $route.path === '/emergency' }">
            <i class="bi bi-heart-fill fs-5"></i>
            <small class="mt-1 text-nowrap">응급처치</small>
          </button>
        </li>

        <li class="nav-item border-start">
          <button
            @click="MyKids"
            class="nav-link d-flex flex-column align-items-center justify-content-center py-2 text-dark"
            :class="{ 'bg-light': $route.path === '/MyKids' }">
            <i class="bi bi-people fs-5"></i>
            <small class="mt-1 text-nowrap">내 아이</small>
          </button>
        </li>
      </ul>
    </nav>
  </div>
</template>

<script>
import { RouterView } from 'vue-router'

export default {
  name: 'App',
  components: { RouterView },

  methods: {
    // 라우팅 이동 메서드
    mypage() { this.$router.push('/mypage') },
    hospitalinformation() { this.$router.push('/hospitalinformation') },
    aichat() { this.$router.push('/aichat') },
    emergency() { this.$router.push('/emergency') },
    MyKids() { this.$router.push('/MyKids') },
    goToHome() { this.$router.push('/') },
  }
}
</script>

<style>
/* ✅ 부트스트랩 및 아이콘 불러오기 */
@import "bootstrap/dist/css/bootstrap.min.css";
@import "bootstrap-icons/font/bootstrap-icons.css";

/* ✅ 헤더/푸터 높이 지정 (화면 계산용) */
:root {
  --header-h: 64px;
  --footer-h: 56px;
}

/* ✅ 전체 화면 높이 확보 (모바일 대응 포함) */
html, body, #app {
  height: 100%;
  margin: 0;
  padding: 0;
}

/* ✅ 메인 콘텐츠 영역 
   - 헤더/푸터 높이만큼 패딩을 주어 겹침 방지
   - 남은 높이를 계산하여 스크롤 영역 확보 */
#main {
  min-height: calc(100dvh - var(--header-h) - var(--footer-h)); /* 남는 세로공간 계산 */
  padding-top: var(--header-h);   /* 헤더에 가리지 않게 */
  overflow: auto;                 /* 내부 스크롤 허용 */
}

/* ✅ 헤더가 항상 다른 요소보다 위에 표시되도록 z-index 지정 */
header.navbar.fixed-top {
  z-index: 1030;
}
</style>
