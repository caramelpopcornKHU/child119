<template>
  <!-- 헤더/푸터는 이미 있다고 가정하고, 본문만 구성 -->
  <div class="edu-page container-fluid px-3 py-3">

    <!-- 상단 탭 -->
    <div class="d-flex justify-content-center mb-3">
      <div></div> <!-- 왼쪽 여백용 -->
      <button type="button" class="btn btn-success rounded-pill px-3 fw-semibold" @click="goToWrite">
        + 게시물 추가
      </button>
    </div>

    <!-- 검색 영역 -->
    <div class="mb-3">
      <div class="input-group edu-search rounded-3 px-2">
        <input type="text" class="form-control border-0 shadow-none p-2" placeholder="검색어를 입력하세요." v-model="keyword" />
        <span class="input-group-text bg-transparent border-0 p-0">
          <!-- 검색 아이콘 (부트스트랩 아이콘 없다는 가정으로 SVG 직접 사용) -->
          <svg xmlns="http://www.w3.org/2000/svg" width="22" height="22" viewBox="0 0 24 24">
            <path fill="#00b894" d="M15.5 14h-.79l-.28-.27A6.471 6.471 0 0 0 16 9.5
                 6.5 6.5 0 1 0 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79L19 20.49
                 20.49 19zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5
                 11.99 14 9.5 14" />
          </svg>
        </span>
      </div>
    </div>

    <!-- 전체 개수 -->
    <p class="text-muted small mb-3">
      전체 {{ filteredItems.length }}건이 등록되어 있습니다.
    </p>

    <!-- 목록 -->
    <div class="edu-list">
      <div v-for="item in filteredItems" :key="item.id" class="edu-item border-top" @click="goToDetail(item.id)">
        <div class="py-3">
          <div class="fw-semibold edu-title">
            {{ item.title }}
          </div>
          <div class="text-muted small mt-2">
            {{ formatDate(item.createdAt) }}
            <span class="mx-2">|</span>
            조회수 {{ item.viewCountNumber || 0 }}
            
          </div>
           <div class="ms-3">
    <button
      type="button"
      class="btn btn-outline-secondary btn-sm rounded-pill"
      @click="goToEdit(item.id)"
    >
      수정
    </button>
  </div>
        </div>
        
      </div>
      
    </div>
      
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getAllEmergencyData } from '../firebase/emergency.js'

const keyword = ref('')
const router = useRouter()

// 게시물 추가 버튼
const goToWrite = () => {
  router.push('/emergencyadd')
}

// Firebase에서 데이터 가져오기
const items = ref([])

onMounted(async () => {
  try {
    items.value = await getAllEmergencyData()
    console.log('응급처치 데이터 로드 완료:', items.value.length, '건')
  } catch (error) {
    console.error('데이터 로드 실패:', error)
    alert('데이터를 불러오는데 실패했습니다.')
  }
})

const filteredItems = computed(() => {
  if (!keyword.value.trim()) return items.value
  const q = keyword.value.trim().toLowerCase()
  return items.value.filter((v) => v.title.toLowerCase().includes(q))
})

// 아이템 클릭 시 상세 페이지로 이동
function goToDetail(id) {
  router.push({ name: 'emergencyorder', params: { id } })
}
function formatDate(timestamp) {
  if (!timestamp) return ''
  const date = timestamp.toDate ? timestamp.toDate() : new Date(timestamp)
  return date.toLocaleDateString('ko-KR', { year: 'numeric', month: '2-digit', day: '2-digit' }).replace(/\. /g, '.').replace(/\.$/, '')
}
</script>


<style scoped>
/* 헤더/푸터 공간 고려 (프로젝트에서 --header-h, --footer-h 쓰고 있다면 활용) */
.edu-page {
  padding-top: calc(var(--header-h, 56px) + 8px);
  padding-bottom: calc(var(--footer-h, 56px) + 8px);
  min-height: calc(100vh - var(--header-h, 56px) - var(--footer-h, 56px));
  background-color: #ffffff;
}

/* 탭 */
.edu-tab {
  flex: 1 1 auto;
  max-width: 160px;
  text-align: center;
  padding: 8px 0;
  margin: 0 8px;
  font-size: 18px;
  font-weight: 700;
  color: #888;
  background: transparent;
  border: none;
  border-bottom: 3px solid transparent;
  border-radius: 0;
}

.edu-tab.active {
  color: #00b894;
  /* 상단 타이틀 느낌 나는 그린 톤 */
  border-bottom-color: #00b894;
}

/* 검색창 */
.edu-search {
  background-color: #f5f5f5;
}

/* 리스트 */
.edu-list {
  background-color: #ffffff;
}

.edu-item {
  cursor: pointer;
}

.edu-item:last-child {
  border-bottom: 1px solid #e9ecef;
}

/* 제목 두 줄까지 표시 후 말줄임 */
.edu-title {
  font-size: 17px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
