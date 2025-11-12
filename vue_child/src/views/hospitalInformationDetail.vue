<template>
  <div class="bg-gray min-vh-100 d-flex justify-content-center align-items-start py-4">
    <div v-if="hospitalData" class="card shadow-sm border-0 rounded-4 p-4 bg-white" style="width: 420px;">
      <!-- 상단 헤더 -->
      <div class="d-flex align-items-center mb-3 gap-2">
        <button class="btn btn-link p-0 me-2 text-dark d-flex" @click="goBack">
          <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24">
            <path fill="#000" d="M20 11v2H8l5.5 5.5l-1.42 1.42L4.16 12l7.92-7.92L13.5 5.5L8 11z" />
          </svg>
        </button>
        <div class="mb-0 text-bold fs-5 d-flex">{{ hospitalData.duty_name }}</div>
      </div>

      <!-- 병원 정보 -->
      <div class="hospital-info">


        <div class="d-flex justify-content-start align-items-center gap-2 mb-2">
          <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24">
            <path fill="#000"
              d="M12 11.5A2.5 2.5 0 0 1 9.5 9A2.5 2.5 0 0 1 12 6.5A2.5 2.5 0 0 1 14.5 9a2.5 2.5 0 0 1-2.5 2.5M12 2a7 7 0 0 0-7 7c0 5.25 7 13 7 13s7-7.75 7-13a7 7 0 0 0-7-7" />
          </svg>
          <div class="d-flex text-plight">
            {{ hospitalData.duty_addr }}
          </div>
        </div>

        <div class="d-flex justify-content-start align-items-center gap-2 mb-3">
          <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24">
            <path fill="#000"
              d="M6.62 10.79c1.44 2.83 3.76 5.15 6.59 6.59l2.2-2.2c.28-.28.67-.36 1.02-.25c1.12.37 2.32.57 3.57.57a1 1 0 0 1 1 1V20a1 1 0 0 1-1 1A17 17 0 0 1 3 4a1 1 0 0 1 1-1h3.5a1 1 0 0 1 1 1c0 1.25.2 2.45.57 3.57c.11.35.03.74-.25 1.02z" />
          </svg>
          <div class=" text-plight">
            {{ hospitalData.duty_tel3 || hospitalData.duty_tel1 }}
          </div>
        </div>

        <div v-if="hospitalType == 'aed'" class="py-2">
          <div class="d-flex flex-column justify-content-start gap-2 mb-2">
            <div>
              <div class="mb-2 text-bold">상세위치 정보</div>
            </div>
            <div class="d-flex text-plight">
              {{ hospitalData.duty_detail }}
            </div>
          </div>
        </div>

        <div class="d-flex flex-column justify-content-start">
          <div class="d-felx text-bold fs-7 mb-2">진료 시간</div>
          <table class="table table-striped table-bordered text-plight ms-2">
            <tbody>
              <tr>
                <th>월</th>
                <td>{{ formatTime(hospitalData.duty_time1s) }} - {{ formatTime(hospitalData.duty_time1c) }}</td>
              </tr>
              <tr>
                <th>화</th>
                <td>{{ formatTime(hospitalData.duty_time2s) }} - {{ formatTime(hospitalData.duty_time2c) }}</td>
              </tr>
              <tr>
                <th>수</th>
                <td>{{ formatTime(hospitalData.duty_time3s) }} - {{ formatTime(hospitalData.duty_time3c) }}</td>
              </tr>
              <tr>
                <th>목</th>
                <td>{{ formatTime(hospitalData.duty_time4s) }} - {{ formatTime(hospitalData.duty_time4c) }}</td>
              </tr>
              <tr>
                <th>금</th>
                <td>{{ formatTime(hospitalData.duty_time5s) }} - {{ formatTime(hospitalData.duty_time5c) }}</td>
              </tr>
              <tr>
                <th>토</th>
                <td>{{ formatTime(hospitalData.duty_time6s) }} - {{ formatTime(hospitalData.duty_time6c) }}</td>
              </tr>
              <tr>
                <th>일</th>
                <td>{{ formatTime(hospitalData.duty_time7s) }} - {{ formatTime(hospitalData.duty_time7c) }}</td>
              </tr>
            </tbody>
          </table>
        </div>

        <div v-if="hospitalData.duty_inf" class="py-2">
          <div class="d-flex flex-column justify-content-start gap-2 mb-2">
            <div>
              <div class="mb-2 text-bold">안내</div>
            </div>
            <div class="d-flex text-plight">
              {{ hospitalData.duty_inf }}
            </div>
          </div>
        </div>

        <div v-if="hospitalType !== 'pharmacy' && hospitalType !=='aed'">
          <div class="info-block">
            <div class="mb-2 text-bold">진료과목</div>

            <div class="departments d-flex flex-wrap gap-2 mb-4">
              <span v-for="(dept, i) in visibleDepartments" :key="i"
                class="badge rounded-pill text-black text-plight px-3 py-2 dept-badge">
                {{ dept }}
              </span>
            </div>

            <div class="text-center pb-5">
              <button v-if="departments.length > 12"
                class="btn btn-sm btn-outline-primary rounded-pill px-3  text-bold" @click="toggleShowAll"><svg
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
  </div>
