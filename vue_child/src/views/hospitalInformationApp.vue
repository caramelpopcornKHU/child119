<template>
  <div class="d-flex justify-content-center align-items-center position-relative mt-1"
    style="height: calc(100vh - 140px); ">

    <!-- 지도 :: start -->
    <div id="map_div" class="position-fixed top-0 start-0 w-100"
      style="height:100dvh; z-index:0;  pointer-events:auto;"></div>
    <!-- 지도 :: end -->

    <!-- 상단 조회 버튼 :: start -->
    <div class="position-fixed start-0 p-3" style="top: calc(var(--header-h) + 8px); z-index: 2;">
      <div class="pe-auto">
        <button type="button" class="btn-light border rounded-pill px-2 ms-1 shadow-sm text-bold top-button"
          data-bs-toggle="modal" data-bs-target="#departmentModal">
          <svg xmlns="http://www.w3.org/2000/svg" width="22" height="22" viewBox="0 0 24 24">
            <path fill="currentColor" d="m17 21l1.8 1.77c.5.5 1.2.1 1.2-.49V18l2.8-3.4A1 1 0 0 0 22 13h-7c-.8 0-1.3 1-.8 1.6L17 18zm-5-10h8V3a2 2 0 0 0-2-2H4c-1.1 0-2 .9-2 2v12a2 2 0 0 0 2 2h9.42L12 15zm0-6h6v4h-6zm-2 10H4v-4h6zm0-6H4V5h6z"/>
          </svg> 진료과
        </button>
        <button class="btn-light border rounded-pill px-2 ms-1 shadow-sm text-bold top-button" title="달빛어린이병원"
          @click="showMoonlight()">
          🌙 달빛어린이병원
        </button>
        <button class="btn-light border rounded-pill px-2 ms-1 shadow-sm text-bold top-button" title="소아과"
          @click="showPediatric()">
          🧒 소아청소년과
        </button>
        <button class="btn-light border rounded-pill px-2 ms-1 shadow-sm text-bold top-button" title="약국"
          @click="showPharmacy()">
          <i class="bi bi-capsule"></i> 약국
        </button>
        <button class="btn-light border rounded-pill px-2 ms-1 shadow-sm text-bold top-button" title="AED"
          @click="showAED()">
          ⚡ AED
        </button>
      </div>
    </div>
    <!-- 상단 조회 버튼 :: end -->

    <!-- 내 위치 새로고침 버튼 :: start -->
    <div class="position-fixed end-0 p-3" style="top: calc(var(--header-h) + 8px); z-index: 2;">
      <button type="button" class="btn btn-primary border rounded-circle shadow-sm" @click="refreshLocation()"
        style="width: 48px; height: 48px; padding: 0;">
        <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24">
          <path fill="currentColor"
            d="M12 8c-2.21 0-4 1.79-4 4s1.79 4 4 4s4-1.79 4-4s-1.79-4-4-4m8.94 3A8.994 8.994 0 0 0 13 3.06V1h-2v2.06A8.994 8.994 0 0 0 3.06 11H1v2h2.06A8.994 8.994 0 0 0 11 20.94V23h2v-2.06A8.994 8.994 0 0 0 20.94 13H23v-2zM12 19c-3.87 0-7-3.13-7-7s3.13-7 7-7s7 3.13 7 7s-3.13 7-7 7" />
        </svg>
      </button>
    </div>
    <!-- 내 위치 새로고침 버튼 :: end -->

  </div>
  <!-- 바텀시트 -->
  <div class="position-fixed start-0 end-0 bg-white  rounded-top-4 shadow-lg " :style="sheetStyle" ref="bottomSheet"
    @touchstart="onTouchStart" @touchmove="onTouchMove" @touchend="onTouchEnd"
    style="z-index: 2; padding-bottom: 150px;">
    <!-- 그립 -->
    <div class="pt-3 d-flex justify-content-center" @click="toggleSheet" style="cursor: grab;">
      <div class="bg-secondary-subtle rounded-pill w-25" style="height:5px;"></div>
    </div>
    <div class="d-flex justify-content-center" @click="toggleSheet"></div>

    <!-- 내용 -->
    <div class="d-flex flex-column" style="height: 100%;">
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
      <div class="d-flex justify-content-between align-items-center mb-3 ms-2 me-2">
        <!-- 왼쪽 -->
        <div class="d-flex align-items-center gap-2">
          <h5 class="fw-bold mb-0">{{ currentListType }} 리스트</h5>
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

      <!-- 리스트 스크롤 영역 :: start -->
      <div class="overflow-auto px-3 flex-grow-1" style="min-height: 0; max-height: 100%;">
        <!-- 카드 -->
        <div class="card shadow-sm mb-3" v-for="hospital in hospitals" :key="hospital.fid || hospital.id">
          <div class="card-body" @click="goToHospitalDetail(hospital.hpid)">
            <div class="d-flex justify-content-between align-items-start">
              <h6 class="card-title fw-bold mb-1">{{ hospital.name || '병원명 없음' }}</h6>
              <span v-if="hospital.type" class="badge bg-success-subtle text-success-emphasis rounded-pill">
                <svg xmlns="http://www.w3.org/2000/svg" width="22px" height="15px" viewBox="0 0 24 24">
                  <path fill="#008A64"
                    d="M2 22V11h7V6h6v11h7v5H2M8 8V6h2v2H8m0 2v2h2v-2H8m6-4v2h2V6h-2m0 4v2h2v-2h-2m0 4v2h2v-2h-2" />
                </svg> {{ hospital.type }}
              </span>
            </div>

            <div v-if="hospital.dept_name" class="mb-2">
              <span
                v-for="(dept, index) in (hospital.dept_name ? hospital.dept_name.split(',').map(d => d.trim()) : [])"
                :key="index" class="badge rounded-pill me-1 mb-1"
                :class="[getDepartmentColor(dept).bg, getDepartmentColor(dept).text]"
                :style="getDepartmentColor(dept).style || ''">
                {{ dept }}
              </span>
            </div>

            <div class="text-muted small d-flex align-items-center mb-3">
              <span><i class="bi bi-clock me-1"></i> {{ hospital.etaMin }}분</span>
              <span class="mx-2">|</span>
              <span><i class="bi bi-geo-alt me-1"></i> {{ hospital.distanceKm }}km</span>
            </div>

            <div class="d-grid gap-2 d-sm-flex">
              <button class="btn btn-outline-primary w-100" v-if="hospital.tel" @click.stop="callHospital(hospital.tel)"
                style="pointer-events: auto; z-index: 10;">
                <i class="bi bi-telephone-fill"></i> 전화
              </button>
              <button class="btn btn-primary w-100"
                v-if="hospital.lat && hospital.lon && hospital.addr && hospital.name"
                @click.stop="requestNavi(hospital.lon, hospital.lat, hospital.name, hospital.addr, hospital.id)">
                <i class="bi bi-sign-turn-right-fill"></i> 길찾기
              </button>
            </div>
          </div>
        </div>

        <div v-if="hospitals.length === 0" class="text-center text-muted py-4">
          <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24"
            style="display: block; margin: 0 auto 8px;">
            <path fill="currentColor"
              d="M9.5 3A6.5 6.5 0 0 1 16 9.5c0 1.61-.59 3.09-1.56 4.23l.27.27h.79l5 5l-1.5 1.5l-5-5v-.79l-.27-.27A6.52 6.52 0 0 1 9.5 16A6.5 6.5 0 0 1 3 9.5A6.5 6.5 0 0 1 9.5 3m0 2C7 5 5 7 5 9.5S7 14 9.5 14S14 12 14 9.5S12 5 9.5 5" />
          </svg>
          <p class="mb-1">주변에 병원 정보가 없습니다.</p>
          <small class="text-muted">진료과를 변경하거나 다시 시도해주세요.</small>
        </div>
      </div>
      <!-- 리스트 스크롤 영역 :: end -->
    </div>
  </div>

  <!-- 진료과 선택 모달 -->
  <div class="modal fade" id="departmentModal" tabindex="-1" aria-labelledby="departmentModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
      <div class="modal-content border-0 shadow">
        <div class="modal-header">
          <h5 class="modal-title" id="departmentModalLabel">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24"
              style="display: inline-block; vertical-align: middle; margin-right: 4px;">
              <path fill="currentColor" d="M19 14h-6v6h-2v-6H5v-2h6V6h2v6h6z" />
            </svg> 진료과 선택
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
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24"
              style="display: inline-block; vertical-align: middle; margin-right: 4px;">
              <path fill="currentColor"
                d="M12 2C6.5 2 2 6.5 2 12s4.5 10 10 10s10-4.5 10-10S17.5 2 12 2m-2 15l-5-5l1.41-1.41L10 14.17l7.59-7.59L19 8z" />
            </svg> {{ selectedDepartments.length }}개 진료과 선택됨
          </div>
        </div>

        <div class="modal-footer">
          <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">취소</button>
          <button type="button" class="btn btn-primary" data-bs-dismiss="modal">
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24"
              style="display: inline-block; vertical-align: middle; margin-right: 4px;">
              <path fill="currentColor"
                d="M9.5 3A6.5 6.5 0 0 1 16 9.5c0 1.61-.59 3.09-1.56 4.23l.27.27h.79l5 5l-1.5 1.5l-5-5v-.79l-.27-.27A6.52 6.52 0 0 1 9.5 16A6.5 6.5 0 0 1 3 9.5A6.5 6.5 0 0 1 9.5 3m0 2C7 5 5 7 5 9.5S7 14 9.5 14S14 12 14 9.5S12 5 9.5 5" />
            </svg> 검색
          </button>
        </div>
      </div>
    </div>
  </div>

