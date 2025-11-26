<template>
  <div class="d-flex justify-content-center align-items-center position-relative mt-1"
    style="height: calc(100vh - 140px); ">

    <!-- Vworld - vw.ol3.Map 지도 -->
    <div id="vmap" class="position-fixed top-0 start-0 w-100" style="height:100dvh; z-index:0; pointer-events:auto;">
    </div>
    <!-- 내 위치 새로고침 버튼 -->
    <!-- 범례 (왼쪽 상단) -->
    <div class="position-fixed start-0 p-3" style="top: calc(var(--header-h) + 8px); z-index: 2;">
      <div class="bg-white border rounded-3 shadow-sm p-3" style="min-width: 140px;">
        <div class="d-flex flex-column gap-2">
          <!-- 여유 -->
          <div class="d-flex align-items-center gap-2">
            <div class="rounded-circle" style="width: 16px; height: 16px; background-color: #16a085; flex-shrink: 0;">
            </div>
            <span class="text-medium" style="font-size: 12px;">여유</span>
          </div>

          <!-- 부족 -->
          <div class="d-flex align-items-center gap-2">
            <div class="rounded-circle" style="width: 16px; height: 16px; background-color: #f3a35cff; flex-shrink: 0;">
            </div>
            <span class="text-medium" style="font-size: 12px;">부족</span>
          </div>

          <!-- 정보없음 -->
          <div class="d-flex align-items-center gap-2">
            <div class="rounded-circle" style="width: 16px; height: 16px; background-color: #95a5a6; flex-shrink: 0;">
            </div>
            <span class="text-medium" style="font-size: 12px;">정보없음</span>
          </div>
        </div>
      </div>
    </div>
    <div class="position-fixed end-0 p-3" style="top: calc(var(--header-h) + 8px); z-index: 2;">
      <button type="button" class="btn btn-primary border rounded-circle shadow-sm" @click="refreshLocation()"
        style="width: 48px; height: 48px; padding: 0;">
        <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24">
          <path fill="currentColor"
            d="M12 8c-2.21 0-4 1.79-4 4s1.79 4 4 4s4-1.79 4-4s-1.79-4-4-4m8.94 3A8.994 8.994 0 0 0 13 3.06V1h-2v2.06A8.994 8.994 0 0 0 3.06 11H1v2h2.06A8.994 8.994 0 0 0 11 20.94V23h2v-2.06A8.994 8.994 0 0 0 20.94 13H23v-2zM12 19c-3.87 0-7-3.13-7-7s3.13-7 7-7s7 3.13 7 7s-3.13 7-7 7" />
        </svg>
      </button>
    </div>
  </div>
  <!-- 바텀시트 -->
  <div class="position-fixed start-0 end-0 bg-white  rounded-top-4 shadow-lg " :style="sheetStyle" ref="bottomSheet"
    @touchstart="onTouchStart" @touchmove="onTouchMove" @touchend="onTouchEnd"
    style="z-index: 2; padding-bottom: 150px;">

    <!-- 그립 -->
    <div class="pt-3 d-flex justify-content-center" @click="toggleSheet" style="cursor: grab;">
      <div class="bg-secondary-subtle rounded-pill w-25" style="height:5px;"></div>
    </div>
    <!-- 핸들 -->
    <div class="d-flex justify-content-center" @click="toggleSheet"></div>


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
        <div class="dropdown">
          <button class="btn dropdown-toggle w-100 d-flex align-items-center justify-content-between 
                    bg-white border rounded-pill px-4 fw-bold" type="button" data-bs-toggle="dropdown"
            aria-expanded="false" style="height: 42px; font-size: 15px; padding-right: 18px !important;">

            <div class="d-flex align-items-center gap-2">
              <!-- 현재 선택된 항목 표시 -->
              <template v-if="currentSort === 'time'">
                <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24">
                  <path fill="currentColor"
                    d="M12 2C6.5 2 2 6.5 2 12s4.5 10 10 10s10-4.5 10-10S17.5 2 12 2m4.3 13.2L11 12.3V7h1.5v4.4l4.5 2.5z" />
                </svg>
                시간순
              </template>

              <template v-else>
                <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24">
                  <path fill="currentColor"
                    d="M6.5 8.11c-.89 0-1.61-.72-1.61-1.61A1.61 1.61 0 0 1 6.5 4.89c.89 0 1.61.72 1.61 1.61A1.61 1.61 0 0 1 6.5 8.11M6.5 2C4 2 2 4 2 6.5c0 3.37 4.5 8.36 4.5 8.36S11 9.87 11 6.5C11 4 9 2 6.5 2m11 6.11a1.61 1.61 0 0 1-1.61-1.61a1.609 1.609 0 1 1 3.22 0a1.61 1.61 0 0 1-1.61 1.61m0-6.11C15 2 13 4 13 6.5c0 3.37 4.5 8.36 4.5 8.36S22 9.87 22 6.5C22 4 20 2 17.5 2m0 14c-1.27 0-2.4.8-2.82 2H9.32a3 3 0 0 0-3.82-1.83A3.003 3.003 0 0 0 3.66 20a3.017 3.017 0 0 0 3.84 1.83c.85-.3 1.5-.98 1.82-1.83h5.37c.55 1.56 2.27 2.38 3.81 1.83A3 3 0 0 0 20.35 18c-.43-1.2-1.57-2-2.85-2m0 4.5A1.5 1.5 0 0 1 16 19a1.5 1.5 0 0 1 1.5-1.5A1.5 1.5 0 0 1 19 19a1.5 1.5 0 0 1-1.5 1.5" />
                </svg>
                거리순
              </template>
            </div>
          </button>

          <!-- dropdown menu -->
          <ul class="dropdown-menu w-100 rounded-4 shadow-sm p-0 overflow-hidden" style="min-width: auto;">

            <!-- 거리순이 아닐 때만 거리순 표시 -->
            <li v-if="currentSort !== 'distance'">
              <button class="dropdown-item d-flex align-items-center gap-3 py-2 fw-bold" style="font-size: 15px;"
                @click="changeSort('distance')">
                <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24">
                  <path fill="currentColor"
                    d="M6.5 8.11c-.89 0-1.61-.72-1.61-1.61A1.61 1.61 0 0 1 6.5 4.89c.89 0 1.61.72 1.61 1.61A1.61 1.61 0 0 1 6.5 8.11M6.5 2C4 2 2 4 2 6.5c0 3.37 4.5 8.36 4.5 8.36S11 9.87 11 6.5C11 4 9 2 6.5 2m11 6.11a1.61 1.61 0 0 1-1.61-1.61a1.609 1.609 0 1 1 3.22 0a1.61 1.61 0 0 1-1.61 1.61m0-6.11C15 2 13 4 13 6.5c0 3.37 4.5 8.36 4.5 8.36S22 9.87 22 6.5C22 4 20 2 17.5 2m0 14c-1.27 0-2.4.8-2.82 2H9.32a3 3 0 0 0-3.82-1.83A3.003 3.003 0 0 0 3.66 20a3.017 3.017 0 0 0 3.84 1.83c.85-.3 1.5-.98 1.82-1.83h5.37c.55 1.56 2.27 2.38 3.81 1.83A3 3 0 0 0 20.35 18c-.43-1.2-1.57-2-2.85-2m0 4.5A1.5 1.5 0 0 1 16 19a1.5 1.5 0 0 1 1.5-1.5A1.5 1.5 0 0 1 19 19a1.5 1.5 0 0 1-1.5 1.5" />
                </svg>
                거리순
              </button>
            </li>

            <!-- 시간순이 아닐 때만 시간순 표시 -->
            <li v-if="currentSort !== 'time'">
              <button class="dropdown-item d-flex align-items-center gap-3 py-2 fw-bold" style="font-size: 15px;"
                @click="changeSort('time')">
                <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24">
                  <path fill="currentColor"
                    d="M12 2C6.5 2 2 6.5 2 12s4.5 10 10 10s10-4.5 10-10S17.5 2 12 2m4.3 13.2L11 12.3V7h1.5v4.4l4.5 2.5z" />
                </svg>
                시간순
              </button>
            </li>
          </ul>
        </div>


      </div>

      <!-- 리스트 스크롤 영역 -->
      <div class="overflow-auto px-3 ">


        <!-- 응급실 목록 -->
        <div class="card shadow-sm mb-3 pb-10" v-for="h in listHospitals" :key="h.hpid || h.id"
          :ref="el => hospitalRefs[h.id] = el" :class="{ 'card-selected-highlight': h.id === selectedMarkerId }">
          <div class="card-body" @click="goToemergencyDetail(h.id)">
            <div class="d-flex justify-content-between align-items-start">
              <h6 class="card-title text-bold mb-1">{{ h.name }}</h6>

              <!-- ✅ 병상 현황: 'avail/total' -->
              <span v-if="h.bedAvail !== null || h.bedTotal !== null" class="badge rounded-pill"
                :class="bedBadgeClass(h)">
                <svg xmlns="http://www.w3.org/2000/svg" width="22" height="15" viewBox="0 0 24 24" class="me-1">
                  <path fill="currentColor"
                    d="M19 7h-8v7H3V5H1v15h2v-3h18v3h2v-9a4 4 0 0 0-4-4M7 13a3 3 0 0 0 3-3a3 3 0 0 0-3-3a3 3 0 0 0-3 3a3 3 0 0 0 3 3" />
                </svg>
                {{ bedLabel(h) }}
              </span>
            </div>

            <!-- 소아 가능/불가능 메시지 표시 (콤마 기준으로 여러 개 뱃지) -->
            <div class="mb-2" v-if="h.messageLines?.length">
              <span v-for="(msg, idx) in h.messageLines" :key="idx" class="badge me-1 hospital-message-chip" :class="msg.includes('가능')
                ? 'bg-success-subtle text-success-emphasis'
                : 'bg-secondary-subtle text-secondary-emphasis'">
                {{ msg }}
              </span>
            </div>


            <!-- ✅ state 상태 표시 -->
            <div class="mb-2" v-if="h.state !== undefined && h.state !== null">
              <span class="badge me-1" :class="{
                'bg-danger-subtle text-danger-emphasis': h.state === 0,
                'bg-warning-subtle text-warning-emphasis': h.state === 1,
                'bg-success-subtle text-success-emphasis': h.state === 2
              }">
                {{
                  h.state === 0
                    ? '진료불가능'
                    : h.state === 1
                      ? '조건부가능'
                      : h.state === 2
                        ? '진료가능'
                        : '정보없음'
                }}
              </span>
            </div>



            <div class="text-muted small d-flex align-items-center mb-3">
              <span><i class="bi bi-clock me-1"></i> {{ h.etaMin }}분</span>
              <span class="mx-2">|</span>
              <span><i class="bi bi-geo-alt me-1"></i> {{ h.distanceKm.toFixed(1) }}km</span>
            </div>

            <div class="d-grid gap-2 d-sm-flex">
              <button class="btn btn-outline-primary w-100" v-if="h.id" @click.stop="callHospital(h.id)">
                <i class="bi bi-telephone-fill"></i> 전화
              </button>
              <button class="btn btn-primary w-100" v-if="h.lat && h.lon && h.addr && h.name && h.id"
                @click.stop="requestNavi(h.lon, h.lat, h.name, h.addr, h.id)">
                <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24">
                  <path fill="#fff"
                    d="M12 11.5A2.5 2.5 0 0 1 9.5 9A2.5 2.5 0 0 1 12 6.5A2.5 2.5 0 0 1 14.5 9a2.5 2.5 0 0 1-2.5 2.5M12 2a7 7 0 0 0-7 7c0 5.25 7 13 7 13s7-7.75 7-13a7 7 0 0 0-7-7" />
                </svg> 길찾기
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



      </div>
    </div>
  </div>

  <!-- 증상 선택 모달 -->

