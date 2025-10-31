<template>
  <div class="d-flex justify-content-center align-items-center position-relative mt-1"
    style="height: calc(100vh - 140px); ">

    <!-- 지도 -->
    <div id="map_div" class="position-fixed top-0 start-0 w-100" style="height:100dvh; z-index:0;"></div>

    <!-- 진료과 선택 -->
    <div class="position-fixed start-0 p-3" style="top: calc(var(--header-h) + 8px); z-index: 2;">
      <div class="pe-auto">
        <button type="button" class="btn btn-light border rounded-pill px-3 shadow-sm text-bold" data-bs-toggle="modal"
          data-bs-target="#departmentModal">
          <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24">
            <path fill="currentColor"
              d="M19 8c.56 0 1 .43 1 1a1 1 0 0 1-1 1c-.57 0-1-.45-1-1c0-.57.43-1 1-1M2 2v9c0 2.96 2.19 5.5 5.14 5.91c.62 3.01 3.28 5.09 6.36 5.09a6.5 6.5 0 0 0 6.5-6.5v-3.69c1.16-.42 2-1.52 2-2.81a3 3 0 0 0-3-3a3 3 0 0 0-3 3c0 1.29.84 2.4 2 2.81v3.6c0 2.5-2 4.5-4.5 4.5c-2 0-3.68-1.21-4.28-3.01C12 16.3 14 13.8 14 11V2h-4v3h2v6a4 4 0 0 1-4 4a4 4 0 0 1-4-4V5h2V2z" />
          </svg> 진료과
        </button>
      </div>
    </div>
    </div>
    <!-- 내 위치 새로고침 버튼 -->
    <div class="position-fixed end-0 p-3" style="top: calc(var(--header-h) + 8px); z-index: 2;">
      <button type="button" class="btn btn-primary border rounded-circle shadow-sm" @click="refreshLocation" style="width: 48px; height: 48px; padding: 0;">
        <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24">
          <path fill="currentColor" d="M12 8c-2.21 0-4 1.79-4 4s1.79 4 4 4s4-1.79 4-4s-1.79-4-4-4m8.94 3A8.994 8.994 0 0 0 13 3.06V1h-2v2.06A8.994 8.994 0 0 0 3.06 11H1v2h2.06A8.994 8.994 0 0 0 11 20.94V23h2v-2.06A8.994 8.994 0 0 0 20.94 13H23v-2zM12 19c-3.87 0-7-3.13-7-7s3.13-7 7-7s7 3.13 7 7s-3.13 7-7 7" />
        </svg>
      </button>
    </div>
  <!-- 바텀시트 -->
  <div class="position-fixed start-0 end-0 bg-white  rounded-top-4 shadow-lg " :style="sheetStyle" ref="bottomSheet"
    @touchstart="onTouchStart" @touchmove="onTouchMove" @touchend="onTouchEnd" style="z-index: 1;">
    <!-- 그립 -->
    <div class="py-3 d-flex justify-content-center" @click="toggleSheet" style="cursor: grab;">
      <div class="rounded-pill bg-body-secondary" style="width:40px;height:5px;"></div>
    </div>

    <!-- 내용 -->
    <div class="d-flex flex-column" style="height: calc(100% - 60px); overflow: hidden;">

      <!-- 경로 결과 정보 (경로 탐색 후 표시) -->
      <div v-if="isRouteMode" class="p-3 border-bottom bg-primary bg-gradient text-white flex-shrink-0">
        <div class="d-flex justify-content-between align-items-center mb-2">
          <h6 class="fw-bold mb-0 text-white">
            <i class="bi bi-geo-alt-fill me-1"></i>{{ selectedHospital?.properties?.name || '경로 안내' }}
          </h6>
          <button type="button" class="btn-close btn-close-white" @click="exitRouteMode" aria-label="닫기"></button>
        </div>

        <!-- 경로 상세 정보 -->
        <div class="bg-white text-dark rounded p-3">
          <div class="row g-2 small">
            <div class="col-6">
              <div class="text-muted mb-1">
                <i class="bi bi-signpost me-1"></i>총 거리
              </div>
              <div class="fw-bold fs-6">{{ routeDetails.distance }}</div>
            </div>
            <div class="col-6">
              <div class="text-muted mb-1">
                <i class="bi bi-clock me-1"></i>소요 시간
              </div>
              <div class="fw-bold fs-6">{{ routeDetails.time }}</div>
            </div>
            <div class="col-6" v-if="transportType === 'car' && routeDetails.fare">
              <div class="text-muted mb-1">
                <i class="bi bi-credit-card me-1"></i>통행 요금
              </div>
              <div class="fw-bold fs-6">{{ routeDetails.fare }}</div>
            </div>
            <div class="col-6" v-if="transportType === 'car' && routeDetails.taxiFare">
              <div class="text-muted mb-1">
                <i class="bi bi-taxi-front me-1"></i>예상 택시 요금
              </div>
              <div class="fw-bold fs-6">{{ routeDetails.taxiFare }}</div>
            </div>
            <div class="col-6" v-if="transportType === 'walk' && routeDetails.walkTime">
              <div class="text-muted mb-1">
                <i class="bi bi-person-walking me-1"></i>도보 시간
              </div>
              <div class="fw-bold fs-6">{{ routeDetails.walkTime }}</div>
            </div>
          </div>
        </div>
      </div>
      <!-- 상단 라인 -->
      <div class="d-flex align-items-center justify-content-between gap-2 mb-3 ms-2 me-2 flex-shrink-0">
        <div class="d-flex align-items-center gap-2 flex-wrap">
          <div class="d-flex flex-wrap gap-1" v-if="selectedMessages.length">
            <span v-for="tag in selectedMessages" :key="tag"
            class="badge text-bg-primary rounded-pill d-inline-flex align-items-center">
            {{ tag }}
            <button type="button" class="btn-close btn-close-white btn-sm ms-1" aria-label="삭제"
            @click="removeMessageTag(tag)" style="transform: scale(.8);"></button>
            </span>
         </div>
        </div>
      </div>

      <!-- 타이틀 -->
      <div class="d-flex justify-content-between align-items-center mb-3 ms-2 me-2 flex-shrink-0">
        <!-- 왼쪽 -->
        <div class="d-flex align-items-center gap-2">
          <h5 class="fw-bold mb-0">병원 리스트</h5>
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
        </div>
      </div>

      <!-- 리스트 스크롤 영역 -->
      <div class="overflow-auto px-3 flex-grow-1" style="min-height: 0; max-height: 100%;">
        <!-- 병원 목록 -->
        <div class="card shadow-sm mb-3" v-for="hospital in hospitals" :key="hospital.id">
          <div class="card-body">
            <div class="d-flex justify-content-between align-items-start">
              <h6 class="card-title fw-bold mb-1">{{ hospital.properties.name || '병원명 없음' }}</h6>
              <span v-if="hospital.properties.hospital_type"
                class="badge bg-success-subtle text-success-emphasis rounded-pill">
                <svg xmlns="http://www.w3.org/2000/svg" width="22px" height="15px" viewBox="0 0 24 24">
                  <path fill="#008A64"
                    d="M19 7h-8v7H3V5H1v15h2v-3h18v3h2v-9a4 4 0 0 0-4-4M7 13a3 3 0 0 0 3-3a3 3 0 0 0-3-3a3 3 0 0 0-3 3a3 3 0 0 0 3 3" />
                </svg> {{ hospital.properties.hospital_type }}
              </span>
            </div>

            <div class="mb-2" v-if="hospital.properties.message">
                <span
                    class="badge me-1"
                    :class="hospital.properties.message.includes('가능')
                    ? 'bg-success-subtle text-success-emphasis'
                    : 'bg-secondary-subtle text-secondary-emphasis'">
                    {{ hospital.properties.message }}
                </span>
            </div>

            <div class="text-muted small d-flex align-items-center mb-3">
              <span><i class="bi bi-geo-alt me-1"></i> {{ hospital.distanceKm }}km</span>
            </div>


            <!-- hospital_tel emergencytable에 전화번호 넣어야함 v-if="hospital.properties.hospital_tel"-->
            <div class="d-grid gap-2 d-sm-flex">
              <button class="btn btn-outline-primary w-100" 
                @click="callHospital(hospital.properties.hospital_tel)">
                <i class="bi bi-telephone-fill"></i> 전화
              </button>
              <button class="btn btn-primary w-100" @click="openRouteModal(hospital)" style="z-index: 2;">
                <i class="bi bi-sign-turn-right-fill"></i> 길찾기
              </button>
            </div>
          </div>
        </div>

        <div v-if="hospitals.length === 0" class="text-center text-muted py-4">
          <i class="bi bi-search fs-1 d-block mb-2"></i>
          <p class="mb-1">주변에 병원 정보가 없습니다.</p>
          <small class="text-muted">필터(소아가능/불가능)를 변경하거나 다시 시도해주세요.</small>
        </div>
      </div>
    </div>
  </div>

  <!-- 진료과 선택 모달 -->
  <div class="modal fade" id="departmentModal" tabindex="-1" aria-labelledby="departmentModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
      <div class="modal-content border-0 shadow">
        <div class="modal-header">
          <h5 class="modal-title" id="departmentModalLabel">
           <i class="bi bi-clipboard2-pulse me-1"></i> 소아 진료 가능여부
          </h5>
          <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="닫기"></button>
        </div>

        <div class="modal-body">
          <div class="d-flex flex-wrap gap-2">
            <template v-for="opt in messageOptions" :key="opt">
                <input class="btn-check" type="checkbox" :id="`msg-${opt}`" :value="opt" v-model="selectedMessages" />
                <label class="btn btn-outline-secondary rounded-pill py-1 px-3" :for="`msg-${opt}`">{{ opt }}</label>
            </template>
          </div>
          <div v-if="selectedMessages.length" class="mt-3 text-muted small">
           <i class="bi bi-check2-circle"></i> {{ selectedMessages.length }}개 상태 선택됨
          </div>
        </div>

        <div class="modal-footer">
          <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">취소</button>
          <button type="button" class="btn btn-primary" data-bs-dismiss="modal">
            <i class="bi bi-search"></i> 검색
          </button>
        </div>
      </div>
    </div>
  </div>

  <!-- 경로 안내 옵션 모달 -->
  <div class="modal fade" id="routeModal" tabindex="-1" aria-labelledby="routeModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
      <div class="modal-content border-0 shadow">
        <div class="modal-header bg-primary text-white">
          <h5 class="modal-title" id="routeModalLabel">
            <i class="bi bi-signpost-2-fill me-2"></i>경로 안내
          </h5>
          <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="닫기"></button>
        </div>

        <div class="modal-body">
          <div v-if="selectedHospital" class="alert alert-light border mb-3">
            <div class="fw-bold text-dark mb-1">
              <i class="bi bi-hospital me-1"></i>{{ selectedHospital.properties.name }}
            </div>
            <small class="text-muted">
              <i class="bi bi-geo-alt me-1"></i>{{ selectedHospital.distanceKm }}km
            </small>
          </div>

          <div class="mb-3">
            <label class="form-label fw-bold">이동 수단 선택</label>
            <div class="btn-group w-100" role="group">
              <input type="radio" class="btn-check" name="transportType" id="transport-car" value="car" v-model="transportType" checked>
              <label class="btn btn-outline-primary" for="transport-car">
                <i class="bi bi-car-front-fill d-block fs-4 mb-1"></i>
                <small>자동차</small>
              </label>

              <input type="radio" class="btn-check" name="transportType" id="transport-walk" value="walk" v-model="transportType">
              <label class="btn btn-outline-primary" for="transport-walk">
                <i class="bi bi-person-walking d-block fs-4 mb-1"></i>
                <small>도보</small>
              </label>

              <input type="radio" class="btn-check" name="transportType" id="transport-transit" value="transit" v-model="transportType">
              <label class="btn btn-outline-primary" for="transport-transit">
                <i class="bi bi-bus-front-fill d-block fs-4 mb-1"></i>
                <small>대중교통</small>
              </label>
            </div>
          </div>
        </div>

        <div class="modal-footer">
          <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">취소</button>
          <button type="button" class="btn btn-primary" @click="findRoute">
            <i class="bi bi-compass-fill me-1"></i>경로 탐색
          </button>
        </div>
      </div>
    </div>
  </div>

