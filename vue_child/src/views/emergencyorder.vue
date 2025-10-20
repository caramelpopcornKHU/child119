<!-- src/views/EmergencyDetail.vue -->
<template>
  <!-- 전폭 컨테이너 -->
  <main class="container-fluid py-3 py-md-4 emergency-detail">

    <!-- 그리드로 중앙 제한 걸지 않고 '전폭' 사용 -->
    <div class="content-wrap">

      <!-- 타이틀 라인 -->
      <div class="d-flex flex-column flex-sm-row align-items-start align-items-sm-center gap-2 mb-3">
        <h2 class="mb-0 lh-sm fw-bold title-responsive">
          일상생활 속 화상, 피가 날 때, 발목 삐었을 때
          <span class="d-inline d-sm-none"><br /></span>
          효과적인 응급처치 방법
        </h2>
      </div>

      <!-- 대표 이미지 -->
      <div class="card border-0 shadow-sm mb-3 full-card">
        <div class="ratio ratio-16x9 ratio-md-21x9">
          <img
            :src="coverImage"
            alt="응급처치 썸네일"
            class="w-100 h-100 object-fit-cover rounded-top"
          />
        </div>
      </div>

      <!-- 본문 -->
      <article class="card border-0 shadow-sm full-card">
        <div class="card-body p-3 p-md-4">
          <section class="mb-4">
            <h3 class="h6 fw-semibold mb-2">화상(뜨거운 물/물체에 의한 화상)</h3>
            <ol class="mb-0 ps-3 d-flex flex-column gap-1 gap-md-2">
              <li>즉시 <strong>시원한 흐르는 물</strong>로 10~20분간 식혀 주세요.</li>
              <li>물집은 터뜨리지 말고, <strong>깨끗한 거즈</strong>로 가볍게 덮어주세요.</li>
              <li>연고·치약 등은 바르지 않습니다.</li>
              <li>얼굴/손/생식기 화상, 넓은 면적, 심한 통증이면 즉시 <strong>119 또는 응급실</strong>.</li>
            </ol>
          </section>

          <hr class="my-3 my-md-4" />

          <section class="mb-4">
            <h3 class="h6 fw-semibold mb-2">출혈(베이거나 찢어진 상처)</h3>
            <ol class="mb-0 ps-3 d-flex flex-column gap-1 gap-md-2">
              <li>깨끗한 거즈나 천으로 <strong>직접 압박</strong>하여 지혈합니다(5~10분).</li>
              <li>상처 안 이물은 억지로 빼지 말고, 깨끗한 물로 주변만 세척합니다.</li>
              <li>지혈이 안 되거나 깊은 상처는 <strong>즉시 병원</strong>에 방문합니다.</li>
            </ol>
          </section>

          <hr class="my-3 my-md-4" />

          <section class="mb-4">
            <h3 class="h6 fw-semibold mb-2">발목 염좌(삐었을 때) – RICE</h3>
            <ul class="mb-0 ps-3 d-flex flex-column gap-1 gap-md-2">
              <li><strong>R</strong>est: 쉬기, 체중 실지 않기</li>
              <li><strong>I</strong>ce: 10~20분 냉찜질, 2~3시간 간격</li>
              <li><strong>C</strong>ompression: 탄력 붕대로 압박</li>
              <li><strong>E</strong>levation: 심장보다 높게</li>
            </ul>
          </section>

          <div class="alert alert-warning small mb-0">
            <strong>주의:</strong> 이 화면은 안내 목적입니다. 증상이 심하거나 소아의 경우
            <strong>반드시 전문가 진료</strong>를 받으세요. 생명 위급(호흡곤란, 의식저하 등) 시 <strong>119</strong>.
          </div>
        </div>
      </article>

      <!-- 하단 네비 공간 확보 -->
      <div class="pb-5 pb-md-4" style="padding-bottom: calc(2rem + env(safe-area-inset-bottom));"></div>
    </div>
  </main>
</template>

<script setup>
import coverImage from '@/assets/emergency.jpg'
</script>

<style scoped>
@import url("https://cdn.jsdelivr.net/gh/orioncactus/pretendard/dist/web/static/pretendard.css");

/* 폰트 */
.emergency-detail {
  font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, 'Segoe UI',
    Roboto, 'Apple SD Gothic Neo', 'Noto Sans KR', 'Malgun Gothic',
    'Helvetica Neue', Arial, sans-serif;
}

/* 🔥 전폭 사용 + 잔존 고정폭 제거 */
.emergency-detail,
.emergency-detail .content-wrap,
.emergency-detail .card,
.emergency-detail .full-card {
  max-width: 100% !important;
  width: 100% !important;
}

/* 페이지 좌우 기본 여백 (원하면 조절) */
.content-wrap {
  padding-left: min(24px, 4vw);
  padding-right: min(24px, 4vw);
}

/* 타이틀 반응형 폰트 */
.title-responsive { font-size: 1.125rem; }
@media (min-width: 576px) { .title-responsive { font-size: 1.25rem; } }
@media (min-width: 768px) { .title-responsive { font-size: 1.5rem; } }
@media (min-width: 1200px){ .title-responsive { font-size: 1.75rem; } }

/* 이미지 커버 */
.object-fit-cover { object-fit: cover; }

/* md 이상 21:9 비율 */
@media (min-width: 768px) {
  .ratio-md-21x9 { position: relative; }
  .ratio-md-21x9::before {
    display: block; padding-top: calc(100% * 9 / 21); content: "";
  }
  .ratio-md-21x9 > * {
    position: absolute; inset: 0; height: 100%; width: 100%;
  }
}
</style>
