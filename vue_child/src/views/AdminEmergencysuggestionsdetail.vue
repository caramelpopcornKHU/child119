<template>
  <!-- 전체 페이지 배경 -->
  <main class="bg-body-tertiary min-vh-100">
    <!-- 컨테이너: 좌우 여백과 상하 여백 적용 -->
    <div class="container-fluid py-4 px-3">

      <!-- 상단 헤더: 뒤로가기 버튼과 제목 -->
      <div class="d-flex align-items-center mb-4 gap-2">
        <!-- 뒤로가기 버튼 -->
        <button 
          class="btn btn-link p-0 text-decoration-none" 
          style="color: #0D6EFD;" 
          @click="goBack"
        >
          <!-- 왼쪽 화살표 아이콘 -->
          <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24">
            <path fill="currentColor" d="M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20v-2z"/>
          </svg>
        </button>
        <!-- 페이지 제목 -->
        <h5 class="mb-0" style="font-family: 'Pretendard', sans-serif; font-weight: 600; color: #212529;">
          건의사항 상세
        </h5>
      </div>

      <!-- 로딩 중 표시 -->
      <div v-if="loading" class="text-center py-5">
        <div class="spinner-border" style="color: #0D6EFD;" role="status">
          <span class="visually-hidden">로딩 중...</span>
        </div>
      </div>

      <!-- 건의사항이 없을 때 -->
      <div v-else-if="!suggestion" class="alert alert-warning" role="alert">
        <h5 class="alert-heading">건의사항을 찾을 수 없습니다</h5>
        <p class="mb-0">존재하지 않거나 삭제된 건의사항입니다.</p>
      </div>

      <!-- 건의사항 상세 내용 -->
      <div v-else>
        <!-- 제목 카드 -->
        <div class="card border-0 shadow-sm rounded-4 mb-3">
          <div class="card-body p-4">
            <label 
              class="form-label fw-semibold mb-3" 
              style="font-family: 'Pretendard', sans-serif; color: #0D6EFD;"
            >
              제목
            </label>
            <div 
              class="p-3 bg-light rounded-3" 
              style="font-family: 'Pretendard', sans-serif; font-size: 16px;"
            >
              {{ suggestion.title }}
            </div>
          </div>
        </div>

        <!-- 내용 카드 -->
        <div class="card border-0 shadow-sm rounded-4 mb-3">
          <div class="card-body p-4">
            <label 
              class="form-label fw-semibold mb-3" 
              style="font-family: 'Pretendard', sans-serif; color: #0D6EFD;"
            >
              내용
            </label>
            <div 
              class="p-3 bg-light rounded-3" 
              style="font-family: 'Pretendard', sans-serif; font-size: 16px; white-space: pre-wrap;"
            >
              {{ suggestion.content }}
            </div>
          </div>
        </div>

        <!-- 작성자 정보 카드 -->
        <div class="card border-0 shadow-sm rounded-4 mb-3">
          <div class="card-body p-4">
            <label 
              class="form-label fw-semibold mb-3" 
              style="font-family: 'Pretendard', sans-serif; color: #0D6EFD;"
            >
              작성자 정보
            </label>
            <div class="row g-3">
              <div class="col-md-6">
                <div class="small text-muted mb-1" style="font-family: 'Pretendard', sans-serif;">이메일</div>
                <div class="p-2 bg-light rounded-2" style="font-family: 'Pretendard', sans-serif;">
                  {{ suggestion.fromEmail }}
                </div>
              </div>
              <div class="col-md-6">
                <div class="small text-muted mb-1" style="font-family: 'Pretendard', sans-serif;">작성일</div>
                <div class="p-2 bg-light rounded-2" style="font-family: 'Pretendard', sans-serif;">
                  {{ formatDate(suggestion.createdAt) }}
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 상태 카드 -->
        <div class="card border-0 shadow-sm rounded-4 mb-3">
          <div class="card-body p-4">
            <label 
              class="form-label fw-semibold mb-3" 
              style="font-family: 'Pretendard', sans-serif; color: #0D6EFD;"
            >
              처리 상태
            </label>
            <div class="d-flex align-items-center gap-3">
              <span 
                class="badge rounded-pill px-4 py-2"
                style="font-family: 'Pretendard', sans-serif; font-size: 14px;"
                :class="suggestion.status === '처리완료' ? 'bg-success' : 'bg-warning text-dark'"
              >
                {{ suggestion.status }}
              </span>
              <div class="small text-muted" style="font-family: 'Pretendard', sans-serif;">
                대상: {{ suggestion.targetRole }}
              </div>
            </div>
          </div>
        </div>

        <!-- 관리자 메모 카드 (관리자만 보임) -->
        <div v-if="suggestion.adminMemo" class="card border-0 shadow-sm rounded-4 mb-4">
          <div class="card-body p-4">
            <label 
              class="form-label fw-semibold mb-3" 
              style="font-family: 'Pretendard', sans-serif; color: #dc3545;"
            >
              관리자 메모
            </label>
            <div 
              class="p-3 bg-light rounded-3" 
              style="font-family: 'Pretendard', sans-serif; font-size: 16px; white-space: pre-wrap;"
            >
              {{ suggestion.adminMemo }}
            </div>
          </div>
        </div>

        <!-- 하단 버튼 영역 -->
        <div
          class="position-sticky bottom-0 bg-body-tertiary pt-2 pb-3"
          style="z-index: 1; margin-bottom: var(--footer-h, 70px);"
        >
          <div class="d-flex gap-2">
            <!-- 뒤로가기 버튼 -->
            <button
              type="button"
              class="btn flex-fill rounded-pill"
              style="font-family: 'Pretendard', sans-serif; background-color: #0D6EFD; color: white; border: none;"
              @click="goBack"
            >
              목록으로 돌아가기
            </button>
          </div>
        </div>
      </div>
    </div>
  </main>