</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, watch} from 'vue'
import axios from 'axios'




//마커에 응급실 이름 
const markerIconHover = 'data:image/svg+xml;utf8,' + encodeURIComponent(`
  <svg xmlns="http://www.w3.org/2000/svg" width="34" height="34" viewBox="0 0 24 24" fill="none">
    <path d="M12 2C8.1 2 5 5.1 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.9-3.1-7-7-7z" fill="#0d6efd"/>
    <circle cx="12" cy="9" r="3.4" fill="white"/>
  </svg>`)





// TMAP API 키
const TMAP_APP_KEY = "pAs3duhDpE5cYgopfzzDmw8anJPaBak8HAdTerg8"

/*소아 진료 가능여부 필터 상태 */
 const messageOptions = ['소아가능', '불가능']           // 필요시 값만 더 추가
 const selectedMessages = ref([])                       // 다중 선택 OR 필터
 function removeMessageTag(tag) {
   selectedMessages.value = selectedMessages.value.filter(t => t !== tag)
 }

/* 병원 데이터 상태 */
const hospitals = ref([])
const userLocation = ref(null)
const searchRadius = ref(5) // 기본 5km 반경

/* 지도 상태 */
const map = ref(null)

/* 경로 탐색 상태 */
const isRouteMode = ref(false)
const selectedHospital = ref(null)
const transportType = ref('car') // car, walk, transit
const searchOption = ref('2') // 교통최적+최소시간
const trafficInfo = ref('N')
const routeDetails = ref({
  distance: '',
  time: '',
  fare: '',
  taxiFare: '',
  walkTime: '' // 도보 시간
})