</template>
<script setup>
import { ref, computed, onMounted, onBeforeUnmount, watch, onActivated } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router';

import { initVworldMap, clearMarkers, addMarkerHospital, getCurrentBBox4326 } from '@/utils/vworldfunction';
import { getEmergencyFromWFSByBBox, calculateDistance } from '@/utils/api';
import proj4 from 'proj4';
import kidLogo from '@/assets/kid_logo.png'

// KATEC 정의 (가장 일반적인 비공식 KATEC: Bessel 타원체, 중앙 경선 128도)
const KATEC_PROJ4_STRING = '+proj=tmerc +lat_0=38 +lon_0=128 +k=0.9999 +x_0=400000 +y_0=600000 +ellps=bessel +units=m +no_defs +towgs84=-115.80,474.99,674.11,1.16,-2.31,-1.63,6.43';
proj4.defs('KATEC', KATEC_PROJ4_STRING);

// 좌표계 ID
const WGS84 = 'EPSG:4326';
const KATEC = 'KATEC';

// vmap 변수
let vmap = null;
let labelOverlays = []
const currentListType = ref("병원")
let moonlightZoomSet = false;

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

/* 진료과별 색상 매핑 */
const departmentColors = {
  '내과': { bg: 'bg-primary-subtle', text: 'text-primary-emphasis', color: '#0d6efd' },
  '외과': { bg: 'bg-danger-subtle', text: 'text-danger-emphasis', color: '#dc3545' },
  '정형외과': { bg: 'bg-warning-subtle', text: 'text-warning-emphasis', color: '#ffc107' },
  '신경외과': { bg: 'bg-info-subtle', text: 'text-info-emphasis', color: '#0dcaf0' },
  '마취통증의학과': { bg: 'bg-secondary-subtle', text: 'text-secondary-emphasis', color: '#6c757d' },
  '소아청소년과': { bg: 'bg-success-subtle', text: 'text-success-emphasis', color: '#198754' },
  '피부과': { bg: 'bg-pink-subtle', text: 'text-pink-emphasis', color: '#d63384', style: 'background-color: #f7d6e6; color: #d63384;' },
  '영상의학과': { bg: 'bg-dark-subtle', text: 'text-dark-emphasis', color: '#212529' },
  '재활의학과': { bg: 'bg-teal-subtle', text: 'text-teal-emphasis', color: '#20c997', style: 'background-color: #d2f4ea; color: #0d6832;' },
  '가정의학과': { bg: 'bg-indigo-subtle', text: 'text-indigo-emphasis', color: '#6610f2', style: 'background-color: #e0cffc; color: #3d0a91;' },
  '응급의학과': { bg: 'bg-danger-subtle', text: 'text-danger-emphasis', color: '#dc3545', style: 'background-color: #f8d7da; color: #842029;' },
  '성형외과': { bg: 'bg-purple-subtle', text: 'text-purple-emphasis', color: '#6f42c1', style: 'background-color: #e2d9f3; color: #432874;' },
  '안과': { bg: 'bg-cyan-subtle', text: 'text-cyan-emphasis', color: '#0dcaf0', style: 'background-color: #cff4fc; color: #055160;' },
  '한방내과': { bg: 'bg-brown-subtle', text: 'text-brown-emphasis', color: '#795548', style: 'background-color: #efebe9; color: #3e2723;' },
  '산부인과': { bg: 'bg-pink-subtle', text: 'text-pink-emphasis', color: '#e91e63', style: 'background-color: #fce4ec; color: #880e4f;' }
}

