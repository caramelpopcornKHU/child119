<template>
  <div class="d-flex justify-content-center align-items-center position-relative mt-1"
    style="height: calc(100vh - 140px); ">

    <!-- 지도 -->
    <div id="map_div" class="position-fixed top-0 start-0 w-100" ></div>



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
  <div class="position-fixed start-0 end-0 bg-white padding-bottom  rounded-top-4 shadow-lg " :style="sheetStyle" ref="bottomSheet"
    @touchstart="onTouchStart" @touchmove="onTouchMove" @touchend="onTouchEnd" style="z-index: 2; padding-bottom: 150px;">
    
    <!-- 그립 -->
    <div class="py-3 d-flex justify-content-center" @click="toggleSheet" style="cursor: grab;">
      <div class="bg-secondary-subtle rounded-pill w-25" style="height:5px;"></div>
    </div>
  <!-- 핸들 -->
    <div class="py-3 d-flex justify-content-center" @click="toggleSheet" ></div>

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
      <svg xmlns="http://www.w3.org/2000/svg" 
      width="24" height="24" viewBox="0 0 24 24">
      <path fill="currentColor" d="M12 20a8 8 0 0 0 8-8a8 8 0 0 0-8-8a8 8 0 0 0-8 8a8 8 0 0 0 8 8m0-18a10 10 0 0 1 10 10a10 10 0 0 1-10 10C6.47 22 2 17.5 2 12A10 10 0 0 1 12 2m.5 5v5.25l4.5 2.67l-.75 1.23L11 13V7z"/>
      </svg>
      시간순
    </button>
  </div>
</div>

      <!-- 리스트 스크롤 영역 -->
      <div class="overflow-auto px-3 ">
        

        <!-- 응급실 목록 (데모) -->
        <div class="card shadow-sm mb-3" v-for="h in listHospitals" :key="h.id">
          <div class="card-body" @click="goToHospitalDetail">
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
              <button class="btn btn-primary w-100">
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
import { ref, computed, onMounted, onBeforeUnmount } from 'vue';


import { useRouter } from 'vue-router';

const router = useRouter()

function goToHospitalDetail() {
  router.push('/hospitalDetail')
}





/* 데모 데이터: hospitals가 비어있을 때 사용 */
// const demoHospitals = ref([
//   { id: 'demo-1', name: '서울대학교병원', distanceKm: 0.8, etaMin: 7 },
//   { id: 'demo-2', name: '구로병원', distanceKm: 0.2, etaMin: 5 },
//   { id: 'demo-3', name: '일산병원', distanceKm: 1, etaMin: 3 },
//   { id: 'demo-4', name: '대림병원', distanceKm: 0.9, etaMin: 2 },
//   { id: 'demo-5', name: '구청병원', distanceKm: 2, etaMin: 10 },
//   { id: 'demo-6', name: '병원', distanceKm: 0.5, etaMin: 15 },
// ])

/* 정렬 상태 (토글용) */
const distanceAsc = ref(true)

/* 실제 렌더링 목록: 검색 결과가 있으면 그걸, 없으면 데모를 사용 */
// const listHospitals = computed(() => {
//   if (hospitals.value.length > 0) {
//     // hospitals는 meter 단위 distance가 이미 있음
//     return hospitals.value.map(h => ({
//       id: h.id,
//       name: h.name,
//       distanceKm: (h.distance ?? 0) / 1000,
//       etaMin: Math.max(1, Math.round((h.distance ?? 0) / 1000 / 0.4)), // 대략 24km/h 가정한 임시 ETA
//     }))
//   }
//   return demoHospitals.value
// })


/* 실제 렌더링 목록: 항상 WFS에서 가져온 hospitals 사용 */
const listHospitals = computed(() => {

  return hospitals.value.map(h => ({
    id: h.id,
    name: h.name,
    distanceKm: (h.distance ?? 0) / 1000,
    // 대략 24km/h 가정(도보/차 혼합 임시치): 0.4km/min
    etaMin: Math.max(1, Math.round(((h.distance ?? 0) / 1000) / 0.4)),
  }))
})