// 경로 탐색 결과물 (마커, 라인)
const resultMarkerArr = ref([])
const resultdrawArr = ref([])

/* 바텀시트 드래그 */
const bottomSheet = ref(null)
const isDragging = ref(false)
const sheetHeightRatio = ref(0)
const dragStart = ref({ y: 0, ratio: 0 })

const MAX_SHEET_HEIGHT = window.innerHeight * 0.7
const MIN_SHEET_HEIGHT = 220

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

/* GeoServer에서 병원 데이터 가져오기 */
const fetchHospitalsFromGeoServer = async (lat, lon, radiusKm) => {
  try {
    // EPSG:4326 (WGS84) 좌표계로 bbox 계산 (위도/경도)
    // 1도 = 약 111km
    const delta = radiusKm / 111
    const bbox = [
      lon - delta,
      lat - delta,
      lon + delta,
      lat + delta
    ].join(',')

    // GeoServer WFS 요청 URL (sql2 레이어 사용, EPSG:4326 좌표계)
    // 개발 환경에서는 프록시 사용, 프로덕션에서는 직접 접속
    const baseUrl = import.meta.env.DEV ? '/geoserver' : 'https://api.child119.com/geoserver'
    const url = `${baseUrl}/hospital/ows?` +
      `service=WFS` +
      `&version=1.1.0` +
      `&request=GetFeature` +
      `&typeName=hospital:emergency` + // hospital 저장소의 sql2 레이어
      `&srsName=EPSG:4326` +
      `&outputFormat=application/json` +
      `&bbox=${bbox},EPSG:4326`

    console.log('GeoServer 요청 URL:', url)
    const response = await axios.get(url)

    console.log('GeoServer 전체 응답:', response)
    console.log('응답 데이터:', response.data)
    console.log('features 개수:', response.data?.features?.length)

    if (response.data && response.data.features) {
      console.log('GeoServer 응답 데이터:', response.data)
      console.log('받아온 병원 개수:', response.data.features.length)

      // 거리 계산 및 필터링
      let hospitalsWithDistance = response.data.features.map(feature => {
   const coords = feature.geometry.coordinates
   const distance = calculateDistance(lat, lon, coords[1], coords[0])

   // 속성 정규화: name, message를 반드시 채움
   const raw = feature.properties || {}
   const name = raw.name ?? raw.hospital_name ?? raw.yadmNm ?? raw.dutyName ?? ''
   const message = raw.message ?? raw.MSG ?? '' // GeoJSON이면 보통 'message' 그대로 옴

   return {
     id: feature.id,
     properties: { ...raw, name, message }, // 표준 키로 덮어쓰기
     coordinates: coords,
     distance,
     distanceKm: (distance / 1000).toFixed(1)
   }
 }).filter(h => h.distance <= radiusKm * 1000)

      console.log('거리 필터링 후 병원 수:', hospitalsWithDistance.length)
      console.log('선택된 message:', selectedMessages.value)

      // 진료과 필터링 (선택된 진료과가 있을 경우)
      // 소아 진료 가능여부 필터 (선택된 메시지가 있을 경우)
 if (selectedMessages.value.length > 0) {
   const before = hospitalsWithDistance.length
   hospitalsWithDistance = hospitalsWithDistance.filter(h => {
     const msg = (h.properties.message || '').trim()
     // '소아가능' 정확히 또는 포함 매칭 (둘 다 허용)
     return selectedMessages.value.some(sel => msg === sel || msg.includes(sel))
   })
   console.log(`message 필터링: ${before}개 → ${hospitalsWithDistance.length}개`)
 }

      // 거리순 정렬
      hospitalsWithDistance.sort((a, b) => a.distance - b.distance) 
      //const finalHospitals = hospitalsWithDistance(0,10)
      hospitals.value = hospitalsWithDistance

      console.log('최종 병원 수:', hospitalsWithDistance.length)
      console.log('hospitals.value:', hospitals.value)

      // 데이터가 있으면 바텀시트 자동으로 올리기
      if (hospitalsWithDistance.length > 0) {
        sheetHeightRatio.value = 0.5 // 중간 높이로 펼치기
      }

      // 지도에 마커 추가
      addHospitalMarkers(hospitalsWithDistance)
    }
  } catch (error) {
    console.error('병원 데이터 가져오기 실패:', error)
    alert('병원 정보를 가져올 수 없습니다. GeoServer 연결을 확인해주세요.')
  }
}