/* 진료과 색상 가져오기 */
function getDepartmentColor(deptName) {
  if (!deptName) return { bg: 'bg-secondary-subtle', text: 'text-secondary-emphasis', color: '#6c757d' }

  // 정확히 일치하는 진료과 찾기
  if (departmentColors[deptName]) {
    return departmentColors[deptName]
  }

  // 부분 일치하는 진료과 찾기 (예: "소아청소년과"가 "소아"를 포함하면)
  for (const [key, value] of Object.entries(departmentColors)) {
    if (deptName.includes(key) || key.includes(deptName)) {
      return value
    }
  }
  // 기본 색상
  return { bg: 'bg-secondary-subtle', text: 'text-secondary-emphasis', color: '#6c757d' }
}

/* 병원 데이터 상태 */
const hospitals = ref([])
const userLocation = ref(null)

/* 지도 상태 */
const map = ref(null)
const cur = ref({ lat: null, lon: null })
const loadingHospitals = ref(false)
const searchAttempted = ref(false)
let myMapObj = null

// 마커 라벨 관련
let existingLabelPositions = []

/* 바텀시트 드래그 */
const bottomSheet = ref(null)
const isDragging = ref(false)
const sheetHeightRatio = ref(0)
const dragStart = ref({ y: 0, ratio: 0 })

