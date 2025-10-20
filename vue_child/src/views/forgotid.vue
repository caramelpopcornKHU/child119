<!-- src/FindIdView.vue -->
<template>
  <div class="min-vh-100 d-flex align-items-start justify-content-center bg-body-tertiary py-4">
    <div class="container" style="max-width: 420px;">
      <!-- Brand: 전체 로고 이미지 -->
      <div class="text-start mb-3">
       
      </div>

      <!-- Card -->
      <div class="card shadow-sm">
        <div class="card-body p-4">
          <h2 class="h5 text-center fw-bold text-primary mb-4">아이디 찾기</h2>

          <form class="needs-validation" novalidate @submit.prevent="onSubmit" :class="{'was-validated': tried}">
            <!-- 이름 -->
            <div class="mb-3">
              <label class="form-label">이름</label>
              <input
                v-model.trim="form.name"
                type="text"
                class="form-control"
                placeholder="이름을 입력해주세요"
                required
              />
              <div class="invalid-feedback">이름을 입력해주세요.</div>
            </div>

            <!-- 연락번호 -->
            <div class="mb-3">
              <label class="form-label">연락번호</label>
              <div class="input-group">
                <span class="input-group-text"><i class="bi bi-telephone"></i></span>
                <input
                  v-model.trim="form.phone"
                  type="tel"
                  class="form-control"
                  placeholder="전화번호를 입력해주세요"
                  pattern="^0[0-9]{1,2}-?[0-9]{3,4}-?[0-9]{4}$"
                  required
                />
                <div class="invalid-feedback">올바른 연락처 형식으로 입력해주세요.</div>
              </div>
              <div class="form-text">예) 010-1234-5678</div>
            </div>

            <!-- 인증번호 -->
            <div class="mb-3">
              <label class="form-label">인증번호</label>
              <div class="input-group">
                <span class="input-group-text"><i class="bi bi-shield-lock"></i></span>
                <input
                  v-model.trim="form.code"
                  type="text"
                  class="form-control"
                  placeholder="인증번호를 입력해주세요"
                  minlength="4"
                  required
                />
                <div class="invalid-feedback">인증번호를 입력해주세요.</div>
              </div>
            </div>

            <!-- 버튼 -->
            <div class="d-flex gap-2 mt-3">
              <button type="button" class="btn btn-outline-secondary flex-fill" @click="onBack">이전</button>
              <button type="submit" class="btn btn-primary flex-fill" :disabled="!canSubmit">아이디 찾기</button>
            </div>
          </form>
        </div>
      </div>

    
    </div>
  </div>
</template>

<script setup>
import { reactive, computed, ref } from "vue";

const form = reactive({
  name: "",
  phone: "",
  code: "",
});

const tried = ref(false);

const canSubmit = computed(() =>
  form.name && form.phone && form.code
);

function onSubmit() {
  tried.value = true;
  if (!canSubmit.value) return;

  // TODO: 실제 아이디 찾기 API 연동 (예: /auth/find-id { name, phone, code })
  alert(`아이디 찾기 요청\n이름: ${form.name}\n연락처: ${form.phone}\n인증번호: ${form.code}`);
}

function onBack() {
  history.back();
}

</script>

<!-- 커스텀 CSS 없음. Bootstrap의 기본 색상(Primary)을 그대로 사용합니다. -->
