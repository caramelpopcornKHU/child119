<template>
  <div id="app" class="d-flex flex-column min-vh-100" :class="{ 'admin-full-width': isAdminRoute }">
    <!-- ===============================
         📌 Header (상단 고정 네비게이션)
         =============================== -->
    <header v-if="!isAdminRoute"
      class="navbar navbar-light bg-white shadow-sm fixed-top">
      <div class="container d-flex align-items-center justify-content-between">
        <!-- 좌측: 로고 + 홈 버튼 -->
        <div class="d-flex align-items-center">
          <button @click="goToHome" class="btn btn-link p-0 border-0">
            <img src="@/assets/kid_logo.png" alt="logo" class="me-2"
              style="width:50px;height:50px;vertical-align:middle;" />
          </button>
          <span class="fw-bold fs-5 text-dark">아이119</span>
        </div>

        <!-- 우측: 마이페이지 버튼 (햄버거 메뉴 아이콘) -->
        <button  @click="logAction"  class="btn text-bold"
                :class="{ 'btn-light' : status === '로그아웃', 'btn-primary' : status === '로그인'}">
          <span>{{ status }}</span>
        </button>
      </div>
    </header>
    

     <!-- 👇 관리자용 헤더 -->
    <header
      v-else
      class="navbar navbar-light bg-white shadow-sm fixed-top"
    >
      <div class="d-flex align-items-center justify-content-between w-100 px-4">
        <!-- 좌측: 로고 + 텍스트 -->
        <div class="d-flex align-items-center" style="cursor: pointer;" @click="goToAdminHome">
          <img
            src="@/assets/kid_logo.png"
            alt="logo"
            class="me-2"
            style="width:50px;height:50px;vertical-align:middle;"
          />
          <span class="fw-bold fs-5 text-dark">아이119 관리자</span>
        </div>

        <!-- 우측: 로그인/로그아웃 버튼 (같은 로직 재사용) -->
        <button
          @click="logAction"
          class="btn text-bold"
          :class="{ 'btn-light' : status === '로그아웃', 'btn-primary' : status === '로그인'}"
        >
          <span>{{ status }}</span>
        </button>
      </div>
    </header>


    <aside v-if="isAdminRoute" class="admin-aside bg-white border-end shadow-sm p-3">
      <h5 class="fw-bold mb-4 text-center">관리자 메뉴</h5>

      <ul class="nav flex-column text-bold">
        <li class="nav-item mb-2">
          <button
              class="nav-link btn-aside text-start w-100"
              :class="{ 'text-bold text-primary': isActive('/AdminHome') }"
              @click="goTo('/AdminHome')"
              style="color: black;"
            >
            👥 사용자 정보
          </button>
        </li>

        <li class="nav-item mb-2">
          <button
              class="nav-link btn-aside text-start w-100"
              :class="{ 'text-bold text-primary': isActive('/AdminManageContents') }"
              @click="goTo('/AdminManageContents')"
              style="color: black;"
            >
            👥 응급 콘텐츠 관리
          </button>
        </li>

        <li class="nav-item mb-2">
          <button
              class="nav-link btn-aside text-start w-100 text-bold"
              :class="{ 'text-bold text-primary': isActive('/AdminAutomation') }"
              @click="goTo('/AdminAutomation')"
              style="color: black;"
            >
            ⚙️ 자동화 로그
          </button>
        </li>

        <li class="nav-item mb-2">
          <button
              class="nav-link btn-aside text-start w-100"
              :class="{ 'text-bold text-primary': isActive('/AdminFeedback') }"
              @click="goTo('/AdminFeedback')"
              style="color: black;"
            >
            💬 건의사항
          </button>
        </li>
      </ul>
    </aside>

    <!-- ===============================
         🧭 Main (헤더/푸터 사이 컨텐츠 영역)
         =============================== -->
    <main id="main" class="flex-grow-1">
      <!-- RouterView는 각 페이지(View) 컴포넌트를 표시 -->
      <!-- 관리자 페이지는 전체 너비, 일반 페이지는 container 적용 -->
      <div :class="isAdminRoute ? 'w-100 px-0' : 'container py-3'">
        <RouterView />
      </div>
    </main>

    <!-- ===============================
         🦶 Footer (하단 고정 네비게이션)
         =============================== -->
    <nav 
    v-if="!isAdminRoute"
      class="navbar bg-white border-top fixed-bottom">
      <ul class="nav nav-fill w-100 align-items-stretch  m-0 flex-nowrap">
        <!-- 각 버튼은 라우터 경로에 따라 아이콘/페이지 전환 -->
        <li class="nav-item">
          <button @click="goToHome"
            class="nav-link d-flex flex-column align-items-center justify-content-center py-2 text-gray"
            :class="{ 'bg-light': $route.path === '/' }">
            <svg xmlns="http://www.w3.org/2000/svg" width="30" height="30" viewBox="0 0 24 24">
              <path fill="currentColor"
                d="M11 0v3h2V0zM7.88 1.46L6.46 2.87L8.59 5L10 3.58zm8.24 0L14 3.58L15.41 5l2.13-2.12zM12 5a2 2 0 0 0-2 2v1H6.5c-.66 0-1.22.42-1.42 1L3 15v8a1 1 0 0 0 1 1h1a1 1 0 0 0 1-1v-1h12v1a1 1 0 0 0 1 1h1a1 1 0 0 0 1-1v-8l-2.08-6c-.2-.58-.76-1-1.42-1H14V7a2 2 0 0 0-2-2M6.5 9.5h11L19 14H5zm0 6.5A1.5 1.5 0 0 1 8 17.5A1.5 1.5 0 0 1 6.5 19A1.5 1.5 0 0 1 5 17.5A1.5 1.5 0 0 1 6.5 16m11 0a1.5 1.5 0 0 1 1.5 1.5a1.5 1.5 0 0 1-1.5 1.5a1.5 1.5 0 0 1-1.5-1.5a1.5 1.5 0 0 1 1.5-1.5" />
            </svg>
            <small class="mt-1 text-nowrap text-medium">응급실</small>
          </button>
        </li>

        <li class="nav-item border-start">
          <button @click="hospitalinformation"
            class="nav-link d-flex flex-column align-items-center justify-content-center py-2 text-gray"
            :class="{ 'bg-light': $route.path === '/hospitalinformation' }">
            <svg xmlns="http://www.w3.org/2000/svg" width="30" height="30" viewBox="0 0 24 24">
              <path fill="currentColor"
                d="M2 22V7a1 1 0 0 1 1-1h4V2h10v4h4a1 1 0 0 1 1 1v15h-8v-5h-4v5zM9 4v6h2V8h2v2h2V4h-2v2h-2V4zM4 20h4v-3H4zm0-5h4v-3H4zm12 5h4v-3h-4zm0-5h4v-3h-4zm-6 0h4v-3h-4z" />
            </svg>
            <small class="mt-1 text-nowrap text-medium">의료시설</small>
          </button>
        </li>

        <li class="nav-item border-start">
          <button @click="aichat"
            class="nav-link d-flex flex-column align-items-center justify-content-center py-2 text-gray"
            :class="{ 'bg-light': $route.path === '/aichat' }">
            <svg xmlns="http://www.w3.org/2000/svg" width="30" height="30" viewBox="0 0 24 24">
              <path fill="currentColor"
                d="M19 8h1v1h-1zm1-3h-1v2h1V6h.5c.28 0 .5-.22.5-.5v-2c0-.28-.22-.5-.5-.5H18v1h2zm-3-2h-1v4h1zm-3.5 12.5a2 2 0 1 0 4 0c0-1.11-.89-2-2-2s-2 .9-2 2M17 8h-1v1h1zm5 6h-1c0-1.5-.47-2.87-1.26-4h-2.77c1.22.91 2.03 2.36 2.03 4v2h2v1h-2v3H5v-3H3v-1h2v-2c0-2.76 2.24-5 5-5h4c.34 0 .68.04 1 .1V7.08c-.33-.05-.66-.08-1-.08h-1V5.73A2 2 0 1 0 10 4c0 .74.4 1.39 1 1.73V7h-1c-3.87 0-7 3.13-7 7H2c-.55 0-1 .45-1 1v3c0 .55.45 1 1 1h1v1a2 2 0 0 0 2 2h14c1.11 0 2-.89 2-2v-1h1c.55 0 1-.45 1-1v-3c0-.55-.45-1-1-1m-13.5-.5c-1.1 0-2 .9-2 2s.9 2 2 2s2-.89 2-2s-.89-2-2-2" />
            </svg>
            <small class="mt-1 text-nowrap text-medium">AI 챗봇</small>
          </button>
        </li>

        <li class="nav-item border-start">
          <button @click="emergency"
            class="nav-link d-flex flex-column align-items-center justify-content-center py-2 text-gray"
            :class="{ 'bg-light': $route.path === '/emergency' }">
            <svg xmlns="http://www.w3.org/2000/svg" width="30" height="30" viewBox="0 0 24 24">
              <path fill="currentColor"
                d="M16.5 2.83c-1.74 0-3.41.81-4.5 2.07c-1.09-1.26-2.76-2.07-4.5-2.07c-3.08 0-5.5 2.41-5.5 5.5c0 3.77 3.4 6.86 8.55 11.53L12 21.17l1.45-1.31C18.6 15.19 22 12.1 22 8.33c0-3.09-2.42-5.5-5.5-5.5m-4.5 15v-4H9l3-7v4h3" />
            </svg>
            <small class="mt-1 text-nowrap text-medium">응급처치</small>
          </button>
        </li>

        <li class="nav-item border-start">
          <button @click="MyKids"
            class="nav-link d-flex flex-column align-items-center justify-content-center py-2 text-gray"
            :class="{ 'bg-light': $route.path === '/MyKids' }">
            <svg xmlns="http://www.w3.org/2000/svg" width="30" height="30" viewBox="0 0 24 24">
              <path fill="currentColor"
                d="M13.07 10.41a5 5 0 0 0 0-5.82A3.4 3.4 0 0 1 15 4a3.5 3.5 0 0 1 0 7a3.4 3.4 0 0 1-1.93-.59M5.5 7.5A3.5 3.5 0 1 1 9 11a3.5 3.5 0 0 1-3.5-3.5m2 0A1.5 1.5 0 1 0 9 6a1.5 1.5 0 0 0-1.5 1.5M16 17v2H2v-2s0-4 7-4s7 4 7 4m-2 0c-.14-.78-1.33-2-5-2s-4.93 1.31-5 2m11.95-4A5.32 5.32 0 0 1 18 17v2h4v-2s0-3.63-6.06-4Z" />
            </svg>
            <small class="mt-1 text-nowrap text-medium">내 아이</small>
          </button>
        </li>
      </ul>
    </nav>
  </div>
