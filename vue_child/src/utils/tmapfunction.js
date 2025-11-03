import { ref } from 'vue'
import axios from 'axios'
import { getHospitalFromWFSByBBox } from '@/utils/api_nono'

// 지도 및 위치 상태
export const map = ref(null)
export const userLocation = ref(null)

// 마커 관리
let hospitalMarkers = []
let currentLocationMarker = null
let currentLocationInfoWindow = null

// 경로 관련
let routeMarkers = []
let routeLines = []
let routeInfoWindows = []

// bbox 관련 debounce & cancelToken
let bboxDebounceTimeout = null


export function getUserLocation(timeout = 10000) {
    return new Promise((resolve, reject) => {
        if (!navigator.geolocation) {
            reject(new Error('현재 위치를 지원하지 않는 브라우저입니다.'))
            return
        }

        navigator.geolocation.getCurrentPosition(
            pos => {
                const { latitude: lat, longitude: lon } = pos.coords
                userLocation.value = { lat, lon }
                resolve({ lat, lon })
            },
            err => {
                reject(new Error('위치 정보를 가져올 수 없습니다.'))
            },
            { enableHighAccuracy: true, timeout, maximumAge: 0 }
        )
    })
}

export function setupBBoxEventListener(onUpdate, filterDepartments = []) {
  if (!map.value) return
  console.log('✅ BBox 이벤트 리스너 등록 완료')

  const handler = (label) => {
    if (!map.value) return
    if (bboxDebounceTimeout) clearTimeout(bboxDebounceTimeout)

    bboxDebounceTimeout = setTimeout(() => {
      const bbox = getCurrentBBox()
      if (!bbox) return console.warn('❌ BBox를 가져올 수 없음')
      updateMarkersByBBox(bbox, onUpdate, filterDepartments, label)
    }, 300)
  }

  // 드래그/줌 변경 시 호출
  window.Tmapv2.event.addListener(map.value, 'dragend', () => handler('dragend'))
  window.Tmapv2.event.addListener(map.value, 'zoom_changed', () => handler('zoom'))
}

/**
 * 지도 초기화
 */
export async function initTmap() {
    const el = document.getElementById('tmap')
    if (!el) throw new Error('tmap 엘리먼트를 찾을 수 없습니다.')

    map.value = new window.Tmapv2.Map('tmap', {
        center: new window.Tmapv2.LatLng(37.491585, 126.8795001),
        width: '100%',
        height: '100%',
        zoom: 14
    })

    return map.value
}


export function getCurrentBBox() {
    if (!map.value) return null
    const bounds = map.value.getBounds()
    if (!bounds?._sw || !bounds?._ne) return null
    const sw = bounds._sw
    const ne = bounds._ne
    return [sw._lng, sw._lat, ne._lng, ne._lat]
}



export async function updateMarkersByBBox(
  bbox,
  onUpdate = () => {},
  filterDepartments = [],
  label = 'update'
) {
  if (!map.value || !bbox) return
  console.log(`📦 [${label.toUpperCase()}] 현재 BBox:`, bbox)

  try {
    const [minX, minY, maxX, maxY] = bbox

    // 1️⃣ WFS 요청 URL
    // GeoServer WFS 1.1.0, EPSG:4326
    const wfsUrl = 'https://api.child119.com/geoserver/hospital/wfs'
    const params = {
      service: 'WFS',
      version: '1.1.0',
      request: 'GetFeature',
      typeName: 'hospital:hospital', // 실제 레이어명
      outputFormat: 'application/json',
      srsName: 'EPSG:4326',
      bbox: `${minY},${minX},${maxY},${maxX},EPSG:4326` // 주의: lat, lon 순
    }

    // axios GET 요청
    const { data } = await axios.get(wfsUrl, { params })

    let hospitalsData = data.features || []

    // 2️⃣ 진료과 필터링
    if (filterDepartments && filterDepartments.length > 0) {
      hospitalsData = hospitalsData.filter(h => {
        const deptName = h.properties.dept_name || ''
        return filterDepartments.some(dept => deptName.includes(dept))
      })
    }

    // 3️⃣ Vue로 데이터 전달
    onUpdate(hospitalsData)
    console.log(`📌 [${label.toUpperCase()}] 병원 데이터 업데이트 완료 (${hospitalsData.length}개)`)

  } catch (err) {
    console.error(`❌ [${label.toUpperCase()}] 병원 데이터 요청 실패:`, err)
  }
}
/**
 * 현재 위치 마커 표시
 */
