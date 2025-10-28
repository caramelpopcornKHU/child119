<template>
  <div class="d-flex justify-content-center align-items-center position-relative mt-1"
    style="height: calc(100vh - 140px); ">
    <!-- 지도 -->
    <div id="map_div" class="position-fixed top-0   start-0 w-100">


      <!-- 좌상단 오버레이 -->
      <div class="position-fixed start-0 p-3" style="top: calc(var(--header-h) + 8px); z-index: 2;">
        <button type="button" class="btn btn-light border rounded-pill px-3 shadow-sm me-0 pe-auto text-bold"
          data-bs-toggle="modal" data-bs-target="#symptomModal">
          <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24">
            <path fill="currentColor"
              d="M19 8c.56 0 1 .43 1 1a1 1 0 0 1-1 1c-.57 0-1-.45-1-1c0-.57.43-1 1-1M2 2v9c0 2.96 2.19 5.5 5.14 5.91c.62 3.01 3.28 5.09 6.36 5.09a6.5 6.5 0 0 0 6.5-6.5v-3.69c1.16-.42 2-1.52 2-2.81a3 3 0 0 0-3-3a3 3 0 0 0-3 3c0 1.29.84 2.4 2 2.81v3.6c0 2.5-2 4.5-4.5 4.5c-2 0-3.68-1.21-4.28-3.01C12 16.3 14 13.8 14 11V2h-4v3h2v6a4 4 0 0 1-4 4a4 4 0 0 1-4-4V5h2V2z" />
          </svg> 증상 확인
        </button>

        <div v-if="selectedSymptoms.length" class="mt-2 d-flex flex-wrap gap-1 pe-auto">
          <span v-for="sym in selectedSymptoms" :key="`chip-map-${sym}`"
            class="badge text-bg-primary rounded-pill d-inline-flex align-items-center">
            {{ sym }}
            <button type="button" class="btn-close btn-close-white btn-sm ms-1" aria-label="삭제"
              @click="removeSymptom(sym)" style="transform: scale(.8); vertical-align: middle;"></button>
          </span>
        </div>
      </div>
    </div>
    <!-- 하단 슬라이딩 시트 -->
    <div class="position-fixed start-0 end-0 bg-white rounded-top shadow z-2" ref="bottomSheet" :style="sheetStyle"
      @touchstart="onTouchStart" @touchmove="onTouchMove" @touchend="onTouchEnd" style="z-index: 2;">
      <!-- 핸들 -->
      <div class="py-3 d-flex justify-content-center" @click="toggleSheet">
        <!-- 너비/높이는 유틸로 근사 (w-25, py-1) -->
        <div class="bg-secondary-subtle rounded-pill w-25" style="height:5px;"></div>
      </div>

      <div class="container overflow-auto" style="height: calc(100% - 50px); padding-bottom: 20px;">
        <!-- 상단 라인 -->
        <div class="d-flex align-items-center justify-content-between gap-2 mb-3">
          <div class="d-flex align-items-center gap-2 flex-wrap">
            <button type="button" class="btn btn-light border rounded-pill px-3" data-bs-toggle="modal"
              data-bs-target="#symptomModal">
              <i class="bi bi-clipboard2-pulse me-1"></i> 증상 확인
            </button>

            <div class="d-flex flex-wrap gap-1">
              <span v-for="sym in selectedSymptoms" :key="sym"
                class="badge text-bg-primary rounded-pill d-inline-flex align-items-center">
                {{ sym }}
                <button type="button" class="btn-close btn-close-white btn-sm ms-1" aria-label="삭제"
                  @click="removeSymptom(sym)" style="transform: scale(.8); vertical-align: middle;"></button>
              </span>
            </div>
          </div>

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
              <h6 class="card-title fw-bold mb-1">서울대학교병원</h6>
              <span class="badge bg-success-subtle text-success-emphasis rounded-pill">
                <i class="bi bi-door-open"></i> 2/7
              </span>
            </div>

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
      </div> <!-- /container -->
    </div> <!-- /bottom-sheet -->

    <!-- 증상 선택 모달 -->
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
            <div class="d-flex flex-wrap gap-2">
              <template v-for="opt in symptomOptions" :key="opt">
                <input class="btn-check" type="checkbox" :id="`sym-${opt}`" :value="opt" v-model="selectedSymptoms" />
                <label class="btn btn-outline-secondary rounded-pill py-1 px-3" :for="`sym-${opt}`">
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

