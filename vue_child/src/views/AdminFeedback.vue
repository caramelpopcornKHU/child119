<template>
  <div class="admin-layout d-flex min-vh-100">
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

        <div class="card shadow-sm border-0 rounded-4">
          <div class="card-body">
            <div class="d-flex justify-content-between align-items-center mb-3">
              <h5 class="fw-semibold mb-0">사용자 건의사항</h5>
            </div>

            <div class="btn-group mb-3">
              <button
                type="button"
                class="btn btn-sm"
                :class="feedbackTab === 'app' ? 'btn-primary' : 'btn-outline-primary'"
                @click="feedbackTab = 'app'"
              >
                앱 건의사항
              </button>
              <button
                type="button"
                class="btn btn-sm"
                :class="feedbackTab === 'emergency' ? 'btn-primary' : 'btn-outline-primary'"
                @click="feedbackTab = 'emergency'"
              >
                응급처치 화면 건의사항
              </button>
            </div>

            <!-- 앱 건의사항 -->
            <div v-if="feedbackTab === 'app'">
              <div class="d-flex justify-content-between align-items-center mb-2">
                <span class="badge bg-secondary-subtle text-dark small">
                  총 {{ appFeedbacks.length }}건
                </span>
              </div>

              <div v-if="appFeedbacks.length === 0" class="text-muted small py-3 text-center">
                아직 앱 건의사항이 없습니다.
              </div>

              <div v-else class="table-responsive">
                <table class="table table-sm align-middle mb-0">
                  <thead class="table-light">
                    <tr>
                      <th style="width: 8%;">No</th>
                      <th>제목</th>
                      <th style="width: 18%;">작성자</th>
                      <th style="width: 18%;">작성일</th>
                      <th style="width: 12%;">상태</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="(f, idx) in emergencyFeedbacks" :key="f.id" @click="goToSuggestionDetail(f.id)"
                      style="cursor: pointer;">
                      <td>{{ idx + 1 }}</td>
                      <td class="text-truncate" style="max-width: 200px;">
                        {{ f.emergencyTitle }}
                      </td>
                      <td class="text-truncate" style="max-width: 260px;">
                        {{ f.content }}
                      </td>
                      <td>{{ f.writer }}</td>
                      <td>{{ formatDate(f.createdAt) }}</td>
                      <td>
                        <span class="badge rounded-pill px-3"
                          :class="f.status === '처리완료' ? 'bg-success-subtle text-success' : 'bg-warning-subtle text-warning'">
                          {{ f.status }}
                        </span>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>

            <!-- 응급처치 화면 건의사항 -->
            <div v-else>
              <div class="d-flex justify-content-between align-items-center mb-2">
                <span class="badge bg-secondary-subtle text-dark small">
                  총 {{ emergencyFeedbacks.length }}건
                </span>
              </div>

              <div v-if="emergencyFeedbacks.length === 0" class="text-muted small py-3 text-center">
                아직 응급처치 화면 건의사항이 없습니다.
              </div>

              <div v-else class="table-responsive">
                <table class="table table-sm align-middle mb-0">
                  <thead class="table-light">
                    <tr>
                      <th style="width: 8%;">No</th>
                      <th>관련 콘텐츠</th>
                      <th>내용</th>
                      <th style="width: 18%;">작성자</th>
                      <th style="width: 18%;">작성일</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="(f, idx) in emergencyFeedbacks" :key="f.id"  @click="goToSuggestionDetail(f.id)" style="cursor: pointer;">
                      <td>{{ idx + 1 }}</td>
                      <td class="text-truncate" style="max-width: 200px;">
                        {{ f.emergencyTitle }}
                      </td>
                      <td class="text-truncate" style="max-width: 260px;">
                        {{ f.content }}
                      </td>
                      <td>{{ f.writer }}</td>
                      <td>{{ formatDate(f.createdAt) }}</td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>

          </div>
        </div>
    </main>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// ----------------------
// 🔹 건의사항 탭 상태
// ----------------------
const feedbackTab = ref('app')

// ----------------------
// 🔹 앱 건의사항
// ----------------------
const appFeedbacks = ref([
  {
    id: 'f1',
    title: '야간 모드가 있으면 좋겠습니다.',
    writer: 'user01@naver.com',
    createdAt: new Date(),
    status: '접수'
  },
  {
    id: 'f2',
    title: '메인 화면에 자주 쓰는 메뉴를 고정하고 싶어요.',
    writer: 'user02@gmail.com',
    createdAt: new Date(),
    status: '처리완료'
  }
])

// ----------------------
// 🔹 응급처치 화면 건의사항
// ----------------------
const emergencyFeedbacks = ref([
  {
    id: 'ef1',
    emergencyTitle: '고열 시 응급처치',
    content: '해열제 종류에 대한 설명도 추가되면 좋겠습니다.',
    writer: 'parent03@example.com',
    createdAt: new Date(),
    status: '접수'
  },
  {
    id: 'ef2',
    emergencyTitle: '기도 막힘 대처법',
    content: '동영상 속도가 빨라서, 좀 더 천천히 설명된 버전이 있으면 좋겠어요.',
    writer: 'parent04@example.com',
    createdAt: new Date(),
    status: '처리완료'
  }
])

// ----------------------
// 🔹 날짜 포맷 함수
// ----------------------
function formatDate(date) {
  if (!date) return ''
  return new Date(date).toLocaleDateString('ko-KR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  })
}

// ----------------------
// 🔹 상세 페이지 이동
// ----------------------
function goToSuggestionDetail(suggestionId) {
  router.push(`/AdminEmergencysuggestionsdetail/${suggestionId}`)
}
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
