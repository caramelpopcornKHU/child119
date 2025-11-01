import { createRouter, createWebHistory } from 'vue-router'
//import HomeView from '../views/HomeView.vue'
import { auth } from '../firebase/index'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: () => import('../views/HomeView.vue'),
    },
    {
      path: '/mypage',
      name: 'mypage',
      component: () => import('../views/mypage.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/login.vue'),
    },
     {
      path: '/aichat',
      name: 'aichat',
      component: () => import('../views/aichat.vue'),
    },
     {
      path: '/emergencyorder',
      name: 'emergencyorder',
      component: () => import('../views/emergencyorder.vue'),
    },
    {
      path: '/forgotid',
      name: 'forgotid',
      component: () => import('../views/forgotid.vue'),
    },
    {
      path: '/forgotpassword',
      name: 'forgotpassword',
      component: () => import('../views/forgotpassword.vue'),
    },
    {
      path: '/sginup',
      name: 'sginup',
      component: () => import('../views/sginup.vue'),
    },
    {
      path: '/MyKids',
      name: 'MyKids',
      component: () => import('../views/MyKids.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/AddKid',
      name: 'AddKid',
      component: () => import('../views/AddKid.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/kidinformationedit/:id',
      name: 'kidinformationedit',
      component: () => import('../views/kidinformationedit.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/hospitalinformation',
      name: 'hospitalinformation',
      component: () => import('../views/hospitalinformation.vue'),
    },
    {
      path: '/emergency',
      name: 'emergency',
      component: () => import('../views/emergency.vue'),
    },
      {
      path: '/FindId',
      name: 'FindId',
      component: () => import('../views/FindId.vue'),
    },
     {
      path: '/FindPassword',
      name: 'FindPassword',
      component: () => import('../views/FindPassword.vue'),
    },
    {
      path: '/hospitalDetail',
      name: 'hospitalDetail',
      component: () => import('../views/hospitalDetail.vue'),
    },
    {
      path: '/emergencyDetail/:hpid',
      name: 'emergencyDetail',
      component: () => import('../views/emergencyDetail.vue'),
      props: true,
    },
    {
      path: '/test',
      name: 'test',
      component: () => import('../views/test.vue'),
    },

    {
      path: '/HomeViewTest',
      name: 'HomeViewTest',
      component: () => import('../views/HomeViewTest.vue'),
    },
    // app 버전 라우팅 ----------------
    {
      path: '/HomeViewApp',
      name: 'HomeViewApp',
      component: () => import('../views/HomeViewApp.vue'),
    },
    {
      path: '/HomeViewHSM',
      name: 'HomeViewHSM',
      component: () => import('../views/HomeViewHSM.vue'),
    },
    {
      path: '/HomeView_SH',
      name: 'HomeView_SH',
      component: () => import('../views/HomeView_SH.vue'),
    },
    {
      path: '/HomeView_nono',
      name: 'HomeView_nono',
      component: () => import('../views/HomeView_nono.vue'),
    },

  ],
})

// 로그인이 필요한 페이지 보호
router.beforeEach((to, from, next) => {
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth)
  const currentUser = auth.currentUser

  if (requiresAuth && !currentUser) {
    // 로그인이 필요한 페이지인데 로그인하지 않은 경우
    alert('로그인이 필요합니다.')
    next('/login')
  } else {
    next()
  }
})

export default router
