<template>
  <div class="d-flex justify-content-center align-items-center position-relative mt-1"
    style="height: calc(100vh - 140px); ">

    <!-- Vworld - vw.ol3.Map 지도 -->
    <div id="vmap" class="position-fixed top-0 start-0 w-100" style="height:100dvh; z-index:0; pointer-events:auto;">
    </div>

    <!-- 증상확인 -->
    <div class="position-fixed start-0 p-3" style="top: calc(var(--header-h) + 8px); z-index: 2;">
      <div class="pe-auto">
        <button type="button" class="btn btn-light border rounded-pill px-3 shadow-sm text-bold" data-bs-toggle="modal"
          data-bs-target="#symptomModal">
          <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24">
            <path fill="currentColor"
              d="M19 8c.56 0 1 .43 1 1a1 1 0 0 1-1 1c-.57 0-1-.45-1-1c0-.57.43-1 1-1M2 2v9c0 2.96 2.19 5.5 5.14 5.91c.62 3.01 3.28 5.09 6.36 5.09a6.5 6.5 0 0 0 6.5-6.5v-3.69c1.16-.42 2-1.52 2-2.81a3 3 0 0 0-3-3a3 3 0 0 0-3 3c0 1.29.84 2.4 2 2.81v3.6c0 2.5-2 4.5-4.5 4.5c-2 0-3.68-1.21-4.28-3.01C12 16.3 14 13.8 14 11V2h-4v3h2v6a4 4 0 0 1-4 4a4 4 0 0 1-4-4V5h2V2z" />
          </svg> 증상 확인
        </button>
      </div>
    </div>
  </div>
  <!-- 바텀시트 -->
  <div class="position-fixed start-0 end-0 bg-white padding-bottom  rounded-top-4 shadow-lg " :style="sheetStyle"
    ref="bottomSheet" @touchstart="onTouchStart" @touchmove="onTouchMove" @touchend="onTouchEnd"
    style="z-index: 2; padding-bottom: 150px;">

    <!-- 그립 -->
    <div class="py-3 d-flex justify-content-center" @click="toggleSheet" style="cursor: grab;">
      <div class="bg-secondary-subtle rounded-pill w-25" style="height:5px;"></div>
    </div>
    <!-- 핸들 -->
    <div class="py-3 d-flex justify-content-center" @click="toggleSheet"></div>

    <!-- 내용 -->
    <div class="d-flex flex-column" style="height: 100%;">
      <!-- 상단 라인 -->
      <div class="d-flex align-items-center justify-content-between gap-2 mb-3 ms-2 me-2">
        <div class="d-flex align-items-center gap-2 flex-wrap">
          <div class="d-flex flex-wrap gap-1" v-if="selectedSymptoms.length">
            <span v-for="sym in selectedSymptoms" :key="sym"
              class="badge text-bg-primary rounded-pill d-inline-flex align-items-center">
              {{ sym }}
              <button type="button" class="btn-close btn-close-white btn-sm ms-1" aria-label="삭제"
                @click="removeSymptom(sym)" style="transform: scale(.8);"></button>
            </span>
          </div>
        </div>
      </div>

      <!-- 타이틀 -->
      <div class="d-flex justify-content-between align-items-center mb-3 ms-2 me-2">
        <!-- 왼쪽 -->
        <div class="d-flex align-items-center gap-2">
          <h5 class="fw-bold mb-0">응급실 리스트</h5>

          <span v-if="hospitals.length > 0" class="badge bg-white text-black">{{ hospitals.length }}개</span>
        </div>

        <!-- 오른쪽: 버튼 그룹 -->
        <div class="d-flex gap-2">
          <button type="button" class="btn btn-light border rounded-pill px-3 text-bold" @click="sortByDistance">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 48 48">
              <path fill="#525252" stroke="#525252" stroke-linejoin="round" stroke-width="4"
                d="m24 42l-9-13h18zm0-36l-9 13h18z" />
            </svg>
            <span class="fw-bold">거리순</span>
          </button>

          <button type="button" class="btn btn-light border rounded-pill px-3 text-bold" @click="sortByTime">
            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24">
              <path fill="currentColor"
                d="M12 20a8 8 0 0 0 8-8a8 8 0 0 0-8-8a8 8 0 0 0-8 8a8 8 0 0 0 8 8m0-18a10 10 0 0 1 10 10a10 10 0 0 1-10 10C6.47 22 2 17.5 2 12A10 10 0 0 1 12 2m.5 5v5.25l4.5 2.67l-.75 1.23L11 13V7z" />
            </svg>
            시간순
          </button>
        </div>
      </div>

      <!-- 리스트 스크롤 영역 -->
      <div class="overflow-auto px-3 ">
        <!-- 응급실 목록 -->
        <div class="card shadow-sm mb-3" v-for="h in listHospitals" :key="h.id">
          <div class="card-body" @click="goToemergencyDetail(h.id)">
            <div class="d-flex justify-content-between align-items-start">
              <h6 class="card-title fw-bold mb-1">{{ h.name }}</h6>
              <span class="badge bg-success-subtle text-success-emphasis rounded-pill">
                <svg xmlns="http://www.w3.org/2000/svg" width="22px" height="15px" viewBox="0 0 24 24">
                  <path fill="#008A64"
                    d="M19 7h-8v7H3V5H1v15h2v-3h18v3h2v-9a4 4 0 0 0-4-4M7 13a3 3 0 0 0 3-3a3 3 0 0 0-3-3a3 3 0 0 0-3 3a3 3 0 0 0 3 3" />
                </svg> 2/7
              </span>
            </div>

            <div class="mb-2">
              <span class="badge bg-danger-subtle text-danger-emphasis me-1">장폐색x</span>
              <span class="badge bg-warning-subtle text-warning-emphasis">화상 진료 불가</span>
            </div>

            <div class="text-muted small d-flex align-items-center mb-3">
              <span><i class="bi bi-clock me-1"></i> {{ h.etaMin }}분</span>
              <span class="mx-2">|</span>
              <span><i class="bi bi-geo-alt me-1"></i> {{ h.distanceKm.toFixed(1) }}km</span>
            </div>

            <div class="d-grid gap-2 d-sm-flex">
              <button class="btn btn-outline-primary w-100">
                <i class="bi bi-telephone-fill"></i> 전화
              </button>
              <button class="btn btn-primary w-100" @click="openRouteModal(hospital)">
                <i class="bi bi-sign-turn-right-fill"></i> 길찾기
              </button>
            </div>
          </div>
        </div>

        <div v-if="loadingHospitals" class="text-center text-muted py-4">
          <div class="spinner-border spinner-border-sm me-2" role="status">
            <span class="visually-hidden">검색 중...</span>
          </div>
          <div class="mt-2">주변 응급실 검색 중...</div>
        </div>

        <div v-else-if="!loadingHospitals && hospitals.length === 0 && searchAttempted"
          class="text-center text-muted py-4">
          <i class="bi bi-search fs-1 d-block mb-2"></i>
          <p class="mb-1">반경 {{ (lastRadius / 1000).toFixed(0) }}km 내 검색 결과가 없습니다.</p>
          <small class="text-muted">증상을 변경하거나 다시 시도해주세요.</small>
        </div>

        <div v-else-if="!searchAttempted" class="text-center text-muted py-4">
          <i class="bi bi-clipboard2-pulse fs-1 d-block mb-2"></i>
          <p>증상을 선택하고 검색을 실행해 주세요.</p>
        </div>
      </div>
    </div>
  </div>

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
          <div class="alert alert-light border py-2 px-3 mb-3">
            <div class="small text-muted mb-1">
              기본 키워드 <strong>"소아응급실"</strong>에 선택한 증상이 붙어 검색됩니다.
            </div>
            <div class="d-flex align-items-center gap-2">
              <i class="bi bi-search"></i>

            </div>
          </div>

          <div class="d-flex flex-wrap gap-2">
            <template v-for="opt in symptomOptions" :key="opt">
              <input class="btn-check" type="checkbox" :id="`sym-${opt}`" :value="opt" v-model="selectedSymptoms" />
              <label class="btn btn-outline-secondary rounded-pill py-1 px-3" :for="`sym-${opt}`">{{ opt }}</label>
            </template>
          </div>

          <div v-if="selectedSymptoms.length" class="mt-3 text-muted small">
            <i class="bi bi-check2-circle"></i> {{ selectedSymptoms.length }}개 증상 선택됨
          </div>
        </div>

        <div class="modal-footer">
          <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">취소</button>
          <button type="button" class="btn btn-primary" data-bs-dismiss="modal" @click="onConfirmSymptoms">
            <i class="bi bi-search"></i> 3km 재검색
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
// import
import { ref, computed, onMounted, onBeforeUnmount } from 'vue';
import { useRouter } from 'vue-router';

