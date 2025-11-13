<template>
  <div class="card shadow-sm border-0 rounded-4">
    <!-- 메인 영역 -->
    <main class="admin-main">
      <!-- 상단 타이틀 & 설명 -->
      <div
        class="d-flex flex-column flex-md-row align-items-md-center justify-content-between mb-3 px-4 pt-3"
      >
        <div>
          <h3 class="text-bold mb-1">관리자 대시보드</h3>
          <p class="text-medium mb-0 small">
            아이119 앱의 크롤링 설정, 사용자/콘텐츠 데이터, 자동화 로그, 건의사항을 한 곳에서 관리합니다.
          </p>
        </div>
      </div>

      <!-- 🔹 크롤링 주기 설정 카드 -->
      <div class="card shadow-sm border-0 rounded-4 text-plight mb-3">
        <div
          class="card-header bg-white border-0 d-flex justify-content-between align-items-center pb-1"
        >
          <div>
            <h5 class="text-bold mb-1">크롤링 주기 설정</h5>
            <p class="small text-muted mb-0">
              외부 데이터(예: 응급 관련 뉴스, 콘텐츠)를 가져오는 크롤러의 실행 주기를 설정합니다.
            </p>
          </div>
          <div class="text-end">
            <div class="small text-muted mb-1">현재 주기</div>
            <span class="badge bg-light text-secondary border small px-3 py-2">
              {{ crawlingInterval }}분
            </span>
          </div>
        </div>

        <div class="card-body pt-3 text-plight ">
          <div class="d-flex flex-wrap align-items-center gap-3">
            <!-- 프리셋 버튼 그룹 -->
            <div>
              <div class="small text-muted mb-1">추천 프리셋</div>
              <div class="btn-group btn-group-sm flex-wrap" role="group">
                <button
                  v-for="m in crawlingOptions"
                  :key="m"
                  type="button"
                  class="btn"
                  :class="crawlingInterval === m ? 'btn-secondary' : 'btn-outline-secondary'"
                  @click="crawlingInterval = m"
                >
                  {{ m }}분
                </button>
              </div>
            </div>



            <!-- 직접 입력 + 저장 -->
            <div
              class="flex-grow-1 d-flex flex-wrap align-items-center justify-content-lg-end gap-2 text-plight"
            >
              <div class="input-group input-group-sm preset-input w-auto btn-save-offset">
                <span class="input-group-text bg-body-secondary border-0">
                  직접 입력
                </span>
                <input
                  type="number"
                  min="1"
                  class="form-control"
                  v-model.number="crawlingInterval"
                  placeholder="분"
                />
                <span class="input-group-text bg-body-secondary border-0">
                  분
                </span>
              </div>

              <button
  type="button"
  class="btn btn-primary btn-sm rounded-pill d-inline-flex align-items-center gap-1 btn-save-offset"
  @click="saveCrawlingInterval"
>
  <i class="bi bi-save"></i>
  <span>저장</span>
</button>

            </div>
          </div>

          <p class="small text-muted mt-2 mb-0">
            <span>현재 설정된 주기: <strong>{{ crawlingInterval }}분</strong></span>
            <span v-if="lastSavedCrawling" class="text-secondary ms-2">
              · 마지막 저장: {{ lastSavedCrawling }}
            </span>
          </p>
        </div>
      </div>

      <!-- 🔹 자동화 실행 + 로그 카드 -->
      <div class="card shadow-sm border-0 rounded-4 text-plight">
        <div
          class="card-header bg-white border-0 d-flex justify-content-between align-items-start pb-1"
        >
          <div>
            <h5 class="text-bold mb-1">데이터 크롤링 자동화</h5>
            <p class="small text-muted mb-0">
              Python(FastAPI) 자동화 서버에 크롤링 작업을 요청하고, 작업 중 발생하는 로그를 실시간으로
              확인합니다.
            </p>
          </div>
          <div class="text-end">
            <div class="small text-muted mb-1">상태</div>
            <span class="badge rounded-pill px-3" :class="automationStatusBadgeClass">
              {{ automationStatusText }}
            </span>
            <div v-if="automationJobId" class="small text-muted mt-1">
              <i class="bi bi-hash"></i>
              Job {{ automationJobId }}
            </div>
          </div>
        </div>

        <div class="card-body pt-3">
          <!-- 상단 버튼 바 -->
          <div class="d-flex flex-wrap align-items-center gap-2 mb-3">
            <div class="btn-toolbar gap-2">
              <button
                type="button"
                class="btn btn-primary btn-sm rounded-pill d-inline-flex align-items-center gap-1"
                @click="startAutomation"
                :disabled="automationStatus === 'running'"
              >
                <template v-if="automationStatus !== 'running'">
                  <i class="bi bi-play-fill"></i>
                  <span>크롤링 즉시 실행</span>
                </template>
                <template v-else>
                  <span
                    class="spinner-border spinner-border-sm me-1"
                    role="status"
                    aria-hidden="true"
                  ></span>
                  <span>실행 중...</span>
                </template>
              </button>

              <button
                type="button"
                class="btn btn-outline-secondary btn-sm rounded-pill d-inline-flex align-items-center gap-1"
                @click="clearAutomationLogs"
              >
                <i class="bi bi-trash3"></i>
                <span>로그 초기화</span>
              </button>
            </div>

            <div class="small text-muted ms-auto">
              FastAPI 엔드포인트:
              <code>{{ AUTOMATION_BASE_URL }}</code>
            </div>
          </div>

          <!-- 로그 영역 -->
          <div class="automation-log-box">
            <div
              v-if="automationLogs.length === 0"
              class="text-muted small h-100 d-flex align-items-center justify-content-center text-center"
            >
              아직 실행된 로그가 없습니다.<br />
              상단의 <strong>"크롤링 즉시 실행"</strong> 버튼을 눌러 자동화를 시작하세요.
            </div>
            <pre v-else class="mb-0">
