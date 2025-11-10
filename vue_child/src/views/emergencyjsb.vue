<template>
  <!-- Educational data list page -->
  <div class="edu-page">
    <!-- 상단 탭 영역 -->
    <div class="tabs">
    </div>

    <!-- 검색 영역 -->
    <div class="search-wrapper">
      <input
        type="text"
        v-model="keyword"
        placeholder="검색어를 입력하세요."
        aria-label="검색"
      />
      <!-- 검색 아이콘 (SVG 직접 사용) -->
      <svg
        xmlns="http://www.w3.org/2000/svg"
        width="22"
        height="22"
        viewBox="0 0 24 24"
      >
        <path
          fill="#00b894"
          d="M15.5 14h-.79l-.28-.27A6.471 6.471 0 0 0 16 9.5 6.5 6.5 0 1 0 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79L19 20.49 20.49 19zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14"
        />
      </svg>
    </div>

    <!-- 전체 개수 표시 -->
    <p class="count">전체 {{ filteredItems.length }}건이 등록되어 있습니다.</p>

    <!-- 목록 렌더링 -->
    <div class="list">
      <div
        v-for="item in filteredItems"
        :key="item.id"
        class="item"
        @click="goToEmergency(item.id)"
      >
        <div class="title">
          {{ item.title }}
        </div>
        <div class="meta">
          {{ item.date }}
          <span class="separator">|</span>
          조회수 {{ item.views }}
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
// This component displays educational articles from the database. When an item is clicked
// it programmatically navigates to the EmergencyOrder view, passing the article id.
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
// 데이터 가져오기용 함수. 필요에 따라 axios 대신 fetch API를 사용할 수 있습니다.
// fetch는 브라우저에서 기본 제공되어 별도 설치가 필요 없습니다.

// 검색 키워드
const keyword = ref('')

// 데이터 배열. API에서 받아올 예정입니다.
const items = ref([])

const router = useRouter()

// 컴포넌트가 마운트될 때 DB에서 목록을 가져옵니다.
onMounted(async () => {
  try {
    // 예시 URL입니다. 실제로는 백엔드에서 제공하는 API 엔드포인트로 변경하세요.
    const response = await fetch('/api/edu-data')
    if (response.ok) {
      const data = await response.json()
      // 응답 형식이 배열이라고 가정합니다.
      items.value = data
    } else {
      console.error('교육자료 목록을 불러오는 데 실패했습니다:', response.statusText)
    }
  } catch (error) {
    console.error('교육자료 목록을 불러오는 중 오류 발생:', error)
  }
})

// 검색어에 따라 결과를 필터링합니다.
const filteredItems = computed(() => {
  const q = keyword.value.trim().toLowerCase()
  if (!q) return items.value
  return items.value.filter((item) => item.title.toLowerCase().includes(q))
})

// 목록 항목 클릭 시 응급 상세 페이지로 이동합니다.
function goToEmergency(id) {
  // EmergencyOrder 라우터 이름에 id를 파라미터로 전달
  router.push({ name: 'EmergencyOrder', params: { id } })
}
</script>

<style scoped>
/* 전체 페이지 레이아웃: 헤더/푸터 높이만큼 패딩을 주고 좌우 여백을 설정 */
.edu-page {
  padding-top: calc(var(--header-h, 56px) + 12px);
  padding-bottom: calc(var(--footer-h, 56px) + 12px);
  min-height: calc(100vh - var(--header-h, 56px) - var(--footer-h, 56px));
  background-color: #ffffff;
  padding-left: 16px;
  padding-right: 16px;
}

/* 탭 영역 */
.tabs {
  display: flex;
  justify-content: center;
  margin-bottom: 12px;
}
.tab {
  flex: 1 1 auto;
  text-align: center;
  font-size: 18px;
  font-weight: bold;
  padding: 8px 0;
  margin: 0 8px;
  border-bottom: 3px solid transparent;
  color: #888;
  cursor: default;
}
.tab.active {
  color: #00b894;
  border-bottom-color: #00b894;
}

/* 검색 영역 */
.search-wrapper {
  display: flex;
  align-items: center;
  background-color: #f5f5f5;
  border-radius: 8px;
  padding: 6px 10px;
  margin-bottom: 12px;
}
.search-wrapper input {
  flex: 1;
  border: none;
  outline: none;
  background: transparent;
  font-size: 16px;
  padding: 0;
}
.search-wrapper svg {
  flex-shrink: 0;
}

/* 결과 개수 표시 */
.count {
  color: #6c757d;
  font-size: 14px;
  margin-bottom: 16px;
}

/* 목록 영역 */
.list {
  background-color: #ffffff;
}
.item {
  border-top: 1px solid #e9ecef;
  cursor: pointer;
  padding: 12px 0;
}
.item:last-child {
  border-bottom: 1px solid #e9ecef;
}

/* 제목과 메타 정보 */
.title {
  font-size: 17px;
  font-weight: 600;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.meta {
  margin-top: 6px;
  font-size: 14px;
  color: #6c757d;
  display: flex;
  align-items: center;
}
.separator {
  margin: 0 8px;
}
</style>