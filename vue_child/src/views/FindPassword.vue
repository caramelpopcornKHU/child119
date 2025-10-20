<!-- src/ForgotPasswordResultView.vue -->
<template>
  <div class="min-vh-100 d-flex align-items-start justify-content-center bg-body-tertiary py-4">
    <div class="container" style="max-width: 420px;">
      <!-- Brand -->
      <div class="text-start mb-4">
        <img
          src="@/assets/logo.png"
          alt="아이119 로고"
          class="img-fluid"
          style="max-width: 160px;"
        />
      </div>

      <!-- 결과 카드 -->
      <div class="card shadow-sm mb-4">
        <div class="card-body p-4">
          <h2 class="h5 text-center fw-bold text-primary mb-3">비밀번호 찾기</h2>

          <p class="mb-0 text-center">
            비밀번호는 <span class="fw-bold">{{ displayPw }}</span>입니다.
          </p>
        </div>
      </div>

      <!-- 버튼 -->
      <button type="button" class="btn btn-primary w-100" @click="goLogin">
        로그인 창으로
      </button>

      <!-- 푸터 -->
      <div class="text-center mt-3 small text-body-secondary">© 2025 아이119</div>
    </div>
  </div>
</template>

<script setup>
import { computed } from "vue";

/**
 * 우선순위
 * 1) props.password
 * 2) URL 쿼리 ?pw=
 * 3) 기본 예시 "111"
 */
const props = defineProps({
  password: { type: String, default: "" },
  /** 마스킹 여부 (원하면 **** 형태로 표시) */
  mask: { type: Boolean, default: false },
});

let queryPw = "";
try {
  const url = new URL(window.location.href);
  queryPw = url.searchParams.get("pw") || "";
} catch {}

const rawPw = computed(() => props.password || queryPw || "111");
const displayPw = computed(() => {
  if (!props.mask) return rawPw.value;
  // 간단 마스킹: 앞 1글자 + 나머지 ****
  const s = rawPw.value.toString();
  return s.length > 1 ? s[0] + "*".repeat(s.length - 1) : "*";
});

function goLogin() {
  // TODO: 라우터 사용 시 router.push('/login')
  alert("로그인 페이지로 이동");
}
</script>

<!-- 커스텀 CSS 없음 -->
