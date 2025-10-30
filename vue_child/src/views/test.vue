<template>
  <div class="p-4">
    <button @click="fetchHospitalDetail" class="btn btn-primary">
      병원 상세 조회
    </button>

    <pre v-if="hospitalData">{{ hospitalData }}</pre>

     <button @click="fetchEgenHospital" class="btn btn-success mt-3">
      위치 기반 병원 조회
    </button>

    <pre v-if="egenHospitalData">{{ egenHospitalData }}</pre>
  </div>
</template>

<script setup>
import { ref } from "vue"
import { getEmergencyHospitalByHPID, searchEgenHospitalByRadius } from "@/utils/api"

const hospitalData = ref(null)
const egenHospitalData = ref(null)

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
        egenHospitalData.value = await searchEgenHospitalByRadius(lat, lon, 20);
        console.log("위치 기반 병원:", egenHospitalData.value);
      },
      (err) => {
        console.error("Geolocation 오류:", err);
        alert("현재 위치를 가져올 수 없습니다. 기본 위치로 조회합니다.");

        // 기본 위치 : 구로구청
        const defaultLat = 37.494348;
        const defaultLon = 126.887676;

        searchEgenHospitalByRadius(defaultLat, defaultLon, 20)
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

</script>
