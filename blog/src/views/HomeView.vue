<template>

  <div class="bg-light-info">
    <h1>홈 화면</h1>
    <p>여기서 버튼을 누르면 안드로이드 네비가 뜹니다.</p>
    
    <button @click="goToNativeNavi">네비게이션 시작하기</button>
  </div>
  
</template>

<script setup>

import { ref, onMounted } from 'vue'


// 스토어 불러오기
import { storeToRefs } from 'pinia'

import { useAppStore } from '@/stores/app'
const appStore = useAppStore();
const { title } = storeToRefs(appStore);


onMounted(() => {
  console.log(`HomeView::onMounted 호출됨`);

  title.value = '홈';

})


  
function goToNativeNavi() {
  // 2. AndroidBridge가 존재하는지 확인 (앱에서 실행된 경우에만 존재)
  if (window.AndroidBridge) {
    // 3. 1단계에서 만든 startNavigation() 함수 호출
    window.AndroidBridge.startNavigation();
  } else {
    // PC 웹 브라우저 등 앱이 아닌 환경일 때
    alert("이 기능은 앱에서만 사용할 수 있습니다.");
  }
}


</script>

<style>

</style>