</template>

<script>
import { onMounted, ref } from 'vue';
import { RouterView } from 'vue-router'
import { onAuthStateChanged } from 'firebase/auth';
import { auth } from './firebase/index';
import { signOut } from 'firebase/auth'

export default {
  name: 'App',
  components: { RouterView },

  computed: {
    isAdminRoute() {
      return this.$route.path.startsWith('/Admin')
    }
  },

  methods: {
    mypage() { this.$router.push('/mypage') },
    hospitalinformation() { this.$router.push('/hospitalInformationApp') },
    aichat() { this.$router.push('/aichat') },
    emergency() { this.$router.push('/emergency') },
    MyKids() { this.$router.push('/MyKids') },
    goToHome() { this.$router.push('/') },
    goToLogin() { this.$router.push('/login') },
    goToAdminHome() { this.$router.push('/AdminHome') },

    goTo(path) {
      this.$router.push(path)
    },

    isActive(path) {
      return this.$route.path === path
    },

    async logout() {
      try {
        await signOut(auth)
        alert('로그아웃 되었습니다.')
        this.$router.push('/login')
      } catch (err) {
        console.error('로그아웃 중 오류 발생:', err)
      }
    },

    logAction() {
      if (this.status === '로그인') this.goToLogin()
      else this.logout()
    }
  },

  setup() {
    const status = ref('로그인')

    onMounted(() => {
      onAuthStateChanged(auth, (user) => {
        status.value = user ? '로그아웃' : '로그인'
      })
    })

    return { status }
  }
}


