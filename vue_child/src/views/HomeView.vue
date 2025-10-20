<template>
  <div class="home-container">
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

      <div class="mt-2 d-flex flex-wrap gap-1">
        <span class="badge text-bg-info rounded-pill">
          <i class="bi bi-search"></i> {{ displayKeyword }}
        </span>
      </div>

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
        <div class="d-flex align-items-center justify-content-between gap-2 mb-3">
          <div class="d-flex align-items-center gap-2 flex-wrap">
            <button
              type="button"
              class="btn btn-light border rounded-pill px-3"
              data-bs-toggle="modal"
              data-bs-target="#symptomModal"
            >
              <i class="bi bi-clipboard2-pulse me-1"></i> 증상 확인
            </button>

            <span class="badge text-bg-info rounded-pill">
              {{ displayKeyword }}
            </span>

            <div class="d-flex flex-wrap gap-1" v-if="selectedSymptoms.length">
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

          <button
            class="btn btn-sm btn-outline-secondary flex-shrink-0"
            @click="sortByDistance"
            :disabled="hospitals.length === 0"
          >
            <i class="bi bi-filter-left"></i> 거리순
          </button>
        </div>

        <div class="d-flex justify-content-between align-items-center mb-3">
          <h5 class="fw-bold mb-0">응급실 리스트</h5>
          <span v-if="hospitals.length > 0" class="badge bg-secondary">{{ hospitals.length }}개</span>
        </div>

        <div class="card shadow-sm mb-3" v-for="h in hospitals" :key="h.id">
          <div class="card-body">
            <div class="d-flex justify-content-between align-items-start mb-2">
              <h6 class="card-title fw-bold mb-0">{{ h.name }}</h6>
              <span class="badge bg-success-subtle text-success-emphasis rounded-pill">
                <i class="bi bi-geo-alt"></i> {{ (h.distance/1000).toFixed(2) }}km
              </span>
            </div>

            <div class="small text-muted mb-2">
              <div class="mb-1"><i class="bi bi-pin-map-fill me-1"></i>{{ h.address || '주소 정보 없음' }}</div>
              <div class="mb-1"><i class="bi bi-telephone me-1"></i>{{ h.tel || '전화번호 정보 없음' }}</div>
              <div class="mb-1"><i class="bi bi-clock-history me-1"></i>{{ h.hours || '운영시간 정보 없음' }}</div>
              <div v-if="h.services?.length"><i class="bi bi-ui-checks me-1"></i>{{ h.services.join(', ') }}</div>
            </div>

            <div class="d-grid gap-2 d-sm-flex">
              <a class="btn btn-outline-primary w-100" :href="h.tel ? `tel:${h.tel}` : '#'" :class="{'disabled': !h.tel}">
                <i class="bi bi-telephone-fill"></i> 전화
              </a>
              <a class="btn btn-primary w-100"
                 :href="`https://map.naver.com/v5/search/${encodeURIComponent(h.name)}`"
                 target="_blank" rel="noopener noreferrer">
                <i class="bi bi-sign-turn-right-fill"></i> 길찾기
              </a>
            </div>
          </div>
        </div>

        <div v-if="loadingHospitals" class="text-center text-muted py-4">
          <div class="spinner-border spinner-border-sm me-2" role="status"><span class="visually-hidden">검색 중...</span></div>
          <div class="mt-2">주변 응급실 검색 중...</div>
        </div>

        <div v-else-if="!loadingHospitals && hospitals.length === 0 && searchAttempted" class="text-center text-muted py-4">
          <i class="bi bi-search fs-1 d-block mb-2"></i>
          <p class="mb-1">반경 {{ (lastRadius/1000).toFixed(0) }}km 내 검색 결과가 없습니다.</p>
          <small class="text-muted">증상을 변경하거나 다시 시도해주세요.</small>
        </div>

        <div v-else-if="!searchAttempted" class="text-center text-muted py-4">
          <i class="bi bi-clipboard2-pulse fs-1 d-block mb-2"></i>
          <p>증상을 선택하고 검색을 실행해 주세요.</p>
        </div>
      </div>
    </div>

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
                <span class="badge text-bg-primary rounded-pill">{{ keywordWithSymptoms }}</span>
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
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'

const symptomOptions = ['발열','감기','기침','호흡곤란','구토','설사','복통','발진','화상','경련']
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

const bottomSheet = ref(null)
const isDragging = ref(false)
const sheetHeightRatio = ref(0)
const dragStart = ref({ y: 0, ratio: 0 })

const MAX_SHEET_HEIGHT = window.innerHeight * 0.8
const MIN_SHEET_HEIGHT = 100

