<!-- src/views/EmergencyFirstAid.vue -->
<template>
  <div class="d-flex justify-content-center align-items-center  flex-column ">

    <!-- 상단 카테고리 + 칩 (상단 고정) -->
    <div class="d-flex flex-column align-items-center sticky-top bg-white border-bottom w-100">
      <!-- 카테고리 아이콘 라인 -->
      <div class="container-fluid ">
        <div class="d-flex align-items-center justify-content-between py-2" >
          <button class="btn btn-link text-body-tertiary px-0"><i class="bi bi-chevron-left"></i></button>

          <div class="d-flex gap-3 text-center">
            <div
              v-for="c in iconCats"
              :key="c.label"
              class="d-flex flex-column align-items-center"
              style="min-width:48px;"

               @click="selectTag(c.label)"
            >
              <div class="d-flex align-items-center justify-content-center rounded-circle border"
                   style="width:40px;height:40px;transition: all .2s; cursor: pointer;">
                <i :class="c.icon"></i>
              </div>
              <small class="text-muted mt-1 text-bold">{{ c.label }}</small>
            </div>
          </div>

          <button class="btn btn-link text-body-tertiary px-0"><i class="bi bi-chevron-right"></i></button>
        </div>
      </div>

     
    </div>

    <!-- 리스트 (남은 높이 채우고 스크롤) -->
    <section class="container-fluid py-2 flex-grow-1 overflow-auto">
      <article
        v-for="v in items"
        :key="v.id"
        class="card border-0 border-bottom rounded-0"
      >
        <div class="card-body p-0">

          <!-- 썸네일 16:9 (부트스트랩 ratio) -->
          <div class="position-relative ratio ratio-16x9">
            <img :src="v.thumbnail" :alt="v.title" class="w-100 h-100 object-fit-cover">
           
          </div>

          <!-- 메타 -->
          <div class="p-2 d-flex gap-2 align-items-start">
            <div class="rounded-circle bg-secondary-subtle flex-shrink-0" style="width:36px;height:36px;"></div>
            <div class="flex-grow-1" >
              <h6 class="mb-1 fw-semibold text-body text-wrap text-bold" >{{ v.title }}</h6>
              <div class="text-muted small text-light">
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

      <!-- 무한 스크롤 센티넬 -->
      <div ref="observerTarget" style="height:1px;"></div>
    </section>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'

/* 아이콘 카테고리 예시 */
const iconCats = [
  { label: '화상', icon: 'bi bi-fire' },
  { label: '감기', icon: 'bi bi-emoji-frown' },
  { label: '복통', icon: 'bi bi-emoji-tear' },
  { label: '발열', icon: 'bi bi-thermometer-sun' },
  { label: '구토', icon: 'bi bi-emoji-dizzy' },
  { label: '경련', icon: 'bi bi-activity' },
]

const tags = ['화상', '감기', '복통', '발열', '구토', '경련', '탈수', '골절']
const selectedTag = ref('화상')

const keyword = ref('')
const showSearch = ref(false)
function selectTag(t){ selectedTag.value = t; resetAndLoad() }
function toggleSearch(){ showSearch.value = !showSearch.value }
function applyFilter(){ resetAndLoad() }

/* 유튜브형 리스트 상태 */
const useYouTubeAPI = !!import.meta.env.VITE_YT_API_KEY
const YT_KEY = import.meta.env.VITE_YT_API_KEY
const items = ref([])
const nextPageToken = ref(null)
const loading = ref(false)
const finished = ref(false)
const observerTarget = ref(null)

const q = computed(() => (keyword.value || selectedTag.value || '응급처치'))

async function loadYouTube() {
  if (loading.value || finished.value) return
  loading.value = true
  try {
    const params = new URLSearchParams({
      key: YT_KEY, part: 'snippet', maxResults: '10', type: 'video',
      q: q.value, pageToken: nextPageToken.value || ''
    })
    const res = await fetch(`https://www.googleapis.com/youtube/v3/search?${params}`)
    const data = await res.json()

    nextPageToken.value = data.nextPageToken || null
    if (!data.items?.length) finished.value = true

    items.value.push(...data.items.map(it => ({
      id: it.id.videoId,
      title: it.snippet.title,
      channel: it.snippet.channelTitle,
      age: new Date(it.snippet.publishedAt).toLocaleDateString(),
      views: '조회수 정보(샘플)',
      thumbnail: it.snippet.thumbnails?.medium?.url || it.snippet.thumbnails?.default?.url
    })))

    if (!nextPageToken.value) finished.value = true
  } catch (e) {
    console.error(e)
    if (items.value.length === 0) {
      items.value = sampleVideos()
      finished.value = true
    }
  } finally {
    loading.value = false
  }
}

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
  loadMore()
}

function mountObserver() {
  const io = new IntersectionObserver((entries) => {
    if (entries.some(e => e.isIntersecting)) loadMore()
  }, { rootMargin: '600px 0px' })
  if (observerTarget.value) io.observe(observerTarget.value)
}

function loadMore() {
  if (useYouTubeAPI) return loadYouTube()
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

<style>
.rounded-circle:hover {
  background-color: #e9ecef; /* 연한 회색 */
  transform: scale(1.1);
  transition: all 0.2s ease;
}



</style>