<span v-for="(line, idx) in automationLogs" :key="idx">{{ line }}</span>
            </pre>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>


<script setup>
import { ref, computed, onBeforeUnmount } from 'vue'

// ----------------------
// 크롤링 주기 설정
// ----------------------
const crawlingOptions = [5, 10, 15, 30, 60]
const crawlingInterval = ref(10)
const lastSavedCrawling = ref('')

function saveCrawlingInterval() {
  lastSavedCrawling.value = new Date().toLocaleString('ko-KR')
  alert(`크롤링 주기가 ${crawlingInterval.value}분으로 저장되었습니다.`)
}

// ----------------------
// 자동화 실행 + 로그 (FastAPI SSE)
// ----------------------
const AUTOMATION_BASE_URL =
  'https://child119messageauto-251868777139.asia-northeast3.run.app'

const automationStatus = ref('idle') // 'idle' | 'running' | 'done' | 'error'
const automationJobId = ref(null)
const automationLogs = ref([])
let automationEventSource = null

const automationStatusText = computed(() => {
  switch (automationStatus.value) {
    case 'running':
      return '실행 중'
    case 'done':
      return '완료'
    case 'error':
      return '오류'
    default:
      return '대기 중'
  }
})

const automationStatusBadgeClass = computed(() => {
  switch (automationStatus.value) {
    case 'running':
      return 'bg-warning-subtle text-warning'
    case 'done':
      return 'bg-success-subtle text-success'
    case 'error':
      return 'bg-danger-subtle text-danger'
    default:
      return 'bg-secondary-subtle text-muted'
  }
})

function clearAutomationLogs() {
  automationLogs.value = []
}

async function startAutomation() {
  if (automationStatus.value === 'running') return

  automationLogs.value = []
  automationStatus.value = 'running'
  automationJobId.value = null

  if (automationEventSource) {
    automationEventSource.close()
    automationEventSource = null
  }

  try {
    const res = await fetch(`${AUTOMATION_BASE_URL}/run`, { method: 'POST' })
    if (!res.ok) throw new Error(`실행 요청 실패 (status: ${res.status})`)

    const data = await res.json()
    automationJobId.value = data.jobId
    connectAutomationLogStream()
  } catch (e) {
    console.error('자동화 실행 요청 실패:', e)
    automationStatus.value = 'error'
    automationLogs.value.push(`❌ 실행 요청 실패: ${e.message}`)
  }
}

function connectAutomationLogStream() {
  if (!automationJobId.value) return

  if (automationEventSource) automationEventSource.close()

  automationEventSource = new EventSource(
    `${AUTOMATION_BASE_URL}/logs/${automationJobId.value}`
  )

  automationEventSource.onmessage = (event) => {
    automationLogs.value.push(event.data + '\n')
  }

  automationEventSource.onerror = () => {
    if (automationEventSource) {
      automationEventSource.close()
      automationEventSource = null
    }
    if (automationStatus.value === 'running') {
      automationStatus.value = 'done'
    }
  }
}

onBeforeUnmount(() => {
  if (automationEventSource) automationEventSource.close()
})
</script>


<style scoped>
.admin-layout {
  padding-top: 72px;
}

/* 메인 영역: 사이드바 너비만큼 밀기, 좌우 여백 제거 */
.admin-main {
  margin-left: 240px; /* 사이드바 width와 일치해야 함 */
  padding-top: 1.5rem;
  padding-bottom: 2.5rem;
  padding-left: 0;
  padding-right: 0;
  min-height: 100vh;
  background-color: transparent;
}

/* 크롤링 주기 입력 인풋 살짝 정리 */
.preset-input {
  min-width: 190px;
}

/* 자동화 로그 박스 */
.automation-log-box {
  background-color: #111;
  color: #e9ecef;
  border-radius: 0.75rem;
  padding: 0.75rem 1rem;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono',
    'Courier New', monospace;
  font-size: 12px;
  height: 260px;
  overflow-y: auto;
  border: 1px solid #343a40;
}

/* 배지 subtle 색상 */
.bg-success-subtle {
  background-color: #d1e7dd !important;
}
.bg-warning-subtle {
  background-color: #fff3cd !important;
}
.bg-secondary-subtle {
  background-color: #e2e3e5 !important;
}
.bg-danger-subtle {
  background-color: #f8d7da !important;
}

/* 원하는 만큼 조절해서 쓰면 됨 */
.btn-save-offset {
  margin-top: 30px;      /* 아래로 2px 내림 */
  /* or */
  /* transform: translateY(-2px);  위로 2px 올리기 */
}

</style>