/* 거리순 정렬: hospitals가 있으면 hospitals를, 아니면 데모를 정렬 */
function sortByDistance() {
  // const arr = hospitals.value.length ? hospitals.value : demoHospitals.value
  // const key = hospitals.value.length ? (h => h.distance ?? Infinity) : (h => h.distanceKm)

  // arr.sort((a, b) => {
  //   const A = key(a)
  //   const B = key(b)
  //   return distanceAsc.value ? A - B : B - A
  // })
  hospitals.value.sort((a, b) => {
    const A = a.distance ?? Infinity
    const B = b.distance ?? Infinity
    return distanceAsc.value ? A - B : B - A
  })
  
}


function sortByTime() {
  //demoHospitals.value.sort((a, b) => a.etaMin - b.etaMin);

  // ETA = (distance(km) / 0.4)
  const eta = (h) => ((h.distance ?? Infinity) / 1000) / 0.4
  hospitals.value.sort((a, b) => eta(a) - eta(b))


}






const symptomOptions = ['발열', '감기', '기침', '호흡곤란', '구토', '설사', '복통', '발진', '화상', '경련']
const selectedSymptoms = ref([])
const removeSymptom = (sym) => { selectedSymptoms.value = selectedSymptoms.value.filter(s => s !== sym) }

const TMAP_APP_KEY = 'pAs3duhDpE5cYgopfzzDmw8anJPaBak8HAdTerg8'
const map = ref(null)
const cur = ref({ lat: null, lon: null })

const hospitals = ref([])
const loadingHospitals = ref(false)
const searchAttempted = ref(false)
const lastRadius = ref(0)
const useSymptomsMode = ref(false)

let markers = []

/* 바텀시트 상태 */
const bottomSheet = ref(null)
const isDragging = ref(false)
const sheetHeightRatio = ref(0)
const dragStart = ref({ y: 0, ratio: 0 })

const MAX_SHEET_HEIGHT = window.innerHeight * 0.8
const MIN_SHEET_HEIGHT = 220

const sheetY = computed(() => (MAX_SHEET_HEIGHT - MIN_SHEET_HEIGHT) * (1 - sheetHeightRatio.value))
const sheetStyle = computed(() => ({
  transition: isDragging.value ? 'none' : 'transform 0.3s ease-out',
  transform: `translateY(${sheetY.value}px)`,
  height: `${MAX_SHEET_HEIGHT}px`,
  bottom: `0px`,
  top: 'auto'
}))

/* 키워드 */
const keywordBase = computed(() => '소아응급실')
const keywordWithSymptoms = computed(() => {
  const sym = selectedSymptoms.value.join(' ')
  return sym ? `소아응급실 ${sym}` : keywordBase.value
})
const displayKeyword = computed(() => useSymptomsMode.value ? keywordWithSymptoms.value : keywordBase.value)



const toRad = (d) => d * Math.PI / 180
function haversineMeter(lat1, lon1, lat2, lon2) {
  const R = 6371000
  const dLat = toRad(lat2 - lat1)
  const dLon = toRad(lon2 - lon1)
  const a = Math.sin(dLat / 2) ** 2 + Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) * Math.sin(dLon / 2) ** 2
  return 2 * R * Math.asin(Math.sqrt(a))
}

function clearMarkers() { markers.forEach(m => m.setMap(null)); markers = [] }

