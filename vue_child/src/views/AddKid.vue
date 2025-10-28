<!-- src/AddKidView.vue -->
<template>
  <div class="bg-body-tertiary">
   

    <!-- 본문 래퍼 -->
    <main class="mx-auto px-3" style="max-width:100vw; padding-top:30px; ">
      <!-- 스크롤 영역 -->
      <div class="overflow-auto" :style="scrollBoxStyle" style="padding-bottom:84px;">
        <!-- 타이틀 카드 -->
        <div class="card shadow-sm mb-3">
          <div class="card-body p-3 d-flex align-items-center gap-2 text-bold">
            <img src="@/assets/kid1.jpg" alt="avatar" class="rounded-circle" style="width:36px;height:36px;object-fit:cover;" />
            <div class="fw-bold">아이 정보</div>
          </div>
        </div>

        <!-- 입력 폼 -->
        <form class="needs-validation" novalidate @submit.prevent="onSubmit" :class="{'was-validated': tried}">
          <div class="card shadow-sm">
            <div class="card-body p-3">

              <!-- 이름 -->
              <div class="mb-3 text-bold">
                <label class="form-label">이름</label>
                <input v-model.trim="form.name" type="text" class="form-control" placeholder="예: 김서아" required />
                <div class="invalid-feedback">이름을 입력해주세요.</div>
              </div>

              <!-- 출생년도 -->
              <div class="mb-3 text-bold">
                <label class="form-label">출생년도</label>
                <input v-model.number="form.birthYear" type="number" class="form-control" placeholder="예: 2022" min="2000" max="2099" required />
                <div class="invalid-feedback">출생년도를 올바르게 입력해주세요.</div>
              </div>

              <!-- 혈액형 -->
              <div class="mb-3 text-bold">
                <label class="form-label">혈액형</label>
                <input v-model.trim="form.blood" type="text" class="form-control" placeholder="예: O형" required />
                <div class="invalid-feedback">혈액형을 입력해주세요.</div>
              </div>

              <!-- 복용 중인 약 -->
              <div class="mb-3 text-bold">
                <label class="form-label">복용 중인 약</label>
                <textarea v-model.trim="form.meds" class="form-control" rows="3" placeholder="예: OOO 알러지약 아침마다 복용"></textarea>
              </div>

              <!-- 키/몸무게 -->
              <div class="mb-3 text-bold">
                <label class="form-label">키(cm)</label>
                <input v-model.number="form.height" type="number" class="form-control" placeholder="예: 95" min="0" />
              </div>
              <div class="mb-3 text-bold">
                <label class="form-label">몸무게(kg)</label>
                <input v-model.number="form.weight" type="number" class="form-control" placeholder="예: 14" min="0" />
              </div>

              <!-- 기저질환 및 병력 -->
              <div class="mb-3 text-bold">
                <label class="form-label">기저질환 및 병력</label>
                <textarea v-model.trim="form.history" class="form-control" rows="3" placeholder="예: 12개월 때 열성 경련 경험 있음"></textarea>
              </div>

              <!-- 알레르기 -->
              <div class="mb-3 text-bold">
                <label class="form-label">알레르기</label>
                <textarea v-model.trim="form.allergy" class="form-control" rows="3" placeholder="예: 딸기, 감귤류 알레르기"></textarea>
              </div>

              <!-- 성별 -->
              <div class="mb-3 text-bold">
                <label class="form-label d-block">성별</label>
                <div class="form-check form-check-inline">
                  <input class="form-check-input" type="radio" id="genderM" value="M" v-model="form.gender" required />
                  <label class="form-check-label" for="genderM">남</label>
                </div>
                <div class="form-check form-check-inline">
                  <input class="form-check-input" type="radio" id="genderF" value="F" v-model="form.gender" />
                  <label class="form-check-label" for="genderF">여</label>
                </div>
                <div class="invalid-feedback d-block" v-if="tried && !form.gender">성별을 선택해주세요.</div>
              </div>

              <!-- 저장 버튼 -->

              <div class="d-flex gap-2 mt-3">
              <button type="button" class="btn btn-outline-secondary w-50 text-bold" @click="goBack">
                이전으로
              </button>
              <button type="submit" class="btn btn-primary w-50 text-bold" :disabled="isLoading">
                  {{ isLoading ? '저장 중...' : '저장하기' }}
              </button>
              </div>
              
            </div>
          </div>
        </form>
      </div>
    </main>

    <!-- 고정 푸터 -->
   
  </div>
</template>

<script setup>
import { reactive, ref, computed } from "vue"
import { useRouter } from 'vue-router'
import { addKid } from '@/firebase/kid.js'

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
