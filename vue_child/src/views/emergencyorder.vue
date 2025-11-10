<template>
  <div class="container-fluid py-5" style="padding-top: calc(var(--header-h, 56px) + 16px) !important; padding-bottom: calc(var(--footer-h, 56px) + 16px) !important;">
    
    <!-- 로딩 중 -->
    <div v-if="loading" class="text-center py-5">
      <div class="spinner-border text-primary" role="status">
        <span class="visually-hidden">로딩 중...</span>
      </div>
    </div>
    
    <!-- 데이터 표시 -->
    <div v-else-if="item" class="container px-3">
      <!-- 제목 -->
      <h1 class="fs-4 fw-bold mb-3">{{ item.title }}</h1>
      
      <!-- 메타 정보 -->
      <div class="d-flex align-items-center text-muted small mb-4">
        <span>{{ formatDate(item.createdAt) }}</span>
        <span class="mx-2">|</span>
        <span>조회수 {{ item.viewCountNumber || 0 }}</span>
      </div>
      
      <!-- 유튜브 썸네일 이미지 (클릭 시 동영상으로) -->
      <div v-if="item.thumbnailUrl && !showVideo" class="mb-4 position-relative" @click="showVideo = true" style="cursor: pointer;">
        <img :src="item.thumbnailUrl" class="w-100 rounded" alt="썸네일" />
        <div class="position-absolute top-50 start-50 translate-middle">
          <div class="bg-danger bg-opacity-75 rounded-circle" style="width: 60px; height: 60px; display: flex; align-items: center; justify-content: center;">
            <svg xmlns="http://www.w3.org/2000/svg" width="30" height="30" fill="white" viewBox="0 0 16 16">
              <path d="M8 15A7 7 0 1 1 8 1a7 7 0 0 1 0 14zm0 1A8 8 0 1 0 8 0a8 8 0 0 0 0 16z"/>
              <path d="M6.271 5.055a.5.5 0 0 1 .52.038l3.5 2.5a.5.5 0 0 1 0 .814l-3.5 2.5A.5.5 0 0 1 6 10.5v-5a.5.5 0 0 1 .271-.445z"/>
            </svg>
          </div>
        </div>
      </div>
      
      <!-- 유튜브 동영상 (썸네일 클릭 후 표시) -->
      <div v-if="item.youtubeUrl && showVideo" class="ratio ratio-16x9 mb-4">
        <iframe 
          :src="getYoutubeEmbedUrl(item.youtubeUrl)"
          allowfullscreen
          class="rounded"
        ></iframe>
      </div>
      
      <!-- 일반 동영상 URL (유튜브가 아닌 경우) -->
      <div v-if="item.youtubeUrl && !item.youtubeId && !item.thumbnailUrl" class="mb-4">
        <a :href="item.youtubeUrl" target="_blank" class="btn btn-outline-primary w-100">
          동영상 보러 가기
        </a>
      </div>
      
      <!-- 해시태그 -->
      <div v-if="item.hashtags && item.hashtags.length > 0" class="mb-3">
        <span v-for="(tag, idx) in item.hashtags" :key="idx" class="badge bg-danger-subtle text-danger me-2">
          #{{ tag }}
        </span>
      </div>
      
      <!-- 증상 -->
      <div v-if="item.symptomList && item.symptomList.length > 0" class="card mb-3">
        <div class="card-header bg-danger text-white fw-bold">
          증상
        </div>
        <div class="card-body">
          <ol class="mb-0">
            <li v-for="(symptom, idx) in item.symptomList" :key="idx" class="mb-2">
              {{ symptom }}
            </li>
          </ol>
        </div>
      </div>
      
      <!-- 응급처치 -->
      <div v-if="item.methodList && item.methodList.length > 0" class="card mb-3">
        <div class="card-header bg-primary text-white fw-bold">
          응급처치 방법
        </div>
        <div class="card-body">
          <ol class="mb-0">
            <li v-for="(method, idx) in item.methodList" :key="idx" class="mb-2">
              {{ method }}
            </li>
          </ol>
        </div>
      </div>
      
      <!-- 원인 -->
      <div v-if="item.reasonList && item.reasonList.length > 0" class="card mb-3">
        <div class="card-header bg-warning text-dark fw-bold">
          원인
        </div>
        <div class="card-body">
          <ol class="mb-0">
            <li v-for="(reason, idx) in item.reasonList" :key="idx" class="mb-2">
              {{ reason }}
            </li>
          </ol>
        </div>
      </div>
      
      <!-- 주의사항 -->
      <div v-if="item.warning" class="alert alert-warning mb-4">
        <strong>⚠️ 주의:</strong> {{ item.warning }}
      </div>
      
      <!-- 목록으로 버튼 -->
      <div class="d-grid">
        <button @click="goBack" class="btn btn-primary btn-lg">
          목록으로
        </button>
      </div>
    </div>
    
    <!-- 데이터 없음 -->
    <div v-else class="text-center py-5">
      <p class="text-muted">데이터를 찾을 수 없습니다.</p>
    </div>
    
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getEmergencyData, incrementViews } from '../firebase/emergency.js'

const route = useRoute()
const router = useRouter()

const item = ref(null)
const loading = ref(true)
const showVideo = ref(false)

const id = computed(() => route.params.id)

onMounted(async () => {
  try {
    // emergency.js의 함수 사용
    item.value = await getEmergencyData(id.value)
    
    // 조회수 증가
    await incrementViews(id.value)
  } catch (error) {
    console.error('데이터 로드 실패:', error)
  } finally {
    loading.value = false
  }
})

function getYoutubeEmbedUrl(url) {
  if (!url) return ''
  const regex = /(?:youtu\.be\/|youtube\.com\/(?:watch\?v=|embed\/))([\w-]{11})/
  const match = url.match(regex)
  return match ? `https://www.youtube.com/embed/${match[1]}` : url
}

function formatDate(timestamp) {
  if (!timestamp) return ''
  const date = timestamp.toDate ? timestamp.toDate() : new Date(timestamp)
  return date.toLocaleDateString('ko-KR', { year: 'numeric', month: '2-digit', day: '2-digit' }).replace(/\. /g, '.').replace(/\.$/, '')
}

function goBack() {
  router.back()
}
</script>

<style scoped>
.bg-danger-subtle {
  background-color: #fdecea !important;
}
</style>