function drawMarkers(items) {
  if (!map.value) return
  clearMarkers()
  const bounds = new window.Tmapv3.LatLngBounds()

  items.forEach(h => {
    const pos = new window.Tmapv3.LatLng(h.lat, h.lon)
    bounds.extend(pos)

    const marker = new window.Tmapv3.Marker({ position: pos, map: map.value })
    const html = `
      <div style="min-width:240px;max-width:280px">
        <div style="font-weight:700;font-size:14px;margin-bottom:4px;">${h.name}</div>
        <div style="font-size:12px;color:#6c757d;margin-bottom:6px;">
          <div><i class="bi bi-pin-map-fill"></i> ${h.address || '주소 정보 없음'}</div>
          <div><i class="bi bi-telephone"></i> ${h.tel || '전화 없음'}</div>
          <div><i class="bi bi-clock-history"></i> ${h.hours || '운영시간 정보 없음'}</div>
          ${h.services?.length ? `<div><i class="bi bi-ui-checks"></i> ${h.services.join(', ')}</div>` : ''}
        </div>
        <div class="d-flex gap-2">
          <a href="${h.tel ? `tel:${h.tel}` : '#'}"
             class="btn btn-outline-primary flex-fill ${h.tel ? '' : 'disabled'}">전화</a>
          <a href="https://map.naver.com/v5/search/${encodeURIComponent(h.name)}" target="_blank" rel="noopener"
             class="btn btn-primary flex-fill">길찾기</a>
        </div>
      </div>`
    const info = new window.Tmapv3.InfoWindow({ position: pos, content: html, type: 2, map: null, offset: new window.Tmapv3.Point(0, -10) })
    window.Tmapv3.event.addListener(marker, 'click', () => {
      markers.forEach(m => m.__info?.setMap(null))
      info.setMap(map.value)
    })
    marker.__info = info
    markers.push(marker)
  })

  if (items.length) map.value.fitBounds(bounds)
}

async function searchER(radiusMeters, useSymptoms) {
  // if (!cur.value.lat || !cur.value.lon) { alert('현재 위치 확인 후 다시 시도해주세요.'); return }

  // const sanitizedRadius = Math.max(0, Number(radiusMeters) || 0)
  // const radiusKm = Math.max(0.1, Math.min(33, sanitizedRadius / 1000 || 0))

  // loadingHospitals.value = true
  // searchAttempted.value = true
  // hospitals.value = []
  // lastRadius.value = sanitizedRadius
  // useSymptomsMode.value = !!useSymptoms

  // try {
  //   const searchKeyword = (useSymptoms ? keywordWithSymptoms.value : keywordBase.value).trim()
  //   const params = new URLSearchParams({
  //     version: '1',
  //     searchKeyword,
  //     centerLon: String(cur.value.lon),
  //     centerLat: String(cur.value.lat),
  //     radius: radiusKm.toString(),
  //     reqCoordType: 'WGS84GEO',
  //     resCoordType: 'WGS84GEO',
  //     page: '1',
  //     count: '20'
  //   })
  //   const url = `https://apis.openapi.sk.com/tmap/pois?${params}`

  //   const res = await fetch(url, {
  //     method: 'GET',
  //     mode: 'cors',
  //     headers: { 'Accept': 'application/json', 'appKey': TMAP_APP_KEY }
  //   })
  //   const raw = await res.text()
  //   if (!res.ok) { console.error('[POI ERROR]', res.status, raw); alert('POI 검색 실패: ' + res.status); return }
  //   if (!raw?.trim()) { alert('응답이 비어 있습니다. (도메인 등록/appKey 확인)'); return }

  //   let data
  //   try { data = JSON.parse(raw) } catch (e) { console.error('파싱 실패', e, raw); alert('검색 응답을 해석할 수 없습니다.'); return }

  //   const pois = data?.searchPoiInfo?.pois?.poi || []
  //   const list = pois.map(poi => {
  //     const lat = parseFloat(poi.noorLat ?? poi.frontLat ?? poi.lat)
  //     const lon = parseFloat(poi.noorLon ?? poi.frontLon ?? poi.lon)
  //     const distance = (lat && lon) ? haversineMeter(cur.value.lat, cur.value.lon, lat, lon) : Infinity
  //     return {
  //       id: poi.id,
  //       name: poi.name,
  //       address:
  //         (poi.upperAddrName && poi.middleAddrName)
  //           ? `${poi.upperAddrName} ${poi.middleAddrName} ${poi.roadName || ''}`.trim()
  //           : (poi.newAddressList?.newAddress?.[0]?.fullAddressRoad || poi.detailAddrName || ''),
  //       tel: poi.telNo || '',
  //       hours: poi.bizhourInfo || '',
  //       services: useSymptoms ? selectedSymptoms.value.slice() : [],
  //       lat, lon, distance
  //     }
  //   })

  //   hospitals.value = list
  //     .filter(x => Number.isFinite(x.distance) && x.distance <= sanitizedRadius)
  //     .sort((a, b) => a.distance - b.distance)

  //   drawMarkers(hospitals.value)
  //   if (hospitals.value.length) sheetHeightRatio.value = 1
  // } catch (e) {
  //   console.error('Tmap POI 검색 실패:', e)
  //   alert('주변 소아응급실 검색 중 오류가 발생했습니다.')
  // } finally {
  //   loadingHospitals.value = false
  // }

if (!cur.value.lat || !cur.value.lon) { alert('현재 위치 확인 후 다시 시도해주세요.'); return }

  const sanitizedRadius = Math.max(0, Number(radiusMeters) || 0)
  loadingHospitals.value = true
  searchAttempted.value = true
  hospitals.value = []
  lastRadius.value = sanitizedRadius
  useSymptomsMode.value = !!useSymptoms

  try {
    const list = await fetchHospitalsFromWFS(cur.value.lon, cur.value.lat, sanitizedRadius)
    hospitals.value = list.sort((a, b) => (a.distance ?? Infinity) - (b.distance ?? Infinity))
    drawMarkers(hospitals.value)
    if (hospitals.value.length) sheetHeightRatio.value = 1
  } catch (e) {
    console.error('WFS 검색 실패:', e)
    alert('주변 병원 검색 중 오류가 발생했습니다. (WFS)')
  } finally {
    loadingHospitals.value = false
  }
 }