/* 두 지점 간 거리 계산 (Haversine 공식) - 미터 단위 */
const calculateDistance = (lat1, lon1, lat2, lon2) => {
  const R = 6371e3 // 지구 반지름 (미터)
  const φ1 = lat1 * Math.PI / 180
  const φ2 = lat2 * Math.PI / 180
  const Δφ = (lat2 - lat1) * Math.PI / 180
  const Δλ = (lon2 - lon1) * Math.PI / 180

  const a = Math.sin(Δφ / 2) * Math.sin(Δφ / 2) +
    Math.cos(φ1) * Math.cos(φ2) *
    Math.sin(Δλ / 2) * Math.sin(Δλ / 2)
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

  return R * c
}

let markers = []

function clearMarkers() {
  markers.forEach(m => m.setMap(null));
  markers = []
}

/* 지도에 병원 마커 추가 */
const addHospitalMarkers = (hospitalList) => {
  if (!map.value) return
  clearMarkers()
  const bounds = new window.Tmapv3.LatLngBounds()

  hospitalList.forEach(hospital => {
    const position = new window.Tmapv3.LatLng(
      hospital.coordinates[1],
      hospital.coordinates[0]
    )
    bounds.extend(position)

    const marker = new window.Tmapv3.Marker({ position: position, map: map.value })

    // 마커 클릭 시 경로 안내 모달 표시
    window.Tmapv3.event.addListener(marker, 'click', () => {
      openRouteModal(hospital)
    })

    marker.__hospital = hospital
    markers.push(marker)
  })

  if (hospitalList.length) map.value.fitBounds(bounds)
}

