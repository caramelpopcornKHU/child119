<template>
  <div class="card shadow-sm border-0 rounded-4">
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

      <!-- 건의사항 카드 -->
      <div class="card shadow-sm border-0 rounded-4">
        <div class="card-body">
          <div class="d-flex justify-content-between align-items-center mb-3">
            <h5 class="fw-semibold mb-0">응급처치 화면 건의사항</h5>
          </div>

          <!-- 건의사항 총 수 -->
          <div class="d-flex justify-content-between align-items-center mb-2">
            <span class="badge bg-secondary-subtle text-dark small">
              총 {{ emergencyFeedbacks.length }}건
            </span>
          </div>

          <!-- 건의사항 없음 -->
          <div v-if="emergencyFeedbacks.length === 0" class="text-muted small py-3 text-center">
            아직 응급처치 화면 건의사항이 없습니다.
          </div>

          <!-- 건의사항 목록 -->
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
                <tr 
                  v-for="(f, idx) in emergencyFeedbacks" 
                  :key="f.id"
                  @click="goToSuggestionDetail(f.id)"
                  style="cursor: pointer;"
                >
                  <td>{{ idx + 1 }}</td>
                  <td class="text-truncate" style="max-width: 240px;">
                    {{ f.emergencyTitle }}
                  </td>
                  <td class="text-truncate" style="max-width: 280px;">
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

    </main>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// ----------------------
// 🔹 응급처치 건의사항만 남김
// ----------------------
const emergencyFeedbacks = ref([
  {
    id: 'ef1',
    emergencyTitle: '고열 시 응급처치',
    content: '해열제 종류에 대한 설명도 추가되면 좋겠습니다.',
    writer: 'parent03@example.com',
    createdAt: new Date()
  },
  {
    id: 'ef2',
    emergencyTitle: '기도 막힘 대처법',
    content: '동영상 속도가 빨라서 좀 더 느린 버전이 필요합니다.',
    writer: 'parent04@example.com',
    createdAt: new Date()
  }
])

// 날짜 포맷
function formatDate(date) {
  if (!date) return ''
  return new Date(date).toLocaleDateString('ko-KR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  })
}

// 상세 페이지 이동
function goToSuggestionDetail(id) {
  router.push(`/AdminEmergencysuggestionsdetail/${id}`)
}
</script>


<style scoped>
.admin-layout {
  padding-top: 72px;
}

.admin-main {
  margin-left: 240px;
  padding-top: 1.5rem;
  padding-bottom: 2.5rem;
  padding-left: 0;
  padding-right: 0;
  min-height: 100vh;
  background-color: transparent;
}

.bg-success-subtle { background-color: #d1e7dd !important; }
.bg-warning-subtle { background-color: #fff3cd !important; }
.bg-secondary-subtle { background-color: #e2e3e5 !important; }
.bg-danger-subtle { background-color: #f8d7da !important; }
</style>
