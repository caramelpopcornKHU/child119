import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
    },
    {
      path: '/mypage',
      name: 'mypage',
      component: () => import('../views/mypage.vue'),
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
    },
    {
      path: '/AddKid',
      name: 'AddKid',
      component: () => import('../views/AddKid.vue'),
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
  ],
})

export default router