</template>

<script setup>
// Vue의 반응형 데이터를 위한 ref 함수 가져오기
import { ref, onMounted } from 'vue'
// 페이지 이동을 위한 라우터 가져오기
import { useRouter, useRoute } from 'vue-router'
// Firebase Firestore 함수 가져오기
import { db } from '@/firebase'
import { doc, getDoc } from 'firebase/firestore'

// 라우터 인스턴스 생성
const router = useRouter()
const route = useRoute()

// 건의사항 데이터를 저장하는 반응형 변수
const suggestion = ref(null)
// 로딩 상태를 저장하는 반응형 변수
const loading = ref(true)

/**
 * 뒤로가기 함수
 * 관리자 페이지로 돌아갑니다
 */
function goBack() {
  router.push('/AdminHome')
}

/**
 * 날짜 포맷팅 함수
 * Firebase Timestamp를 한국어 날짜 형식으로 변환합니다
 */
function formatDate(date) {
  if (!date) return ''
  return new Date(date).toLocaleDateString('ko-KR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

/**
 * Firebase에서 건의사항 데이터를 가져오는 함수
 */
async function loadSuggestion() {
  try {
    loading.value = true
    
    // URL에서 건의사항 ID 가져오기
    const suggestionId = route.params.id
    
    if (!suggestionId) {
      console.error('건의사항 ID가 없습니다.')
      suggestion.value = null
      return
    }

    // Firestore에서 해당 ID의 문서 가져오기
    const docRef = doc(db, 'suggestions', suggestionId)
    const docSnap = await getDoc(docRef)

    // 문서가 존재하는지 확인
    if (docSnap.exists()) {
      const data = docSnap.data()
      
      // suggestion 변수에 데이터 저장
      suggestion.value = {
        id: docSnap.id,
        title: data.title || '(제목 없음)',
        content: data.content || '(내용 없음)',
        fromEmail: data.fromEmail || '(이메일 없음)',
        fromUid: data.fromUid || '',
        status: data.status || '접수',
        targetRole: data.targetRole || '',
        adminMemo: data.adminMemo || '',
        createdAt: data.createdAt ? data.createdAt.toDate() : new Date(),
        updatedAt: data.updatedAt ? data.updatedAt.toDate() : new Date()
      }
      
      console.log('건의사항 로딩 완료:', suggestion.value)
    } else {
      console.error('건의사항을 찾을 수 없습니다.')
      suggestion.value = null
    }
  } catch (error) {
    console.error('건의사항 로딩 중 오류:', error)
    alert('건의사항을 불러오는 중 오류가 발생했습니다.')
    suggestion.value = null
  } finally {
    loading.value = false
  }
}

// 페이지가 로드되면 건의사항 데이터를 가져옵니다
onMounted(() => {
  loadSuggestion()
})
</script>

<style scoped>
/* Pretendard 폰트 임포트 */
@import url('https://cdn.jsdelivr.net/gh/orioncactus/pretendard/dist/web/static/pretendard.css');

/* 전역 폰트 설정 */
* {
  font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
}
</style>