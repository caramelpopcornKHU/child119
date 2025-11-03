<template>
  <div class="d-flex justify-content-center align-items-center position-relative mt-1"
    style="height: calc(100vh - 140px); ">

    <!-- 지도 DIV에 pointer-events 보강 -->
    <div id="map_div" class="position-fixed top-0 start-0 w-100" style="height:100dvh; z-index:0; pointer-events:auto;">
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
    <!-- 내 위치 새로고침 버튼 -->
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
        <div class="card shadow-sm mb-3" v-for="h in listHospitals" :key="h.hpid || h.id">
          <div class="card-body" @click="goToemergencyDetail(h.id)">
            <div class="d-flex justify-content-between align-items-start">
              <h6 class="card-title fw-bold mb-1">{{ h.name }}</h6>
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


            <!-- ✅ 병원 데이터의 message 필드를 이용해 소아가능 / 불가능 자동 표시 -->
            <div class="mb-2" v-if="h.message">
              <span class="badge me-1" :class="h.message.includes('가능')
                ? 'bg-success-subtle text-success-emphasis'
                : 'bg-secondary-subtle text-secondary-emphasis'">
                {{ h.message }}
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
              <button class="btn btn-outline-primary w-100" @click.stop="callHospital(h.id)">
                <i class="bi bi-telephone-fill"></i> 전화
              </button>
              <button class="btn btn-primary w-100" @click.stop="openRouteModal(h)">
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
import { ref, computed, onMounted, onBeforeUnmount, onActivated } from 'vue';
import { getEmergencyHospitalByHPID } from '@/utils/api'

import { useRouter } from 'vue-router';

// <script setup> 맨 위 근처
const isDesktop = matchMedia('(pointer:fine)').matches

// 침대 아이콘으로 변경 - 이미지와 동일한 디자인 (2/7 형식)
const createMarkerIcon = (bedAvail, bedTotal, isHover = false) => {
  const size = isHover ? 75 : 70

  // 병상 가용률 계산
  let bedColor = '#dc3545' // 기본 빨강 (0개 또는 정보 없음)
  let bedText = '0/0'
  let mainBgColor = '#16a085' // 기본 초록색 배경

  if (Number.isFinite(bedAvail) && Number.isFinite(bedTotal) && bedTotal > 0) {
    bedText = `${bedAvail}/${bedTotal}` // 2/7 형식

    if (bedAvail > 0) {
      bedColor = '#28a745' // 초록 (가용 병상 있음)
      mainBgColor = '#16a085' // 진한 초록
    } else {
      bedColor = '#dc3545' // 빨강 (가용 병상 0개)
      mainBgColor = '#e74c3c' // 배경도 빨강으로
    }
  } else {
    // 정보 없을 때는 회색
    mainBgColor = '#95a5a6'
    bedColor = '#7f8c8d'
  }

  // 테두리 효과 (hover)
  const strokeWidth = isHover ? '4' : '3'
  const shadowOpacity = isHover ? '0.4' : '0.25'

  return 'data:image/svg+xml;utf8,' + encodeURIComponent(`
    <svg xmlns="http://www.w3.org/2000/svg" width="${size}" height="${size}" viewBox="0 0 80 90">
      <!-- 그림자 효과 -->
      <ellipse cx="40" cy="82" rx="18" ry="4" fill="black" opacity="${shadowOpacity}"/>

      <!-- 메인 원형 배경 (초록색) -->
      <circle cx="40" cy="35" r="28" fill="${mainBgColor}" stroke="white" stroke-width="${strokeWidth}"/>

      <!-- 침대 아이콘 (흰색) - 크게 -->
      <g transform="translate(40, 35)">
        <path transform="translate(-14, -12) scale(1.2)" fill="white"
          d="M19 7h-8v7H3V5H1v15h2v-3h18v3h2v-9a4 4 0 0 0-4-4M7 13a3 3 0 0 0 3-3a3 3 0 0 0-3-3a3 3 0 0 0-3 3a3 3 0 0 0 3 3"/>
      </g>

      <!-- 하단 숫자 배지 (빨강 또는 초록) -->
      <circle cx="40" cy="68" r="16" fill="${bedColor}" stroke="white" stroke-width="2.5"/>

      <!-- 병상 숫자 텍스트 (2/7 형식) -->
      <text x="40" y="72.5" text-anchor="middle" dominant-baseline="central"
            fill="white" font-size="12" font-weight="bold" font-family="Arial, sans-serif">${bedText}</text>
    </svg>
  `)
}