</template>

<script setup>
// import
import { ref, computed, onMounted, onBeforeUnmount, onActivated, nextTick } from 'vue';
import { useRouter } from 'vue-router';
import kidLogo from '@/assets/kid_logo.png'


import {
  initVworldMap,
  clearMarkers,
  addMarkerEmergency,
  getCurrentBBox4326,
  drawRoute,
  setupMarkerClick,
  removeRouteLayer,
  addMarkerHospital
} from '@/utils/vworldfunction';
import { calculateDistance, getEmergencyFromWFSByBBox, getTmapRoute, getEmergencyFromWFSSortByDistance, getEmergencyHospitalByHPID } from '@/utils/api';

const delay = ms => new Promise(resolve => setTimeout(resolve, ms));

import proj4 from 'proj4';

// KATEC 정의 (가장 일반적인 비공식 KATEC: Bessel 타원체, 중앙 경선 128도)
const KATEC_PROJ4_STRING = '+proj=tmerc +lat_0=38 +lon_0=128 +k=0.9999 +x_0=400000 +y_0=600000 +ellps=bessel +units=m +no_defs +towgs84=-115.80,474.99,674.11,1.16,-2.31,-1.63,6.43';
proj4.defs('KATEC', KATEC_PROJ4_STRING);

// 좌표계 ID
const WGS84 = 'EPSG:4326';
const KATEC = 'KATEC';

