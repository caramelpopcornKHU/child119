<!-- src/views/EmergencyFirstAid.vue -->
<template>
  <main class="ef-container">
    
    <!-- 상단 카테고리 라인 (아이콘 카테고리) -->
    <div class="ef-top">
    <div class="ef-cat-icons border-bottom">
      <div class="container">
        <div class="d-flex align-items-center justify-content-between py-2">
          <button class="btn btn-link text-body-tertiary px-0"><i class="bi bi-chevron-left"></i></button>

          <div class="d-flex gap-3 text-center">
            <div class="ef-cat-item" v-for="c in iconCats" :key="c.label">
              <div class="ef-cat-circle d-flex align-items-center justify-content-center mx-auto mb-1">
                <i :class="c.icon"></i>
              </div>
              <small class="text-muted">{{ c.label }}</small>
            </div>
          </div>

          <button class="btn btn-link text-body-tertiary px-0"><i class="bi bi-chevron-right"></i></button>
        </div>
      </div>
    </div>

    <!-- 증상 칩 스와이프/가로 스크롤 -->
    <div class="ef-chips border-bottom bg-white">
      <div class="container py-2">
        <div class="d-flex align-items-center gap-2 overflow-auto no-scrollbar">
          <button
            v-for="t in tags"
            :key="t"
            class="btn btn-light border rounded-pill px-3 py-1"
            :class="{'active-chip': selectedTag === t}"
            @click="selectTag(t)"
          >
            {{ t }}
          </button>
        </div>
      </div>
    </div>
    </div>

    <!-- 리스트 -->
   <!-- 유튜브형 리스트 -->
<section class="container py-2 ef-list">
  <article v-for="v in items" :key="v.id" class="card border-0 border-bottom rounded-0">
    <div class="card-body p-0">
      <!-- 썸네일 16:9 -->
      <div class="position-relative">
        <img :src="v.thumbnail" :alt="v.title" class="w-100 ef-thumb" />
        <!-- 길이 오버레이(샘플) -->
        <span class="badge bg-dark opacity-75 position-absolute bottom-0 end-0 m-2">5:14</span>
      </div>

      <!-- 메타 -->
      <div class="p-2 d-flex gap-2">
        <div class="rounded-circle bg-secondary-subtle" style="width:36px;height:36px;"></div>
        <div class="flex-grow-1">
          <h6 class="mb-1 fw-semibold text-body two-line">{{ v.title }}</h6>
          <div class="text-muted small">
            {{ v.channel }} · {{ v.views }} · {{ v.age }}
          </div>
        </div>
        <button class="btn btn-link text-body-tertiary px-1"><i class="bi bi-three-dots-vertical"></i></button>
      </div>
    </div>
  </article>

  <!-- 로딩/끝 표시 -->
  <div class="text-center py-3" v-if="loading">
    <div class="spinner-border" role="status" style="width:1.8rem;height:1.8rem;"></div>
  </div>
  <div class="text-center text-muted py-3" v-else-if="finished">끝까지 불러왔습니다</div>

  <!-- 무한 스크롤 관찰용 센티넬 -->
  <div ref="observerTarget" style="height:1px;"></div>
</section>

    <!-- 하단 탭바 -->
    
  </main>
</template>
<script setup>
import { ref, computed, onMounted } from 'vue'

const appIcon = '/favicon.ico'

// 카테고리/칩은 기존 그대로

const tags = ['화상', '감기', '복통', '발열', '구토', '경련', '탈수', '골절']
const selectedTag = ref('화상')

const keyword = ref('')
const showSearch = ref(false)
function selectTag(t){ selectedTag.value = t; resetAndLoad() }
function toggleSearch(){ showSearch.value = !showSearch.value }
function applyFilter(){ resetAndLoad() }

/* ------------------ 유튜브형 리스트 상태 ------------------ */
const useYouTubeAPI = !!import.meta.env.VITE_YT_API_KEY
const YT_KEY = import.meta.env.VITE_YT_API_KEY
const items = ref([])            // 화면에 뿌릴 영상 목록
const nextPageToken = ref(null)  // 유튜브 페이징 토큰
const loading = ref(false)
const finished = ref(false)
const observerTarget = ref(null) // 무한 스크롤 관찰점

// 유튜브 검색 쿼리 만들기
const q = computed(() => (keyword.value || selectedTag.value || '응급처치'))

