<template>
  <div class="d-flex justify-content-center" >
    <div class="card shadow-sm w-100" style="max-width: 100vw; ">
      <!-- 메시지 영역 -->
      <div class="card-body overflow-auto border-bottom" ref="messagesContainer" style="height: 70vh;">
        <div
          v-for="(msg, i) in messages"
          :key="i"
          class="mb-3"
          :class="msg.type === 'user' ? 'text-end' : 'text-start'"
        >
          <span
            class="d-inline-block px-3 py-2 rounded-pill"
            :class="msg.type === 'user' ? 'bg-primary text-white' : 'bg-light text-dark'"
            style="max-width: 70%; word-break: break-word;"
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
          class="form-control rounded-pill px-3"
          placeholder="메시지를 입력하세요..."
        />
        <button class="d-flex btn btn-primary rounded-pill ms-2 px-4" type="submit"
                style="white-space: nowrap; flex-wrap: wrap;">
          전송
        </button>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick } from "vue";

const input = ref("");
const messages = ref([]);
const messagesContainer = ref(null);
let socket = null;

// 연결 설정
onMounted(() => {
 // 1. 기존 로컬/사설 IP 주소 (주석 처리)
 // socket = new WebSocket("ws://172.168.10.12:8088/ws/chat");

 // 2. ✅ Google Cloud Run 엔드포인트로 변경
  // (https 이므로 wss:// 프로토콜을 사용해야 합니다)
 socket = new WebSocket("wss://child119-251868777139.asia-northeast3.run.app/ws/chat");

  // 만약 두 번째 URL을 사용해야 한다면 위 코드를 주석처리하고 아래 코드의 주석을 해제하세요.
  // socket = new WebSocket("wss://child119-62eqwc2q4q-du.a.run.app/ws/chat");


 socket.onopen = () => {
  console.log("✅ WebSocket 연결 성공");
 };

 socket.onmessage = async (event) => {
 // 서버에서 받은 AI 응답 표시
  messages.value.push({ text: event.data, type: "ai" });
  await nextTick();
  messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight;
 };

 socket.onclose = () => {
  console.log("❌ WebSocket 연결 종료");
 };

 socket.onerror = (error) => {
  console.error("⚠️ WebSocket 오류:", error);
 };
});

onBeforeUnmount(() => {
 if (socket && socket.readyState === WebSocket.OPEN) {
  socket.close();
 }
});

// 메시지 전송
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

</style>
