<template>
  <!--<div class="p-4">
    <button @click="fetchHospitalDetail" class="btn btn-primary">
      병원 상세 조회
    </button>

    <pre v-if="hospitalData">{{ hospitalData }}</pre>

     <button @click="fetchEgenHospital" class="btn btn-success mt-3">
      위치 기반 병원 조회
    </button>

    <pre v-if="egenHospitalData">{{ egenHospitalData }}</pre>

    <button @click="fetchEmergencyFromWFS" class="btn btn-info mt-3">
      WFS 위치 기반 병원 조회 
    </button>
    <pre v-if="wfsData">{{ wfsData }}</pre>
  </div>-->
  <div class="map-container">
    <div id="map_div" style="width: 100%; height: 600px;"></div>
    <div class="info-panel">
      <p>마커 개수: {{ wfsData.length }}개</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount  } from "vue"
import axios from "axios"

import { getEmergencyHospitalByHPID, searchEgenHospitalByRadius, loadTmapScript, getEmergencyFromWFSByBBox, TMAP_APP_KEY } from "@/utils/api"

const hospitalData = ref(null)
const egenHospitalData = ref(null)

const map = ref(null);
const markers = [];
const wfsData = ref([]);
let debounceTimeout = null
let cancelToken = null


async function fetchHospitalDetail() {
  try {
    hospitalData.value = await getEmergencyHospitalByHPID("A1100001")
    console.log("✅ 병원 정보:", hospitalData.value)
  } catch (err) {
    console.error("❌ 조회 실패:", err)
  }
}

async function fetchEgenHospital() {
  try {
    if (!navigator.geolocation) {
      alert("이 브라우저에서는 위치 정보를 지원하지 않습니다.");
      return;
    }

    navigator.geolocation.getCurrentPosition(
      async (pos) => {
        const lat = pos.coords.latitude;
        const lon = pos.coords.longitude;
        egenHospitalData.value = await searchEgenHospitalByRadius(lat, lon, 1000);
        console.log("위치 기반 병원:", egenHospitalData.value);
      },
      (err) => {
        console.error("Geolocation 오류:", err);
        alert("현재 위치를 가져올 수 없습니다. 기본 위치로 조회합니다.");

        // 기본 위치 : 구로구청
        const defaultLat = 37.494348;
        const defaultLon = 126.887676;

        searchEgenHospitalByRadius(defaultLat, defaultLon, 1000)
          .then((res) => {
            egenHospitalData.value = res;
            console.log("기본 위치 기반 병원:", egenHospitalData.value);
          })
          .catch((err) => console.error("위치 기반 병원 조회 실패:", err));
      }
    );
  } catch (err) {
    console.error("위치 기반 병원 조회 실패:", err);
  }
}

// homeview fetchHospitalFromWFS 수정
async function fetchEmergencyFromWFS() {
  try {
    if (!navigator.geolocation) {
      alert("이 브라우저에서는 위치 정보를 지원하지 않습니다.");
      return;
    }
 navigator.geolocation.getCurrentPosition(
      async (pos) => {
        const lat = pos.coords.latitude;
        const lon = pos.coords.longitude;
        wfsData.value = await getEmergencyFromWFS(lat, lon, 3000);
        console.log("위치 기반 병원:", wfsData.value);
      },
      (err) => {
        console.error("Geolocation 오류:", err);
        alert("현재 위치를 가져올 수 없습니다. 기본 위치로 조회합니다.");

        // 기본 위치 : 구로구청
        const defaultLat = 37.494348;
        const defaultLon = 126.887676;

        getEmergencyFromWFS(defaultLat, defaultLon, 3000)
          .then((res) => {
            wfsData.value = res;
            console.log("기본 위치 기반 병원:", wfsData.value);
          })
          .catch((err) => console.error("위치 기반 병원 조회 실패:", err));
      }
    );
  } catch (err) {
    console.error("위치 기반 병원 조회 실패:", err);
  }

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

//TMAP 
// 사용자 위치 가져오기
const getUserLocation = () =>
  new Promise((resolve) => {
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (pos) => resolve({ lat: pos.coords.latitude, lon: pos.coords.longitude }),
        () => resolve({ lat: 37.494348, lon: 126.887676 }) // 기본: 구로구청
      );
    } else {
      resolve({ lat: 37.494348, lon: 126.887676 });
    }
  });

// BBox 기반 WFS 마커 갱신
const updateMarkersByBBox = async (bbox, label = "update") => {
  if (!map.value) return;
  console.log(`📦 [${label.toUpperCase()}] BBox:`, bbox);

  // ✅ window에서 Tmapv2 가져오기
  const { Tmapv2 } = window;
  if (!Tmapv2) {
    console.error("❌ Tmapv2를 찾을 수 없습니다!");
    return;
  }

  // 이전 요청 취소
  if (cancelToken) {
    cancelToken.cancel("새로운 요청으로 인한 취소");
  }
  cancelToken = axios.CancelToken.source();

  try {
    const features = await getEmergencyFromWFSByBBox(
      ...bbox,
      { cancelToken: cancelToken.token }
    );
    wfsData.value = features;
    console.log(`✅ [${label.toUpperCase()}] ${features.length}개 마커 로드`);

    // ✅ 기존 마커 제거 (안전하게)
    if (markers.value && Array.isArray(markers.value)) {
      markers.value.forEach((m) => m.setMap(null));
      markers.value = [];
    } else {
      markers.value = []; // 초기화
    }

    // 새 마커 추가
    features.forEach((f) => {
      const geom = f.geometry;
      if (geom?.type === "Point" && Array.isArray(geom.coordinates)) {
        const [lon, lat] = geom.coordinates;
        const marker = new Tmapv2.Marker({
          position: new Tmapv2.LatLng(lat, lon),
          map: map.value,
          title: f.properties?.emergency_nm || "응급실",
        });
        markers.value.push(marker);
      }
    });
  } catch (err) {
    if (!axios.isCancel(err)) {
      console.error("❌ WFS 마커 업데이트 실패:", err);
    }
  }
};