// 현재 위치
const cur = ref({ lat: null, lon: null })
const userLocation = ref(null)


// vmap 변수
let vmap = null;
let moveEndListener = null;

// 선택된 증상 - 반응형
const symptomOptions = ['발열', '감기', '기침', '호흡곤란', '구토', '설사', '복통', '발진', '화상', '경련']
const selectedSymptoms = ref([])
const removeSymptom = (sym) => { selectedSymptoms.value = selectedSymptoms.value.filter(s => s !== sym) }


const hospitals = ref([])
const loadingHospitals = ref(false)
const useSymptomsMode = ref(false)

// 마커 관련
let debounceTimer = null
const selectedMarkerId = ref(null)
const hospitalRefs = ref({})

// marker click시 실행 함수
const onMarkerClick = (id) => {
  selectedMarkerId.value = id;
  // 1) 리스트에서 해당 카드가 맨 위 근처로 오도록 스크롤
  scrollCard(id)

  // 2) 그 카드 하나 보일 만큼만 바텀시트 올리기
  openSheetJustForCard(id)
}


// 스크롤 함수
const scrollCard = (id) => {
  nextTick(() => {
    const el = hospitalRefs.value[id]
    if (el) {
      const scrollcontainer = el.closest('.overflow-auto.px-3')
      if (scrollcontainer) {
        scrollcontainer.scrollTo({
          top: el.offsetTop - scrollcontainer.offsetTop - 10,
          behavior: 'smooth'
        })
      }
    }
  })
}