import {
  initVworldMap,
  getVMap,
  clearMarkers,
  addMarker,
  getCurrentBBox4326
} from '@/utils/vworldfunction';
import { getEmergencyFromWFSByBBox } from '@/utils/api';

// vmap 변수
let vmap = null;

// 선택된 증상 - 반응형
const symptomOptions = ['발열', '감기', '기침', '호흡곤란', '구토', '설사', '복통', '발진', '화상', '경련']
const selectedSymptoms = ref([])
const removeSymptom = (sym) => { selectedSymptoms.value = selectedSymptoms.value.filter(s => s !== sym) }

// 현재 위치
const cur = ref({ lat: null, lon: null })

const hospitals = ref([])
const loadingHospitals = ref(false)
const searchAttempted = ref(false)
const lastRadius = ref(0)
const useSymptomsMode = ref(false)

// 마커
let markers = []
let debounceTimer = null

/* 바텀시트 상태 start */
const bottomSheet = ref(null)
const isDragging = ref(false)
const sheetHeightRatio = ref(0)
const dragStart = ref({ y: 0, ratio: 0 })

const MAX_SHEET_HEIGHT = window.innerHeight * 0.8
const MIN_SHEET_HEIGHT = 220

const sheetY = computed(() => (MAX_SHEET_HEIGHT - MIN_SHEET_HEIGHT) * (1 - sheetHeightRatio.value))
const sheetStyle = computed(() => {
  const interactive = sheetHeightRatio.value > 0.12
  return {
    transition: isDragging.value ? 'none' : 'transform 0.3s ease-out',
    transform: `translateY(${sheetY.value}px)`,
    height: `${MAX_SHEET_HEIGHT}px`,
    bottom: '0px',
    top: 'auto',
    willChange: 'transform'
  }
})
/* 바텀시트 상태 end */

