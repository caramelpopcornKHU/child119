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

        <!-- users + kidinformation 정보 -->
        <div class="card shadow-sm border-0 rounded-4">
          <div class="card-body">
            <div class="d-flex justify-content-between align-items-center mb-3">
              <h5 class="text-bold mb-0">사용자 정보 (users + kidinformation)</h5>
              <span class="badge bg-secondary-subtle text-dark text-plight small">
                총 {{ usersWithKids.length }}명
              </span>
            </div>

            <div class="table-responsive">
              <table class="table table-sm align-middle mb-0">
                <thead class="table-light">
                  <tr>
                    <th style="width: 4%;">No</th>
                    
                    <th style="width: 12%;">아이디</th>
                    <th style="width: 12%;">이름</th>
                    <th style="width: 12%;">전화번호</th>
                    <th style="width: 10%;">자녀 수</th>
                    <th style="width: 12%;">가입일</th>
                    <th style="width: 10%;">동의</th>
                    <th style="width: 10%;">관리</th>
                  </tr>
                </thead>

                <tbody>
                  <tr v-for="(u, idx) in usersWithKids" :key="u.uid">
                    <td>{{ idx + 1 }}</td>
                    <td>{{ u.username || '-' }}</td>
                    <td>{{ u.name }}</td>
                    <td>{{ u.phone || '-' }}</td>

                    <td>
                      <div class="small">
                        <div v-if="u.kids.length">
                          <strong>{{ u.kids.length }}명</strong>
                          <button
                            type="button"
                            class="btn btn-link btn-sm p-0 ms-1"
                            @click="openKidModal(u)"
                          >
                            상세보기
                          </button>
                        </div>
                        <div v-else class="text-muted">
                          등록된 자녀 정보 없음
                        </div>
                      </div>
                    </td>

                    <td>{{ formatDate(u.createdAt) }}</td>

                    <td>
                      <div class="d-flex flex-column gap-1 small">
                        <span
                          class="badge rounded-pill px-2"
                          :class="u.agreeTerms ? 'bg-success-subtle text-success' : 'bg-secondary-subtle text-muted'"
                        >
                          이용약관 {{ u.agreeTerms ? '동의' : '미동의' }}
                        </span>
                        <span
                          class="badge rounded-pill px-2"
                          :class="u.agreeMarketing ? 'bg-info-subtle text-info' : 'bg-secondary-subtle text-muted'"
                        >
                          마케팅 {{ u.agreeMarketing ? '동의' : '미동의' }}
                        </span>
                      </div>
                    </td>

                    <td>
                      <div class="d-flex flex-column gap-1">
                        <button
                          type="button"
                          class="btn btn-outline-primary btn-sm rounded-pill"
                          @click="goToUserEdit(u.uid)"
                        >
                          수정
                        </button>
                        <button
                          type="button"
                          class="btn btn-outline-danger btn-sm rounded-pill"
                          @click="deleteUser(u)"
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
              ※ 실제 구현 시, Firestore의 <code>users</code> 컬렉션과
              <code>kidinformation</code> 컬렉션을 join해서 가져오면 됩니다.
            </p>
          </div>
        </div>
        <div v-if="kidModalUser" class="kid-modal-backdrop" @click.self="closeKidModal">
        <div class="kid-modal card shadow-sm">
          <div class="card-body">
            <div class="d-flex justify-content-between align-items-center mb-2">
              <h5 class="fw-semibold mb-0">
                {{ kidModalUser.name }} 님의 자녀 정보
              </h5>
              <button
                type="button"
                class="btn btn-sm btn-outline-secondary rounded-pill"
                @click="closeKidModal"
              >
                닫기
              </button>
            </div>

            <div v-if="kidModalUser.kids.length === 0" class="text-muted small">
              등록된 자녀 정보가 없습니다.
            </div>

            <div v-else class="d-flex flex-column gap-2">
              <div
                v-for="(k, idx) in kidModalUser.kids"
                :key="k.id || idx"
                class="border rounded-3 p-2"
              >
                <div class="fw-semibold mb-1">
                  {{ idx + 1 }}. {{ k.name || '(이름 없음)' }}
                </div>
                <div class="small text-muted">
                  나이: {{ k.age }}세 / 성별: {{ k.gender || '-' }}
                </div>
                <div class="small text-muted mt-1">
                  키: {{ k.heightCm ?? '-' }}cm /
                  몸무게: {{ k.weightKg ?? '-' }}kg /
                  혈액형: {{ k.bloodType || '-' }}
                </div>
                <div class="small text-muted mt-1">
                  알레르기: {{ k.allergy || '-' }}<br />
                  기저질환: {{ k.medicalHistory || '-' }}<br />
                  복용약: {{ k.medication || '-' }}
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { db } from '@/firebase'
import {
  collection,
  getDocs,
  doc,
  query,
  where,
  writeBatch
} from 'firebase/firestore'

