import axios from "axios";
import { requestConfig } from "../../app.config";

export const api = axios.create({
  baseURL: requestConfig.baseUrl + "/api/post",
  timeout: 15000,
  headers: { "Content-Type": "application/json" },
});

export const api_ai = axios.create({
  baseURL: requestConfig.baseUrlDocker,
  timeout: 15000,
  headers: { "Content-Type": "multipart/form-data" },
});


// 공공데이터 응급실 기본정보 API
export const publicApi = axios.create({
  baseURL: "https://apis.data.go.kr/B552657/ErmctInfoInqireService",
  timeout: 15000,
});

// HPID 기반 응급실 정보 조회
export async function getEmergencyHospitalByHPID(HPID, pageNo = 1, numOfRows = 10) {
  try {
    const res = await publicApi.get("/getEgytBassInfoInqire", {
      params: {
        serviceKey: "98986084fc2db23e3d5b8ef8512cef9b239bd3610a28a75a801674004cd35828",
        HPID,
        pageNo,
        numOfRows,
        _type: "json", // json 응답
      },
    });
    return res.data.response.body.items.item || [];
  } catch (e) {
    console.error("공공데이터 응급실 조회 실패", e);
    throw e;
  }
}

// egen API
export const egenApi = axios.create({
  baseURL: "https://mediboard.nemc.or.kr/api/v1/search",
  timeout: 15000,
});

export async function searchEgenHospitalByRadius(lat, lon, radius = 1) {
  try {
    const res = await egenApi.get("/handy", {
      params: {
        searchCondition: "radius",
        lat,
        lon,
        radius,
      },
    });
    return res.data.result.data || [];
  } catch (e) {
    console.error("egen 병원 조회 실패", e);
    throw e;
  }
}

// geoserver WFS API
export const WFSApi = axios.create({
  baseURL: "https://api.child119.com/geoserver/hospital/wfs",
  timeout: 15000,
});

export async function getEmergencyFromWFSByBBox(minX, minY, maxX, maxY, options) {
  try {
    // GeoServer WFS 1.1.0 + EPSG:4326 축 순서 혼동 방지용 srsName 명시
    const bboxParam = `${minX},${minY},${maxX},${maxY},EPSG:4326`

    const res = await WFSApi.get("", {
      params: {
        service: "WFS",
        version: "1.1.0",                 // 문제가 있으면 '1.0.0'으로 바꿔 축순서 혼선 제거
        request: "GetFeature",
        typeName: "hospital:emergency",
        srsName: "EPSG:4326",             // ✅ 명시적으로 설정
        outputFormat: "application/json", // GeoServer는 이 값으로 GeoJSON 반환 가능
        bbox: bboxParam,                  // lon,lat 순서
      },
      ...options,
    })
    return res.data.features || []
  } catch (e) {
    console.error("WFS 조회 실패", e)
    throw e
  }
}

// tmap API
export const TMAP_APP_KEY = "pAs3duhDpE5cYgopfzzDmw8anJPaBak8HAdTerg8";

export function loadTmapScript() {
  return new Promise((resolve, reject) => {
    if (window.Tmapv2) return resolve();
    const script = document.createElement("script");
    script.src = `https://apis.openapi.sk.com/tmap/jsv2?version=1&appKey=${TMAP_APP_KEY}`;
    script.onload = () => resolve();
    script.onerror = (err) => reject(err);
    document.head.appendChild(script);
  });
}








/** ✅ Tmap 길찾기 API (출발지~도착지 경로 계산) */
export async function getTmapRoute(startX, startY, endX, endY) {
  try {
    const res = await tmapApi.post("/tmap/routes?version=1&format=json", {
      startX,
      startY,
      endX,
      endY,
      reqCoordType: "WGS84GEO",
      resCoordType: "WGS84GEO",
      searchOption: "0", // 기본(추천) 경로
    });
    return res.data;
  } catch (e) {
    console.error("🚨 Tmap 경로 조회 실패:", e);
    throw e;
  }
}