let routeLine = null; // 현재 경로 Polyline 저장용

let autoPickedOnce = false; // ✅ 자동 테스트 1회 제한 플래그



// async function callHospital(hpid) {
//   try {
//     const cleanedHpid = hpid.replace('emergency.', '');
//     const hospitalData = await getEmergencyHospitalByHPID(cleanedHpid); 

//     console.log("클라이언트가 받은 hospitalData:", hospitalData);

//     if (hospitalData && hospitalData.length > 0) {
//         const hospitalInfo = hospitalData[0];
//         const emergencyTel = hospitalInfo.dutyTel3; // 응급실 전화번호
//         const emerHospitalTel = hospitalInfo.dutyTel1; // 병원 대표 전화번호

//         let message = '';
//         let returnTel = null;

//         // 1. 응급실 연락처(dutyTel3)가 유효한지 확인 (null, undefined, 또는 빈 문자열이 아닌지)
//         if (emergencyTel && emergencyTel.trim()) {
//             message = `응급실 연락처: ${emergencyTel}`;
//             returnTel = emergencyTel;
//         } 
//         // 2. 응급실 연락처가 없으면, 병원 대표 연락처(dutyTel1)를 확인
//         else if (emerHospitalTel && emerHospitalTel.trim()) {
//             message = `응급실 전화번호가 등록되지 않았습니다.\n병원 대표 연락처: ${emerHospitalTel}`;
//             returnTel = emerHospitalTel;
//         }
//         // 3. 둘 다 없는 경우
//         else {
//             message = '등록된 응급실 및 병원 대표 연락처가 없습니다.';
//         }

//         alert(message);
//         return returnTel;
//     } else {
//         // 병원 코드는 유효하지만, 데이터가 아예 조회되지 않은 경우 (totalCount: 0)
//         alert('요청하신 병원 정보(데이터)를 찾을 수 없습니다.');
//         return null;
//     }

//   } catch (err) {
//       console.error(`callHospital error -> ${err}`);
//       alert(`병원 정보 처리 중 오류 발생: ${err.message || '알 수 없는 오류'}`);
//   }
// }

async function callHospital(hpid) {
  try {
    const cleanedHpid = hpid.replace('emergency.', '');
    // getEmergencyHospitalByHPID는 API 호출 함수라고 가정합니다.
    const hospitalData = await getEmergencyHospitalByHPID(cleanedHpid);

    console.log("클라이언트가 받은 hospitalData:", hospitalData);

    if (hospitalData && hospitalData.length > 0) {
      const hospitalInfo = hospitalData[0];
      // 응급실 전화번호
      const emergencyTel = hospitalInfo.dutyTel3;
      // 병원 대표 전화번호
      const emerHospitalTel = hospitalInfo.dutyTel1;

      let message = '';
      let finalTel = null; // 최종적으로 사용할 전화번호

      // 1. 응급실 연락처(dutyTel3)가 유효한지 확인
      if (emergencyTel && emergencyTel.trim()) {
        message = `응급실(${emergencyTel})로 연결합니다.`;
        finalTel = emergencyTel;
      }
      // 2. 응급실 연락처가 없으면, 병원 대표 연락처(dutyTel1)를 확인
      else if (emerHospitalTel && emerHospitalTel.trim()) {
        message = `응급실 전화번호가 등록되지 않아\n병원 대표번호(${emerHospitalTel})로 연결합니다.`;
        finalTel = emerHospitalTel;
      }
      // 3. 둘 다 없는 경우
      else {
        message = '등록된 응급실 및 병원 대표 연락처가 없습니다.';
      }

      if (finalTel) {
        // 실제 전화 걸기 로직 실행
        window.location.href = `tel:${finalTel.replace(/[^0-9]/g, '')}`;

      } else {
        // 전화번호가 없는 경우 사용자에게 알림
        alert(message);
      }

      // 함수에서는 더 이상 값 반환을 하지 않아도 됩니다. (필요 시 return finalTel;)
    } else {
      alert('요청하신 병원 정보(데이터)를 찾을 수 없습니다.');
    }

  } catch (err) {
    console.error(`callHospital error -> ${err}`);
    alert(`병원 정보 처리 중 오류 발생: ${err.message || '알 수 없는 오류'}`);
  }
}