// 현재 지도의 BBox 계산
const getCurrentBBox = () => {
  if (!map.value) return null;
  const bounds = map.value.getBounds();
  return [
    bounds._sw.lng(), // minX
    bounds._sw.lat(), // minY
    bounds._ne.lng(), // maxX
    bounds._ne.lat(), // maxY
  ];
};

// 이벤트 핸들러: 드래그 끝
const onDragend = () => {
  console.log("🗺️ dragend 이벤트 발생!");
  if (debounceTimeout) clearTimeout(debounceTimeout);
  debounceTimeout = setTimeout(() => {
    const bbox = getCurrentBBox();
    if (bbox) updateMarkersByBBox(bbox, "dragend");
  }, 300);
};

// 이벤트 핸들러: 줌 변경
const onZoomChanged = () => {
  console.log("🔍 zoom_changed 이벤트 발생!");
  if (debounceTimeout) clearTimeout(debounceTimeout);
  debounceTimeout = setTimeout(() => {
    const bbox = getCurrentBBox();
    if (bbox) updateMarkersByBBox(bbox, "zoom");
  }, 300);
};


// 지도 초기화
const setupMap = async () => {
  const { lat, lon } = await getUserLocation();

  // ✅ window에서 Tmapv2 가져오기
  const { Tmapv2 } = window;
  
  if (!Tmapv2) {
    console.error("❌ Tmapv2가 로드되지 않았습니다!");
    return;
  }

  console.log("✅ Tmapv2 확인:", Tmapv2);
  console.log("📍 Tmapv2.Map:", Tmapv2.Map);
  console.log("📍 Tmapv2.LatLng:", Tmapv2.LatLng);
  console.log("📍 Tmapv2.Marker:", Tmapv2.Marker);

  map.value = new Tmapv2.Map("map_div", {
    center: new Tmapv2.LatLng(lat, lon),
    width: "100%",
    height: "600px",
    zoom: 14,
  });

  // 현재 위치 마커
  new Tmapv2.Marker({
    position: new Tmapv2.LatLng(lat, lon),
    map: map.value,
    title: "현재 위치",
    icon: "https://tmapapi.sktelecom.com/upload/tmap/marker/pin_r_m_s.png",
  });

  // 초기 BBox로 마커 로드
  const initialBbox = getCurrentBBox();
  if (initialBbox) {
    console.log("🚀 [INITIAL] BBox:", initialBbox);
    await updateMarkersByBBox(initialBbox, "initial");
  }

  // 이벤트 리스너 등록
  map.value.addListener("dragend", onDragend);
  map.value.addListener("zoom_changed", onZoomChanged);

  console.log("✅ 지도 초기화 완료 및 이벤트 리스너 등록됨");
};

// 컴포넌트 마운트
onMounted(() => {
  // ✅ Tmap v2는 2단계 로드 필요
  const loadTmapV2 = async () => {
    // 1단계: 메인 스크립트
    await loadTmapScript();
    console.log("✅ 1단계 스크립트 로드 완료");
    
    // 2단계: 실제 라이브러리 (tmapjs2.min.js)
    return new Promise((resolve, reject) => {
      const script2 = document.createElement("script");
      // ✅ 실제 라이브러리 직접 로드
      script2.src = "https://topopentile1.tmap.co.kr/scriptSDKV2/tmapjs2.min.js?version=20231206";
      script2.onload = () => {
        console.log("✅ 2단계 스크립트 로드 완료");
        console.log("📍 window.Tmapv2:", window.Tmapv2);
        console.log("📍 Tmapv2.Map:", window.Tmapv2?.Map);
        resolve();
      };
      script2.onerror = reject;
      document.head.appendChild(script2);
    });
  };

  loadTmapV2()
    .then(() => {
      // 약간의 지연 추가 (스크립트 초기화 대기)
      return new Promise(resolve => setTimeout(resolve, 100));
    })
    .then(() => setupMap())
    .catch((err) => {
      console.error("❌ 지도 로드 실패:", err);
    });
});

// 컴포넌트 언마운트
onBeforeUnmount(() => {
  if (debounceTimeout) clearTimeout(debounceTimeout);
  if (cancelToken) cancelToken.cancel("컴포넌트 언마운트");
  
  // 마커 정리
  markers.value.forEach((m) => m.setMap(null));
  markers.value = [];
});
</script>
<style>
.map-container {
  width: 100%;
  height: 500px; /* 필요에 따라 높이 조절 */
}

.map {
  width: 100%;
  height: 100%;
}
</style>