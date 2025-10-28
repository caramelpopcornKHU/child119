<template>
  <div class="d-flex justify-content-center">
    <div class="card shadow-sm w-100" style="max-width: 100vw;">
      <div
        v-if="showPopup"
        class="position-fixed top-0 start-0 w-100 h-100 d-flex justify-content-center align-items-center bg-dark bg-opacity-50"
        style="z-index: 9999;"
      >
        <div class="bg-white rounded-4 shadow p-5 position-relative" style="width: 90%; max-width: 420px;">
          <button
            type="button"
            class="btn-close position-absolute top-0 start-0 m-3"
            aria-label="Close"
            @click="cancelPopup"
          ></button>

          <h5 class="text-center fw-bold mb-4 mt-2">안내</h5>
          <p class="text-medium text-center mb-3 guide-text">
            챗봇은 사용자가 입력한 증상 정보를 기반으로 가까운 병원과 참고용 응급처치 정보를 안내합니다.
          </p>

          <ul class="small text-light-emphasis mb-4 guide-text">
            <li>증상의 정도에 따라 안내되는 결과가 달라질 수 있습니다.</li>
            <li>이용 시점과 실제 병원 방문 시점의 상태 변화에 따라 결과가 달라질 수 있습니다.</li>
            <li>의식 저하, 심한 출혈, 호흡곤란 등 응급상황일 경우 즉시 119에 연락하세요.</li>
            <li>본 서비스는 참고용으로 제공되며, 의료 전문가의 진단을 대체하지 않습니다.</li>
            <li>최종 판단과 책임은 사용자 본인에게 있습니다. 계속 진행하시겠습니까?</li>
          </ul>


          <div class="mt-5">
            <button
              class="btn w-100 py-2 fw-medium"
              style="background-color: #0D6EFD; color: white;"
              @click="confirmPopup"
            >
              확인
            </button>
          </div>
        </div>
      </div>

    <!-- 메시지 영역 -->
    <div class="card-body overflow-auto border-bottom" ref="messagesContainer" style="height: 70vh;">
      <div
        v-for="(msg, i) in messages"
        :key="i"
        class="mb-3"
        :class="msg.type === 'user' ? 'text-end' : 'text-start'"
      >
        <span
          class="d-inline-block text-light px-3 py-2 rounded-4"
          :class="msg.type === 'user' ? 'bg-primary text-white' : 'bg-light text-dark'"
          style="max-width: 70%; white-space: pre-line; word-break: keep-all; line-height: 1.6;"
        >
          {{ msg.text }}
        </span>
      </div>
    </div>


      <!-- 입력 폼 -->
      <form class="d-flex p-2" @submit.prevent="sendMessage">
        <input
          v-model="input"
          type="text"
          class="form-control rounded-pill text-medium px-3"
          placeholder="메시지를 입력하세요..."
        />
        <button
          class="d-flex btn btn-primary rounded-pill text-medium ms-2 px-4"
          style="background-color: #0D6EFD; color: white; flex-wrap: wrap; white-space: nowrap;"
          type="submit"
        >
          전송
        </button>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick } from "vue";
import { useRouter } from "vue-router";

const router = useRouter();

const input = ref("");
const messages = ref([]);
const messagesContainer = ref(null);
let socket = null;

// ✅ 팝업 상태
const showPopup = ref(false);

// ✅ 팝업 동작
const cancelPopup = () => {
  showPopup.value = false;
  router.push("/"); // 확인 시 홈으로 이동
};

const confirmPopup = () => {
  showPopup.value = false; // X 버튼 클릭 시 닫기
};

// ✅ WebSocket 연결
onMounted(() => {
  // 페이지 진입 시 팝업 표시
  showPopup.value = true;

  socket = new WebSocket("wss://child119-251868777139.asia-northeast3.run.app/ws/chat");

  socket.onopen = () => console.log("✅ WebSocket 연결 성공");
  socket.onmessage = async (event) => {
    messages.value.push({ text: event.data, type: "ai" });
    await nextTick();
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight;
  };
  socket.onclose = () => console.log("❌ WebSocket 연결 종료");
  socket.onerror = (error) => console.error("⚠️ WebSocket 오류:", error);
});

onBeforeUnmount(() => {
  if (socket && socket.readyState === WebSocket.OPEN) socket.close();
});

// ✅ 메시지 전송
const sendMessage = async () => {
  if (!input.value.trim() || !socket || socket.readyState !== WebSocket.OPEN) return;

  messages.value.push({ text: input.value, type: "user" });
  socket.send(input.value);

  input.value = "";
  await nextTick();
  messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight;
};
</script>
<style scoped>
.guide-text {
  white-space: pre-line;    
  word-break: keep-all; 
  line-height: 1.6; 
  letter-spacing: 0.2px; 
}
</style>
