<!-- src/views/kidinformationedit.vue -->
<template>
  <main class="bg-body-tertiary min-vh-100">
    <div class="container-fluid py-4 px-3">
      <!-- 상단 헤더 -->
      <div class="d-flex align-items-center mb-3">
        <!-- 아이콘은 원하는 이미지로 교체 -->
        <img src="@/assets/kid_logo.png" alt="아이 아이콘" width="40" height="40" class="me-2" />
        <h5 class="fw-bold mb-0">아이 정보 수정</h5>
      </div>

      <!-- 카드 래핑 (회색 배경 위 흰 카드 느낌) -->
      <div class="card border-0 shadow-sm">
        <div class="card-body">
          <form @submit.prevent="save">
            <!-- 이름 -->
            <div class="mb-3">
              <label class="form-label">이름</label>
              <input v-model.trim="form.name" type="text" class="form-control" placeholder="김서아" />
            </div>

            <!-- 출생년도 -->
            <div class="mb-3">
              <label class="form-label">출생년도</label>
              <input v-model.number="form.birthYear" type="number" class="form-control" placeholder="2022" />
            </div>

            <!-- 혈액형 -->
            <div class="mb-3">
              <label class="form-label">혈액형</label>
              <input v-model.trim="form.blood" type="text" class="form-control" placeholder="O형" />
            </div>

            <!-- 복용 중인 약 (멀티라인) -->
            <div class="mb-3">
              <label class="form-label">복용 중인 약</label>
              <textarea v-model.trim="form.meds" class="form-control" rows="2" placeholder="OOO 알러지약 아침마다 복용"></textarea>
            </div>

            <!-- 키(cm) -->
            <div class="mb-3">
              <label class="form-label">키(cm)</label>
              <input v-model.number="form.height" type="number" class="form-control" placeholder="95" />
            </div>

            <!-- 몸무게(kg) -->
            <div class="mb-3">
              <label class="form-label">몸무게(kg)</label>
              <input v-model.number="form.weight" type="number" class="form-control" placeholder="14" />
            </div>

            <!-- 기저질환 및 병력 -->
            <div class="mb-3">
              <label class="form-label">기저질환 및 병력</label>
              <textarea v-model.trim="form.history" class="form-control" rows="2" placeholder="12개월 때 열성 경련 경험 있음"></textarea>
            </div>

            <!-- 알레르기 -->
            <div class="mb-3">
              <label class="form-label">알레르기</label>
              <input v-model.trim="form.allergy" type="text" class="form-control" placeholder="땅콩, 감귤류 알레르기" />
            </div>

            <!-- 성별 -->
            <div class="mb-4">
              <label class="form-label d-block">성별</label>
              <div class="form-check form-check-inline">
                <input class="form-check-input" type="radio" id="genderM" value="M" v-model="form.gender" />
                <label class="form-check-label" for="genderM">남</label>
              </div>
              <div class="form-check form-check-inline">
                <input class="form-check-input" type="radio" id="genderF" value="F" v-model="form.gender" />
                <label class="form-check-label" for="genderF">여</label>
              </div>
            </div>

            <!-- 로딩 중 표시 -->
            <div v-if="loading" class="text-center py-3">
              <div class="spinner-border text-primary" role="status">
                <span class="visually-hidden">로딩 중...</span>
              </div>
            </div>

            <!-- 저장하기 버튼 (파란색, 가득) -->
            <button v-else class="btn btn-primary w-100 mb-2" type="submit" :disabled="saving">
              {{ saving ? '저장 중...' : '저장하기' }}
            </button>

            <!-- 삭제 버튼 (빨간색) -->
            <button v-if="!loading" class="btn btn-outline-danger w-100" type="button" @click="deleteKid">
              <i class="bi bi-trash me-1"></i> 삭제하기
            </button>
          </form>
        </div>
      </div>

      <!-- 뒤로가기 버튼 -->
      <div class="mt-3">
        <button class="btn btn-outline-secondary w-100" @click="goBack">뒤로가기</button>
      </div>
    </div>
  </main>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getKid, updateKid, removeKid } from '@/firebase/kid.js'

const route = useRoute()
const router = useRouter()
const kidId = route.params.id

const form = reactive({
  name: '',
  birthYear: '',
  blood: '',
  meds: '',
  height: '',
  weight: '',
  history: '',
  allergy: '',
  gender: '',
})

const loading = ref(true)
const saving = ref(false)

onMounted(async () => {
  try {
    const kidData = await getKid(kidId)
    form.name = kidData.kid || ''  // kid 필드 사용
    form.birthYear = kidData.birthYear || ''
    form.blood = kidData.bloodType || ''  // bloodType 필드 사용
    form.meds = kidData.medication || ''  // medication 필드 사용
    form.height = kidData.heightCm || ''  // heightCm 필드 사용
    form.weight = kidData.weightKg || ''  // weightKg 필드 사용
    form.history = kidData.medicalHistory || ''  // medicalHistory 필드 사용
    form.allergy = kidData.allergy || ''
    form.gender = kidData.gender || ''
    loading.value = false
  } catch (error) {
    console.error('아이 정보 불러오기 실패:', error)
    alert('아이 정보를 불러올 수 없습니다.')
    router.push('/MyKids')
  }
})

async function save() {
  if (!form.name || !form.birthYear || !form.blood || !form.gender) {
    alert('필수 항목을 입력해주세요.')
    return
  }

  saving.value = true
  try {
    await updateKid(kidId, form)
    alert('아이 정보가 수정되었습니다!')
    router.push('/MyKids')
  } catch (error) {
    console.error('아이 정보 수정 실패:', error)
    alert('정보 수정에 실패했습니다.')
  } finally {
    saving.value = false
  }
}

async function deleteKid() {
  if (!confirm('정말로 이 아이의 정보를 삭제하시겠습니까?')) {
    return
  }

  try {
    await removeKid(kidId)
    alert('아이 정보가 삭제되었습니다.')
    router.push('/MyKids')
  } catch (error) {
    console.error('아이 정보 삭제 실패:', error)
    alert('정보 삭제에 실패했습니다.')
  }
}

function goBack() {
  router.back()
}
</script>