/* 증상 선택 상태 */
const symptomOptions = ['발열', '감기', '기침', '호흡곤란', '구토', '설사', '복통', '발진', '화상', '경련']
const selectedSymptoms = ref([])
function removeSymptom(sym) { selectedSymptoms.value = selectedSymptoms.value.filter(s => s !== sym) }

/* 지도 상태 */
const map = ref(null)

/* 바텀시트 드래그 */
const bottomSheet = ref(null)
const isDragging = ref(false)
const sheetHeightRatio = ref(0)
const dragStart = ref({ y: 0, ratio: 0 })

const MAX_SHEET_HEIGHT = window.innerHeight * 0.8
const MIN_SHEET_HEIGHT = 210

const sheetY = computed(() =>
  (MAX_SHEET_HEIGHT - MIN_SHEET_HEIGHT) * (1 - sheetHeightRatio.value)
)

const sheetStyle = computed(() => ({
  transition: isDragging.value ? 'none' : 'transform 0.3s ease-out',
  transform: `translateY(${sheetY.value}px)`,
  height: `${MAX_SHEET_HEIGHT}px`,
  bottom: `0px`,
  top: 'auto'
}))

/* 지도 초기화 */
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
          position: currentPos, content,
          offset: new window.Tmapv3.Point(0, -30), type: 2, map: map.value
        })
        map.value.setCenter(currentPos)
        map.value.setZoom(17)
      },
      err => { console.error('Geolocation 오류:', err); alert('현재 위치를 가져올 수 없습니다.') }
    )
  } else {
    alert('이 브라우저에서는 위치 정보를 지원하지 않습니다.')
  }
}

/* 터치 핸들러 */
const onTouchStart = e => {
  isDragging.value = true
  dragStart.value = { y: e.touches[0].clientY, ratio: sheetHeightRatio.value }
}
const onTouchMove = e => {
  if (!isDragging.value) return
  const deltaY = e.touches[0].clientY - dragStart.value.y
  const deltaRatio = -deltaY / (MAX_SHEET_HEIGHT - MIN_SHEET_HEIGHT)
  let newRatio = dragStart.value.ratio + deltaRatio
  sheetHeightRatio.value = Math.max(0, Math.min(1, newRatio))
}
const onTouchEnd = () => {
  isDragging.value = false
  sheetHeightRatio.value = sheetHeightRatio.value > 0.5 ? 1 : 0
}
const toggleSheet = () => { sheetHeightRatio.value = sheetHeightRatio.value > 0.5 ? 0 : 1 }

/* 외부 스크립트 로더 */
function loadTmapScript() {
  return new Promise((resolve, reject) => {
    if (window.Tmapv3) return resolve()
    const exist = document.querySelector('script[data-tmap="v3"]')
    if (exist) { exist.addEventListener('load', () => resolve()); exist.addEventListener('error', e => reject(e)); return }
    const script = document.createElement('script')
    script.src = 'https://apis.openapi.sk.com/tmap/vectorjs?version=1&appKey=pAs3duhDpE5cYgopfzzDmw8anJPaBak8HAdTerg8'
    script.dataset.tmap = 'v3'
    script.onload = () => resolve()
    script.onerror = e => reject(e)
    document.head.appendChild(script)
  })
}
function waitTmapReady(retry = 10) {
  return new Promise((resolve, reject) => {
    const tick = () => {
      if (window.Tmapv3 && typeof window.Tmapv3.Map === 'function') return resolve()
      if (retry-- <= 0) return reject(new Error('Tmapv3 초기화 지연'))
      setTimeout(tick, 100)
    }
    tick()
  })
}

/* 생명주기 */
onMounted(() => {
  sheetHeightRatio.value = 0
  loadTmapScript().then(waitTmapReady).then(initTmap).catch(e => {
    console.error('Tmap 로딩 실패:', e)
    alert('지도를 불러오지 못했습니다. 네트워크 상태를 확인해주세요.')
  })
})
onBeforeUnmount(() => { if (map.value) map.value.destroy() })
</script>

<!-- 커스텀 CSS 없음: Bootstrap/Icons 유틸만 사용 -->