/* 거리순 정렬 */
const distanceAsc = ref(true)
function sortByDistance() {
  const arr = hospitals.value
  arr.sort((a, b) => {
    const A = a.distance ?? Infinity
    const B = b.distance ?? Infinity
    return distanceAsc.value ? A - B : B - A
  })
  distanceAsc.value = !distanceAsc.value
}

/* 병원 전화 걸기 */
function callHospital(tel) {
  if (!tel) {
    alert('전화번호가 없습니다.')
    return
  }
  window.location.href = `tel:${tel}`
}

/* 길찾기 모달 열기 */
function openRouteModal(hospital) {
  selectedHospital.value = hospital

  // Bootstrap 5 Modal API 사용
  const modalEl = document.getElementById('routeModal')
  if (modalEl) {
    // Bootstrap이 로드되어 있는지 확인
    if (typeof window.bootstrap !== 'undefined' && window.bootstrap.Modal) {
      const modal = new window.bootstrap.Modal(modalEl)
      modal.show()
    } else {
      // Bootstrap이 없으면 직접 모달 표시 (fallback)
      modalEl.classList.add('show')
      modalEl.style.display = 'block'
      document.body.classList.add('modal-open')

      // 백드롭 추가
      const backdrop = document.createElement('div')
      backdrop.className = 'modal-backdrop fade show'
      backdrop.id = 'route-modal-backdrop'
      document.body.appendChild(backdrop)
    }
  }
}

