<template>
  <div class="min-vh-100 d-flex align-items-start align-items-lg-center justify-content-center bg-body-tertiary py-4">
    <div class="container" style="max-width: 420px;">
      <!-- Brand -->
<div class="text-center mb-3">
 
</div>


      <!-- Card -->
      <div class="card shadow-sm">
        <div class="card-body p-4">
          <h2 class="h5 text-center fw-bold mb-4">회원가입</h2>

          <form class="needs-validation" novalidate @submit.prevent="onSubmit" :class="{'was-validated': tried}">
            <!-- 이름 -->
            <div class="mb-3">
              <label class="form-label">이름</label>
              <input
                v-model.trim="form.name"
                type="text"
                class="form-control"
                placeholder="이름을 입력하세요"
                required
              />
              <div class="invalid-feedback">이름을 입력해주세요.</div>
            </div>

            <!-- 연락처 -->
            <div class="mb-3">
              <label class="form-label">연락처</label>
              <div class="input-group">
                <span class="input-group-text"><i class="bi bi-telephone"></i></span>
                <input
                  v-model.trim="form.phone"
                  type="tel"
                  class="form-control"
                  placeholder="전화번호를 입력하세요"
                  pattern="^0[0-9]{1,2}-?[0-9]{3,4}-?[0-9]{4}$"
                  required
                />
                <div class="invalid-feedback">올바른 연락처 형식으로 입력해주세요.</div>
              </div>
              <div class="form-text">예) 010-1234-5678</div>
            </div>

            <!-- 아이디 -->
            <div class="mb-3">
              <label class="form-label">아이디</label>
              <input
                v-model.trim="form.username"
                type="text"
                class="form-control"
                placeholder="아이디를 입력해주세요"
                minlength="4"
                required
              />
              <div class="invalid-feedback">아이디(4자 이상)를 입력해주세요.</div>
            </div>

            <!-- 비밀번호 -->
            <div class="mb-3">
              <label class="form-label">비밀번호</label>
              <input
                v-model="form.password"
                type="password"
                class="form-control"
                placeholder="비밀번호를 입력해주세요"
                minlength="8"
                required
              />
              <div class="form-text">영문/숫자/특수문자 조합 8자 이상 권장</div>
              <div class="invalid-feedback">비밀번호(8자 이상)를 입력해주세요.</div>
            </div>

            <!-- 비밀번호 확인 -->
            <div class="mb-3">
              <label class="form-label">비밀번호 확인</label>
              <input
                v-model="form.password2"
                type="password"
                class="form-control"
                placeholder="비밀번호를 다시 입력해주세요"
                :class="{'is-invalid': tried && !passwordsMatch}"
                required
              />
              <div class="invalid-feedback">비밀번호가 일치하지 않습니다.</div>
            </div>

            <!-- 체크박스들 -->
            <div class="form-check mb-2">
              <input
                class="form-check-input"
                type="checkbox"
                id="terms"
                v-model="form.agreeTerms"
                required
              />
              <label class="form-check-label" for="terms">
                이용약관 및 개인정보처리방침에 동의합니다.
              </label>
              <div class="invalid-feedback">약관 동의가 필요합니다.</div>
            </div>

            <div class="form-check mb-3">
              <input
                class="form-check-input"
                type="checkbox"
                id="marketing"
                v-model="form.agreeMarketing"
              />
              <label class="form-check-label" for="marketing">
                마케팅 정보 수신에 동의합니다. (선택)
              </label>
            </div>

            <!-- 버튼들 -->
            <div class="d-flex gap-2 mt-4">
              <button type="button" class="btn btn-outline-secondary flex-fill" @click="onBack">
                이전
              </button>
              <button type="submit" class="btn btn-primary flex-fill" :disabled="!canSubmit">
                가입완료
              </button>
            </div>
          </form>

          <!-- 하단 링크 -->
          <div class="text-center mt-4 small text-body-secondary">
            이미 계정이 있으신가요?
            <a href="#" class="link-primary text-decoration-none" @click.prevent="goLogin">로그인</a>
          </div>
        </div>
      </div>

      <!-- 푸터(선택) -->
      <div class="text-center mt-3 small text-body-secondary">
        © 2025 아이119
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, computed, ref } from "vue";

const form = reactive({
  name: "",
  phone: "",
  username: "",
  password: "",
  password2: "",
  agreeTerms: false,
  agreeMarketing: false,
});

const tried = ref(false);
const passwordsMatch = computed(() => form.password && form.password === form.password2);

const canSubmit = computed(() =>
  form.name &&
  form.phone &&
  form.username &&
  form.password &&
  form.password2 &&
  passwordsMatch.value &&
  form.agreeTerms
);

function onSubmit() {
  tried.value = true;

  // 브라우저 기본 검증 + 추가 검증
  if (!canSubmit.value) return;

  // 실제 가입 로직 자리 (API 연동 등)
  // 예시로 값 출력
  alert("가입이 완료되었습니다!");
}

function onBack() {
  // 이전 단계 이동 로직(라우터 사용 시 replace/back 등)
  history.back();
}

function goLogin() {
  // 로그인 페이지로 이동 (라우터 사용 시 교체)
  alert("로그인 페이지로 이동");
}
</script>

<!--
  색상을 "그대로" 맞춰야 하면 --bs-primary 만 조정하세요.
  (Bootstrap 변수 1줄만 사용하는 최소 CSS)
-->
<style>
:root {
  /* 필요 시 앱의 파란색으로 바꾸세요 (예: #1677ff, #1e6bff 등) */
  /* --bs-primary: #0d6efd;  기본값 */
}
</style>
