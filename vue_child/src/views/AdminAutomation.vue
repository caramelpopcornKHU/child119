<template>
  <div class="card shadow-sm border-0 rounded-4">
    <!-- 메인 영역 -->
    <main class="admin-main">
      <!-- 상단 타이틀 & 설명 -->
      <div class="d-flex flex-column flex-md-row align-items-md-center justify-content-between mb-3 px-4 pt-3">
        <div>
          <h3 class="text-bold mb-1">관리자 대시보드</h3>
          <p class="text-medium mb-0 small">
            아이119 앱의 크롤링 설정, 사용자/콘텐츠 데이터, 자동화 로그, 건의사항을 한 곳에서 관리합니다.
          </p>
        </div>
      </div>
        <!-- 크롤링 주기 설정 -->
        <div class="card shadow-sm border-0 rounded-4 text-plight">
          <div class="card-body">
            <div class="d-flex justify-content-between align-items-center mb-2">
              <h5 class="text-bold mb-0">크롤링 주기 설정</h5>
            </div>
            <p class="small text-muted">
              외부 데이터(예: 응급 관련 뉴스, 콘텐츠)를 가져오는 크롤러의 실행 주기를 설정합니다.
            </p>

            <div class="d-flex flex-wrap align-items-center gap-3">
              <div class="d-flex flex-wrap gap-2">
                <button
                  v-for="m in crawlingOptions"
                  :key="m"
                  type="button"
                  class="btn btn-sm rounded-pill"
                  :class="crawlingInterval === m ? 'btn-primary' : 'btn-outline-primary'"
                  @click="crawlingInterval = m"
                >
                  {{ m }}분
                </button>
              </div>
              <div class="ms-auto">
              <button
                type="button"
                class="btn btn-primary btn-sm rounded-3 px-3"
                @click="saveCrawlingInterval"
              >
                설정 저장
              </button>                
              </div>
            </div>

            <p class="small text-muted mt-2 mb-0">
              현재 설정된 주기: <strong>{{ crawlingInterval }}분</strong>
              <span v-if="lastSavedCrawling" class="text-secondary">
                (마지막 저장: {{ lastSavedCrawling }})
              </span>
            </p>
          </div>
        </div>

        <!--자동화 실행+로그 카드-->
        <div class="card shadow-sm border-0 rounded-4 text-plight">
          <div class="card-body">
            <div class="d-flex justify-content-between align-items-center mb-2">
              <h5 class="text-bold mb-0 ">데이터 크롤링 자동화</h5>
              <span class="badge rounded-pill px-3"
                    :class="automationStatusBadgeClass">
                {{ automationStatusText }}
              </span>
            </div>
            <p class="small text-muted mb-3">
              아래 버튼을 누르면 Python 자동화 서버에 크롤링 작업을 요청하고,
              작업 중 발생하는 로그를 실시간으로 표시합니다.
            </p>

            <div class="d-flex align-items-center gap-2 mb-3">
              <button
                type="button"
                class="btn btn-primary rounded-pill"
                @click="startAutomation"
                :disabled="automationStatus === 'running'"
              >
                <span v-if="automationStatus !== 'running'">자동화 실행</span>
                <span v-else>
                  <span class="spinner-border spinner-border-sm me-1" role="status" aria-hidden="true"></span>
                  실행 중...
                </span>
              </button>

              <button
                type="button"
                class="btn btn-outline-secondary btn-sm rounded-pill"
                @click="clearAutomationLogs"
              >
                로그 초기화
              </button>

              <div class="small text-muted ms-auto" v-if="automationJobId">
                Job ID: <code>{{ automationJobId }}</code>
              </div>
            </div>

            <!-- 로그 영역 -->
            <div class="automation-log-box">
              <div
                v-if="automationLogs.length === 0"
                class="text-muted small h-100 d-flex align-items-center justify-content-center"
              >
                아직 실행된 로그가 없습니다. 상단의 "자동화 실행" 버튼을 눌러주세요.
              </div>
              <pre v-else class="mb-0">
<span v-for="(line, idx) in automationLogs" :key="idx">{{ line }}</span>
              </pre>
            </div>

            <p class="small text-muted mt-2 mb-0">
              ※ FastAPI 자동화 서버 엔드포인트:
              <code>{{ AUTOMATION_BASE_URL }}</code>
            </p>
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

.admin-main {
  padding-top: 1.5rem;
  padding-bottom: 2.5rem;
}

/* 메인 영역: 사이드바 너비만큼 밀기, 좌우 여백 제거 */
.admin-main {
  margin-left: 240px; /* 사이드바 width와 일치해야 함 */
  padding-top: 1.5rem;
  padding-bottom: 2.5rem;
  padding-left: 0;  /* 왼쪽 여백 없는 상태로 시작 */
  padding-right: 0; /* 오른쪽 여백 없음 (필요하면 수정) */
  min-height: 100vh;
  background-color: transparent;
}

.admin-layout {
  padding-top: 0; /* 필요 시 헤더 높이에 맞춰 조정 */
}

/* 자동화 로그 박스 */
.automation-log-box {
  background-color: #111;
  color: #e9ecef;
  border-radius: 0.75rem;
  padding: 0.75rem 1rem;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", "Courier New", monospace;
  font-size: 12px;
  height: 260px;
  overflow-y: auto;
  border: 1px solid #343a40;
}

/* 사용자 증감 그래프 스타일은 삭제함 (stats 탭 제거) */

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

/* 자녀 모달 */
.kid-modal-backdrop {
  position: fixed;
  inset: 0;
  background-color: rgba(0,0,0,0.35);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
}

.kid-modal {
  max-width: 480px;
  width: 90%;
  max-height: 80vh;
  overflow-y: auto;
}
</style>
