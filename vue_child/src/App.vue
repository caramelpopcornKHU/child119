<template>
  <div id="app" class="d-flex flex-column min-vh-100">
    <!-- Header -->
    <header class="navbar navbar-light bg-white shadow-sm fixed-top">
      <div class="container d-flex align-items-center justify-content-between">
        <div class="d-flex align-items-center">
          <button @click="goToHome()" class="btn btn-link p-0 border-0">
          <img src="@/assets/kid_logo.png" alt="logo" class="me-2" style="width: 50px; height: 50px; vertical-align: middle;" />
          </button>
          <span class="fw-bold fs-5 text-dark">아이119</span>  
        </div>
        <button @click="mypage()"
        class="navbar-toggler"
        type="button"
        data-bs-toggle="collapse"
        data-bs-target="#navbarToggleExternalContent"
        aria-controls="navbarToggleExternalContent"
        aria-expanded="false"
        aria-label="Toggle navigation"
      >
        <span class="navbar-toggler-icon"></span>
      </button>
      </div>
    </header>

    <!-- Main Content -->
    <main class="flex-grow-1 mt-5 mb-5 p-3">
      <!-- 실제 페이지 내용 -->
      <router-view />
    </main>

    <!-- Footer -->
    <footer class="navbar navbar-light bg-white border-top fixed-bottom">
      <div class="container d-flex justify-content-around py-1">
        <button  @click ="goToHome()" class="btn btn-link text-dark text-center">
          <i class="bi bi-hospital fs-5"></i><br />
          <small>응급실</small>
        </button>
        <button @click="hospitalinformation()" class="btn btn-link text-dark text-center">
          <i class="bi bi-hospital fs-5"></i><br />
          <small>병의원</small>
        </button>
        <button @click="aichact()" class="btn btn-link text-dark text-center">
          <i class="bi bi-heart-pulse fs-5"></i><br />
          <small>AI 추천</small>
        </button>
        <button @click="emergency()" class="btn btn-link text-dark text-center">
          <i class="bi bi-geo-alt fs-5"></i><br />
          <small>응급지시지</small>
        </button>
        <button @click="MyKids()" class="btn btn-link text-dark text-center">
          <i class="bi bi-person fs-5"></i><br />
          <small>내 아이</small>
        </button>
      </div>
    </footer>
  </div>
</template>

<script>
import Hospitalinformation from './views/hospitalinformation.vue';

export default {
  name: "App",
  methods: {
    mypage() {
      this.$router.push('/mypage'); 
    },
    // 약국 페이지로 이동
    hospitalinformation() {
      this.$router.push('/hospitalinformation');
    },
    // AI 추천 페이지로 이동
    aichact() {
      this.$router.push('/aichat');
    },
    // 응급지시지 페이지로 이동 (요청하신 부분)
    emergency() {
      this.$router.push('/emergency');
    },
    // 내 아이 페이지로 이동
    MyKids() {
      this.$router.push('/MyKids');
  },
  goToHome(){
    this.$router.push('/');
  },
}
}
</script>

<style>
@import "bootstrap/dist/css/bootstrap.min.css";
@import "bootstrap-icons/font/bootstrap-icons.css";

/* Pretendard 폰트 적용 */
@import url("https://cdn.jsdelivr.net/gh/orioncactus/pretendard/dist/web/static/pretendard.css");

body {
  font-family: "Pretendard", sans-serif;
  background-color: #f8f9fa;
  margin: 0;
}

/* Header/Footer 공간 확보 */
main {
  padding-top: 60px; /* header 높이 */
  padding-bottom: 70px; /* footer 높이 */
}
/* 푸터 버튼 기본 스타일 */
footer .btn-link {
  text-decoration: none !important;
  color: #6c757d; /* 기본 회색 */
  transition: all 0.25s ease;
}

/* Hover 시 효과 */
footer .btn-link:hover {
  color: #0d6efd; /* Bootstrap primary color (파란색) */
  transform: translateY(-3px);
}

/* Hover 시 아이콘만 살짝 커지게 */
footer .btn-link:hover i {
  transform: scale(1.2);
  transition: transform 0.25s ease;
}

/* 클릭 시 살짝 눌리는 효과 */
footer .btn-link:active {
  transform: scale(0.95);
}

</style>
