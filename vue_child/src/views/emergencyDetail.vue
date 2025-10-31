<template>
  <div class="bg-gray min-vh-100 d-flex justify-content-center align-items-start py-4">
    <div class="card shadow-sm border-0 rounded-4 p-4 bg-white" style="width: 420px;">
      <!-- 상단 헤더 -->
      <div class="d-flex align-items-center mb-3 gap-2">
        <button class="btn btn-link p-0 me-2 text-dark d-flex" @click="goBack">
          <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24">
            <path fill="#000" d="M20 11v2H8l5.5 5.5l-1.42 1.42L4.16 12l7.92-7.92L13.5 5.5L8 11z" />
          </svg>
        </button>
        <div class="mb-0 text-bold fs-5 d-flex">{{ hospitalData.dutyName }}</div>
      </div>

      <!-- 병원 정보 -->
      <div class="hospital-info">


        <div class="d-flex justify-content-start align-items-center gap-2 mb-2">
          <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24">
            <path fill="#000"
              d="M12 11.5A2.5 2.5 0 0 1 9.5 9A2.5 2.5 0 0 1 12 6.5A2.5 2.5 0 0 1 14.5 9a2.5 2.5 0 0 1-2.5 2.5M12 2a7 7 0 0 0-7 7c0 5.25 7 13 7 13s7-7.75 7-13a7 7 0 0 0-7-7" />
          </svg>
          <div class="d-flex text-plight">
            {{ hospitalData.dutyAddr }}
          </div>
        </div>

        <div class="d-flex justify-content-start align-items-center gap-2 mb-3">
          <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24">
            <path fill="#000"
              d="M6.62 10.79c1.44 2.83 3.76 5.15 6.59 6.59l2.2-2.2c.28-.28.67-.36 1.02-.25c1.12.37 2.32.57 3.57.57a1 1 0 0 1 1 1V20a1 1 0 0 1-1 1A17 17 0 0 1 3 4a1 1 0 0 1 1-1h3.5a1 1 0 0 1 1 1c0 1.25.2 2.45.57 3.57c.11.35.03.74-.25 1.02z" />
          </svg>
          <div class=" text-plight">
            {{ hospitalData.dutyTel3 }}
          </div>
        </div>

        <div class="d-flex flex-column justify-content-start mb-2">
          <div class="d-flex fs-7 text-bold ">소아 응급실 현황</div>
          <div class="d-flex align-items-center gap-2 ms-2">
            <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24">
              <path fill="#000"
                d="M19 7h-8v7H3V5H1v15h2v-3h18v3h2v-9a4 4 0 0 0-4-4M7 13a3 3 0 0 0 3-3a3 3 0 0 0-3-3a3 3 0 0 0-3 3a3 3 0 0 0 3 3" />
            </svg>
            <div class="text-medium badge rounded-pill bg-success d-flex" v-if="realTimeData">
              {{ realTimeData.bed_avail }} / {{ realTimeData.bed_total }}
            </div>
            <div class="d-flex text-medium" v-if="realTimeData">{{ realTimeData.state }}</div>
          </div>
        </div>

        <div class="d-flex flex-column justify-content-start">
          <div class="d-felx text-bold fs-7 mb-2">진료 시간</div>
          <table class="table table-striped table-bordered text-plight ms-2">
            <tbody>
              <tr>
                <th>월</th>
                <td>{{ hospitalData.dutyTime1s }} - {{ hospitalData.dutyTime1c }}</td>
              </tr>
              <tr>
                <th>화</th>
                <td>{{ hospitalData.dutyTime2s }} - {{ hospitalData.dutyTime2c }}</td>
              </tr>
              <tr>
                <th>수</th>
                <td>{{ hospitalData.dutyTime3s }} - {{ hospitalData.dutyTime3c }}</td>
              </tr>
              <tr>
                <th>목</th>
                <td>{{ hospitalData.dutyTime4s }} - {{ hospitalData.dutyTime4c }}</td>
              </tr>
              <tr>
                <th>금</th>
                <td>{{ hospitalData.dutyTime5s }} - {{ hospitalData.dutyTime5c }}</td>
              </tr>
              <tr>
                <th>토</th>
                <td>{{ hospitalData.dutyTime6s }} - {{ hospitalData.dutyTime6c }}</td>
              </tr>
              <tr>
                <th>일</th>
                <td>{{ hospitalData.dutyTime7s }} - {{ hospitalData.dutyTime7c }}</td>
              </tr>
            </tbody>
          </table>
        </div>


        <div class="info-block">
          <div class="mb-2 text-medium">진료과목</div>

          <div class="departments d-flex flex-wrap gap-2 mb-4">
            <span v-for="(dept, i) in visibleDepartments" :key="i"
              class="badge rounded-pill text-black text-medium px-3 py-2 dept-badge">
              {{ dept }}
            </span>
          </div>

          <div class="text-center">
            <button v-if="hospital.departments.length > 6"
              class="btn btn-sm btn-outline-primary rounded-pill px-3 text-bold" @click="toggleShowAll"><svg
                xmlns="http://www.w3.org/2000/svg" width="10" height="20" viewBox="0 0 24 24">
                <path fill="#0D6EFD" d="M1 3h22L12 22" />
              </svg>
              {{ showAll ? '접기' : '더보기' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from "vue";
import { useRouter } from "vue-router";


const router = useRouter();
const goBack = () => router.back();

// 더미데이터
const showAll = ref(false);
const hospital = ref({
  dutyName: "고대구로병원",
  dutyAddr: "서울특별시 동대문구 경희대로 23(회기동)",
  dutyTel3: "02-958-8114",
  childEmergencyAvailable: 4,
  childEmergencyTotal: 5,
  departments: [
    "내과", "소아청소년과", "신경과", "정신건강의학과",
    "외과", "피부과", "심장혈관흉부외과", "정형외과",
    "신경외과", "성형외과", "산부인과", "이비인후과",
    "마취통증의학과", "영상의학과", "가정의학과"
  ]
});

const visibleDepartments = computed(() => {
  return showAll.value ? hospital.value.departments : hospital.value.departments.slice(0, 12);
});

const toggleShowAll = () => {
  showAll.value = !showAll.value;
};

// api 연결
const state = ref('원활');
import { getEmergencyHospitalByHPID } from '@/utils/api'

let hospitalData = ref(null)
let realTimeData = ref(null)

// homeview에서 클릭할때 param 전달
const hpid = 'A1100001';

async function getHospitalData(hpid) {
  try{
    hospitalData.value = await getEmergencyHospitalByHPID(hpid);
  } catch(err) {
    console.error(`병원 정보 조회 실패 -> ${err}`)
  }
}

getHospitalData(hpid);

async function getRealTimeData(hpid) {

  const base = 'https://api.child119.com/geoserver/hospital/wfs'
  const params = new URLSearchParams({
    service: 'WFS',
    version: '1.0.0',
    request: 'GetFeature',
    typeName: 'hospital:emergency',
    outputFormat: 'application/json',
    FeatureID_filter: hpid
  })
  const url = `${base}?${params.toString()}`
  const res = await fetch(url, { method: 'GET' })
  if (!res.ok) {
    const t = await res.text().catch(() => '')
    throw new Error(`WFS ${res.status} ${t}`)
  }
  const response = await res.json()
  realTimeData.value = response.features[0].properties
  console.log(realTimeData)
  return realTimeData;
}

getRealTimeData();





</script>

<style scoped>
.bg-gray {
  background-color: rgba(170, 170, 188, 0.1);
  min-height: 100vh;
}

.box {
  display: flex;
  align-items: center;
  background-color: rgba(170, 170, 188, 0.05);
  width: 100%;
  padding: 0.6rem 0.8rem;
  border-radius: 0.6rem;
  border: 1px solid rgba(170, 170, 188, 0.4);
  color: #333;
  font-size: 0.95rem;
}

.dept-badge {
  background-color: rgba(170, 170, 188, 0.15);
  border: 1px solid rgba(170, 170, 188, 0.4);
  font-size: 0.9rem;
  transition: all 0.2s ease;
}

.dept-badge:hover {
  background-color: rgba(170, 170, 188, 0.3);
  transform: scale(1.05);
}



.text-accent {
  color: #0D6EFD;
}

.bg-white {
  background-color: #ffffff !important;
}
</style>