export function showCurrentLocationMarker(lat, lon) {
    if (!map.value) return
    clearCurrentLocationMarker()

    const position = new window.Tmapv2.LatLng(lat, lon)
    currentLocationMarker = new window.Tmapv2.Marker({ position, map: map.value })
    currentLocationInfoWindow = new window.Tmapv2.InfoWindow({
        position,
        content: "<div class='p-1 bg-white border rounded-2'>현재 위치</div>",
        offset: new window.Tmapv2.Point(0, -30),
        type: 2,
        map: map.value
    })

    map.value.setCenter(position)
}

/**
 * 현재 위치 마커 제거
 */
export function clearCurrentLocationMarker() {
    if (currentLocationMarker) currentLocationMarker.setMap(null)
    if (currentLocationInfoWindow) currentLocationInfoWindow.setMap(null)
    currentLocationMarker = null
    currentLocationInfoWindow = null
}

/**
 * 병원 마커 추가
 */
export function addHospitalMarkers(hospitals, onMarkerClick = () => { }) {
    // ... (기존 코드 유지)
    if (!map.value) return
    clearHospitalMarkers()

    hospitals.forEach(hospital => {
        const pos = new window.Tmapv2.LatLng(hospital.coordinates[1], hospital.coordinates[0])
        const marker = new window.Tmapv2.Marker({ position: pos, map: map.value })
        if (onMarkerClick)
            window.Tmapv2.event.addListener(marker, 'click', () => onMarkerClick(hospital))
        hospitalMarkers.push(marker)
        // ❌ markers.value.push(marker) 제거
    })
}

/**
 * 병원 마커 제거
 */
export function clearHospitalMarkers() {
    hospitalMarkers.forEach(m => m.setMap(null))
    hospitalMarkers = []
}


/**
 * 경로 그리기
 */
export function drawRoute(resultData) {
    if (!map.value) return

    // 기존 경로 제거
    clearRoute()

    for (const feature of resultData) {
        const geometry = feature.geometry
        const properties = feature.properties

        if (geometry.type === "LineString") {
            // 경로 라인 그리기
            const pathCoords = geometry.coordinates.map(coord =>
                new window.Tmapv2.LatLng(coord[1], coord[0])
            )

            const polyline = new window.Tmapv2.Polyline({
                path: pathCoords,
                strokeColor: "#0d6efd",
                strokeWeight: 6,
                map: map.value
            })

            routeLines.push(polyline)

        } else if (properties.pointType === "S" || properties.pointType === "E") {
            // 출발지/도착지 마커
            const [lng, lat] = geometry.coordinates
            const position = new window.Tmapv2.LatLng(lat, lng)

            const label = properties.pointType === "S" ? "출발" : "도착"
            const color = properties.pointType === "S" ? "#0d6efd" : "#dc3545"

            const marker = new window.Tmapv2.Marker({
                position: position,
                map: map.value,
                title: label
            })

            const infoWindow = new window.Tmapv2.InfoWindow({
                position: position,
                content: `<div style="padding:5px 10px;background:${color};color:white;border-radius:5px;font-weight:bold;font-size:12px;">${label}</div>`,
                type: 2,
                map: map.value,
                offset: new window.Tmapv2.Point(0, -10)
            })

            routeMarkers.push(marker)
            routeInfoWindows.push(infoWindow)
        }
    }
}

/**
 * 경로 제거
 */
export function clearRoute() {
    routeMarkers.forEach(marker => marker.setMap(null))
    routeMarkers = []

    routeLines.forEach(line => line.setMap(null))
    routeLines = []

    routeInfoWindows.forEach(iw => iw.setMap(null))
    routeInfoWindows = []
}


/**
 * 지도 정리
 */
export function destroyMap() {
    clearHospitalMarkers()
    clearRoute()
    clearCurrentLocationMarker()
    if (map.value) {
        map.value.destroy()
        map.value = null
    }
}