function clearRoute() {
  if (routeLine) {
    routeLine.setMap(null);
    routeLine = null;
  }
}



async function drawRouteTo(destLat, destLon) {
  if (!map.value) return
  if (!cur.value.lat || !cur.value.lon) {
    alert('출발 위치가 설정되지 않았습니다. (HTTPS 위치 권한 필요)')
    return
  }

  const startX = +cur.value.lon
  const startY = +cur.value.lat
  const endX = +destLon
  const endY = +destLat

  try {
    clearRoute()
    const url = `https://apis.openapi.sk.com/tmap/routes?version=1&format=json&appKey=${TMAP_APP_KEY}` // ✅ 쿼리에도 appKey
    console.log('[routes] req', { startX, startY, endX, endY, url })

    const res = await fetch(url, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json',      // ✅ 추가
        'appKey': TMAP_APP_KEY             // ✅ 헤더도 유지
      },
      body: JSON.stringify({
        startX, startY, endX, endY,
        reqCoordType: 'WGS84GEO',
        resCoordType: 'WGS84GEO',
        searchOption: '0',
      }),
    })

    let data = {}
    try { data = await res.json() } catch { /* noop */ }
    console.log('[routes] status', res.status, res.ok, data)

    if (!res.ok) {
      alert(`경로 실패 (${res.status})`)
      return
    }

    const features = Array.isArray(data.features) ? data.features : []
    const paths = []
    for (const f of features) {
      const g = f.geometry
      if (!g) continue
      if (g.type === 'LineString') {
        paths.push(...g.coordinates.map(([x, y]) => new window.Tmapv3.LatLng(y, x)))
      } else if (g.type === 'MultiLineString') {
        g.coordinates.forEach(seg => {
          paths.push(...seg.map(([x, y]) => new window.Tmapv3.LatLng(y, x)))
        })
      }
    }

    if (!paths.length) {
      console.warn('[routes] empty features', data)
      alert('표시할 경로가 없습니다.')
      return
    }

    routeLine = new window.Tmapv3.Polyline({
      path: paths,
      strokeColor: '#0d6efd',
      strokeWeight: 6,
      strokeOpacity: 0.9,
      map: map.value,
    })

    const b = new window.Tmapv3.LatLngBounds()
    paths.forEach(p => b.extend(p))
    map.value.fitBounds(b)
  } catch (e) {
    console.error('경로 그리기 실패:', e)
    alert('경로 요청 중 오류가 발생했습니다.')
  }
}



