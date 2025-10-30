<template>
  <main class="bg-body-tertiary min-vh-100">
    <div class="container-fluid py-4 px-3">
      <!-- 상단 헤더 -->
      <div class="d-flex align-items-center mb-3">
        <button class="btn btn-link p-0 me-2 text-dark" @click="goBack">
          <i class="bi bi-arrow-left fs-5"></i>
        </button>
        <img :src="getKidImage(form.gender)" alt="avatar" class="rounded-circle me-2"
          style="width:36px;height:36px;object-fit:cover;" />
        <h5 class="mb-0 text-bold">내 아이 추가</h5>
      </div>

      <!-- 본문 래퍼 -->
      <div class="card border-0 shadow-sm">
        <div class="card-body">
          <form class="needs-validation" novalidate @submit.prevent="onSubmit" :class="{ 'was-validated': tried }">
            <!-- 이름 -->
            <div class="mb-3">
              <label class="form-label text-medium">이름</label>
              <input v-model.trim="form.name" type="text" class="form-control text-light" placeholder="예: 김서아"
                required />
              <div class="invalid-feedback text-medium">이름을 입력해주세요.</div>
            </div>

            <!-- 출생년도 -->
            <div class="mb-3">
              <label class="form-label text-medium">출생년도</label>
              <input v-model.number="form.birthYear" type="number" class="form-control text-light" placeholder="예: 2022"
                min="2000" max="2099" required />
              <div class="invalid-feedback text-medium">출생년도를 올바르게 입력해주세요.</div>
            </div>

            <!-- 혈액형 -->
            <div class="mb-3">
              <label class="form-label text-medium">혈액형</label>
              <input v-model.trim="form.blood" type="text" class="form-control text-light" placeholder="예: O형"
                required />
              <div class="invalid-feedback text-medium">혈액형을 입력해주세요.</div>
            </div>

            <!-- 복용 중인 약 -->
            <div class="mb-3">
              <label class="form-label text-medium">복용 중인 약</label>
              <textarea v-model.trim="form.meds" class="form-control text-light" rows="3"
                placeholder="예: OOO 알러지약 아침마다 복용"></textarea>
            </div>

            <!-- 키/몸무게 -->
            <div class="mb-3">
              <label class="form-label text-medium">키(cm)</label>
              <input v-model.number="form.height" type="number" class="form-control text-light" placeholder="예: 95"
                min="0" />
            </div>
            <div class="mb-3">
              <label class="form-label text-medium">몸무게(kg)</label>
              <input v-model.number="form.weight" type="number" class="form-control text-light" placeholder="예: 14"
                min="0" />
            </div>

            <!-- 기저질환 및 병력 -->
            <div class="mb-3">
              <label class="form-label text-medium">기저질환 및 병력</label>
              <textarea v-model.trim="form.history" class="form-control text-light" rows="3"
                placeholder="예: 12개월 때 열성 경련 경험 있음"></textarea>
            </div>

            <!-- 알레르기 -->
            <div class="mb-3">
              <label class="form-label text-medium">알레르기</label>
              <textarea v-model.trim="form.allergy" class="form-control text-light" rows="3"
                placeholder="예: 딸기, 감귤류 알레르기"></textarea>
            </div>

            <!-- 성별 -->
            <div class="mb-3">
              <label class="form-label d-block text-medium">성별</label>
              <div class="form-check form-check-inline">
                <input class="form-check-input" type="radio" id="genderM" value="M" v-model="form.gender" required />
                <label class="form-check-label text-medium" for="genderM">남</label>
              </div>
              <div class="form-check form-check-inline">
                <input class="form-check-input" type="radio" id="genderF" value="F" v-model="form.gender" />
                <label class="form-check-label text-medium" for="genderF">여</label>
              </div>
              <div class="invalid-feedback d-block text-medium" v-if="tried && !form.gender">성별을 선택해주세요.</div>
            </div>

            <!-- 저장 버튼 -->
            <button type="submit" class="btn btn-primary w-100 text-medium mb-5" :disabled="isLoading">
              {{ isLoading ? '저장 중...' : '저장하기' }}
            </button>
          </form>
        </div>
      </div>
    </div>
  </main>
</template>

<script setup>
import { reactive, ref, computed } from "vue"
import { useRouter } from 'vue-router'
import { addKid } from '@/firebase/kid.js'
import kidboyImg from '@/assets/kidboy.png'
import kidgirlImg from '@/assets/kidgirl.png'
import kidDefaultImg from '@/assets/kid1.jpg'

const router = useRouter()

const form = reactive({
  name: "",
  birthYear: null,
  blood: "",
  meds: "",
  height: null,
  weight: null,
  allergy: "",
  gender: "",
  history: ""
})

const tried = ref(false)
const isLoading = ref(false)


const getKidImage = (gender) => {
  if (gender === 'M') return kidboyImg
  if (gender === 'F') return kidgirlImg
  return kidDefaultImg
}
/* 본문 스크롤 영역 높이 계산 (헤더/푸터 고정 고려) */
const scrollBoxStyle = computed(() => ({
  height: "calc(100vh - 64px - 84px)", // 헤더 64px, 푸터 84px 기준
}))

const canSubmit = computed(() =>
  form.name &&
  form.birthYear &&
  form.blood &&
  !!form.gender
)

async function onSubmit() {
  tried.value = true
  if (!canSubmit.value) {
    alert('모든 필수 항목을 입력해주세요.')
    return
  }

  isLoading.value = true
  try {
    await addKid(form)
    alert('아이 정보가 추가되었습니다!')
    router.push('/MyKids')
  } catch (error) {
    console.error('아이 추가 실패:', error)
    if (error.message === '로그인이 필요합니다.') {
      alert('로그인이 필요합니다.')
      router.push('/login')
    } else {
      alert('아이 정보 추가에 실패했습니다.')
    }
  } finally {
    isLoading.value = false
  }
}

function goBack() {
  router.back()
}
</script>