// 유튜브 API로 로드
async function loadYouTube() {
  if (loading.value || finished.value) return
  loading.value = true
  try {
    const params = new URLSearchParams({
      key: YT_KEY,
      part: 'snippet',
      maxResults: '10',
      type: 'video',
      q: q.value,
      pageToken: nextPageToken.value || ''
    })
    const res = await fetch(`https://www.googleapis.com/youtube/v3/search?${params}`)
    const data = await res.json()

    nextPageToken.value = data.nextPageToken || null
    if (!data.items?.length) finished.value = true

    // 카드에 필요한 필드로 매핑
    items.value.push(
      ...data.items.map((it) => ({
        id: it.id.videoId,
        title: it.snippet.title,
        channel: it.snippet.channelTitle,
        age: new Date(it.snippet.publishedAt).toLocaleDateString(),
        views: '조회수 정보(샘플)',
        thumbnail: it.snippet.thumbnails?.medium?.url || it.snippet.thumbnails?.default?.url
      }))
    )

    // nextPageToken 없으면 마지막
    if (!nextPageToken.value) finished.value = true
  } catch (e) {
    console.error(e)
    // 실패 시 목데이터 한 번만 제공
    if (items.value.length === 0) {
      items.value = sampleVideos()
      finished.value = true
    }
  } finally {
    loading.value = false
  }
}

// API키 없거나 실패 시 사용할 목데이터
function sampleVideos() {
  return Array.from({ length: 20 }).map((_, i) => ({
    id: `mock-${i}`,
    title: `응급처치 샘플 영상 ${i + 1}`,
    channel: '아이119',
    views: `${(i + 1) * 3}만회`,
    age: '1년 전',
    thumbnail: 'https://i.ytimg.com/vi/2Vv-BfVoq4g/hqdefault.jpg'
  }))
}

function resetAndLoad() {
  items.value = []
  nextPageToken.value = null
  finished.value = false
  loadMore() // 첫 페이지 로드
}

// IntersectionObserver로 무한 스크롤
function mountObserver() {
  const io = new IntersectionObserver((entries) => {
    if (entries.some(e => e.isIntersecting)) loadMore()
  }, { rootMargin: '600px 0px' })
  if (observerTarget.value) io.observe(observerTarget.value)
}

function loadMore() {
  if (useYouTubeAPI) return loadYouTube()
  // API 미사용: 목데이터 한 번만
  if (!loading.value && items.value.length === 0) {
    items.value = sampleVideos()
    finished.value = true
  }
}

onMounted(() => {
  resetAndLoad()
  mountObserver()
})
</script>



<style scoped>
/* 전체 컨테이너: 모바일 기준 높이 */
.ef-container {
  height: 100vh;                 /* 화면 높이 고정 */
  display: flex;
  flex-direction: column;
  background: #f8f9fa;
  font-family: Pretendard, system-ui, -apple-system, Segoe UI, Roboto, 'Helvetica Neue', Arial, 'Noto Sans KR', 'Apple SD Gothic Neo', 'Malgun Gothic', sans-serif;
}

/* 헤더 */
.ef-header { position: sticky; top: 0; z-index: 1050; }

/* 아이콘 카테고리 줄 */
.ef-cat-icons,
.ef-chips {
  flex: 0 0 auto;                /* 내용만큼 높이 차지 */
  background: #fff;
  z-index: 1040;
}

/* 칩 라인 */
.ef-chips { position: sticky; top: 100px; z-index: 1030; }
.no-scrollbar::-webkit-scrollbar { display: none; }
.active-chip { background: #e7f1ff !important; color: #0d6efd; border-color: #cfe2ff !important; }

/* 리스트 썸네일 비율 유지 */
.ef-thumb { object-fit: cover; aspect-ratio: 16/9; }

/* 하단 탭바 */
.ef-tabbar {
  position: sticky; bottom: 0; z-index: 1050;
}

/* 폭 제한 (선택) */
@media (min-width: 480px) {
  .ef-container > * .container { max-width: 420px; }
}
/* 썸네일 16:9 비율 */
.ef-thumb { aspect-ratio: 16 / 9; object-fit: cover; display: block; }

/* 제목 2줄 말줄임 */
.two-line {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.ef-list {
  flex: 1 1 0;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch; /* iOS 부드러운 스크롤 */
}
.ef-top {
  position: sticky;
  top: 60px;          /* App.vue에 자체 헤더가 있으면 그 높이로 조정(예: top: 56px) */
  z-index: 1040;
  background: #fff;
}
</style>