// 선택된 카드 하나 보일 만큼만 바텀시트를 올려주는 함수
const openSheetJustForCard = (id) => {
  nextTick(() => {
    const el = hospitalRefs.value[id]
    if (!el) return

    // 실제 카드 높이
    const cardHeight = el.offsetHeight || 200

    // 헤더(그립 + 증상뱃지 + 타이틀/정렬 드롭다운) 대략 높이 추정
    const ESTIMATED_HEADER_HEIGHT = 170 // px 정도로 감으로 잡기

    // 카드 + 헤더 + 여유 패딩
    const neededVisible = ESTIMATED_HEADER_HEIGHT + cardHeight + 30

    // 보이는 높이 = MIN_SHEET_HEIGHT ~ MAX_SHEET_HEIGHT 사이로 클램프
    const clampedVisible = Math.max(
      MIN_SHEET_HEIGHT,
      Math.min(MAX_SHEET_HEIGHT, neededVisible)
    )

    // visibleHeight = MIN + (MAX - MIN) * ratio
    const ratio =
      (clampedVisible - MIN_SHEET_HEIGHT) / (MAX_SHEET_HEIGHT - MIN_SHEET_HEIGHT)

    sheetHeightRatio.value = ratio
  })
}


/* 바텀시트 상태 start */
const bottomSheet = ref(null)
const isDragging = ref(false)
const sheetHeightRatio = ref(0)
const dragStart = ref({ y: 0, ratio: 0 })

const MAX_SHEET_HEIGHT = window.innerHeight * 0.8
const MIN_SHEET_HEIGHT = 220

