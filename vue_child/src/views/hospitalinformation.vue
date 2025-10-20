<template>
  <div class="home-container">
    <!-- 지도 -->
    <div id="map_div"></div>
    <div class="map-overlay-tl">
  <button
    type="button"
    class="btn btn-light border rounded-pill px-3 shadow-sm"
    data-bs-toggle="modal"
    data-bs-target="#symptomModal"
  >
    <i class="bi bi-clipboard2-pulse me-1"></i> 증상 확인
  </button>

  <!-- 선택 칩 간단 요약 (선택된 항목이 있을 때만 표시) -->
  <div v-if="selectedSymptoms.length" class="mt-2 d-flex flex-wrap gap-1">
    <span
      v-for="sym in selectedSymptoms"
      :key="`chip-map-${sym}`"
      class="badge text-bg-primary rounded-pill"
    >
      {{ sym }}
      <button
        type="button"
        class="btn-close btn-close-white btn-sm ms-1"
        aria-label="삭제"
        @click="removeSymptom(sym)"
        style="transform: scale(.8); vertical-align: middle;"
      ></button>
    </span>
  </div>
</div>

    
    <!-- 하단 슬라이딩 시트 -->
    <div
      class="bottom-sheet"
      ref="bottomSheet"
      :style="sheetStyle"
      @touchstart="onTouchStart"
      @touchmove="onTouchMove"
      @touchend="onTouchEnd"
    >
      <div class="handle-wrapper" @click="toggleSheet">
        <div class="handle"></div>
      </div>

      <div class="sheet-content container">
        <!-- 상단 라인: [증상 확인 버튼] + [선택 칩] + [거리순] -->
        <div class="d-flex align-items-center justify-content-between gap-2 mb-3">
          <div class="d-flex align-items-center gap-2 flex-wrap">
            <!-- 증상 확인 버튼 -->
            <button
              type="button"
              class="btn btn-light border rounded-pill px-3"
              data-bs-toggle="modal"
              data-bs-target="#symptomModal"
            >
              <i class="bi bi-clipboard2-pulse me-1"></i> 증상 확인
            </button>

            <!-- 선택된 증상 칩 -->
            <div class="d-flex flex-wrap gap-1">
              <span
                v-for="sym in selectedSymptoms"
                :key="sym"
                class="badge text-bg-primary rounded-pill"
              >
                {{ sym }}
                <button
                  type="button"
                  class="btn-close btn-close-white btn-sm ms-1"
                  aria-label="삭제"
                  @click="removeSymptom(sym)"
                  style="transform: scale(.8); vertical-align: middle;"
                ></button>
              </span>
            </div>
          </div>

          <!-- 거리순 버튼 -->
          <button class="btn btn-sm btn-outline-secondary">
            <i class="bi bi-filter-left"></i> 거리순
          </button>
        </div>

        <!-- 타이틀 -->
        <h5 class="fw-bold mb-3">응급실 리스트</h5>

        <!-- 응급실 목록 (데모) -->
        <div class="card shadow-sm mb-3" v-for="i in 5" :key="i">
          <div class="card-body">
            <div class="d-flex justify-content-between align-items-start">
              <h6 class="card-title fw-bold">서울대학교병원</h6>
              <span class="badge bg-success-subtle text-success-emphasis rounded-pill">
                <i class="bi bi-door-open"></i> 2/7
              </span>
            </div>

            <!-- (예시) 병원 특이 사항 -->
            <div class="mb-2">
              <span class="badge bg-danger-subtle text-danger-emphasis me-1">장폐색x</span>
              <span class="badge bg-warning-subtle text-warning-emphasis">화상 진료 불가</span>
            </div>

            <div class="text-muted small d-flex align-items-center mb-3">
              <span><i class="bi bi-clock me-1"></i> 7분</span>
              <span class="mx-2">|</span>
              <span><i class="bi bi-geo-alt me-1"></i> 0.8km</span>
            </div>

            <div class="d-grid gap-2 d-sm-flex">
              <button class="btn btn-outline-primary w-100">
                <i class="bi bi-telephone-fill"></i> 전화
              </button>
              <button class="btn btn-primary w-100">
                <i class="bi bi-sign-turn-right-fill"></i> 길찾기
              </button>
            </div>
          </div>
        </div>
      </div> <!-- /sheet-content -->
    </div> <!-- /bottom-sheet -->

    <!-- 🔹 증상 선택 모달 -->
    <div class="modal fade" id="symptomModal" tabindex="-1" aria-labelledby="symptomModalLabel" aria-hidden="true">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content border-0 shadow">
          <div class="modal-header">
            <h5 class="modal-title" id="symptomModalLabel">
              <i class="bi bi-clipboard2-pulse me-1"></i> 증상 확인
            </h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="닫기"></button>
          </div>

          <div class="modal-body">
            <!-- 토글 체크버튼으로 증상 선택 -->
            <div class="d-flex flex-wrap gap-2">
              <template v-for="opt in symptomOptions" :key="opt">
                <input
                  class="btn-check"
                  type="checkbox"
                  :id="`sym-${opt}`"
                  :value="opt"
                  v-model="selectedSymptoms"
                />
                <label
                  class="btn btn-outline-secondary rounded-pill py-1 px-3"
                  :for="`sym-${opt}`"
                >
                  {{ opt }}
                </label>
              </template>
            </div>
          </div>

          <div class="modal-footer">
            <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">닫기</button>
            <button type="button" class="btn btn-primary" data-bs-dismiss="modal">확인</button>
          </div>
        </div>
      </div>
    </div>
    <!-- /증상 선택 모달 -->
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'