const sheetY = computed(() => (MAX_SHEET_HEIGHT - MIN_SHEET_HEIGHT) * (1 - sheetHeightRatio.value))
const sheetStyle = computed(() => ({
  transition: isDragging.value ? 'none' : 'transform 0.3s ease-out',
  transform: `translateY(${sheetY.value}px)`,
  height: `${MAX_SHEET_HEIGHT}px`,
  bottom: `0px`,
  top: 'auto'
}))

const keywordBase = computed(() => '소아응급실')
const keywordWithSymptoms = computed(() => {
  const sym = selectedSymptoms.value.join(' ')
  return sym ? `소아응급실 ${sym}` : keywordBase.value
})
const displayKeyword = computed(() => useSymptomsMode.value ? keywordWithSymptoms.value : keywordBase.value)

function sortByDistance() {
  hospitals.value = [...hospitals.value].sort((a, b) => a.distance - b.distance)
}

const toRad = (d) => d * Math.PI / 180

function haversineMeter(lat1, lon1, lat2, lon2) {
  const R = 6371000
  const dLat = toRad(lat2 - lat1)
  const dLon = toRad(lon2 - lon1)
  const a = Math.sin(dLat/2)**2 + Math.cos(toRad(lat1))*Math.cos(toRad(lat2))*Math.sin(dLon/2)**2
  return 2 * R * Math.asin(Math.sqrt(a))
}

function clearMarkers() {
  markers.forEach(m => m.setMap(null))
  markers = []
}

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
        <div style="display:flex;gap:8px;">
          <a href="${h.tel ? `tel:${h.tel}` : '#'}"
             style="flex:1;text-align:center;border:1px solid #0d6efd;color:#0d6efd;border-radius:6px;padding:6px 8px;text-decoration:none;${h.tel?'':'pointer-events:none;opacity:.5;'}">전화</a>
          <a href="https://map.naver.com/v5/search/${encodeURIComponent(h.name)}" target="_blank" rel="noopener"
             style="flex:1;text-align:center;background:#0d6efd;color:#fff;border-radius:6px;padding:6px 8px;text-decoration:none;">길찾기</a>
        </div>
      </div>`
    const info = new window.Tmapv3.InfoWindow({ position: pos, content: html, type: 2, map: null, offset: new window.Tmapv3.Point(0,-10) })
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
  if (!cur.value.lat || !cur.value.lon) {
    alert('현재 위치 확인 후 다시 시도해주세요.')
    return
  }

  const sanitizedRadius = Math.max(0, Number(radiusMeters) || 0)
  const radiusKm = Math.max(0.1, Math.min(33, sanitizedRadius / 1000 || 0))

  loadingHospitals.value = true
  searchAttempted.value = true
  hospitals.value = []
  lastRadius.value = sanitizedRadius
  useSymptomsMode.value = !!useSymptoms

  try {
    const searchKeyword = (useSymptoms ? keywordWithSymptoms.value : keywordBase.value).trim()

    // ❌ appKey를 쿼리에 넣지 말고
    const params = new URLSearchParams({
      version: '1',
      searchKeyword,
      centerLon: String(cur.value.lon),
      centerLat: String(cur.value.lat),
      radius: radiusKm.toString(),     // km 단위
      reqCoordType: 'WGS84GEO',
      resCoordType: 'WGS84GEO',
      page: '1',
      count: '20'
    })

    const url = `https://apis.openapi.sk.com/tmap/pois?${params.toString()}`

    // ✅ 헤더에만 appKey 전달 + Accept 명시
    const res = await fetch(url, {
      method: 'GET',
      mode: 'cors',
      headers: {
        'Accept': 'application/json',
        'appKey': TMAP_APP_KEY
      }
    })

    const raw = await res.text()

    if (!res.ok) {
      console.error('[POI ERROR]', res.status, raw)
      alert('POI 검색 실패: ' + res.status)
      return
    }

    if (!raw || !raw.trim()) {
      console.warn('Tmap POI 응답이 비어 있습니다.', { url })
      alert('응답이 비어 있어 검색 결과를 불러오지 못했습니다. (도메인 등록/appKey 확인)')
      return
    }

    let data
    try {
      data = JSON.parse(raw)
    } catch (e) {
      console.error('Tmap POI 응답 파싱 실패:', e, raw)
      alert('검색 응답을 해석할 수 없습니다.')
      return
    }

    const pois = data?.searchPoiInfo?.pois?.poi || []

    const list = pois.map(poi => {
      const lat = parseFloat(poi.noorLat ?? poi.frontLat ?? poi.lat)
      const lon = parseFloat(poi.noorLon ?? poi.frontLon ?? poi.lon)
      const distance = (lat && lon) ? haversineMeter(cur.value.lat, cur.value.lon, lat, lon) : Infinity
      return {
        id: poi.id,
        name: poi.name,
        address: poi.upperAddrName && poi.middleAddrName
          ? `${poi.upperAddrName} ${poi.middleAddrName} ${poi.roadName || ''}`.trim()
          : (poi.newAddressList?.newAddress?.[0]?.fullAddressRoad || poi.detailAddrName || ''),
        tel: poi.telNo || '',
        hours: poi.bizhourInfo || '',
        services: useSymptoms ? selectedSymptoms.value.slice() : [],
        lat, lon, distance
      }
    })

    hospitals.value = list
      .filter(x => Number.isFinite(x.distance) && x.distance <= sanitizedRadius)
      .sort((a,b) => a.distance - b.distance)

    drawMarkers(hospitals.value)
    if (hospitals.value.length) sheetHeightRatio.value = 1

  } catch (e) {
    console.error('Tmap POI 검색 실패:', e)
    alert('주변 소아응급실 검색 중 오류가 발생했습니다.')
  } finally {
    loadingHospitals.value = false
  }
}