</template>

<script setup>
import { ref, computed, watch } from "vue";
import { useRouter } from "vue-router";

const router = useRouter();
const goBack = () => router.back();

const hospitalData = ref(null);
const showAll = ref(false);
const hospitalType = ref("hospital");

// props에서 hpid 받아오기
const props = defineProps({ hpid: String });
const hpid = ref("");

// props가 들어오면 디코딩
watch(() => props.hpid, (val) => {
  if (!val) return
  console.log('✅ 전달받은 HPID:', val)
  getHospitalAllByHPID(val)
}, { immediate: true })

function formatTime(time) {
  if (!time) return '정보 없음';
  const timeStr = String(time).padStart(4, '0');
  return `${timeStr.substring(0, 2)}:${timeStr.substring(2, 4)}`;
}

async function getHospitalAllByHPID(hpid) {
  const layers = [
    "hospital:moonlight_hospital",
    "hospital:children_hospital",
    "hospital:pharmacy",
    "hospital:aed"
  ];

  try {
    const reqs = layers.map(async (layer) => {
      // ✅ fid 변수를 여기서 선언해야 함!!
      const fid = `${layer.split(":")[1]}.${hpid}`;
      const url = `https://api.child119.com/geoserver/hospital/ows?service=WFS&version=1.0.0&request=GetFeature&typeName=${layer}&outputFormat=application/json&featureId=${fid}`;
      
      console.log("🌍 요청 URL:", url);

      const res = await fetch(url);
      const data = await res.json().catch(() => null);
      if (data?.features?.[0]) {
        // ✅ 찾은 레이어 종류를 기록
        hospitalType.value = layer.split(":")[1];
      }
      return data?.features?.[0] || null;
    });

    const results = await Promise.all(reqs);
    const found = results.find(Boolean);

    if (!found) {
      console.warn(`⚠️ HPID ${hpid} 에 해당하는 병원이 어떤 레이어에서도 발견되지 않음`);
      hospitalData.value = null;
      return;
    }

    // ✅ 병원 데이터 저장
    hospitalData.value = found.properties;
    console.log("✅ 병원 데이터 로드 완료:", hospitalData.value);
  } catch (err) {
    console.error("🌐 GeoServer 조회 실패:", err);
    hospitalData.value = null;
  }
}

// 진료과목 표시용 computed
const departments = computed(() => {
  if (hospitalData.value && typeof hospitalData.value.dgid_id_name === "string") {
    return hospitalData.value.dgid_id_name.split(",");
  }
  return [];
});

const visibleDepartments = computed(() =>
  showAll.value ? departments.value : departments.value.slice(0, 12)
);

const toggleShowAll = () => {
  showAll.value = !showAll.value;
};
</script>

<style scoped>
.bg-gray {
  background-color: rgba(170, 170, 188, 0.1);
  min-height: 100vh;
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
.bg-white {
  background-color: #ffffff !important;
}
</style>