/* 경로 탐색 실행 */
async function findRoute() {
  if (!map.value || !userLocation.value || !selectedHospital.value) {
    alert('위치 정보를 확인할 수 없습니다.')
    return
  }

  // 모달 닫기
  const modalEl = document.getElementById('routeModal')
  if (modalEl) {
    if (typeof window.bootstrap !== 'undefined' && window.bootstrap.Modal) {
      const modal = window.bootstrap.Modal.getInstance(modalEl)
      if (modal) modal.hide()
    } else {
      modalEl.classList.remove('show')
      modalEl.style.display = 'none'
      document.body.classList.remove('modal-open')
      const backdrop = document.getElementById('route-modal-backdrop')
      if (backdrop) backdrop.remove()
    }
  }

  // 기존 병원 마커 숨기기
  clearMarkers()

  // 기존 경로 정보 초기화
  clearRouteData()

  const headers = {
    "appKey": TMAP_APP_KEY,
    "Content-Type": "application/json"
  }

  try {
    let resultData, props

    // 이동 수단에 따라 다른 API 호출
    if (transportType.value === 'car') {
      // 자동차 경로
      const data = {
        "startX": String(userLocation.value.lon),
        "startY": String(userLocation.value.lat),
        "endX": String(selectedHospital.value.coordinates[0]),
        "endY": String(selectedHospital.value.coordinates[1]),
        "reqCoordType": "WGS84GEO",
        "resCoordType": "WGS84GEO",
        "searchOption": searchOption.value,
        "trafficInfo": trafficInfo.value
      }

      const response = await axios.post(
        "https://apis.openapi.sk.com/tmap/routes?version=1&format=json",
        data,
        { headers: headers }
      )

      resultData = response.data.features
      props = resultData[0].properties

      routeDetails.value = {
        distance: `${(props.totalDistance / 1000).toFixed(1)}km`,
        time: `${(props.totalTime / 60).toFixed(0)}분`,
        fare: `${props.totalFare}원`,
        taxiFare: `${props.taxiFare}원`,
        walkTime: ''
      }
    } else if (transportType.value === 'walk') {
      // 도보 경로
      const data = {
        "startX": String(userLocation.value.lon),
        "startY": String(userLocation.value.lat),
        "endX": String(selectedHospital.value.coordinates[0]),
        "endY": String(selectedHospital.value.coordinates[1]),
        "reqCoordType": "WGS84GEO",
        "resCoordType": "WGS84GEO",
        "startName": "출발지",
        "endName": selectedHospital.value.properties.name || "도착지"
      }

      const response = await axios.post(
        "https://apis.openapi.sk.com/tmap/routes/pedestrian?version=1&format=json",
        data,
        { headers: headers }
      )

      resultData = response.data.features
      props = resultData[0].properties

      routeDetails.value = {
        distance: `${(props.totalDistance / 1000).toFixed(1)}km`,
        time: `${(props.totalTime / 60).toFixed(0)}분`,
        fare: '',
        taxiFare: '',
        walkTime: `${(props.totalTime / 60).toFixed(0)}분`
      }
    } else if (transportType.value === 'transit') {
      // 대중교통 경로 (간단한 직선 거리로 대체 - TMAP 대중교통 API는 별도 상품)
      alert('대중교통 경로는 현재 지원하지 않습니다. 도보 또는 자동차를 선택해주세요.')
      return
    }

    isRouteMode.value = true

    // 경로 및 마커 그리기 - 파란색 라인
    for (const i in resultData) {
      const geometry = resultData[i].geometry
      const properties = resultData[i].properties

      if (geometry.type === "LineString") {
        const drawInfoArr = []
        for (const coord of geometry.coordinates) {
          drawInfoArr.push(new window.Tmapv3.LatLng(coord[1], coord[0]))
        }
        drawLine(drawInfoArr) // 파란색 라인으로 그리기
      } else {
        // 출발지(S)와 도착지(E) 마커만 표시
        if (properties.pointType === "S" || properties.pointType === "E") {
          const [lng, lat] = geometry.coordinates
          const position = new window.Tmapv3.LatLng(lat, lng)

          const label = properties.pointType === "S" ? "출발" : "도착"
          const color = properties.pointType === "S" ? "#0d6efd" : "#dc3545"

          const marker = new window.Tmapv3.Marker({
            position: position,
            map: map.value,
            title: label
          })

          // 커스텀 라벨 추가
          const infoWindow = new window.Tmapv3.InfoWindow({
            position: position,
            content: `<div style="padding:5px 10px;background:${color};color:white;border-radius:5px;font-weight:bold;font-size:12px;">${label}</div>`,
            type: 2,
            map: map.value,
            offset: new window.Tmapv3.Point(0, -10)
          })

          resultMarkerArr.value.push(marker)
        }
      }
    }

    // 바텀시트 올리기
    sheetHeightRatio.value = 0.5

  } catch (error) {
    console.error("경로 탐색 API 오류:", error)
    alert("경로 탐색에 실패했습니다.")
    if (error.response) {
      console.error("응답 데이터:", error.response.data)
    }
  }
}