function onConfirmSymptoms() {
  searchER(3000, true)
}

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
    navigator.geolocation.getCurrentPosition(
      pos => {
        const lat = pos.coords.latitude
        const lon = pos.coords.longitude
        const currentPos = new window.Tmapv3.LatLng(lat, lon)
        cur.value = { lat, lon }

        new window.Tmapv3.Marker({ position: currentPos, map: map.value })
        new window.Tmapv3.InfoWindow({
          position: currentPos,
          content: "<div style='padding:5px;background:#fff;border:1px solid #ccc;border-radius:5px;'>현재 위치</div>",
          offset: new window.Tmapv3.Point(0,-30),
          type:2,
          map: map.value
        })
        map.value.setCenter(currentPos)
        map.value.setZoom(17)

        searchER(5000, false)
      },
      err => {
        console.error('Geolocation 오류:', err)
        alert('현재 위치를 가져올 수 없습니다. (위치 권한/HTTPS 확인)')
        cur.value = { lat: 37.5662952, lon: 126.9779451 }
        searchER(5000, false)
      }
    )
  } else {
    alert('이 브라우저에서는 위치 정보를 지원하지 않습니다.')
    cur.value = { lat: 37.5662952, lon: 126.9779451 }
    searchER(5000, false)
  }
}

const onTouchStart = e => {
  isDragging.value = true
  dragStart.value = { y: e.touches[0].clientY, ratio: sheetHeightRatio.value }
  e.preventDefault()
}

const onTouchMove = e => {
  if (!isDragging.value) return
  const deltaY = e.touches[0].clientY - dragStart.value.y
  const deltaRatio = -deltaY / (MAX_SHEET_HEIGHT - MIN_SHEET_HEIGHT)
  sheetHeightRatio.value = Math.min(1, Math.max(0, dragStart.value.ratio + deltaRatio))
}

const onTouchEnd = () => {
  isDragging.value = false
  sheetHeightRatio.value = sheetHeightRatio.value > 0.5 ? 1 : 0
}

const toggleSheet = () => {
  sheetHeightRatio.value = sheetHeightRatio.value > 0.5 ? 0 : 1
}

function loadTmapScript () {
  return new Promise((resolve, reject) => {
    if (window.Tmapv3) return resolve()
    const exist = document.querySelector('script[data-tmap="v3"]')
    if (exist) {
      exist.addEventListener('load', resolve)
      exist.addEventListener('error', reject)
      return
    }
    const script = document.createElement('script')
    script.src = `https://apis.openapi.sk.com/tmap/vectorjs?version=1&appKey=${ pAs3duhDpE5cYgopfzzDmw8anJPaBak8HAdTerg8}`
    script.dataset.tmap = 'v3'
    script.onload = resolve
    script.onerror = reject
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

onMounted(() => {
  sheetHeightRatio.value = 0
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

<style scoped>
.map-overlay-tl {
  position: absolute;
  top: 12px;
  left: 12px;
  z-index: 1100;
  pointer-events: none;
}
.map-overlay-tl > * { pointer-events: auto; }

@media (max-width: 400px) {
  .map-overlay-tl .btn { padding: 4px 10px; font-size: 0.9rem; }
}

.home-container {
  position: relative;
  height: calc(100vh - 120px);
  width: 100%;
  overflow: hidden;
}
#map_div {
  position: absolute; inset: 0;
  width: 100%; height: 100%;
}
.bottom-sheet {
  position: fixed; left: 0; right: 0;
  background-color: #ffffff;
  border-top-left-radius: 20px; border-top-right-radius: 20px;
  box-shadow: 0 -4px 20px rgba(0,0,0,.1);
  z-index: 1000;
}
.handle-wrapper { padding: 15px; cursor: grab; display: flex; justify-content: center; }
.handle { width: 40px; height: 5px; background-color: #dee2e6; border-radius: 2.5px; }
.sheet-content { overflow-y: auto; height: calc(100% - 50px); padding-bottom: 20px; }
#symptomModal .btn-outline-secondary { white-space: nowrap; }
</style>