const MAX_SHEET_HEIGHT = window.innerHeight * 0.8
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

let searchTimer = null;
const debounceSearch = () => {
  clearTimeout(searchTimer);
  searchTimer = setTimeout(() => {
    // searchAttempted.value는 초기화 후 첫 검색(showMoonlight()) 시 true로 설정되어야 합니다.
    // 현재 코드에서는 showLayerInView 호출 시 searchAttempted가 true로 변경된다고 가정합니다.
    if (vmap && searchAttempted.value) {
      reSearchCurrentLayer();
    }
  }, 500); // 0.5초 대기 후 실행
};

const currentSort = ref("time");

function changeSort(type) {
  currentSort.value = type;
  type === "distance" ? sortByDistance() : sortByTime();
}

// 🔹 병원 상세 페이지로 이동
const router = useRouter();

function goToHospitalDetail(item) {
  // item이 어떤 형태든 안전하게 파싱
  let hpid = null;

  if (!item) {
    console.warn("⚠️ 병원 객체가 없음:", item);
    return;
  }

  // GeoServer feature 형태 (f.id 존재)
  if (item.id && typeof item.id === "string" && item.id.includes(".")) {
    hpid = item.id.split(".")[1];
  }

  // 일반 hospital 데이터 객체 (properties 구조)
  else if (item.hpid) {
    hpid = item.hpid;
  }

  // 혹시 string으로 넘어온 경우
  else if (typeof item === "string") {
    hpid = item;
  }

  if (!hpid) {
    console.warn("⚠️ hpid를 찾을 수 없습니다:", item);
    return;
  }

  console.log("✅ 추출된 HPID:", hpid);

  if (!hpid || typeof hpid !== 'string') {
    console.warn(`잘못된 hpid:`, hpid)
    return
  }
  router.push({
    name: "hospitalInformationDetail",
    params: { hpid },
  });
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

function isHospitalOpen(props) {
  const now = new Date()
  const day = now.getDay() === 0 ? 7 : now.getDay()

  const openKey = `duty_time${day}s`
  const closeKey = `duty_time${day}c`
  const openTime = String(props[openKey] || '').padStart(4, '0')
  const closeTime = String(props[closeKey] || '').padStart(4, '0')

  if (!openTime || !closeTime || openTime === 'null' || closeTime === 'null') return false

  // ✅ 24시간 운영 케이스 추가 (모든 변형 커버)
  if (
    ['0000', '0'].includes(openTime) &&
    ['2400', '0000', '0'].includes(closeTime)
  ) {
    return true
  }

  const nowMinutes = now.getHours() * 60 + now.getMinutes()
  const openMinutes = parseInt(openTime.slice(0, 2)) * 60 + parseInt(openTime.slice(2))
  const closeMinutes = parseInt(closeTime.slice(0, 2)) * 60 + parseInt(closeTime.slice(2))

  // ✅ 일반적인 당일 운영
  if (openMinutes < closeMinutes) {
    return nowMinutes >= openMinutes && nowMinutes <= closeMinutes
  }

  // ✅ 자정 넘어가는 운영 (예: 2300 ~ 0700)
  if (openMinutes > closeMinutes) {
    return nowMinutes >= openMinutes || nowMinutes <= closeMinutes
  }

  return false
}

/** 상단 버튼 요소 별로 함수 호출 */
/** 상단 버튼 요소별로 GeoServer Layer 조회 후 마커 표시 */
async function showLayerInView({ url, iconUrl, nameKey, addrKey, listType }) {
  if (!vmap) {
    alert("지도가 아직 준비되지 않았습니다.")
    return
  }
  searchAttempted.value = true

  try {
    currentListType.value = listType

    clearMarkers()
    clearLabels()
    refreshUserMarker()

    // 현재 bbox 계산
    const extent = vmap.getView().calculateExtent(vmap.getSize())
    const [minX, minY, maxX, maxY] = window.ol.proj.transformExtent(extent, 'EPSG:3857', 'EPSG:4326')
    const bbox = [minX, minY, maxX, maxY].join(',')

    // GeoServer 호출
    const fullUrl = `${url}&bbox=${bbox},EPSG:4326`
    console.log(`조회 URL: ${fullUrl}`)
    const res = await fetch(fullUrl)
    const geojson = await res.json()

    const filtered = selectedDepartments.value.length
      ? geojson.features.filter(f => {
        const depts = (f.properties?.dgid_id_name || '').split(',').map(d => d.trim())
        return selectedDepartments.value.some(sel =>
          depts.some(dept => dept.includes(sel))
        )
      })
      : geojson.features

    // OPEN 병원 먼저 정렬
    filtered.sort((a, b) => {
      const openA = isHospitalOpen(a.properties) ? 1 : 0
      const openB = isHospitalOpen(b.properties) ? 1 : 0
      return openB - openA  // open=true 먼저
    })

    // 병원별 마커 & 라벨 추가
    filtered.forEach(f => {
      const [lon, lat] = f.geometry.coordinates
      const props = f.properties
      const open = isHospitalOpen(props)
      const hpid = f.id?.split(".")[1] || props.hpid
      const name = props?.[nameKey] || listType

      const finalIconUrl = open ? iconUrl : "/icons/grayLocation.svg"

      if (!isNaN(lon) && !isNaN(lat)) {
        // 마커 표시
        addMarkerHospital(lon, lat, {
          iconUrl: finalIconUrl,
          name,
          addr: props?.[addrKey] || '',
          onClick: () => goToHospitalDetail(hpid)
        })

        // 라벨 표시 (Pretendard 서체 적용됨)
        if (open) {
          addLabelToMarker(lat, lon, name, () => goToHospitalDetail(hpid))
        }
      }
    })

    addToHospitalList(filtered, listType)
    console.log(`✅ ${listType} ${filtered.length}개 표시 완료`)
  } catch (err) {
    console.error(`${listType} 조회 실패:`, err)
    alert(`${listType} 데이터를 불러오는 중 오류가 발생했습니다.`)
  }
}

async function showLayerInMoonView({ url, iconUrl, nameKey, addrKey, listType }) {
  if (!vmap) {
    alert("지도가 아직 준비되지 않았습니다.")
    return
  }
  searchAttempted.value = true

  try {
    currentListType.value = listType

    clearMarkers()
    clearLabels()
    refreshUserMarker()

    // 현재 bbox 계산
    const extent = vmap.getView().calculateExtent(vmap.getSize())
    const [minX, minY, maxX, maxY] = window.ol.proj.transformExtent(extent, 'EPSG:3857', 'EPSG:4326')
    const bbox = [minX, minY, maxX, maxY].join(',')

    // GeoServer 호출
    const fullUrl = `${url}&bbox=${bbox},EPSG:4326`
    console.log(`조회 URL: ${fullUrl}`)
    const res = await fetch(fullUrl)
    const geojson = await res.json()

    const filtered = selectedDepartments.value.length
      ? geojson.features.filter(f => {
        const depts = (f.properties?.dgid_id_name || '').split(',').map(d => d.trim())
        return selectedDepartments.value.some(sel =>
          depts.some(dept => dept.includes(sel))
        )
      })
      : geojson.features

    // OPEN 병원 먼저 정렬
    filtered.sort((a, b) => {
      const openA = isHospitalOpen(a.properties) ? 1 : 0
      const openB = isHospitalOpen(b.properties) ? 1 : 0
      return openB - openA  // open=true 먼저
    })
    
    if (!moonlightZoomSet) {
      vmap.getView().setZoom(14)
      moonlightZoomSet = true
    }

    // 병원별 마커 & 라벨 추가
    filtered.forEach(f => {
      const [lon, lat] = f.geometry.coordinates
      const props = f.properties
      const open = isHospitalOpen(props)
      const hpid = f.id?.split(".")[1] || props.hpid
      const name = props?.[nameKey] || listType

      const finalIconUrl = open ? iconUrl : "/icons/grayLocation.svg"

      if (!isNaN(lon) && !isNaN(lat)) {
        // 마커 표시
        addMarkerHospital(lon, lat, {
          iconUrl: finalIconUrl,
          name,
          addr: props?.[addrKey] || '',
          onClick: () => goToHospitalDetail(hpid)
        })

        // 라벨 표시 (Pretendard 서체 적용됨)
        if (open) {
          addLabelToMarker(lat, lon, name, () => goToHospitalDetail(hpid))
        }
      }
    })

    addToHospitalList(filtered, listType)
    console.log(`✅ ${listType} ${filtered.length}개 표시 완료`)
  } catch (err) {
    console.error(`${listType} 조회 실패:`, err)
    alert(`${listType} 데이터를 불러오는 중 오류가 발생했습니다.`)
  }
}

// 달빛어린이병원 조회 (GeoServer)
function showMoonlight() {
  showLayerInMoonView({
    url: "https://api.child119.com/geoserver/hospital/ows?service=WFS&version=1.0.0&request=GetFeature&typeName=hospital:moonlight_hospital&outputFormat=application/json",
    iconUrl: "/icons/moonlight.svg",
    nameKey: "duty_name",
    addrKey: "duty_addr",
    listType: "달빛어린이병원"
  })
}

// 소아청소년과 조회 (GeoServer)
function showPediatric() {
  showLayerInView({
    url: "https://api.child119.com/geoserver/hospital/ows?service=WFS&version=1.0.0&request=GetFeature&typeName=hospital%3Achildren_hospital&outputFormat=application%2Fjson&maxFeature=50",
    iconUrl: "/icons/children.svg",
    nameKey: "duty_name",
    addrKey: "duty_addr",
    listType: "소아청소년과"
  })
}

// 약국 조회
async function showPharmacy() {
  showLayerInView({
    url: "https://api.child119.com/geoserver/hospital/ows?service=WFS&version=1.0.0&request=GetFeature&typeName=hospital%3Apharmacy&outputFormat=application%2Fjson&maxFeature=50",
    iconUrl: "/icons/pharmacy.svg",
    nameKey: "duty_name",
    addrKey: "duty_addr",
    listType: "약국"
  })
}

// ⚡ AED 조회
async function showAED() {
  showLayerInView({
    url: "https://api.child119.com/geoserver/hospital/ows?service=WFS&version=1.0.0&request=GetFeature&typeName=hospital%3Aaed&outputFormat=application%2Fjson&maxFeature=50",
    iconUrl: '/icons/aed.svg',
    nameKey: "duty_name",
    addrKey: "duty_addr",
    listType: "AED"
  })
}

function reSearchCurrentLayer() {
  switch (currentListType.value) {
    case '달빛어린이병원':
      showMoonlight();
      break;
    case '소아청소년과':
      showPediatric();
      break;
    case '약국':
      showPharmacy();
      break;
    case 'AED':
      showAED();
      break;
    // 초기 상태나 다른 검색 유형이 있다면 여기에 추가
    default:
      console.log(`현재 검색 유형(${currentListType.value})에 대한 재조회 로직이 없습니다.`);
      break;
  }
}

watch(selectedDepartments, (newVal, oldVal) => {
  // 배열 내용이 변경되었을 때만 재조회
  if (newVal.length !== oldVal.length || newVal.some((v, i) => v !== oldVal[i])) {
    // 진료과목 선택/해제 후 자동으로 현재 활성화된 레이어를 다시 검색
    reSearchCurrentLayer();
  }
}, { deep: true }); // 배열의 내부 변화를 감지하기 위해 deep: true 설정

// API 응답을 hospitals 배열로 통일시켜주는 함수
function addToHospitalList(list, type) {
  // 1) 선택한 진료과 필터링 (여기서는 fid/hpid 불필요)
  let filteredList = list
  if (selectedDepartments.value.length > 0) {
    filteredList = list.filter(item => {
      const props = item.properties || {}
      const deptNames = (props.dgid_id_name || '').split(',').map(d => d.trim())
      return selectedDepartments.value.some(sel =>
        deptNames.some(dept => dept.includes(sel))
      )
    })
  }

  // 2) 표준화된 병원 리스트로 매핑
  hospitals.value = filteredList.map((item) => {
    const props = item.properties || {}

    // ✅ fid/hpidFromFid는 여기서 먼저 계산!
    const fid = item.id || props.fid || null               // ex) "children_hospital.A1100647"
    const hpidFromFid = fid && fid.includes('.') ? fid.split('.')[1] : null // ex) "A1100647"

    // 좌표 추출 (GeoJSON Point 기준)
    const [lon, lat] = item.geometry?.coordinates || [NaN, NaN]

    // 거리/ETA 계산
    let distanceKm = null
    let etaMin = null
    if (userLocation.value && Number.isFinite(lat) && Number.isFinite(lon)) {
      const dist = calculateDistance(userLocation.value.lat, userLocation.value.lon, lat, lon)
      distanceKm = (dist / 1000).toFixed(1)
      // 대략 시속 24km(분당 0.4km) 가정 → 필요시 조정
      etaMin = Math.max(1, Math.round((dist / 1000) / 0.4))
    }

    return {
      // 🔑 고유키들
      fid,                                      // 전체 fid 보관(디버깅/로그용)
      id: hpidFromFid || props.hpid || fid,     // v-for :key 용
      hpid: hpidFromFid || props.hpid || null,  // 상세로 넘길 “진짜 HPID”

      // 메타
      type,
      name:
        props.duty_name || props.hospital_name || props.name || props.dutyName || "이름 없음",
      addr:
        props.duty_addr || props.address || props.addr || props.dutyAddr || "주소 없음",
      tel:
        props.duty_tel1 || props.dutytel3 || props.hospital_tel || props.dutyTel1 || "",

      // 위치/거리
      lat,
      lon,
      dept_name: props.dgid_id_name ?? '',
      distanceKm,
      etaMin,
    }
  })

  console.log(`✅ ${type} ${hospitals.value.length}개 표시 완료`)
  console.log("리스트 데이터 확인:", JSON.parse(JSON.stringify(hospitals.value)))
}

function clearLabels() {
  if (!vmap) return
  labelOverlays.forEach(overlay => vmap.removeOverlay(overlay))
  labelOverlays = []
  existingLabelPositions = []
}

function addLabelToMarker(lat, lon, text, onClick) {
  if (!vmap) return

  // 위도경도 → 화면 픽셀 좌표 변환
  const pixel = vmap.getPixelFromCoordinate(window.ol.proj.fromLonLat([lon, lat]))

  // 1️⃣ 기존 라벨들과 거리 계산 (px 단위)
  const tooCloseIndex = existingLabelPositions.findIndex(p => {
    const dx = p[0] - pixel[0]
    const dy = p[1] - pixel[1]
    return Math.sqrt(dx * dx + dy * dy) < 25 // 25px 이하 → 겹침
  })

  // 2️⃣ 기존 라벨과 겹치면 → 기존 라벨 제거 + 현재 것도 추가 안 함
  if (tooCloseIndex !== -1) {
    const oldOverlay = labelOverlays[tooCloseIndex]
    if (oldOverlay && vmap) {
      vmap.removeOverlay(oldOverlay)
    }

    // 배열에서도 제거
    labelOverlays.splice(tooCloseIndex, 1)
    existingLabelPositions.splice(tooCloseIndex, 1)
    return // 이번 라벨도 추가 안 함 (둘 다 제거)
  }

  // 3️⃣ 겹치지 않으면 → 새 라벨 추가
  existingLabelPositions.push(pixel)

  const element = document.createElement("div")
  element.className = "marker-label"
  element.textContent = text

  if (onClick) element.addEventListener("click", onClick)

  const overlay = new window.ol.Overlay({
    position: window.ol.proj.fromLonLat([lon, lat]),
    positioning: "bottom-center",
    offset: [0, -25],
    element,
  })

  vmap.addOverlay(overlay)
  labelOverlays.push(overlay)
  return overlay
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
        //id //하드코딩 
        'emergency.A1100029'
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
async function updateUserLocation(zoom = 16) {
  try {
    const position = await getCurrentLocation()
    const { lat, lon } = position
    userLocation.value = { lat, lon }

    const center = window.ol.proj.fromLonLat([lon, lat])
    vmap.getView().setCenter(center)
    vmap.getView().setZoom(zoom)

    //clearMarkers()
    addMarkerHospital(lon, lat, {
      name: '현재 위치',
      iconUrl: 'icons/currentLocation.svg',
      anchor: [0.5, 0.5]
    })
    console.log(`현재 위치: ${lat},${lon}`)
  } catch (err) {
    console.error('위치 업데이트 실패:', err)
    alert('현재 위치를 가져올 수 없습니다.')

  }
}

async function refreshUserMarker() {
  try {
    // 위치만 새로 받아옴
    const position = await getCurrentLocation()
    const { lat, lon } = position
    userLocation.value = { lat, lon }

    const logoDataUrl = await toDataUrl(kidLogo)
    const iconUrl = createUserPinIcon(logoDataUrl)

    // 기존 마커만 교체
    addMarkerHospital(lon, lat, {
      name: '현재 위치',
      iconUrl,
      anchor: [0.5, 0.5]
    })
    console.log(`현재 위치 마커만 갱신됨: ${lat},${lon}`)
  } catch (err) {
    console.error('위치 마커 갱신 실패:', err)
  }
}

/* 내 위치 새로고침 */
async function refreshLocation() {
  try {
    const position = await getCurrentLocation()
    const { lat, lon } = position
    userLocation.value = { lat, lon }

    const center = window.ol.proj.fromLonLat([lon, lat])
    const view = vmap.getView()

    // 부드럽게 중앙으로 이동
    if (!view.animate) {
      const start = view.getCenter()
      const steps = 20
      let i = 0
      const timer = setInterval(() => {
        const t = ++i / steps
        const lon = start[0] + (center[0] - start[0]) * t
        const lat = start[1] + (center[1] - start[1]) * t
        view.setCenter([lon, lat])
        if (i >= steps) clearInterval(timer)
      }, 10)
    }

    const logoDataUrl = await toDataUrl(kidLogo)
    const iconUrl = createUserPinIcon(logoDataUrl)

    // 현재 위치 마커 갱신
    addMarkerHospital(lon, lat, {
      name: '현재 위치',
      iconUrl,
      anchor: [0.5, 0.5]
    })

    console.log(`화면 중심만 내 위치로 이동: ${lat}, ${lon}`)
  } catch (err) {
    console.error('moveToUserLocation 실패:', err)
    alert('현재 위치를 가져올 수 없습니다.')
  }
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

/* 바텀시트 제스처 */
const onTouchStart = e => {
  const target = e.target
  if (target.closest('button') || target.closest('.btn') || target.closest('.card-body')) {
    return
  }

  isDragging.value = true
  dragStart.value = { y: e.touches[0].clientY, ratio: sheetHeightRatio.value }
  e.preventDefault()
}

const onTouchMove = e => {
  if (!isDragging.value) return
  const target = e.target
  if (target.closest('button') || target.closest('.btn')) {
    return
  }

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

// onMounted - 초기실행
// onMounted(async () => {
//   await new Promise((resolve) => {
//     const checkVworld = setInterval(() => {
//       if (window.vw && window.vw.ol3 && window.ol) {
//         clearInterval(checkVworld)
//         resolve()
//       }
//     }, 100)
//   })

//   try {
//     // 1️⃣ 지도 초기화
//     const defaultCenter = window.ol.proj.transform([126.978, 37.5665], 'EPSG:4326', 'EPSG:3857')
//     vmap = initVworldMap({
//       containerId: 'map_div',
//       defaultCenter,
//       defaultZoom: 15,
//       autoFetch: false
//     })
//     clearMarkers()
//     if (!vmap) throw new Error('vmap 초기화 실패')

//     showMoonlight()

//     // 2️⃣ 현재 위치 표시
//     await updateUserLocation(16)
//   } catch (err) {
//     console.error('초기화 실패:', err)
//   }

// })
onMounted(async () => {
  await new Promise((resolve) => {
    const checkVworld = setInterval(() => {
      if (window.vw && window.vw.ol3 && window.ol) {
        clearInterval(checkVworld)
        resolve()
      }
    }, 100)
  })

  try {
    // 1️⃣ 지도 초기화
    const defaultCenter = window.ol.proj.transform([126.978, 37.5665], 'EPSG:4326', 'EPSG:3857')
    vmap = initVworldMap({
      containerId: 'map_div',
      defaultCenter,
      defaultZoom: 15,
      autoFetch: false
    })
    clearMarkers()
    if (!vmap) throw new Error('vmap 초기화 실패')

    // 🔔 초기 검색: 지도 초기화 후 바로 실행
    // 이 시점에서 searchAttempted.value = true로 변경되어야 합니다.
    showMoonlight()

    // 2️⃣ 현재 위치 표시 및 지도 중앙 이동
    await updateUserLocation(16)

    // 3️⃣ 지도 이벤트 리스너 추가 (vmap 초기화 및 중앙 이동 완료 후)
    if (vmap) {
      const view = vmap.getView();
      // 지도 이동 및 줌 변경 이벤트 감지
      view.on('change:center', debounceSearch);
      view.on('change:resolution', debounceSearch);
      console.log("✅ 지도 이동/줌 이벤트 리스너 추가 완료");
    }

  } catch (err) {
    console.error('초기화 실패:', err)
  }
})

onBeforeUnmount(() => {
  clearMarkers()
  if (vmap && vmap.getView) {
    vmap.setTarget(null)
    vmap = null
  }
})

</script>
<style scoped>
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

/* 클릭(눌림) */
.top-button:focus {
  background-color: #0d6efd;
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

:deep(.marker-label) {
  font-family: 'Pretendard', sans-serif;
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(0, 0, 0, 0.2);
  border-radius: 10px;
  padding: 2px 6px;
  font-size: 12px;
  font-weight: 500;
  color: #333;
  white-space: nowrap;
  pointer-events: auto;
  cursor: pointer;
  user-select: none;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.15);
  transform: translateY(-4px);
  transition: all 0.2s ease;
}

:deep(.marker-label:hover) {
  background: #0d6efd;
  color: #fff;
}

.dropdown-toggle::after {
  margin-left: 8px !important;
}
</style>