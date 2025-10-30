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

export async function searchEgenHospitalByRadius(lat, lon, radius = 20) {
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