<!-- src/views/forgotpassword.vue -->
<template>
  <div class="min-vh-100 d-flex align-items-start justify-content-center bg-body-tertiary py-4">
    <div class="container" style="max-width: 420px;">
      <!-- Brand: 전체 이미지를 사용 -->
      <div class="text-start mb-3">
      
      </div>

      <!-- Card -->
      <div class="card shadow-sm">
        <div class="card-body p-4">
          <h2 class="h5 text-center fw-bold text-primary mb-4">비밀번호 찾기</h2>

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

            <!-- 연락처 -->
            <div class="mb-3">
              <label class="form-label">연락처</label>
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

            <!-- 아이디 (이메일) -->
            <div class="mb-3">
              <label class="form-label">아이디 (이메일)</label>
              <input
                v-model.trim="form.username"
                type="text"
                class="form-control"
                placeholder="아이디(이메일)를 입력해주세요"
                required
              />
              <div class="invalid-feedback">아이디를 입력해주세요.</div>
            </div>

            <!-- 버튼 -->
            <div class="d-flex gap-2 mt-3">
              <button type="button" class="btn btn-outline-secondary flex-fill" @click="onBack">이전</button>
              <button type="submit" class="btn btn-primary flex-fill" :disabled="!canSubmit || isLoading">
                <span v-if="isLoading" class="spinner-border spinner-border-sm" aria-hidden="true"></span>
                <span v-else>비밀번호 재설정</span>
              </button>
            </div>
          </form>
        </div>
      </div>

      <!-- 푸터 -->
      <div class="text-center mt-3 small text-body-secondary">© 2025 아이119</div>
    </div>
  </div>
</template>

<script setup>
import { reactive, computed, ref } from "vue"
import { useRouter } from "vue-router"
import { db, auth } from "@/firebase"
import { collection, query, where, getDocs } from "firebase/firestore"
import { sendPasswordResetEmail } from "firebase/auth"

const router = useRouter()

const form = reactive({
  name: "",
  phone: "",
  username: "",
})

const tried = ref(false)
const isLoading = ref(false)

const canSubmit = computed(() =>
  form.name && form.phone && form.username
)

async function onSubmit() {
  tried.value = true
  if (!canSubmit.value) {
    alert('모든 항목을 입력해주세요.')
    return
  }

  isLoading.value = true

  try {
    // Firebase Firestore에서 이름, 연락처, 아이디(username)로 사용자 검색
    const userCollection = collection(db, 'user')
    
    // 연락처 정규화 (하이픈 제거)
    const normalizedPhone = form.phone.replace(/-/g, '')
    
    // username으로 쿼리
    const q = query(
      userCollection,
      where('username', '==', form.username)
    )

    const querySnapshot = await getDocs(q)
    
    // 일치하는 사용자 찾기
    let foundUser = null
    
    querySnapshot.forEach((doc) => {
      const userData = doc.data()
      // 이름과 연락처가 모두 일치하는지 확인
      const dbPhone = userData.phone.replace(/-/g, '')
      if (userData.name === form.name && dbPhone === normalizedPhone) {
        foundUser = userData
      }
    })

    if (foundUser) {
      // 사용자를 찾았을 때
      // Firebase Auth로 비밀번호 재설정 이메일 전송
      // foundUser의 이메일이 Firebase Auth 이메일과 동일하다고 가정
      const userEmail = foundUser.username // username이 이메일인 경우
      
      try {
        await sendPasswordResetEmail(auth, userEmail)
        
        // 성공 메시지와 함께 결과 페이지로 이동
        router.push({
          path: '/ForgotPasswordResult',
          query: { 
            email: userEmail,
            success: 'true'
          }
        })
      } catch (emailError) {
        console.error('이메일 전송 오류:', emailError)
        
        if (emailError.code === 'auth/user-not-found') {
          alert('해당 이메일로 등록된 계정이 없습니다.')
        } else if (emailError.code === 'auth/invalid-email') {
          alert('유효하지 않은 이메일 주소입니다.')
        } else {
          alert('비밀번호 재설정 이메일 전송에 실패했습니다.\n다시 시도해주세요.')
        }
      }
    } else {
      alert('일치하는 회원 정보를 찾을 수 없습니다.\n입력한 정보를 다시 확인해주세요.')
    }

  } catch (error) {
    console.error('비밀번호 찾기 중 오류 발생:', error)
    alert('오류가 발생했습니다. 다시 시도해주세요.')
  } finally {
    isLoading.value = false
  }
}

function onBack() {
  router.back()
}
</script>

<!-- 커스텀 CSS 없음. 색상은 Bootstrap Primary를 그대로 사용합니다. -->