</script>

<style>

/* ✅ 부트스트랩 및 아이콘 불러오기 */
@import "bootstrap/dist/css/bootstrap.min.css";
@import "bootstrap-icons/font/bootstrap-icons.css";

.admin-full-width {
  max-width: 100% !important;
}

.admin-full-width header .container,
.admin-full-width main > div {
  max-width: 100% !important;
  padding-left: 0 !important;
  padding-right: 0 !important;
}

.admin-aside {
  width: 240px;
  position: fixed;
  top: 10px;
  left: 0;
  bottom: 0;
  padding-top: 72px; /* 상단 네비(고정 헤더)가 있으면 높이 맞춤. 없으면 0으로 바꿔도 됨 */
  z-index: 200;
}

.btn-aside {
  display: block;
  width: 100%;
  text-align: left;
  padding: 0.6rem 0.75rem;
  border: none;
  background: transparent;
  color: #333;
  font-weight: 600;
  border-radius: 8px;
  cursor: pointer;
  transition: background-color 0.12s, color 0.12s, transform 0.12s;
}

/* hover */
.btn-aside:hover {
  background-color: #f8f9fa;
  transform: translateX(4px);
}

/* active */
.btn-aside.active {
  background-color: #0d6efd;
  color: #fff;
  box-shadow: 0 1px 3px rgba(13,110,253,0.12);
}

