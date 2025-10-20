<template>
  <div class="min-vh-100 d-flex align-items-start justify-content-center bg-body-tertiary py-4">
    <!-- 고정 폭 컨테이너 (반응형 X) -->
    <div class="container" style="max-width: 420px;">
      <!-- Brand: 전체 이미지를 쓰는 경우 -->
      <div class="text-center mb-3">
        <!-- 전체 로고(아이콘+텍스트)가 하나의 이미지인 경우 -->
        <!-- src/assets/logo.png 파일 준비되어 있으면 아래 경로 그대로 사용 -->
       
      </div>
<div class="card shadow-sm">
        <div class="card-body p-4">
          <h2 class="h5 text-center fw-bold mb-4" style="color: var(--bs-primary);">로그인</h2>

          <form class="needs-validation" novalidate @submit.prevent="onSubmit" :class="{'was-validated': tried}">
            <!-- 아이디 -->
            <div class="mb-3">
              <label class="form-label">아이디</label>
              <input
                v-model.trim="form.username"
                type="text"
                class="form-control"
                placeholder="아이디를 입력해주세요"
                required
              />
              <div class="invalid-feedback">아이디를 입력해주세요.</div>
            </div>

            <!-- 비밀번호 -->
            <div class="mb-3">
              <label class="form-label">비밀번호</label>
              <div class="input-group">
                <span class="input-group-text"><i class="bi bi-lock"></i></span>
                <input
                  v-model="form.password"
                  type="password"
                  class="form-control"
                  placeholder="비밀번호를 입력해주세요"
                  required
                />
                <div class="invalid-feedback">비밀번호를 입력해주세요.</div>
              </div>
            </div>

            <!-- 로그인 버튼 -->
            <button type="submit" class="btn btn-primary w-100">로그인</button>
          </form>

          <!-- 하단 링크: 아이디 찾기 | 비밀번호 찾기 | 회원가입 -->
          <div class="d-flex align-items-center justify-content-center gap-3 mt-4 small">
            <a href="#" class="link-secondary text-decoration-none" @click.prevent="goFindId">아이디 찾기</a>
            <span class="text-body-secondary">|</span>
            <a href="#" class="link-secondary text-decoration-none" @click.prevent="goFindPw">비밀번호 찾기</a>
            <span class="text-body-secondary">|</span>
            <a href="#" class="link-secondary text-decoration-none" @click.prevent="goSignup">회원가입</a>
          </div>
        </div>
      </div>

      
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, computed } from "vue";
import {useRouter} from "vue-router";
const router = useRouter();

const form = reactive({
  username: "",
  password: "",
});

const tried = ref(false);
const canSubmit = computed(() => form.username && form.password);

function onSubmit() {
  tried.value = true;
  if (!canSubmit.value) return;
  // TODO: 실제 로그인 API 연동
  alert("로그인 시도!");
}

function goFindId() {
  // TODO: 라우팅 처리
  alert("아이디 찾기 페이지로 이동");
  router.push("/forgotid")
  
}
function goFindPw() {
  // TODO: 라우팅 처리
  alert("비밀번호 찾기 페이지로 이동");
  router.push("/forgotpassword")
}
function goSignup() {
  // TODO: 라우팅 처리
  alert("회원가입 페이지로 이동");
  router.push("/sginup")
}
</script>
