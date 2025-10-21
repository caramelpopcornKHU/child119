<template>
  <div class="min-vh-100 d-flex align-items-start justify-content-center bg-body-tertiary py-4">
    <div class="container" style="max-width: 420px;">
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

            <!-- 버튼 -->
            <div class="d-flex gap-2 mt-3">
              <button type="button" class="btn btn-outline-secondary flex-fill" @click="onBack">이전</button>
              <button type="submit" class="btn btn-primary flex-fill" :disabled="loading">
                <span v-if="loading" class="spinner-border spinner-border-sm" aria-hidden="true"></span>
                <span v-else>아이디 찾기</span>
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, computed, ref } from "vue";
import { useRouter } from "vue-router"; // 1. vue-router import
import { db } from "@/firebase"; // 2. Firebase db 인스턴스 import
import { collection, query, where, getDocs } from "firebase/firestore"; // 3. Firestore 함수 import

const router = useRouter();

const form = reactive({
  name: "",
  phone: "",
});

const tried = ref(false);
const loading = ref(false); // 로딩 상태 추가

// '인증번호' 필드를 사용하지 않으므로 canSubmit에서 제거했습니다.
const canSubmit = computed(() => form.name && form.phone);

async function onSubmit() { // 4. async 함수로 변경
  tried.value = true;
  if (!canSubmit.value) return;

  loading.value = true; // 로딩 시작

  try {
    // 5. Firestore 쿼리 생성
    const usersRef = collection(db, "user"); // 'user' 컬렉션에 접근
    const q = query(
      usersRef,
      where("name", "==", form.name), // 이름이 일치하고
      where("phone", "==", form.phone) // 전화번호가 일치하는
    );

    // 6. 쿼리 실행
    const querySnapshot = await getDocs(q);

    if (querySnapshot.empty) {
      // 7. 일치하는 사용자가 없을 경우
      alert("일치하는 사용자 정보가 없습니다.");
    } else {
      // 8. 일치하는 사용자를 찾았을 경우
      const foundUser = querySnapshot.docs[0].data(); // 첫 번째 매칭 결과
      const foundId = foundUser.username;

      // 9. 결과 페이지로 아이디와 함께 이동
      router.push({ name: 'FindId', query: { id: foundId } });
    }
  } catch (error) {
    console.error("아이디 찾기 중 오류 발생:", error);
    alert("오류가 발생했습니다. 다시 시도해주세요.");
  } finally {
    loading.value = false; // 로딩 종료
  }
}

function onBack() {
  history.back();
}
</script>
