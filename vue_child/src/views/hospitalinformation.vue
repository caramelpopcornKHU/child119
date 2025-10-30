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
  <!-- 바텀시트 -->
  <div class="position-fixed start-0 end-0 bg-white  rounded-top-4 shadow-lg " :style="sheetStyle" ref="bottomSheet"
    @touchstart="onTouchStart" @touchmove="onTouchMove" @touchend="onTouchEnd" style="z-index: 2;">
    <!-- 그립 -->
    <div class="py-3 d-flex justify-content-center" @click="toggleSheet" style="cursor: grab;">
      <div class="rounded-pill bg-body-secondary" style="width:40px;height:5px;"></div>
    </div>

    <!-- 내용 -->
    <div class="d-flex flex-column" style="height: calc(100% - 60px); overflow: hidden;">
      <!-- 상단 라인 -->
      <div class="d-flex align-items-center justify-content-between gap-2 mb-3 ms-2 me-2 flex-shrink-0">
        <div class="d-flex align-items-center gap-2 flex-wrap">
          <div class="d-flex flex-wrap gap-1" v-if="selectedDepartments.length">
            <span v-for="dept in selectedDepartments" :key="dept"
              class="badge text-bg-primary rounded-pill d-inline-flex align-items-center">
              {{ dept }}
              <button type="button" class="btn-close btn-close-white btn-sm ms-1" aria-label="삭제"
                @click="removeDepartment(dept)" style="transform: scale(.8);"></button>
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
              <h6 class="card-title fw-bold mb-1">{{ hospital.properties.hospital_name || '병원명 없음' }}</h6>
              <span v-if="hospital.properties.hospital_type"
                class="badge bg-success-subtle text-success-emphasis rounded-pill">
                <svg xmlns="http://www.w3.org/2000/svg" width="22px" height="15px" viewBox="0 0 24 24">
                  <path fill="#008A64"
                    d="M19 7h-8v7H3V5H1v15h2v-3h18v3h2v-9a4 4 0 0 0-4-4M7 13a3 3 0 0 0 3-3a3 3 0 0 0-3-3a3 3 0 0 0-3 3a3 3 0 0 0 3 3" />
                </svg> {{ hospital.properties.hospital_type }}
              </span>
            </div>

            <div class="mb-2" v-if="hospital.properties.dept_name">
              <span class="badge bg-danger-subtle text-danger-emphasis me-1">{{ hospital.properties.dept_name }}</span>
            </div>

            <div class="text-muted small d-flex align-items-center mb-3">
              <span><i class="bi bi-geo-alt me-1"></i> {{ hospital.distanceKm }}km</span>
            </div>

            <div class="d-grid gap-2 d-sm-flex">
              <button class="btn btn-outline-primary w-100" v-if="hospital.properties.hospital_tel"
                @click="callHospital(hospital.properties.hospital_tel)">
                <i class="bi bi-telephone-fill"></i> 전화
              </button>
              <button class="btn btn-primary w-100" @click="navigateToHospital(hospital.coordinates)">
                <i class="bi bi-sign-turn-right-fill"></i> 길찾기
              </button>
            </div>
          </div>
        </div>

        <div v-if="hospitals.length === 0" class="text-center text-muted py-4">
          <i class="bi bi-search fs-1 d-block mb-2"></i>
          <p class="mb-1">주변에 병원 정보가 없습니다.</p>
          <small class="text-muted">진료과를 변경하거나 다시 시도해주세요.</small>
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
            <i class="bi bi-clipboard2-pulse me-1"></i> 진료과 선택
          </h5>
          <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="닫기"></button>
        </div>

        <div class="modal-body">
          <div class="d-flex flex-wrap gap-2">
            <template v-for="opt in departmentOptions" :key="opt">
              <input class="btn-check" type="checkbox" :id="`dept-${opt}`" :value="opt" v-model="selectedDepartments" />
              <label class="btn btn-outline-secondary rounded-pill py-1 px-3" :for="`dept-${opt}`">{{ opt }}</label>
            </template>
          </div>

          <div v-if="selectedDepartments.length" class="mt-3 text-muted small">
            <i class="bi bi-check2-circle"></i> {{ selectedDepartments.length }}개 진료과 선택됨
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

</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, watch } from 'vue'
import axios from 'axios'

/* 진료과 필터 상태 */
const departmentOptions = [
  '내과', '외과', '정형외과', '신경외과', '마취통증의학과',
  '소아청소년과', '피부과', '영상의학과', '재활의학과', '가정의학과',
  '응급의학과', '성형외과', '안과', '한방내과', '산부인과'
]
const selectedDepartments = ref([])
function removeDepartment(dept) {
  selectedDepartments.value = selectedDepartments.value.filter(d => d !== dept)
}