/* -------------------- 증상 선택 상태 -------------------- */
const symptomOptions = [
  '발열', '감기', '기침', '호흡곤란', '구토', '설사',
  '복통', '발진', '화상', '경련'
]
const selectedSymptoms = ref([])

function removeSymptom(sym) {
  selectedSymptoms.value = selectedSymptoms.value.filter(s => s !== sym)
}

/* -------------------- T-map 상태 -------------------- */
const map = ref(null)

/* -------------------- 바텀시트 드래그 -------------------- */
const bottomSheet = ref(null)
const isDragging = ref(false)
const sheetHeightRatio = ref(0) // 0: 닫힘, 1: 열림
const dragStart = ref({ y: 0, ratio: 0 })

const MAX_SHEET_HEIGHT = window.innerHeight * 0.8
const MIN_SHEET_HEIGHT = 100

const sheetY = computed(() => {
  return (MAX_SHEET_HEIGHT - MIN_SHEET_HEIGHT) * (1 - sheetHeightRatio.value)
})

const sheetStyle = computed(() => ({
  transition: isDragging.value ? 'none' : 'transform 0.3s ease-out',
  transform: `translateY(${sheetY.value}px)`,
  height: `${MAX_SHEET_HEIGHT}px`,
  bottom: `0px`,
  top: 'auto'
}))

/* -------------------- 지도 초기화 -------------------- */
const initTmap = () => {
  if (!document.getElementById('map_div')) return

  map.value = new window.Tmapv3.Map('map_div', {
    center: new window.Tmapv3.LatLng(37.56259379, 126.99243652),
    width: '100%',
    height: '100%',
    zoom: 16
  })

  if (navigator.geolocation) {
    navigator.geolocation.getCurrentPosition(
      pos => {
        const lat = pos.coords.latitude
        const lon = pos.coords.longitude
        const currentPos = new window.Tmapv3.LatLng(lat, lon)

        new window.Tmapv3.Marker({ position: currentPos, map: map.value })

        const content = `<div style='padding:5px;background:#fff;border:1px solid #ccc;border-radius:5px;'>현재 위치</div>`
        new window.Tmapv3.InfoWindow({
          position: currentPos,
          content,
          offset: new window.Tmapv3.Point(0, -30),
          type: 2,
          map: map.value
        })

        map.value.setCenter(currentPos)
        map.value.setZoom(17)
      },
      err => {
        console.error('Geolocation 오류:', err)
        alert('현재 위치를 가져올 수 없습니다.')
      }
    )
  } else {
    alert('이 브라우저에서는 위치 정보를 지원하지 않습니다.')
  }
}

/* -------------------- 바텀시트 터치 -------------------- */
const onTouchStart = e => {
  isDragging.value = true
  dragStart.value = { y: e.touches[0].clientY, ratio: sheetHeightRatio.value }
  e.preventDefault()
}