const sheetY = computed(() => (MAX_SHEET_HEIGHT - MIN_SHEET_HEIGHT) * (1 - sheetHeightRatio.value))
const sheetStyle = computed(() => {
  return {
    transition: isDragging.value ? 'none' : 'transform 0.3s ease-out',
    transform: `translateY(${sheetY.value}px)`,
    height: `${MAX_SHEET_HEIGHT}px`,
    bottom: '0px',
    top: 'auto',
    willChange: 'transform'
  }
})


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

const currentSort = ref("time");

function changeSort(type) {
  currentSort.value = type;
  type === "distance" ? sortByDistance() : sortByTime();
}

/* 바텀시트 제스처 end */


/* 실제 렌더링 목록 */
const listHospitals = computed(() => {

  return hospitals.value.map(h => ({
    id: h.id,
    hpid: h.hpid,               // ← 추가
    name: h.name,
    message: h.message,
    messageLines: h.messageLines || [],
    state: h.state,
    bedAvail: h.bedAvail,   // ✅ 추가
    bedTotal: h.bedTotal,
    distanceKm: (h.distance ?? 0) / 1000,
    etaMin: Math.max(1, Math.round(((h.distance ?? 0) / 1000) / 0.4)),
    lat: h.lat, lon: h.lon,     // (선택) 모달/길찾기 즉시 사용
    tel: h.tel || '',
    addr: h.addr
  }))
})

// 🔹 WFS 마커 로드 함수
const loadWFSMarkers = async () => {
  if (!vmap) {
    console.warn('vmap이 아직 초기화되지 않았습니다.')
    return
  }

  // 1. bbox 가져오기
  const bbox = getCurrentBBox4326()

  if (!bbox) {
    console.warn('bbox를 가져올 수 없습니다.')
    return
  }

  const [minX, minY, maxX, maxY] = bbox


  try {

    // 2. bbox에 걸리는 데이터 geoserver에서 가져오기
    const feats = await getEmergencyFromWFSByBBox(minX, minY, maxX, maxY);
    console.log('✅ WFS 응답:', feats.length, '개');


    // 3. 사용자 현재 위치 설정
    const centerLon = cur.value.lon;
    const centerLat = cur.value.lat;

    // JSON형태의 데이터 정제 함수
    const processedHospitals = feats.map((f, i) => {
      const [lon, lat] = f.geometry?.coordinates || [];
      const p = f.properties || {};

      // 문자열 병상수 ->  Number 타입
      const toInt = (v) => {
        if (v === null || v === undefined) return null;
        const n = parseInt(String(v).trim(), 10);
        return Number.isFinite(n) ? n : null;
      }

      const rawMessage = p.message || '';

      const messageLines = rawMessage
        .split(',')                 // 콤마 기준으로 자르기
        .map(s => s.trim())         // 양옆 공백 제거
        .filter(Boolean);           // 빈 문자열 제거




      const state = p.state
      const name = p.nickname
      const bedAvail = toInt(p.bed_avail)
      const bedTotal = toInt(p.bed_total)
      const addr = p.address

      // 거리 계산 로직
      const distance = (Number.isFinite(lat) && Number.isFinite(lon))
        ? calculateDistance(centerLat, centerLon, lat, lon)
        : Infinity;

      // 정제된 객체 반환
      return {
        id: f.id || `wfs-${i}`,
        hpid: p.hpid,
        lat,
        lon,
        distance,
        name,
        message: rawMessage,
        messageLines,
        state,
        bedAvail,
        bedTotal,
        addr
      };
    });

    // 4. 기존 마커 제거
    clearMarkers();

    // 5. 정제된 데이터(processedHospitals)로 마커 변경
    processedHospitals.forEach((hospital) => {
      addMarkerEmergency(hospital.lon, hospital.lat, {
        id: hospital.id,
        name: hospital.name,
        message: hospital.message,
        state: hospital.state,
        bedAvail: hospital.bedAvail,
        bedTotal: hospital.bedTotal,
      });
    });

    // 6. 바텀시트에 관련 데이터 넣기 
    hospitals.value = processedHospitals;

  } catch (error) {
    console.error("WFS 데이터 처리 중 오류 발생:", error);
    // 에러 발생 시 목록 비우기
    clearMarkers();
    hospitals.value = [];
  }
}