/* 키워드 start */
const keywordBase = computed(() => '소아응급실')
const keywordWithSymptoms = computed(() => {
  const sym = selectedSymptoms.value.join(' ')
  return sym ? `소아응급실 ${sym}` : keywordBase.value
})
const displayKeyword = computed(() => useSymptomsMode.value ? keywordWithSymptoms.value : keywordBase.value)
/* 키워드 end */

/* 바텀시트 제스처 start */
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
/* 바텀시트 제스처 end */

/* 그립 액션 start*/
const onGripTouchStart = (e) => {
  isDragging.value = true
  dragStart.value = { y: e.touches[0].clientY, ratio: sheetHeightRatio.value }
  e.preventDefault()
}
const onGripTouchMove = (e) => {
  if (!isDragging.value) return
  e.preventDefault()

  const dY = e.touches[0].clientY - dragStart.value.y
  const dR = -dY / (MAX_SHEET_HEIGHT - MIN_SHEET_HEIGHT)
  sheetHeightRatio.value = Math.min(1, Math.max(0, dragStart.value.ratio + dR))
}
const onGripTouchEnd = () => {
  isDragging.value = false
  sheetHeightRatio.value = sheetHeightRatio.value > 0.5 ? 1 : 0
}
/* 그립 액션 end */

/* 실제 렌더링 목록 */
const listHospitals = computed(() => {
  return hospitals.value.map((h, idx) => ({
    id: h.id || `hospital-${idx}`,
    name: h.name || '병원명 미상',
    distanceKm: 0,
    etaMin: 0,
  }))
})

// 🔹 WFS 마커 로드 함수
const loadWFSMarkers = async () => {
  if (!vmap) {
    console.warn('vmap이 아직 초기화되지 않았습니다.')
    return
  }

  const bbox = getCurrentBBox4326()
  if (!bbox) {
    console.warn('bbox를 가져올 수 없습니다.')
    return
  }

  const [minX, minY, maxX, maxY] = bbox

  console.log('🔍 WFS 조회 시작:', { minX, minY, maxX, maxY })

  loadingHospitals.value = true
  searchAttempted.value = true

  try {
    const featuresData = await getEmergencyFromWFSByBBox(minX, minY, maxX, maxY)

    console.log('✅ WFS 응답:', featuresData.length, '개')

    // 기존 마커 제거
    clearMarkers()
    hospitals.value = []

    featuresData.forEach((f) => {
      if (f.geometry?.type === 'Point' && Array.isArray(f.geometry.coordinates)) {
        const [lon, lat] = f.geometry.coordinates
        const props = f.properties || {}

        // 마커 추가
        addMarker(lon, lat, { id: f.id, ...props })

        // hospitals 배열에 추가
        hospitals.value.push({
          id: f.id || `wfs-${hospitals.value.length}`,
          name: props.dutyName || props.name || '응급실',
          lon,
          lat,
          properties: props
        })
      }
    })

    console.log('📍 마커 추가 완료:', hospitals.value.length, '개')

  } catch (e) {
    console.error('❌ WFS 마커 로드 실패', e)
  } finally {
    loadingHospitals.value = false
  }
}