/** GeoServer WFS에서 DWithin으로 병원 받아오기 */
async function fetchHospitalsFromWFS(centerLon, centerLat, radiusMeters) {
  // CQL: DWithin(geom, POINT(lon lat), radius, meters)
  const cql = `DWithin(geom, POINT(${centerLon} ${centerLat}), ${radiusMeters}, meters)`

  const base = 'https://api.child119.com/geoserver/hospital/wfs'
  const params = new URLSearchParams({
    service: 'WFS',
    version: '1.0.0',
    request: 'GetFeature',
    typeName: 'hospital:emergency',
    outputFormat: 'application/json',
    cql_filter: cql
  })

  const url = `${base}?${params.toString()}`
  const res = await fetch(url, { method: 'GET' })
  if (!res.ok) {
    const t = await res.text().catch(() => '')
    throw new Error(`WFS ${res.status} ${t}`)
  }
  const data = await res.json()
  const feats = Array.isArray(data?.features) ? data.features : []

  // 안전한 포인트 추출(Polygon 등일 경우 중심점 계산은 간단히 첫 좌표 사용)
  const getLonLat = (geom) => {
    if (!geom) return [undefined, undefined]
    if (geom.type === 'Point') return geom.coordinates
    // Multi* 또는 Line/Polygon의 경우 대충 첫 좌표 사용 (꼭 필요하면 centroid 계산로직 추가)
    try {
      let coords = geom.coordinates
      while (Array.isArray(coords[0])) coords = coords[0]
      return coords // [lon, lat] 가정
    } catch { return [undefined, undefined] }
  }

  const coalesce = (...vals) => vals.find(v => v !== undefined && v !== null && String(v).trim() !== '') ?? ''

  return feats.map((f, i) => {
    const [lon, lat] = getLonLat(f.geometry)
    const p = f.properties || {}
    const name = coalesce(p.name, p.hospname, p.yadmNm, p.inst_nm, p.emergency_nm, `병원 ${i + 1}`)
    const address = coalesce(p.addr, p.road_address, p.addrRoad, p.address, p.doro, '')
    const tel = coalesce(p.tel, p.phone, p.telno, p.telNo, '')
    const hours = coalesce(p.hours, p.oper_time, '')
    const distance = (Number.isFinite(lat) && Number.isFinite(lon))
      ? haversineMeter(centerLat, centerLon, lat, lon)
      : Infinity
    return { id: f.id || `wfs-${i}`, name, address, tel, hours, lat, lon, distance }
  })
}






