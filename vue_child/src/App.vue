<template>
  <div id="app" class="d-flex flex-column min-vh-100">
    <!-- Header -->
    <header class="navbar navbar-light bg-white shadow-sm fixed-top">
      <div class="container d-flex align-items-center justify-content-between">
        <div class="d-flex align-items-center">
          <button @click="goToHome()" class="btn btn-link p-0 border-0">
            <img src="@/assets/kid_logo.png" alt="logo" class="me-2" style="width:50px;height:50px;vertical-align:middle;" />
          </button>
          <span class="fw-bold fs-5 text-dark">아이119</span>
        </div>

        <button
          @click="mypage()"
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

    <!-- Main Content -->
    <main class="flex-grow-1 mt-5 mb-5 p-3">
      <RouterView />
    </main>

    <!-- Footer (Bootstrap-only, no custom CSS) -->
    <nav class="navbar bg-white border-top fixed-bottom">
      <ul class="nav nav-fill w-100 align-items-stretch">
        <!-- 응급실 -->
        <li class="nav-item">
          <button
            @click="goToHome()"
            class="nav-link d-flex flex-column align-items-center justify-content-center py-2 text-dark"
            :class="{'bg-light': $route.path === '/'}">
            <i class="bi bi-truck-front fs-5"></i>
            <small class="mt-1">응급실</small>
          </button>
        </li>

        <!-- 병의원 -->
        <li class="nav-item border-start">
          <button
            @click="hospitalinformation()"
            class="nav-link d-flex flex-column align-items-center justify-content-center py-2 text-dark"
            :class="{'bg-light': $route.path === '/hospitalinformation'}">
            <i class="bi bi-hospital fs-5"></i>
            <small class="mt-1">병의원</small>
          </button>
        </li>

        <!-- AI 챗봇 -->
        <li class="nav-item border-start">
          <button
            @click="aichact()"
            class="nav-link d-flex flex-column align-items-center justify-content-center py-2 text-dark"
            :class="{'bg-light': $route.path === '/aichat'}">
            <i class="bi bi-robot fs-5"></i>
            <small class="mt-1">AI 챗봇</small>
          </button>
        </li>

        <!-- 응급처치 -->
        <li class="nav-item border-start">
          <button
            @click="emergency()"
            class="nav-link d-flex flex-column align-items-center justify-content-center py-2 text-dark"
            :class="{'bg-light': $route.path === '/emergency'}">
            <i class="bi bi-heart-fill fs-5"></i>
            <small class="mt-1">응급처치</small>
          </button>
        </li>

        <!-- 내 아이 -->
        <li class="nav-item border-start">
          <button
            @click="MyKids()"
            class="nav-link d-flex flex-column align-items-center justify-content-center py-2 text-dark"
            :class="{'bg-light': $route.path === '/MyKids'}">
            <i class="bi bi-people fs-5"></i>
            <small class="mt-1">내 아이</small>
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
    mypage() { this.$router.push('/mypage') },
    hospitalinformation() { this.$router.push('/hospitalinformation') },
    aichact() { this.$router.push('/aichat') },
    emergency() { this.$router.push('/emergency') },
    MyKids() { this.$router.push('/MyKids') },
    goToHome() { this.$router.push('/') },
  }
}
</script>

<style>
@import "bootstrap/dist/css/bootstrap.min.css";
@import "bootstrap-icons/font/bootstrap-icons.css";
/* 커스텀 CSS 없음 (Bootstrap만 사용) */
</style>