// 🔹 현재 위치 가져오기
function getCurrentLocation() {
  return new Promise((resolve, reject) => {
    if (!navigator.geolocation) {
      reject(new Error('Geolocation not supported'))
      return
    }

    navigator.geolocation.getCurrentPosition(
      position => {
        cur.value.lat = position.coords.latitude
        cur.value.lon = position.coords.longitude
        resolve({ lat: cur.value.lat, lon: cur.value.lon })
      },
      error => reject(error),
      { enableHighAccuracy: true, timeout: 10000, maximumAge: 0 }
    )
  })
}

// onMounted - 초기실행
onMounted(async () => {
  // 1. VWorld 라이브러리 로드 대기
  await new Promise((resolve) => {
    const checkVworld = setInterval(() => {
      if (window.vw && window.vw.ol3 && window.ol) {
        clearInterval(checkVworld)
        resolve()
      }
    }, 100)
  })

  try {
    // 2. 현재 위치 가져오기
    let centerCoords
    try {
      const location = await getCurrentLocation()
      centerCoords = ol.proj.transform([location.lon, location.lat], 'EPSG:4326', 'EPSG:3857')
    } catch {
      centerCoords = ol.proj.transform([126.978, 37.5665], 'EPSG:4326', 'EPSG:3857')
    }

    // 3. 지도 초기화
    vmap = initVworldMap({
      containerId: 'vmap',
      defaultCenter: centerCoords,
      defaultZoom: 15,
      autoFetch: false
    })

    if (!vmap) throw new Error('vmap 초기화 실패')

    // ✅ 여기서 바로 이벤트 등록
    vmap.on('moveend', () => {
      console.log('🗺️ 지도 이동/줌 감지 (vmap 이벤트)')
      if (debounceTimer) clearTimeout(debounceTimer)
      debounceTimer = setTimeout(() => {
        loadWFSMarkers()
      }, 300)
    })

    // 4. 초기 마커 로드
    setTimeout(() => loadWFSMarkers(), 500)

  } catch (error) {
    console.error('초기화 실패:', error)
    alert('지도를 초기화할 수 없습니다.')
  }
})

// 🔹 정렬 함수들
const sortByDistance = () => {
  hospitals.value.sort((a, b) => a.name.localeCompare(b.name))
}

const sortByTime = () => {
  hospitals.value.sort((a, b) => a.name.localeCompare(b.name))
}

// 🔹 증상 확인 후 재검색
const onConfirmSymptoms = async () => {
  await loadWFSMarkers()
  if (hospitals.value.length) sheetHeightRatio.value = 1
}

// 🔹 병원 상세 페이지로 이동
const router = useRouter()
function goToemergencyDetail(hpid) {
  router.push({
    name: 'emergencyDetailApp',
    params: { hpid: hpid }, // URL엔 필수 키만
  })

}

// cleanup
onBeforeUnmount(() => {
  if (debounceTimer) clearTimeout(debounceTimer)

  const grip = document.querySelector('.sheet-grip')
  if (grip) {
    grip.removeEventListener('touchstart', onGripTouchStart)
    grip.removeEventListener('touchmove', onGripTouchMove)
    grip.removeEventListener('touchend', onGripTouchEnd)
  }
})
</script>

<style scoped>
/* 리스트 위에 살짝 여유(선택) */
.er-list {
  padding-top: 4px;
}

/* 카드 기본/호버/클릭 */
.card {
  cursor: pointer;
  /* 핵심: 위쪽을 기준으로 확대 → 위로는 안 튐 */
  transform-origin: top center;
  transition:
    transform 0.18s ease,
    box-shadow 0.18s ease,
    background-color 0.25s ease,
    border-color 0.18s ease;
  background-color: #fff;
}

/* 호버: 아래쪽으로만 커짐 */
.card:hover {
  transform: scale(1.02);
  /* ↑ translateY(-) 제거 */
  background-color: #f0f6ff;
  border-color: #0d6efd;
  box-shadow: 0 6px 20px rgba(13, 110, 253, .15);
}

/* 클릭(눌림) */
.card:active {
  transform: scale(0.98);
  /* 위아래 이동 없이 눌림만 */
  background-color: #e2ebff;
  box-shadow: 0 3px 10px rgba(0, 0, 0, .15);
}

/* 스크롤 영역: iOS/WebView 부드러운 스크롤 */
.sheet-scroll {
  -webkit-overflow-scrolling: touch;
  overscroll-behavior: contain;
  /* 스크롤 제스처를 브라우저가 처리하도록 명시 */
  touch-action: pan-y;
}

/* 그립: 제스처는 우리가 처리 (스크롤로 해석되지 않도록) */
.sheet-grip {
  touch-action: none;
}
</style>