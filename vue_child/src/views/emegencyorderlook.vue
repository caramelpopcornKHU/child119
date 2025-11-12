<template>
  <main class="bg-body-tertiary min-vh-100">
    <div class="container-fluid py-4 px-3">

<!-- 상단 헤더 -->
<div class="d-flex align-items-center mb-3 gap-2">
  <button class="btn btn-link p-0 text-dark" @click="goBack">
    <i class="bi bi-arrow-left fs-5"></i>
  </button>
  <h5 class="mb-0 text-bold d-flex align-items-center gap-2">
    {{ info.title }}
    <i :class="categoryIcon" class="text-danger fs-5"></i>
    <span class="badge bg-danger text-light">{{ info.category }}</span>
  </h5>
</div>


<!-- 대표 이미지 -->
<div class="mb-3 rounded-4 overflow-hidden position-relative">
  <img :src="info.thumbnail" alt="응급처치 이미지" class="w-100 h-100 object-fit-cover" />

  <!-- 중앙 재생 버튼 -->
<a
  :href="info.url"
  target="_blank"
  class="position-absolute top-50 start-50 translate-middle text-decoration-none play-btn"
>
  <i class="bi bi-play-circle-fill text-danger fs-1 shadow-lg"></i>
</a>
</div>

      <!-- 본문 카드 -->
      <div class="card border-0 shadow-sm rounded-4">
        <div class="card-body p-3 p-md-4">
          <!-- 증상 -->
          <div class="mb-4">
            <h6 class="text-danger text-bold mb-2">증상</h6>
            <ul class="text-black text-light ps-3">
              <li v-for="(s, idx) in info.symptomList" :key="idx">{{ s }}</li>
            </ul>
          </div>

          <!-- 응급처치 -->
          <div class="mb-4">
            <h6 class="text-danger text-bold mb-2">응급처치</h6>
            <ol class="mb-0 ps-3 text-black text-light d-flex flex-column gap-2">
              <li v-for="(m, idx) in info.methodList" :key="idx">{{ m }}</li>
            </ol>
          </div>

          <!-- 원인 -->
          <div class="mb-4">
            <h6 class="text-danger text-bold mb-2">원인</h6>
            <ul class="text-black text-light ps-3">
              <li v-for="(s, idx) in info.reasonList" :key="idx">{{ s }}</li>
            </ul>
          </div>

          <!-- 주의 문구 -->
          <div class="alert alert-warning text-black text-light small mb-0">
            <strong class="text-danger text-bold">주의:</strong> {{ info.warning }}
          </div>
        </div>
      </div>

      <!-- 하단 여백 -->
      <div class="pb-5" style="padding-bottom: calc(2rem + env(safe-area-inset-bottom));"></div>
    </div>
  </main>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { computed } from 'vue'

const router = useRouter()
function goBack() { router.back() }

// 아이콘 카테고리 매핑
const iconCats = [
  { label: '화상', icon: 'bi bi-fire' },
  { label: '감기', icon: 'bi bi-emoji-frown' },
  { label: '복통', icon: 'bi bi-emoji-tear' },
  { label: '열', icon: 'bi bi-thermometer-sun' },
  { label: '구토', icon: 'bi bi-emoji-dizzy' },
  { label: '경련', icon: 'bi bi-activity' }
]

// 데이터
const info = {
  title: '고열',
  url: 'https://www.youtube.com/watch?v=6x7ZCxCu1oM',
  thumbnail: getYoutubeThumbnail('https://youtu.be/6x7ZCxCu1oM?si=1xlFdsQ5qQ56Ld9k'),
  category: '열',
  symptomList: [
    '아이의 체온은 연령에 따라 달라지며 성인에 비해 1-2도 정도 높은 것이 일반적입니다.',
    '0-2세까지는 36.7~38°C까지도 정상으로 볼 수 있고, 3세에서 5세는 37~37.2°C 정도입니다.',
    '측정 부위, 체온계에 따라서 달라질 수도 있습니다.',
    '측정치에 차이가 있는 경우, 아이의 가장 높은 체온을 현재 체온으로 판단하여 처치하는 것이 좋습니다.'
  ],
  methodList: [
    '열과 함께 구토, 두통, 호흡곤란, 복통, 경련 등의 증상이 있다면 즉시 병원으로 가서 의사와 상의하세요.',
    '무조건 해열제를 먹이는 것은 아이의 상태에 좋지 않은 영향을 줄 수 있습니다.',
    '열이 나면서 오한(춥다고 떠는 증상)이 있을 경우, 아이가 너무 추워하고 입술이나 손끝이 푸르게 변하면 얇은 이불을 살짝 덮어주세요.',
    '약 20~30분 후 몸을 떠는 것이 멎고 따뜻해지면 그때 미온수로 몸을 닦아 체온을 낮춰주세요.'
  ],
  reasonList: [
    '바이러스 감염(비특이적 바이러스 감염, 상기도 감염)',
    '중이염(귀를 잡아당기면 통증, 붉어진 고막)',
    '탈수',
    '편도염(주로 바이러스성, 목 아픔, 편도샘의 비대, 숨 쉴 때 고약한 냄새)',
    '독감(열, 기침, 두통, 식욕부진, 관절통)',
    '발진을 동반한 바이러스 질환(수두, 홍역, 풍진, 비특이적 바이러스 발진)',
    '폐렴(기침, 빠른 호흡)',
    '요로감염(빈뇨, 배뇨장애, 허리통증, 구토)',
    '가와사키병(발진, 결막염, 림프절병증, 입술 균열, 열, 피부 벗겨짐)',
    '예방접종 후',
    '중증의 혈액감염',
    '인위적인 원인(뜨거운 음료를 마신 후 체온측정, 과도한 울음 또는 힘든 일, 너무 감싸서 온도가 올라감)'
  ],
  warning: '이 내용은 안내용입니다. 증상이 심하거나 아이의 경우 반드시 의료진의 진료를 받으세요. 생명이 위급한 경우 119에 신고하세요.'
}

// computed로 카테고리 아이콘 선택
const categoryIcon = computed(() => {
  const found = iconCats.find(c => c.label === info.category)
  return found ? found.icon : 'bi bi-question-circle'
})

function getYoutubeThumbnail(url) {
  // youtu.be 단축 URL 또는 youtube.com/watch URL 모두 처리
  const regex = /(?:youtu\.be\/|youtube\.com\/(?:watch\?v=|embed\/))([\w-]{11})/
  const match = url.match(regex)
  return match ? `https://img.youtube.com/vi/${match[1]}/maxresdefault.jpg` : ''
}

</script>

<style scoped>
.object-fit-cover { object-fit: cover; }
.card-body ol li { line-height: 1.5rem; }
.play-btn i {
  transition: transform 0.2s ease-in-out;
  cursor: pointer;
}

.play-btn i:hover {
  transform: scale(1.3);
}
</style>