/* 경로 라인 그리기 - 깔끔한 파란색 */
function drawLine(arrPoint) {
  if (!window.Tmapv3) return

  // 파란색 경로 라인
  const polyline_ = new window.Tmapv3.Polyline({
    path: arrPoint,
    strokeColor: "#0d6efd", // Bootstrap primary 파란색
    strokeWeight: 6,
    map: map.value
  })
  resultdrawArr.value.push(polyline_)
}

/* 경로 데이터 초기화 */
function clearRouteData() {
  resultMarkerArr.value.forEach(marker => marker.setMap(null))
  resultMarkerArr.value = []

  resultdrawArr.value.forEach(line => line.setMap(null))
  resultdrawArr.value = []
}

/* 경로 모드 종료 */
function exitRouteMode() {
  isRouteMode.value = false
  selectedHospital.value = null
  clearRouteData()

  // 병원 마커 다시 표시
  if (userLocation.value) {
    fetchHospitalsFromGeoServer(userLocation.value.lat, userLocation.value.lon, searchRadius.value)
  }
}

/* 경로 정보 토글 */
function toggleRouteInfo() {
  isRouteInfoExpanded.value = !isRouteInfoExpanded.value
}

/* 지도 초기화 */
const initTmap = () => {
  const el = document.getElementById('map_div')
  if (!el) return
  map.value = new window.Tmapv3.Map('map_div', {
    center: new window.Tmapv3.LatLng(37.5662952, 126.9779451),
    width: '100%',
    height: '100%',
    zoom: 16
  })

  if (navigator.geolocation) {
    // 위치 정보 옵션 설정
    const geoOptions = {
      enableHighAccuracy: true,  // 높은 정확도 요청
      timeout: 10000,            // 10초 타임아웃
      maximumAge: 0              // 캐시된 위치 사용 안함
    }

    navigator.geolocation.getCurrentPosition(
      pos => {
        const lat = pos.coords.latitude
        const lon = pos.coords.longitude
        console.log('위치 정보 가져오기 성공:', { lat, lon })
        const currentPos = new window.Tmapv3.LatLng(lat, lon)
        userLocation.value = { lat, lon }
        new window.Tmapv3.Marker({ position: currentPos, map: map.value })
        new window.Tmapv3.InfoWindow({
          position: currentPos,
          content: "<div class='p-1 bg-white border rounded-2'>현재 위치</div>",
          offset: new window.Tmapv3.Point(0, -30),
          type: 2,
          map: map.value
        })
        map.value.setCenter(currentPos)
        map.value.setZoom(17)
        fetchHospitalsFromGeoServer(lat, lon, searchRadius.value)
      },
      err => {
        console.error('Geolocation 오류 코드:', err.code, '메시지:', err.message)
        let errorMsg = '현재 위치를 가져올 수 없습니다.\n'

        switch(err.code) {
          case 1: // PERMISSION_DENIED
            errorMsg += '위치 권한이 거부되었습니다. 브라우저 설정에서 위치 권한을 허용해주세요.'
            break
          case 2: // POSITION_UNAVAILABLE
            errorMsg += '위치 정보를 사용할 수 없습니다.'
            break
          case 3: // TIMEOUT
            errorMsg += '위치 정보 요청 시간이 초과되었습니다.'
            break
          default:
            errorMsg += 'HTTPS 환경인지 확인해주세요.'
        }

        alert(errorMsg)
        console.log('기본 위치(서울시청)로 설정합니다.')
        userLocation.value = { lat: 37.5662952, lon: 126.9779451 }
        fetchHospitalsFromGeoServer(37.5662952, 126.9779451, searchRadius.value)
      },
      geoOptions
    )
  } else {
    alert('이 브라우저에서는 위치 정보를 지원하지 않습니다.')
    userLocation.value = { lat: 37.5662952, lon: 126.9779451 }
    fetchHospitalsFromGeoServer(37.5662952, 126.9779451, searchRadius.value)
  }
}