// 지도 초기화 및 최초 실행시 마커 표출
const initMapAndCoords = async () => {
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

    let location = { lon: 126.978, lat: 37.5665 };

    try {
      const userLocation = await getCurrentLocation()
      location = userLocation; // 위치 획득 성공 시 값 업데이트
      // location.lon = 127.727366
      // location.lat = 37.891997
      console.log(`현재 위치 -> ${location.lon}, ${location.lat}`)
      centerCoords = ol.proj.transform([location.lon, location.lat], 'EPSG:4326', 'EPSG:3857')
    } catch {
      console.log(`위치 가져오기 실패, 기본 위치 사용`)
      // 실패 시, 위에서 설정한 기본 location 값(126.978, 37.5665)이 사용됨
      centerCoords = ol.proj.transform([location.lon, location.lat], 'EPSG:4326', 'EPSG:3857')
    }

    // 3. 지도 초기화
    vmap = initVworldMap({
      containerId: 'vmap',
      defaultCenter: centerCoords,
      autoFetch: false
    })

    if (!vmap) throw new Error('vmap 초기화 실패')


    // 4. 리스너 함수 정의 
    moveEndListener = () => {
      console.log(`지도 이동/줌 감지`)
      if (debounceTimer) clearTimeout(debounceTimer)
      debounceTimer = setTimeout(() => {
        loadWFSMarkers()
      }, 300)
    }

    vmap.on('moveend', moveEndListener)

    // 5. 초기 마커 로드
    setTimeout(() => loadWFSMarkers(), 500)

    // 6-1. 현재 위치에서 가장 가까운 응급실 가져오기
    const emergencyRes = await getEmergencyFromWFSSortByDistance(location.lon, location.lat)
    const top3Emergency = emergencyRes.slice(0, 3)

    const routeResArray = []

    for (const [index, feature] of top3Emergency.entries()) {
      const [endX, endY] = feature.geometry.coordinates

      try {
        // 6.2 경로 데이터 불러오기
        const routeData = await getTmapRoute(location.lon, location.lat, endX, endY)
        routeResArray.push(routeData)
      } catch (err) {
        console.error(`${index + 1}번째 경로 요청 오류`)
        routeResArray.push(null)
      }


      // // api 요청 차레로 보내기
      // if (index < top3Emergency.length - 1) {
      //   await delay(800)
      // }

    }

    // 6-3. 기존 경로 레이어 삭제
    await removeRouteLayer(vmap);

    // ⭐ 6-4-1. 줌 맞추기를 위한 Extent (영역) 계산 (출발지 + Top 3 병원) ⭐
    let totalExtent = ol.extent.createEmpty();

    // 1) 현재 위치 (출발지) Extent 포함
    const startPoint3857 = ol.proj.transform([location.lon, location.lat], 'EPSG:4326', 'EPSG:3857');
    ol.extent.extend(totalExtent, startPoint3857);

    // 2) Top 3 병원의 Extent 포함
    top3Emergency.forEach(feature => {
      const [endX, endY] = feature.geometry.coordinates; // 병원 좌표 (4326)
      const endPoint3857 = ol.proj.transform([endX, endY], 'EPSG:4326', 'EPSG:3857');
      ol.extent.extend(totalExtent, endPoint3857);
    });


    // ⭐ 6-4-2. 지도 뷰를 전체 영역에 맞춥니다. (fit 완료 보장) ⭐
    if (!ol.extent.isEmpty(totalExtent)) {
      // moveend 완료를 await로 대기하여 bbox 감지 성공률을 높입니다.
      await new Promise(resolve => {
        const view = vmap.getView();

        const listener = view.on('moveend', () => {
          ol.Observable.unByKey(listener);
          console.log("✅ [initMapAndCoords] Top 3 Extent fit 애니메이션 종료 확인.");
          resolve();
        });

        // 뷰를 계산된 현재 위치 + Top 3 병원 영역에 맞춥니다.
        view.fit(totalExtent, {
          size: vmap.getSize(),
          padding: [150, 150, 150, 150], // 여백 설정
          duration: 1000
        });
      });
    }


    // ⭐ 6-4-3. 경로 그리기 (하나의 경로만 유지) ⭐
    if (routeResArray.length > 0 && vmap) {
      console.log(`경로 그리기 함수 실행`)

      // Top 1 병원의 경로 데이터만 drawRoute에 전달하여 하나만 표시합니다.
      if (routeResArray[0]) {
        drawRoute(vmap, routeResArray[0], 1)
      }
    }
    // marker click 함수
    if (vmap) { setupMarkerClick(onMarkerClick) }

    await delay(400);
    loadWFSMarkers(); // bbox 감지 시도

  } catch (error) {
    console.error('초기화 실패:', error)
    alert('지도를 초기화할 수 없습니다.')
  }
}