const router = useRouter()
const activeTab = ref('manage')

// ----------------------
// 🔹 모달 관리
// ----------------------
const kidModalUser = ref(null)
const openKidModal = (user) => { kidModalUser.value = user }
const closeKidModal = () => { kidModalUser.value = null }

// ----------------------
// 🔹 사용자 + 자녀 정보 로딩
// ----------------------
const usersWithKids = ref([])

function calcAge(birthYear) {
  if (!birthYear) return '-'
  const year = new Date().getFullYear()
  return year - birthYear
}

async function loadUsersWithKids() {
  try {
    const usersSnap = await getDocs(collection(db, 'users'))
    const kidsSnap = await getDocs(collection(db, 'kidinformation'))

    const kidsByParent = {}
    kidsSnap.forEach(docSnap => {
      const data = docSnap.data()
      const parentUid = data.parentUid
      if (!parentUid) return

      if (!kidsByParent[parentUid]) kidsByParent[parentUid] = []

      kidsByParent[parentUid].push({
        id: docSnap.id,
        name: data.kid || '(이름 없음)',
        age: calcAge(data.birthYear),
        gender: data.gender || '-',
        heightCm:data.heightCm,
        weightKg:data.weightKg,
        bloodType:data.bloodType,
        allergy:data.allergy,
        medicalHistory:data.medicalHistory,
        medication:data.medication
      })
    })

    usersWithKids.value = usersSnap.docs.map(docSnap => {
      const u = docSnap.data()
      const uid = docSnap.id
      return {
        uid,
        email: u.email || '(이메일 없음)',
        username: u.username || '',
        name: u.name || '(이름 없음)',
        phone: u.phone || '',
        agreeTerms: u.agreeTerms ?? false,
        agreeMarketing: u.agreeMarketing ?? false,
        createdAt: u.createdAt ? new Date(u.createdAt) : null,
        kids: kidsByParent[uid] || []
      }
    })
  } catch (e) {
    console.error('users + kidinformation 조회 실패:', e)
  }
}

// ----------------------
// 🔹 사용자 수정 / 삭제
// ----------------------
const goToUserEdit = (uid) => {
  router.push(`/AdminUserEdit/${uid}`)
}

const deleteUser = async (user) => {
  const ok = confirm(`정말로 ${user.name} 사용자를 삭제하시겠습니까?\n(자녀 정보도 함께 삭제됩니다.)`)
  if (!ok) return

  try {
    const batch = writeBatch(db)

    const userRef = doc(db, 'users', user.uid)
    batch.delete(userRef)

    const kidQ = query(collection(db, 'kidinformation'), where('parentUid', '==', user.uid))
    const kidSnap = await getDocs(kidQ)
    kidSnap.forEach(d => batch.delete(d.ref))

    await batch.commit()
    alert('사용자와 자녀 정보가 삭제되었습니다.')
    await loadUsersWithKids()
  } catch (e) {
    console.error('사용자 삭제 실패:', e)
    alert('삭제 중 오류가 발생했습니다.')
  }
}

// ----------------------
// 🔹 공통 함수
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
// 🔹 Mounted 시 데이터 로드
// ----------------------
onMounted(() => {
  loadUsersWithKids()
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