/* 병원 데이터 상태 */
const hospitals = ref([])
const userLocation = ref(null)
const searchRadius = ref(5) // 기본 5km 반경

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
      `&typeName=hospital:sql2` + // hospital 저장소의 sql2 레이어
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

        return {
          id: feature.id,
          properties: feature.properties,
          coordinates: coords,
          distance: distance,
          distanceKm: (distance / 1000).toFixed(1)
        }
      }).filter(h => h.distance <= radiusKm * 1000)

      console.log('거리 필터링 후 병원 수:', hospitalsWithDistance.length)
      console.log('선택된 진료과:', selectedDepartments.value)

      // 진료과 필터링 (선택된 진료과가 있을 경우)
      if (selectedDepartments.value.length > 0) {
        const beforeFilter = hospitalsWithDistance.length
        hospitalsWithDistance = hospitalsWithDistance.filter(h => {
          const deptName = h.properties.dept_name || ''
          // 선택된 진료과 중 하나라도 dept_name에 포함되면 true (OR 조건)
          return selectedDepartments.value.some(dept => deptName.includes(dept))
        })
        console.log(`진료과 필터링: ${beforeFilter}개 → ${hospitalsWithDistance.length}개`)
      }

      // 거리순 정렬
      hospitalsWithDistance.sort((a, b) => a.distance - b.distance)

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
    const html = `
      <div style="min-width:240px;max-width:280px">
        <div style="font-weight:700;font-size:14px;margin-bottom:4px;">${hospital.properties.hospital_name || '병원명 없음'}</div>
        <div style="font-size:12px;color:#6c757d;margin-bottom:6px;">
          <div><i class="bi bi-pin-map-fill"></i> ${hospital.properties.hospital_address || '주소 정보 없음'}</div>
          <div><i class="bi bi-telephone"></i> ${hospital.properties.hospital_tel || '전화 없음'}</div>
          ${hospital.properties.dept_name ? `<div><i class="bi bi-ui-checks"></i> ${hospital.properties.dept_name}</div>` : ''}
        </div>
        <div class="d-flex gap-2">
          <a href="${hospital.properties.hospital_tel ? `tel:${hospital.properties.hospital_tel}` : '#'}"
             class="btn btn-outline-primary flex-fill ${hospital.properties.hospital_tel ? '' : 'disabled'}">전화</a>
          <a href="https://map.naver.com/v5/search/${encodeURIComponent(hospital.properties.hospital_name || '')}" target="_blank" rel="noopener"
             class="btn btn-primary flex-fill">길찾기</a>
        </div>
      </div>`
    const info = new window.Tmapv3.InfoWindow({
      position: position,
      content: html,
      type: 2,
      map: null,
      offset: new window.Tmapv3.Point(0, -10)
    })
    window.Tmapv3.event.addListener(marker, 'click', () => {
      markers.forEach(m => m.__info?.setMap(null))
      info.setMap(map.value)
    })
    marker.__info = info
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

/* 병원 길찾기 */
function navigateToHospital(coordinates) {
  if (!coordinates || coordinates.length < 2) {
    alert('위치 정보가 없습니다.')
    return
  }
  // Naver 지도로 길찾기 (목적지 좌표 전달)
  const lon = coordinates[0]
  const lat = coordinates[1]
  const naverMapUrl = `nmap://route/car?dlat=${lat}&dlng=${lon}&dname=목적지&appname=com.example.child119`

  // 모바일에서 앱 열기 시도, 실패하면 웹으로
  window.location.href = naverMapUrl

  // 2초 후에도 페이지가 안 바뀌면 웹 버전으로
  setTimeout(() => {
    window.open(`https://map.naver.com/v5/directions/-/-/-/car?c=${lon},${lat},15,0,0,0,dh`, '_blank')
  }, 2000)
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
    script.src = `https://apis.openapi.sk.com/tmap/vectorjs?version=1&appKey=pAs3duhDpE5cYgopfzzDmw8anJPaBak8HAdTerg8`
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
  if (userLocation.value) {
    fetchHospitalsFromGeoServer(userLocation.value.lat, userLocation.value.lon, newRadius)
  }
})

watch(selectedDepartments, () => {
  if (userLocation.value) {
    fetchHospitalsFromGeoServer(userLocation.value.lat, userLocation.value.lon, searchRadius.value)
  }
}, { deep: true })

onMounted(() => {
  sheetHeightRatio.value = 0.3 // 초기에 살짝 올려서 리스트 보이게
  loadTmapScript().then(waitTmapReady).then(initTmap).catch(e => {
    console.error('Tmap 로딩 실패:', e)
    alert('지도를 불러오지 못했습니다. 네트워크/Tmap 콘솔 설정을 확인해주세요.')
  })
})
onBeforeUnmount(() => {
  clearMarkers()
  if (map.value) map.value.destroy()
})
</script>

<!-- 커스텀 CSS 없음: Bootstrap/Icons 유틸만 사용 -->