const onTouchMove = e => {
  if (!isDragging.value) return
  const deltaY = e.touches[0].clientY - dragStart.value.y
  const deltaRatio = -deltaY / (MAX_SHEET_HEIGHT - MIN_SHEET_HEIGHT)
  let newRatio = dragStart.value.ratio + deltaRatio
  if (newRatio < 0) newRatio = 0
  if (newRatio > 1) newRatio = 1
  sheetHeightRatio.value = newRatio
}

const onTouchEnd = () => {
  isDragging.value = false
  sheetHeightRatio.value = sheetHeightRatio.value > 0.5 ? 1 : 0
}

const toggleSheet = () => {
  sheetHeightRatio.value = sheetHeightRatio.value > 0.5 ? 0 : 1
}

/* -------------------- 외부 스크립트 로더 -------------------- */
function loadTmapScript () {
  return new Promise((resolve, reject) => {
    if (window.Tmapv3) return resolve()

    const exist = document.querySelector('script[data-tmap="v3"]')
    if (exist) {
      exist.addEventListener('load', () => resolve())
      exist.addEventListener('error', e => reject(e))
      return
    }

    const script = document.createElement('script')
    script.src = 'https://apis.openapi.sk.com/tmap/vectorjs?version=1&appKey=pAs3duhDpE5cYgopfzzDmw8anJPaBak8HAdTerg8'
    script.dataset.tmap = 'v3'
    script.onload = () => resolve()
    script.onerror = e => reject(e)
    document.head.appendChild(script)
  })
}

function waitTmapReady (retry = 10) {
  return new Promise((resolve, reject) => {
    const tick = () => {
      if (window.Tmapv3 && typeof window.Tmapv3.Map === 'function') return resolve()
      if (retry-- <= 0) return reject(new Error('Tmapv3 초기화 지연'))
      setTimeout(tick, 100)
    }
    tick()
  })
}

/* -------------------- 생명주기 -------------------- */
onMounted(() => {
  sheetHeightRatio.value = 0 // 닫힘
  loadTmapScript()
    .then(waitTmapReady)
    .then(initTmap)
    .catch(e => {
      console.error('Tmap 로딩 실패:', e)
      alert('지도를 불러오지 못했습니다. 네트워크 상태를 확인해주세요.')
    })
})

onBeforeUnmount(() => {
  if (map.value) map.value.destroy()
})
</script>

<style scoped>
.map-overlay-tl {
  position: absolute;
  top: 12px;
  left: 12px;
  z-index: 1100;             /* 지도(타일/마커) 위에 오도록 */
  pointer-events: none;      /* 기본은 클릭 통과 */
}

/* 내부 요소는 클릭 가능하도록 다시 활성화 */
.map-overlay-tl > * {
  pointer-events: auto;
}

/* 작은 화면에서 버튼이 너무 커 보이지 않게 */
@media (max-width: 400px) {
  .map-overlay-tl .btn {
    padding: 4px 10px;
    font-size: 0.9rem;
  }
}
.home-container {
  position: relative;
  /* App.vue의 헤더, 푸터를 고려한 전체 높이 설정 */
  height: calc(100vh - 120px);
  width: 100%;
  overflow: hidden;
}

#map_div {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
}

.bottom-sheet {
  position: fixed;
  left: 0;
  right: 0;
  background-color: #ffffff;
  border-top-left-radius: 20px;
  border-top-right-radius: 20px;
  /* ⬇ 오탈자 수정 (R20px → 20px) */
  box-shadow: 0 -4px 20px rgba(0, 0, 0, 0.1);
  z-index: 1000;
}

.handle-wrapper {
  padding: 15px;
  cursor: grab;
  display: flex;
  justify-content: center;
}

.handle {
  width: 40px;
  height: 5px;
  background-color: #dee2e6;
  border-radius: 2.5px;
}

.sheet-content {
  overflow-y: auto;
  height: calc(100% - 50px); /* 핸들 높이 제외 */
  padding-bottom: 20px;
}

/* 모달에서 버튼 라벨 줄바꿈 방지 */
#symptomModal .btn-outline-secondary {
  white-space: nowrap;
}
</style>