function onConfirmSymptoms() { searchER(3000, true) }

/* 지도 초기화 */
const initTmap = () => {
  const el = document.getElementById('map_div')
  if (!el) return
  map.value = new window.Tmapv3.Map('map_div', {
    center: new window.Tmapv3.LatLng(37.566295, 126.9779451),
    width: '100%', height: '100%', zoom: 16
  })

  if (navigator.geolocation) {
    navigator.geolocation.getCurrentPosition(
      pos => {
        const lat = pos.coords.latitude, lon = pos.coords.longitude
        const currentPos = new window.Tmapv3.LatLng(lat, lon)
        cur.value = { lat, lon }
        new window.Tmapv3.Marker({ position: currentPos, map: map.value })
        new window.Tmapv3.InfoWindow({
          position: currentPos,
          content: "<div class='p-1 bg-white border rounded-2'>현재 위치</div>",
          offset: new window.Tmapv3.Point(0, -30),
          type: 2, map: map.value
        })
        map.value.setCenter(currentPos); map.value.setZoom(17)
        searchER(5000, false)
      },
      err => {
        console.error('Geolocation 오류:', err)
        alert('현재 위치를 가져올 수 없습니다. (위치 권한/HTTPS 확인)')
        cur.value = { lat: 37.566295, lon: 126.9779451 }
        searchER(5000, false)
      }
    )
  } else {
    alert('이 브라우저에서는 위치 정보를 지원하지 않습니다.')
    cur.value = { lat: 37.566295, lon: 126.9779451 }
    searchER(5000, false)
  }
}

/* 바텀시트 제스처 */
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


/* Tmap 스크립트 로더 */
function loadTmapScript() {
  return new Promise((resolve, reject) => {
    if (window.Tmapv3) return resolve()
    const exist = document.querySelector('script[data-tmap="v3"]')
    if (exist) { exist.addEventListener('load', resolve); exist.addEventListener('error', reject); return }
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

onMounted(() => {
  sheetHeightRatio.value = 0; loadTmapScript().then(waitTmapReady).then(initTmap).catch(e => {
    console.error('Tmap 로딩 실패:', e)
    alert('지도를 불러오지 못했습니다. 네트워크/Tmap 콘솔 설정을 확인해주세요.')
  })
})
onBeforeUnmount(() => { clearMarkers(); if (map.value) map.value.destroy() })




onMounted(() => {
  const grip = document.querySelector('.sheet-grip')
  if (grip) {
    grip.addEventListener('touchstart', onGripTouchStart, { passive: false })
    grip.addEventListener('touchmove', onGripTouchMove, { passive: false })
    grip.addEventListener('touchend', onGripTouchEnd, { passive: true })
  }
})






const onGripTouchStart = (e) => {
  isDragging.value = true
  dragStart.value = { y: e.touches[0].clientY, ratio: sheetHeightRatio.value }
  e.preventDefault()
}
const onGripTouchMove = (e) => {
  if (!isDragging.value) return
  e.preventDefault() // iOS에서 필수

  const dY = e.touches[0].clientY - dragStart.value.y
  const dR = -dY / (MAX_SHEET_HEIGHT - MIN_SHEET_HEIGHT)
  sheetHeightRatio.value = Math.min(1, Math.max(0, dragStart.value.ratio + dR))
}




const onGripTouchEnd = () => {
  isDragging.value = false
  sheetHeightRatio.value = sheetHeightRatio.value > 0.5 ? 1 : 0
}








</script>




<style scoped>
/* 리스트 위에 살짝 여유(선택) */
.er-list { padding-top: 4px; }

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
  transform: scale(1.02);              /* ↑ translateY(-) 제거 */
  background-color: #f0f6ff;
  border-color: #0d6efd;
  box-shadow: 0 6px 20px rgba(13,110,253,.15);
}

/* 클릭(눌림) */
.card:active {
  transform: scale(0.98);              /* 위아래 이동 없이 눌림만 */
  background-color: #e2ebff;
  box-shadow: 0 3px 10px rgba(0,0,0,.15);
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