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
            <!-- 이메일 -->
            <div class="mb-3">
              <label class="form-label">이메일</label>
              <input
                v-model.trim="form.email"
                type="email"
                class="form-control"
                placeholder="이메일을 입력해주세요"
                required
              />
              <div class="invalid-feedback">이메일을 입력해주세요.</div>
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
            <button type="submit" class="btn btn-primary w-100" :disabled="!canSubmit || isLoading">
              {{ isLoading ? '로그인 중...' : '로그인' }}
            </button>
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
import { reactive, ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { auth, db } from '@/firebase'
import { signInWithEmailAndPassword } from 'firebase/auth'
import { doc, getDoc } from 'firebase/firestore'

const router = useRouter()

const form = reactive({
  email: '',
  password: '',
})

const tried = ref(false)
const isLoading = ref(false)

const canSubmit = computed(() => form.email && form.password)

async function onSubmit() {
  tried.value = true
  
  // 아이디와 비밀번호 입력하지 않으면 alert창 띄우기
  if (!form.email || !form.password) {
    alert('이메일과 비밀번호를 입력해주세요.')
    return
  }

  try {
    // 로그인하기
    isLoading.value = true
    const userCredential = await signInWithEmailAndPassword(
      auth,
      form.email,
      form.password
    )
    
    const user = userCredential.user

    // Firestore에서 사용자 정보 가져오기 (선택사항)
    const userDocRef = doc(db, 'user', user.uid)
    const userDoc = await getDoc(userDocRef)
    
    if (userDoc.exists()) {
      console.log('사용자 정보:', userDoc.data())
    }

    // 로그인 성공
    alert('로그인에 성공했습니다!')
    
    // 메인 페이지 또는 홈으로 이동
    router.push('/')

  } catch (error) {
    console.error('로그인 오류:', error)
    
    // Firebase Auth 에러 처리
    switch (error.code) {
      case 'auth/invalid-email':
        alert('올바른 이메일 형식을 입력해주세요.')
        break
      case 'auth/user-not-found':
        alert('등록되지 않은 이메일입니다.')
        break
      case 'auth/wrong-password':
        alert('비밀번호가 일치하지 않습니다.')
        break
      case 'auth/invalid-credential':
        alert('이메일 또는 비밀번호를 다시 확인해주세요.')
        break
      case 'auth/too-many-requests':
        alert('로그인 시도가 너무 많습니다. 잠시 후 다시 시도해주세요.')
        break
      default:
        alert('이메일, 비밀번호를 다시 확인해주세요.')
        break
    }
  } finally {
    isLoading.value = false
  }
}

function goFindId() {
  router.push('/forgotid')
}

function goFindPw() {
  router.push('/forgotpassword')
}

function goSignup() {
  router.push('/sginup')
}
</script>