// 네비 실행 함수
async function requestNavi(endX, endY, name, addr, hpid) {
  try {

    // 기본 위치 설정
    let location = { lon: 126.978, lat: 37.5665 };

    let startX = null
    let startY = null
    const startName = "내위치"

    // hpid 수정
    const hpidArray = await hpid.split(".")
    const id = hpidArray[1]
    console.log(`✨ hpid -> ${id}`)

    // 현재 위치 불러오기
    try {
      location = await getCurrentLocation()
      startX = location.lon
      startY = location.lat

      console.log(`requestNavi 현재 위치 불러오기 성공 X: ${startX}, Y: ${startY}`)

    } catch (err) {
      console.error(`requestNavi 현재 위치 불러오기 실패 -> ${err}`)
    }

    // 1. WGS84(EPSG:4326) -> KATEC으로 좌표 변환
    const [start_x, start_y] = proj4(WGS84, KATEC, [startX, startY])
    const [end_x, end_y] = proj4(WGS84, KATEC, [endX, endY]);

    // 2. Android Bridge 호출
    if (window.AndroidBridge && typeof window.AndroidBridge.startNavigation === 'function') {
      window.AndroidBridge.startNavigation(
        startName, // 현위치
        Math.round(start_x),
        Math.round(start_y),
        name, // 도착지 이름 (병원 이름)
        Math.round(end_x),
        Math.round(end_y),
        addr,
        id
      );
      console.log(`✅ 길찾기 요청: ${name} (KATEC X:${Math.round(end_x)}, Y:${Math.round(end_y)})`);

    } else {
      // 안드로이드 앱이 아닐 경우 (예: PC 웹)
      alert(`[웹 환경] 안드로이드 앱에서 실행됩니다. 도착지: ${name}, KATEC 좌표: (${Math.round(end_x)}, ${Math.round(end_y)})`);
    }
  } catch (e) {
    console.error("좌표 변환 또는 네비게이션 호출 실패:", e);
    alert("네비게이션 실행에 문제가 발생했습니다.");
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

async function refreshLocation() {
  try {
    const position = await getCurrentLocation()
    const { lat, lon } = position
    userLocation.value = { lat, lon }

    const center = window.ol.proj.fromLonLat([lon, lat])
    const view = vmap.getView()

    // 부드럽게 중앙 이동
    if (!view.animate) {
      const start = view.getCenter()
      const steps = 20
      let i = 0
      const timer = setInterval(() => {
        const t = ++i / steps
        const x = start[0] + (center[0] - start[0]) * t
        const y = start[1] + (center[1] - start[1]) * t
        view.setCenter([x, y])
        if (i >= steps) clearInterval(timer)
      }, 10)
    }

    // 🔹 여기서 kidLogo를 이용해서 SVG 아이콘 생성
    const logoDataUrl = await toDataUrl(kidLogo)
    const iconUrl = createUserPinIcon(logoDataUrl)

    addMarkerHospital(lon, lat, {
      name: '현재 위치',
      iconUrl,
      anchor: [0.5, 1],
      isUserLocation: true,
    })


    console.log(`화면 중심만 내 위치로 이동: ${lat}, ${lon}`)
  } catch (err) {
    console.error('moveToUserLocation 실패:', err)
    alert('현재 위치를 가져올 수 없습니다.')
  }
}


console.log('kidLogo url =>', kidLogo)

onMounted(async () => {
  await initMapAndCoords()
  await refreshLocation()
})

// zoom / moveend 됐을 때
onActivated(async () => {
  console.log(`onActivated 활성화`)
  await getCurrentPositionApp()
});

onBeforeUnmount(() => {
  if (debounceTimer) clearTimeout(debounceTimer)

  if (vmap && moveEndListener) {
    vmap.un('moveend', moveEndListener)
    console.log(`리스너 해제 완료.`)
  }
})

async function getCurrentPositionApp() {
  getCurrentLocation();
  loadWFSMarkers();
}

// 🔹 정렬 함수들 -> 로직 수정
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

// 병원 전화 걸기 
async function callHospital(hpid) {
  const hpidArray = await hpid.split(".")
  const id = hpidArray[1]

  const res = await getEmergencyHospitalByHPID(id)
  console.log(JSON.stringify(res))
  const tel = res[0].dutyTel3
  console.log(`res -> ${tel}`)

  if (!tel) {
    alert('전화번호가 없습니다.')
    return
  }
  window.location.href = `tel:${tel}`
}




// 바텀시트 오른쪽 병상 badge
function bedLabel(h) {
  const a = h.bedAvail
  const t = h.bedTotal
  const showA = (a === null || a === undefined) ? '?' : a
  const showT = (t === null || t === undefined) ? '?' : t
  return `${showA}/${showT}`
}

function bedBadgeClass(h) {
  // 가용률에 따라 색상 분기 (선택 로직)
  const a = h.bedAvail
  const t = h.bedTotal
  if (!Number.isFinite(a) || !Number.isFinite(t) || t <= 0) {
    return 'bg-secondary-subtle text-secondary-emphasis' // 정보 부족
  }
  const ratio = a / t
  if (ratio >= 0.5) return 'bg-success-subtle text-success-emphasis'   // 여유
  if (ratio > 0) return 'bg-warning-subtle text-warning-emphasis'    // 부족
  return 'bg-danger-subtle text-danger-emphasis'                        // 없음
}


function createUserPinIcon(logoDataUrl, size = 70) {
  const svg = `
    <svg xmlns="http://www.w3.org/2000/svg"
         xmlns:xlink="http://www.w3.org/1999/xlink"
         width="${size}" height="${size}" viewBox="0 0 80 80">

      <!-- 바깥 핀 -->
      <path 
        d="M40 0C23 0 10 13 10 30c0 18 20 40 30 50c10-10 30-32 30-50C70 13 57 0 40 0z"
        fill="#464654"
      />

      <!-- 안쪽 흰 원 -->
      <circle cx="40" cy="30" r="22" fill="#ffffff" />

      <!-- 내부 로고 이미지 -->
      <clipPath id="clipLogo">
        <circle cx="40" cy="30" r="18" />
      </clipPath>

      <image 
        href="${logoDataUrl}"
        xlink:href="${logoDataUrl}"
        x="22"
        y="12"
        width="36"
        height="36"
        clip-path="url(#clipLogo)"
        preserveAspectRatio="xMidYMid slice"
      />
    </svg>
  `

  return `data:image/svg+xml;utf8,${encodeURIComponent(svg)}`
}



async function toDataUrl(url) {
  const res = await fetch(url)
  const blob = await res.blob()

  return await new Promise((resolve) => {
    const reader = new FileReader()
    reader.onloadend = () => resolve(reader.result) // result: data:image/png;base64,....
    reader.readAsDataURL(blob)
  })
}




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

/*선택된 카드 하이라이트*/
.card-selected-highlight {
  background-color: #e2ebff;
  /* 약간 밝은 파란색 계열 */
  border-color: #0d6efd !important;
  box-shadow: 0 4px 12px rgba(13, 110, 253, .3);
  /* 선택되었을 때 기본 카드와 구별되도록 약간의 스케일업 */
  transform: scale(1.01);
}

.dropdown-toggle::after {
  margin-left: 8px !important;
}
</style>