/** 가까운 3곳 경로 모두 표시 */
async function drawRoutesToTop3() {
  if (!map.value || !cur.value.lat || !cur.value.lon) {
    alert('출발 위치가 없습니다. (위치 권한 필요)')
    return
  }

  // 기존 경로 지우기
  if (routeLine) {
    if (Array.isArray(routeLine)) routeLine.forEach(r => r.setMap(null))
    else routeLine.setMap(null)
  }

  // 상위 3개 병원만 선택
  const top3 = hospitals.value
    .filter(h => Number.isFinite(h.distance))
    .sort((a, b) => a.distance - b.distance)
    .slice(0, 3)

  if (top3.length === 0) {
    alert('표시할 병원이 없습니다.')
    return
  }

  const colors = ['#0d6efd', '#198754', '#ffc107'] // 파랑/초록/노랑

  const startX = +cur.value.lon
  const startY = +cur.value.lat
  const polylines = []

  for (let i = 0; i < top3.length; i++) {
    const dest = top3[i]
    const endX = +dest.lon
    const endY = +dest.lat

    try {
      const url = `https://apis.openapi.sk.com/tmap/routes?version=1&format=json&appKey=${TMAP_APP_KEY}`
      const res = await fetch(url, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'application/json',
          'appKey': TMAP_APP_KEY
        },
        body: JSON.stringify({
          startX, startY, endX, endY,
          reqCoordType: 'WGS84GEO',
          resCoordType: 'WGS84GEO',
          searchOption: '0'
        })
      })

      const data = await res.json()
      const features = Array.isArray(data.features) ? data.features : []

      const paths = []
      for (const f of features) {
        const g = f.geometry
        if (!g) continue
        if (g.type === 'LineString')
          paths.push(...g.coordinates.map(([x, y]) => new window.Tmapv3.LatLng(y, x)))
        else if (g.type === 'MultiLineString')
          g.coordinates.forEach(seg =>
            paths.push(...seg.map(([x, y]) => new window.Tmapv3.LatLng(y, x)))
          )
      }

      if (!paths.length) continue

      const poly = new window.Tmapv3.Polyline({
        path: paths,
        strokeColor: colors[i % colors.length],
        strokeWeight: 6,
        strokeOpacity: 0.9,
        map: map.value
      })
      polylines.push(poly)
    } catch (e) {
      console.error(`경로 ${i + 1} 실패:`, e)
    }
  }

  // Polyline 저장 (나중에 clearRoute()에서 제거용)
  routeLine = polylines

  // 모든 경로가 그려졌으면 지도 범위 맞춤
  const b = new window.Tmapv3.LatLngBounds()
  hospitals.value.slice(0, 3).forEach(h => {
    if (h.lat && h.lon) b.extend(new window.Tmapv3.LatLng(h.lat, h.lon))
  })
  map.value.fitBounds(b)
}



// 선택된 병원(모달/경로 공용)
const selectedHospital = ref(null)

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



// ✅ 내 위치/마커 상태
const currentLocationMarker = ref(null)
const currentLocationInfoWindow = ref(null)
const userLocation = ref({ lat: null, lon: null })

// ✅ 경로 모드 여부 (지금은 경로 모드가 따로 없으니 false 유지)
const isRouteMode = ref(false)

// ✅ 새로고침 시 재검색 반경(m). 초기에 5000m, 이후엔 마지막 반경 유지
const searchRadius = ref(5000)



/* 내 위치 새로고침 */
function refreshLocation() {
  if (!map.value) {
    alert('지도를 불러오는 중입니다. 잠시 후 다시 시도해주세요.')
    return
  }

  if (!navigator.geolocation) {
    alert('이 브라우저에서는 위치 정보를 지원하지 않습니다.')
    return
  }

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
      console.log('위치 정보 새로고침 성공:', { lat, lon })

      const currentPos = new window.Tmapv3.LatLng(lat, lon)
      userLocation.value = { lat, lon }
      cur.value = { lat, lon }            // ✅ 검색에 쓰는 기준점도 업데이트

      // 기존 내 위치 마커와 정보창 제거
      if (currentLocationMarker.value) {
        currentLocationMarker.value.setMap(null)
      }
      if (currentLocationInfoWindow.value) {
        currentLocationInfoWindow.value.setMap(null)
      }

      // 새로운 내 위치 마커 생성
      currentLocationMarker.value = new window.Tmapv3.Marker({
        position: currentPos,
        map: map.value
      })

      currentLocationInfoWindow.value = new window.Tmapv3.InfoWindow({
        position: currentPos,
        content: "<div class='p-1 bg-white border rounded-2'>현재 위치</div>",
        offset: new window.Tmapv3.Point(0, -30),
        type: 2,
        map: map.value
      })

      // 지도 중심 이동
      map.value.setCenter(currentPos)
      map.value.setZoom(17)

      // ✅ 현재 좌표 갱신
      cur.value = { lat, lon }

      // ✅ 경로 모드가 아닐 때만 병원 재검색 (이 컴포넌트의 WFS 검색 사용)
      if (!isRouteMode.value) {
        // 직전 반경이 있으면 그걸 쓰고, 없으면 기본 5000m
        const radius = lastRadius.value > 0 ? lastRadius.value : searchRadius.value
        searchER(radius, false)
      }

    },
    err => {
      console.error('Geolocation 오류 코드:', err.code, '메시지:', err.message)
      let errorMsg = '현재 위치를 가져올 수 없습니다.\n'

      switch (err.code) {
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
    },
    geoOptions
  )
}