@font-face {
  font-family: 'Pretendard';
  src: url('https://cdn.jsdelivr.net/gh/projectnoonnu/pretendard@1.0/Pretendard-Light.woff2') format('woff2');
  font-weight: 300;
  font-display: swap;
}


@font-face {
  font-family: 'Pretendard';
  src: url('https://cdn.jsdelivr.net/gh/projectnoonnu/pretendard@1.0/Pretendard-Medium.woff2') format('woff2');
  font-weight: 500;
  font-display: swap;
}


@font-face {
  font-family: 'Pretendard';
  src: url('https://cdn.jsdelivr.net/gh/projectnoonnu/pretendard@1.0/Pretendard-Bold.woff2') format('woff2');
  font-weight: 700;
  font-display: swap;
}


.text-plight {
  font-family: 'Pretendard';
  font-weight: 300;
}

.text-medium {
  font-family: 'Pretendard';
  font-weight: 500;
}

.text-bold {
  font-family: 'Pretendard';
  font-weight: 700;
}

.text-gray {
  color: #00000090 !important;
}

.nav-link.bg-light {
  color: #0D6EFD !important;
}

.btn {
  font-family: 'Pretendard' !important;
  font-weight: 700 !important;
}
.btn-primary {
  background-color: #0D6EFD;
  color: #ffff;
 
}
.btn-gray {
  background-color: #00000090;
  color: #ffff;
}

.badge {
  font-family: 'Pretendard' !important;
  font-weight: 500 !important;
}





/* ✅ 헤더/푸터 높이 지정 (화면 계산용) */
:root {
  --header-h: 64px;
  --footer-h: 56px;
}

/* ✅ 전체 화면 높이 확보 (모바일 대응 포함) */
html,
body,
#app {
  height: 100%;
  margin: 0;
  padding: 0;
}

/* ✅ 메인 콘텐츠 영역 
   - 헤더/푸터 높이만큼 패딩을 주어 겹침 방지
   - 남은 높이를 계산하여 스크롤 영역 확보 */
#main {
  min-height: calc(100dvh - var(--header-h) - var(--footer-h));
  /* 남는 세로공간 계산 */
  padding-top: var(--header-h);
  /* 헤더에 가리지 않게 */
}

/* ✅ 헤더가 항상 다른 요소보다 위에 표시되도록 z-index 지정 */
header.navbar.fixed-top {
  z-index: 1030;
}
</style>
