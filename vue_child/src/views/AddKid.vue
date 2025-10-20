<!-- src/AddKidView.vue -->
<template>
  <div class="bg-body-tertiary">
   

    <!-- 본문 래퍼 -->
    <main class="mx-auto px-3" style="max-width:420px; padding-top:64px; padding-bottom:84px;">
      <!-- 스크롤 영역 -->
      <div class="overflow-auto" :style="scrollBoxStyle">
        <!-- 타이틀 카드 -->
        <div class="card shadow-sm mb-3">
          <div class="card-body p-3 d-flex align-items-center gap-2">
            <img src="@/assets/kid1.jpg" alt="avatar" class="rounded-circle" style="width:36px;height:36px;object-fit:cover;" />
            <div class="fw-bold">아이 정보</div>
          </div>
        </div>

        <!-- 입력 폼 -->
        <form class="needs-validation" novalidate @submit.prevent="onSubmit" :class="{'was-validated': tried}">
          <div class="card shadow-sm">
            <div class="card-body p-3">

              <!-- 이름 -->
              <div class="mb-3">
                <label class="form-label">이름</label>
                <input v-model.trim="form.name" type="text" class="form-control" placeholder="예: 김서아" required />
                <div class="invalid-feedback">이름을 입력해주세요.</div>
              </div>

              <!-- 출생년도 -->
              <div class="mb-3">
                <label class="form-label">출생년도</label>
                <input v-model.number="form.birthYear" type="number" class="form-control" placeholder="예: 2022" min="2000" max="2099" required />
                <div class="invalid-feedback">출생년도를 올바르게 입력해주세요.</div>
              </div>

              <!-- 혈액형 -->
              <div class="mb-3">
                <label class="form-label">혈액형</label>
                <input v-model.trim="form.blood" type="text" class="form-control" placeholder="예: O형" required />
                <div class="invalid-feedback">혈액형을 입력해주세요.</div>
              </div>

              <!-- 복용 중인 약 -->
              <div class="mb-3">
                <label class="form-label">복용 중인 약</label>
                <textarea v-model.trim="form.meds" class="form-control" rows="3" placeholder="예: OOO 알러지약 아침마다 복용"></textarea>
              </div>

              <!-- 키/몸무게 -->
              <div class="mb-3">
                <label class="form-label">키(cm)</label>
                <input v-model.number="form.height" type="number" class="form-control" placeholder="예: 95" min="0" />
              </div>
              <div class="mb-3">
                <label class="form-label">몸무게(kg)</label>
                <input v-model.number="form.weight" type="number" class="form-control" placeholder="예: 14" min="0" />
              </div>

              <!-- 알레르기 -->
              <div class="mb-3">
                <label class="form-label">알레르기</label>
                <textarea v-model.trim="form.allergy" class="form-control" rows="3" placeholder="예: 딸기, 감귤류 알레르기"></textarea>
              </div>

              <!-- 성별 -->
              <div class="mb-3">
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
              <button type="submit" class="btn btn-primary w-100">저장하기</button>
            </div>
          </div>
        </form>
      </div>
    </main>

    <!-- 고정 푸터 -->
   
  </div>
</template>

<script setup>
import { reactive, ref, computed } from "vue";
import {useRouter} from 'vue-router'
const router = useRouter
const avatar = "https://via.placeholder.com/72x72.png?text=%F0%9F%A4%B8";

const form = reactive({
  name: "",
  birthYear: null,
  blood: "",
  meds: "",
  height: null,
  weight: null,
  allergy: "",
  gender: "",
});

const tried = ref(false);

/* 본문 스크롤 영역 높이 계산 (헤더/푸터 고정 고려) */
const scrollBoxStyle = computed(() => ({
  height: "calc(100vh - 64px - 84px)", // 헤더 64px, 푸터 84px 기준
}));

const canSubmit = computed(() =>
  form.name &&
  form.birthYear &&
  form.blood &&
  !!form.gender
);

function onSubmit() {
  tried.value = true;
  if (!canSubmit.value) return;

  // TODO: 실제 저장 API 연동
  alert(`아이 추가 완료\n이름: ${form.name}`);
}

function goBack() { history.back(); }

/* 하단 탭 (라우팅 전 임시 동작) */
function goEmergency() { alert("응급검색"); }
function goMap() { alert("병원맵"); }
function goHome() { alert("홈"); }
function call119() { alert("응급신고"); }
function goMy() { alert("내 아이"); }
</script>