const router = useRouter()

function goToemergencyDetail(hpid) {
  router.push({
    name: 'emergencyDetail',
    params: { hpid: hpid }, // URL엔 필수 키만
  })

}



/* 정렬 상태 (토글용) */
const distanceAsc = ref(true)




/* 실제 렌더링 목록: 항상 WFS에서 가져온 hospitals 사용 */
const listHospitals = computed(() => {

  return hospitals.value.map(h => ({
    id: h.id,
    hpid: h.hpid,               // ← 추가
    name: h.name,
    message: h.message,
    state: h.state,
    bedAvail: h.bedAvail,   // ✅ 추가
    bedTotal: h.bedTotal,
    distanceKm: (h.distance ?? 0) / 1000,
    etaMin: Math.max(1, Math.round(((h.distance ?? 0) / 1000) / 0.4)),
    lat: h.lat, lon: h.lon,     // (선택) 모달/길찾기 즉시 사용
    tel: h.tel || ''
  }))
})






/* 거리순 정렬: hospitals가 있으면 hospitals를, 아니면 데모를 정렬 */
function sortByDistance() {

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
const sheetStyle = computed(() => {
  const interactive = sheetHeightRatio.value > 0.12 // ⬅️ 0.12로 상향
  return {
    transition: isDragging.value ? 'none' : 'transform 0.3s ease-out',
    transform: `translateY(${sheetY.value}px)`,
    height: `${MAX_SHEET_HEIGHT}px`,
    bottom: '0px',
    top: 'auto',
    //pointerEvents: interactive ? 'auto' : 'none',
    willChange: 'transform'
  }
})

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

    // 🔹 병상 정보를 반영한 커스텀 침대 아이콘으로 마커 생성
    const customIcon = createMarkerIcon(h.bedAvail, h.bedTotal, false)
    const customIconHover = createMarkerIcon(h.bedAvail, h.bedTotal, true)

    let marker = new window.Tmapv3.Marker({
      position: pos,
      map: map.value,
      icon: customIcon,      // 실시간 병상 정보 반영 아이콘
      title: h.name || ''    // 접근성/툴팁
    })

    // hover용 아이콘 저장
    marker.__customIcon = customIcon
    marker.__customIconHover = customIconHover

    // (선택) 인포윈도우는 그대로
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
    const info = new window.Tmapv3.InfoWindow({
      position: pos,
      content: html,
      type: 2,
      map: null,
      offset: new window.Tmapv3.Point(0, -10)
    })

    // 클릭 → 인포윈도우 + 경로
    const handlePick = () => {
      console.log('[marker] picked:', h.name, h.lat, h.lon)
      markers.forEach(m => m.__info?.setMap?.(null))
      info.setMap(map.value)
      drawRouteTo(h.lat, h.lon) // ✅ GeoServer 좌표 사용
    }

    // 클릭/터치 (중복/과다 금지)
    window.Tmapv3.event.addListener(marker, 'click', handlePick)
    window.Tmapv3.event.addListener(marker, 'touchend', handlePick)

    // 호버(포인터 있는 환경에서만)
    if (isDesktop) {
      window.Tmapv3.event.addListener(marker, 'mouseover', () => {
        marker.setIcon?.(marker.__customIconHover)
        marker.setZIndex?.(1000)
        const el = document.getElementById('map_div'); if (el) el.style.cursor = 'pointer'
      })
      window.Tmapv3.event.addListener(marker, 'mouseout', () => {
        marker.setIcon?.(marker.__customIcon)
        marker.setZIndex?.(0)
        const el = document.getElementById('map_div'); if (el) el.style.cursor = ''
      })
    }

    marker.__info = info
    markers.push(marker)
  })

  if (items.length) map.value.fitBounds(bounds)

  // ✅ 루프와 fitBounds 이후에 (그리고 필요 시 one-time 플래그 사용)
  if (!autoPickedOnce && markers[0]) {
    // autoPickedOnce = true
    // console.log('[auto-pick test] first marker')
    // const p = markers[0].getPosition?.()
    // const mLat = p._lat ?? p.lat?.() ?? p.lat
    // const mLon = p._lng ?? p.lng?.() ?? p.lng
    // drawRouteTo(mLat, mLon)

    console.log('[auto-pick test] top 3 routes')
    drawRoutesToTop3()

  }






}


