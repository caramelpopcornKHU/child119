<!-- src/MyKidsView.vue -->
<template>
  <div class="bg-body-tertiary">
    <!-- 고정 헤더 -->
    

    <!-- 본문 래퍼: 헤더/푸터 높이만큼 패딩 확보 -->
    <main class="mx-auto px-3" style="max-width:420px; padding-top:64px; padding-bottom:84px;">
      <!-- 스크롤 영역: 헤더/푸터 제외 높이 계산 -->
      <div class="overflow-auto" :style="scrollBoxStyle">
        <!-- 타이틀 -->
        <h5 class="fw-bold mb-3">내 아이</h5>

        <!-- 아이 카드(아코디언 형태) -->
        <div class="card shadow-sm mb-3" v-for="(kid, i) in kids" :key="kid.id">
          <div class="card-body p-3">
            <!-- 상단 요약행 -->
            <div class="d-flex align-items-center">
              <img src="@/assets/kid1.jpg" alt="avatar" class="rounded-circle me-2" style="width:36px;height:36px;object-fit:cover;" />
              <div class="flex-grow-1 fw-semibold">{{ kid.label }}</div>
            

              <button class="btn btn-link px-2 text-primary" @click="editKid(i)" aria-label="수정">
                <i class="bi bi-pencil-square"></i>
              </button>
              <button class="btn btn-link px-2 text-body" @click="toggle(i)" :aria-expanded="kid.open" :aria-controls="'kid'+i">
                <i class="bi" :class="kid.open ? 'bi-caret-up-fill' : 'bi-caret-down-fill'"></i>
              </button>
            </div>

            <!-- 상세 -->
            <div :id="'kid'+i" v-show="kid.open" class="mt-3">
              <dl class="row mb-0">
                <dt class="col-4 text-body-secondary">이름</dt>
                <dd class="col-8 mb-2">{{ kid.name }}</dd>

                <dt class="col-4 text-body-secondary">출생년도</dt>
                <dd class="col-8 mb-2">{{ kid.birthYear }}</dd>

                <dt class="col-4 text-body-secondary">혈액형</dt>
                <dd class="col-8 mb-2">{{ kid.blood }}</dd>

                <dt class="col-4 text-body-secondary">특이 체질</dt>
                <dd class="col-8 mb-2">{{ kid.trait || '-' }}</dd>

                <dt class="col-4 text-body-secondary">복용 중인 약</dt>
                <dd class="col-8 mb-2">{{ kid.meds || '-' }}</dd>

                <dt class="col-4 text-body-secondary">키(cm)</dt>
                <dd class="col-8 mb-2">{{ kid.height }}</dd>

                <dt class="col-4 text-body-secondary">몸무게(kg)</dt>
                <dd class="col-8 mb-2">{{ kid.weight }}</dd>

                <dt class="col-4 text-body-secondary">알레르기</dt>
                <dd class="col-8 mb-2">{{ kid.allergy || '-' }}</dd>

                <dt class="col-4 text-body-secondary">성별</dt>
                <dd class="col-8 mb-0">
                  <span v-if="kid.gender==='F'"><i class="bi bi-gender-female text-danger me-1"></i>여</span>
                  <span v-else-if="kid.gender==='M'"><i class="bi bi-gender-male text-primary me-1"></i>남</span>
                  <span v-else>-</span>
                </dd>
              </dl>
            </div>
          </div>
        </div>

        <!-- 추가 버튼 -->
        <button class="btn btn-primary w-100 mb-3" @click="addKid">내 아이 +</button>
      </div>
    </main>

    <!-- 고정 푸터 -->
    
  </div>
</template>

<script setup>
import { reactive, computed } from "vue";
import {useRouter} from 'vue-router'
const router = useRouter()

const defaultAvatar = "https://via.placeholder.com/72x72.png?text=%F0%9F%A4%B8";

/* 데모 데이터 (실제로는 API 연동) */
const kids = reactive([
  {
    id: 1,
    label: "아이1",
    name: "김서",
    birthYear: 2022,
    blood: "A형",
    trait: "어릴 때 아토피",
    meds: "OOO 알러지약 이따금 복용",
    height: 95,
    weight: 14,
    allergy: "달걀, 귤/감귤 일부",
    gender: "F",
    avatar: "",
    open: true,
  },
  {
    id: 2,
    label: "아이2",
    name: "김우",
    birthYear: 2020,
    blood: "O형",
    trait: "",
    meds: "",
    height: 110,
    weight: 18,
    allergy: "",
    gender: "M",
    avatar: "",
    open: false,
  },
]);

/* 본문 스크롤 영역 높이 계산: 헤더/푸터 고정값을 제외 */
const scrollBoxStyle = computed(() => ({
  height: "calc(100vh - 64px - 84px)", // 헤더64 + 푸터84와 동일한 상·하 padding 포함 값
}));

function toggle(i) {
  kids[i].open = !kids[i].open;
}
function editKid(i) {
  alert(`${kids[i].label} 정보 수정`);
}
function addKid() {
    router.push("/AddKid");
 alert("아이 추가");
}

/* 하단 탭 이벤트 (라우터 연동 전 임시) */
function goEmergency() { alert("응급검색"); }
function goMap() { alert("병원맵"); }
function goHome() { alert("홈"); }
function call119() { alert("응급신고"); }
function goMy() { alert("내 아이"); }

/* 헤더 아이콘 */
function onSearch() { alert("검색"); }
function onMenu() { alert("메뉴"); }
</script>
