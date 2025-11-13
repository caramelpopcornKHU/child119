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

        <!-- emergencyData 테이블 정보 -->
        <div class="card shadow-sm border-0 rounded-4">
          <div class="card-body">
            <div class="d-flex justify-content-between align-items-center mb-3">
              <h5 class="fw-semibold mb-0">응급 교육 콘텐츠 (emergencyData)</h5>
              <button
                type="button"
                class="btn btn-outline-primary btn-sm rounded-pill"
                @click="goToAdminEmergency"
              >
                콘텐츠 관리 화면으로 이동
              </button>
            </div>

            <div class="table-responsive">
              <table class="table table-sm align-middle mb-0">
                <thead class="table-light">
                  <tr>
                    <th style="width: 6%;">No</th>
                    <th style="width: 12%;">썸네일</th>
                    <th>제목</th>
                    <th style="width: 16%;">등록일</th>
                    <th style="width: 10%;">조회수</th>
                    <th style="width: 22%;">해시태그</th>
                    <th style="width: 12%;">관리</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="(c, idx) in emergencyList" :key="c.id">
                    <td>{{ idx + 1 }}</td>

                    <td>
                      <div v-if="c.thumbnailUrl" class="ratio ratio-16x9" style="max-width: 120px;">
                        <img
                          :src="c.thumbnailUrl"
                          :alt="c.title"
                          class="rounded-3 w-100 h-100"
                          style="object-fit: cover;"
                        />
                      </div>
                      <span v-else class="text-muted small">없음</span>
                    </td>

                    <td class="text-truncate" style="max-width: 260px;">
                      <button
                        type="button"
                        class="btn btn-link p-0 text-start text-truncate"
                        style="max-width: 260px;"
                        @click="goToEmergencyDetail(c.id)"
                      >
                        {{ c.title }}
                      </button>
                    </td>

                    <td>{{ formatDate(c.createdAt) }}</td>
                    <td>{{ c.viewCount.toLocaleString() }}</td>

                    <td>
                      <div class="small text-muted">
                        <span
                          v-for="(tag, tIdx) in c.hashtags"
                          :key="tIdx"
                          class="badge bg-light text-secondary border me-1"
                        >
                          #{{ tag }}
                        </span>
                        <span v-if="!c.hashtags || c.hashtags.length === 0" class="text-muted">
                          -
                        </span>
                      </div>
                    </td>

                    <td>
                      <div class="d-flex flex-column gap-1">
                        <button
                          type="button"
                          class="btn btn-outline-primary btn-sm rounded-pill"
                          @click="goToAdminEmergencyEdit(c.id)"
                        >
                          수정
                        </button>
                        <button
                          type="button"
                          class="btn btn-outline-danger btn-sm rounded-pill"
                          @click="deleteEmergency(c)"
                        >
                          삭제
                        </button>
                      </div>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>

            <p class="small text-muted mt-2 mb-0">
              ※ 이 목록은 관리자용 요약입니다. 상세 편집은 별도의 응급 콘텐츠 관리 화면에서 진행하세요.
            </p>
          </div>
        </div>

    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { db } from '@/firebase'
import { collection, getDocs, doc, deleteDoc, orderBy, query } from 'firebase/firestore'

const router = useRouter()

// 🔹 응급 콘텐츠 리스트
const emergencyList = ref([])

// 🔹 응급 콘텐츠 목록 로드
async function loadEmergencyList() {
  try {
    const q = query(collection(db, 'emergencyData'), orderBy('createdAt', 'desc'))
    const snap = await getDocs(q)

    emergencyList.value = snap.docs.map((d) => {
      const data = d.data()
      return {
        id: d.id,
        title: data.title || '(제목 없음)',
        createdAt: data.createdAt ? data.createdAt.toDate() : null,
        viewCount: data.viewCount ?? 0,
        hashtags: data.hashtags || [],
        thumbnailUrl: data.thumbnailUrl || '',
        youtubeId: data.youtubeId || '',
        youtubeUrl: data.youtubeUrl || '',
      }
    })
  } catch (e) {
    console.error('emergencyData 로딩 실패:', e)
  }
}

// 🔹 응급 콘텐츠 상세보기 이동
function goToEmergencyDetail(id) {
  router.push({ name: 'AdminEmergencyorder', params: { id } })
}

// 🔹 응급 콘텐츠 수정 페이지 이동
function goToAdminEmergencyEdit(id) {
  router.push(`/AdminEmergencyEdit/${id}`)
}

// 🔹 응급 콘텐츠 관리 화면 이동
function goToAdminEmergency() {
  router.push('/AdminEmergency')
}

// 🔹 응급 콘텐츠 삭제
async function deleteEmergency(item) {
  const ok = confirm(`정말로 "${item.title}" 콘텐츠를 삭제하시겠습니까?`)
  if (!ok) return

  try {
    await deleteDoc(doc(db, 'emergencyData', item.id))
    alert('콘텐츠가 삭제되었습니다.')
    await loadEmergencyList()
  } catch (e) {
    console.error('응급 교육 콘텐츠 삭제 실패:', e)
    alert('삭제 중 오류가 발생했습니다. Firestore rules를 확인해주세요.')
  }
}

// 🔹 날짜 포맷
function formatDate(date) {
  if (!date) return ''
  return new Date(date).toLocaleDateString('ko-KR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  })
}

// 🔹 페이지 로드 시 목록 불러오기
onMounted(() => {
  loadEmergencyList()
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