async function searchER(radiusMeters, useSymptoms) {


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

    // 사용자 요청(증상 검색)일 때만 자동 펼침
    if (useSymptoms && hospitals.value.length) sheetHeightRatio.value = 1

  } catch (e) {
    console.error('WFS 검색 실패:', e)
    alert('주변 병원 검색 중 오류가 발생했습니다. (WFS)')
  } finally {
    loadingHospitals.value = false
  }
}

/** GeoServer WFS에서 DWithin으로 병원 받아오기 */
/** GeoServer WFS에서 DWithin으로 병원 받아오기 */
async function fetchHospitalsFromWFS(centerLon, centerLat, radiusMeters) {
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
  const res = await fetch(url)
  const data = await res.json()
  const feats = data.features || []

  const coalesce = (...vals) =>
    vals.find(v => v !== undefined && v !== null && String(v).trim() !== '') ?? ''

  return feats.map((f, i) => {
    const [lon, lat] = f.geometry?.coordinates || []
    const p = f.properties || {}

    const message = coalesce(p.message, p.MSG, p.msg, '') // ✅ 여기에 소아가능여부 속성 추가
    const state = Number(p.state ?? -1) // ✅ state 추가



    const name = coalesce(p.name, p.hospname, p.yadmNm, `병원 ${i + 1}`)
    const address = coalesce(p.addr, p.address, '')
    const tel = coalesce(p.tel, p.phone, '')
    const hours = coalesce(p.hours, p.oper_time, '')
    const distance = (Number.isFinite(lat) && Number.isFinite(lon))
      ? haversineMeter(centerLat, centerLon, lat, lon)
      : Infinity


    const toInt = (v) => {
      if (v === null || v === undefined) return null
      const n = parseInt(String(v).trim(), 10)
      return Number.isFinite(n) ? n : null
    }

    const bedAvail = toInt(p.bed_avail ?? p.bedAvail ?? p.bedavail)
    const bedTotal = toInt(p.bed_total ?? p.bedTotal ?? p.bedtotal)


    return {
      id: f.id || `wfs-${i}`, hpid: p.hpid, name, address, tel, hours, lat, lon, distance, message, state, bedAvail,
      bedTotal
    }
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

  // 지도 클릭 → 가장 가까운 마커를 선택으로 간주 (모바일/웹뷰에서 클릭 누락 보완)
  window.Tmapv3.event.addListener(map.value, 'click', (evt) => {
    if (!evt || !evt.latLng) return
    const lat = evt.latLng._lat ?? evt.latLng.lat?.() ?? evt.latLng.lat
    const lon = evt.latLng._lng ?? evt.latLng.lng?.() ?? evt.latLng.lng

    const z = map.value.getZoom?.() ?? 16
    const threshold = z >= 17 ? 20 : z >= 15 ? 35 : 60

    let best = null, bestDist = Infinity
    markers.forEach(m => {
      const p = m.getPosition?.(); if (!p) return
      const mLat = p._lat ?? p.lat?.() ?? p.lat
      const mLon = p._lng ?? p.lng?.() ?? p.lng
      const d = haversineMeter(lat, lon, mLat, mLon)
      if (d < bestDist) { bestDist = d; best = m }
    })

    if (best && bestDist <= threshold) {
      // ✅ 먼저 다 닫고
      markers.forEach(m => m.__info?.setMap?.(null))
      // ✅ 다시 열고
      best.__info?.setMap?.(map.value)

      const p = best.getPosition?.()
      const mLat = p._lat ?? p.lat?.() ?? p.lat
      const mLon = p._lng ?? p.lng?.() ?? p.lng
      console.log('[map click -> nearest marker]', mLat, mLon) // 디버그
      drawRouteTo(mLat, mLon)
    }
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