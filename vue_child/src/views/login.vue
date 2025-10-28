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
            <button type="submit" class="btn btn-primary w-100" :disabled="!canSubmit || isLoading">
              {{ isLoading ? '로그인 중...' : '로그인' }}
            </button>
          </form>

          <!-- 하단 링크: 아이디 찾기 | 비밀번호 찾기 | 회원가입 -->
          <div class="d-flex align-items-center justify-content-center gap-3 mt-4 small">
            <a href="#" class="link-secondary text-decoration-none text-nowrap text-bold" @click.prevent="goFindId">아이디 찾기</a>
            <span class="text-body-secondary">|</span>
            <a href="#" class="link-secondary text-decoration-none text-nowrap text-bold" @click.prevent="goFindPw">비밀번호 찾기</a>
            <span class="text-body-secondary">|</span>
            <a href="#" class="link-secondary text-decoration-none text-nowrap text-bold" @click.prevent="goSignup">회원가입</a>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { auth, db } from '@/firebase/index'
import { signInWithEmailAndPassword } from 'firebase/auth'
import { collection, query, where, getDocs, doc, getDoc } from 'firebase/firestore'
import { setUser } from '@/utils/userService.js'

const router = useRouter()

const form = reactive({
  username: '',
  password: '',
})

const tried = ref(false)
const isLoading = ref(false)

const canSubmit = computed(() => form.username && form.password)

async function onSubmit() {
  tried.value = true

  // 아이디와 비밀번호 입력하지 않으면 alert창 띄우기
  if (!form.username || !form.password) {
    alert('아이디와 비밀번호를 입력해주세요.')
    return
  }

  try {
    isLoading.value = true

    // 1단계: Firestore에서 username으로 이메일 찾기
    const usersCollection = collection(db, 'users')
    const q = query(usersCollection, where('username', '==', form.username))
    const querySnapshot = await getDocs(q)

    if (querySnapshot.empty) {
      alert('등록되지 않은 아이디입니다.')
      isLoading.value = false
      return
    }

    // 첫 번째 일치하는 사용자의 이메일 가져오기
    const userDocData = querySnapshot.docs[0].data()
    const userEmail = userDocData.email

    if (!userEmail) {
      alert('사용자 정보를 찾을 수 없습니다.')
      isLoading.value = false
      return
    }

    // 2단계: 찾은 이메일로 Firebase Auth 로그인
    const userCredential = await signInWithEmailAndPassword(
      auth,
      userEmail,
      form.password
    )

    const user = userCredential.user

    // 3단계: Firestore에서 사용자 정보 가져오기
    const userDocRef = doc(db, 'users', user.uid)
    const userDoc = await getDoc(userDocRef)

    let userData = {
      uid: user.uid,
      name: user.displayName || '사용자',
      username: form.username,
      email: userEmail,
      phone: ''
    }

    if (userDoc.exists()) {
      const firestoreData = userDoc.data()
      userData = {
        uid: firestoreData.uid || user.uid,
        name: firestoreData.name || user.displayName || '사용자',
        username: firestoreData.username || form.username,
        email: userEmail,
        phone: firestoreData.phone || ''
      }
      console.log('사용자 정보:', firestoreData)
    }

    // 4단계: localStorage에 사용자 정보 저장
    setUser(userData)

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
        alert('등록되지 않은 아이디입니다.')
        break
      case 'auth/wrong-password':
        alert('비밀번호가 일치하지 않습니다.')
        break
      case 'auth/invalid-credential':
        alert('아이디 또는 비밀번호를 다시 확인해주세요.')
        break
      case 'auth/too-many-requests':
        alert('로그인 시도가 너무 많습니다. 잠시 후 다시 시도해주세요.')
        break
      default:
        alert('아이디, 비밀번호를 다시 확인해주세요.')
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