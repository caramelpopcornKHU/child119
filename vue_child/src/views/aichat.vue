<template>
  <div class="d-flex justify-content-center align-items-center vh-100 bg-body-tertiary">
    <div class="card shadow-sm w-100" style="max-width: 600px; height: 80vh;">
      <!-- 메시지 영역 -->
      <div class="card-body overflow-auto border-bottom" ref="messagesContainer">
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
        <button class="btn btn-primary rounded-pill ms-2 px-4" type="submit">
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
  socket = new WebSocket("ws://172.168.10.12:8088/ws/chat");

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
body {
  background-color: #f0f2f5;
}
</style>