/* 바텀시트 제스처 */
const onTouchStart = e => {
  isDragging.value = true
  dragStart.value = { y: e.touches[0].clientY, ratio: sheetHeightRatio.value }
  e.preventDefault()
}
const onTouchMove = e => {
  if (!isDragging.value) return
  const dY = e.touches[0].clientY - dragStart.value.y
  const dR = -dY / (MAX_SHEET_HEIGHT - MIN_SHEET_HEIGHT)
  sheetHeightRatio.value = Math.min(1, Math.max(0, dragStart.value.ratio + dR))
}
const onTouchEnd = () => {
  isDragging.value = false
  sheetHeightRatio.value = sheetHeightRatio.value > 0.5 ? 1 : 0
}
const toggleSheet = () => {
  sheetHeightRatio.value = sheetHeightRatio.value > 0.5 ? 0 : 1
}

/* Tmap 스크립트 로더 */
function loadTmapScript() {
  return new Promise((resolve, reject) => {
    if (window.Tmapv3) return resolve()
    const exist = document.querySelector('script[data-tmap="v3"]')
    if (exist) {
      exist.addEventListener('load', resolve)
      exist.addEventListener('error', reject)
      return
    }
    const script = document.createElement('script')
    script.src = `https://apis.openapi.sk.com/tmap/vectorjs?version=1&appKey=${TMAP_APP_KEY}`
    script.dataset.tmap = 'v3'
    script.onload = resolve
    script.onerror = reject
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

/* 반경 또는 진료과 변경 시 재검색 */
watch(searchRadius, (newRadius) => {
  if (userLocation.value && !isRouteMode.value) {
    fetchHospitalsFromGeoServer(userLocation.value.lat, userLocation.value.lon, newRadius)
  }
})

watch(selectedMessages, () => {
   if (userLocation.value && !isRouteMode.value) {
     fetchHospitalsFromGeoServer(userLocation.value.lat, userLocation.value.lon, searchRadius.value)
   }
 }, { deep: true })
onMounted(() => {
  sheetHeightRatio.value = 0.3
  loadTmapScript().then(waitTmapReady).then(initTmap).catch(e => {
    console.error('Tmap 로딩 실패:', e)
    alert('지도를 불러오지 못했습니다. 네트워크/Tmap 콘솔 설정을 확인해주세요.')
  })
})

onBeforeUnmount(() => {
  clearMarkers()
  clearRouteData()
  if (map.value) map.value.destroy()
})
</script>

<!-- 커스텀 CSS 없음: Bootstrap/Icons 유틸만 사용 -->
