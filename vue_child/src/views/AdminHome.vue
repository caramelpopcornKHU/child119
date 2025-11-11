<template>
  <div class="admin-layout bg-light min-vh-100">
    <!-- 고정 헤더 (생략되어 있음) -->

    <!-- 메인 영역 -->
    <main class="admin-main container">
      <!-- 상단 타이틀 & 설명 -->
      <div class="d-flex flex-column flex-md-row align-items-md-center justify-content-between mb-3">
        <div>
          <h3 class="fw-bold mb-1">관리자 대시보드</h3>
          <p class="text-muted mb-0 small">
            아이119 앱의 크롤링 설정, 사용자/콘텐츠 데이터, 자동화 로그, 건의사항을 한 곳에서 관리합니다.
          </p>
        </div>
      </div>

      <!-- 상단 탭 -->
      <ul class="nav nav-pills nav-fill admin-tabs mb-4">
        <li class="nav-item">
          <button
            class="nav-link"
            :class="{ active: activeTab === 'manage' }"
            @click="activeTab = 'manage'"
          >
            관리
          </button>
        </li>
        <li class="nav-item">
          <button
            class="nav-link"
            :class="{ active: activeTab === 'automation' }"
            @click="activeTab = 'automation'"
          >
            자동화 로그
          </button>
        </li>
        <li class="nav-item">
          <button
            class="nav-link"
            :class="{ active: activeTab === 'feedback' }"
            @click="activeTab = 'feedback'"
          >
            건의사항
          </button>
        </li>
      </ul>

      <!-- ░░░ 관리 탭 ░░░ -->
      <section v-if="activeTab === 'manage'" class="d-flex flex-column gap-3">
        <!-- 크롤링 주기 설정 -->
        <div class="card shadow-sm border-0 rounded-4">
          <div class="card-body">
            <div class="d-flex justify-content-between align-items-center mb-2">
              <h5 class="fw-semibold mb-0">크롤링 주기 설정</h5>
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
                  class="btn btn-success btn-sm rounded-pill"
                  @click="saveCrawlingInterval"
                >
                  저장
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

        <!-- users + kidinformation 정보 -->
        <div class="card shadow-sm border-0 rounded-4">
          <div class="card-body">
            <div class="d-flex justify-content-between align-items-center mb-3">
              <h5 class="fw-semibold mb-0">사용자 정보 (users + kidinformation)</h5>
              <span class="badge bg-secondary-subtle text-dark small">
                총 {{ usersWithKids.length }}명
              </span>
            </div>

            <div class="table-responsive">
              <!-- 원래 코드에 table 태그가 없었는데, 기존 구조 유지 -->
              <thead class="table-light">
                <tr>
                  <th style="width: 4%;">No</th>
                  <th style="width: 18%;">이메일</th>
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
                  <td>{{ u.email }}</td>
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
            </div>

            <p class="small text-muted mt-2 mb-0">
              ※ 실제 구현 시, Firestore의 <code>users</code> 컬렉션과
              <code>kidinformation</code> 컬렉션을 join해서 가져오면 됩니다.
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
      </section>

      <!-- ░░░ 자동화 로그 탭 ░░░ -->
      <section v-else-if="activeTab === 'automation'" class="d-flex flex-column gap-3">
        <div class="card shadow-sm border-0 rounded-4">
          <div class="card-body">
            <div class="d-flex justify-content-between align-items-center mb-2">
              <h5 class="fw-semibold mb-0">데이터 크롤링 자동화</h5>
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
      </section>

      <!-- ░░░ 건의사항 탭 ░░░ -->
      <section v-else-if="activeTab === 'feedback'" class="d-flex flex-column gap-3">
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
                  <tbody> <!-- 추가한 코드 행응 선택하면 건의 사항의 아이디에 따라서 페이지가 이동하고 그걸 파이어베이스에 전달하여 호춣-->
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
                    <tr v-for="(f, idx) in emergencyFeedbacks" :key="f.id"  @click="goToSuggestionDetail(f.id)">
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
      </section>

      <!-- ✅ 자녀 상세정보 모달 -->
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
import { ref, onMounted, onBeforeUnmount, computed } from 'vue'
import { useRouter } from 'vue-router'

// ✅ Firebase Auth
import { onAuthStateChanged, signOut } from 'firebase/auth'
import { auth } from '@/firebase'

// ✅ Firestore
import { db } from '@/firebase'
import {
  collection,
  getDocs,
  doc,
  orderBy,
  deleteDoc,
  query,
  where,
  writeBatch
} from 'firebase/firestore'

const router = useRouter()
const kidModalUser = ref(null)

const openKidModal = (user) => {
  kidModalUser.value = user
}

const closeKidModal = () => {
  kidModalUser.value = null
}

const goToAdminEmergencyEdit = (id) => {
  router.push(`/AdminEmergencyEdit/${id}`)
}
const deleteEmergency = async (item) => {
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

// ----------------------
// 로그인 버튼 상태
// ----------------------
const status = ref('로그인')

onMounted(() => {
  onAuthStateChanged(auth, (user) => {
    if (user) {
      status.value = '로그아웃'
    } else {
      status.value = '로그인'
    }
  })
})

const goToLogin = () => {
  router.push('/login')
}

const goToUserEdit = (uid) => {
  router.push(`/AdminUserEdit/${uid}`)
}
const goToEmergencyDetail = (id) => {
  router.push({ name: 'emergencyorder', params: { id } })
}

const logout = async () => {
  try {
    await signOut(auth)
    alert('로그아웃 되었습니다.')
  } catch (err) {
    console.error('로그아웃 중 오류 발생:', err)
    alert('로그아웃 중 오류가 발생했습니다.')
  }
}

const logAction = () => {
  if (status.value === '로그인') {
    goToLogin()
  } else {
    logout()
  }
}

const deleteUser = async (user) => {
  const ok = confirm(`정말로 ${user.email} 사용자를 삭제하시겠습니까?\n(자녀 정보도 함께 삭제됩니다.)`)
  if (!ok) return

  try {
    const batch = writeBatch(db)

    const userRef = doc(db, 'users', user.uid)
    batch.delete(userRef)

    const kidQ = query(
      collection(db, 'kidinformation'),
      where('parentUid', '==', user.uid)
    )
    const kidSnap = await getDocs(kidQ)
    kidSnap.forEach(d => {
      batch.delete(d.ref)
    })

    await batch.commit()

    alert('사용자와 자녀 정보가 삭제되었습니다.')
    await loadUsersWithKids()
  } catch (e) {
    console.error('사용자 삭제 실패:', e)
    alert('삭제 중 오류가 발생했습니다. Firestore rules도 확인해주세요.')
  }
}

// ----------------------
// 상단 탭 상태
// ----------------------
const activeTab = ref('manage') // 'manage' | 'automation' | 'feedback'

// ----------------------
// 크롤링 주기 설정
// ----------------------
const crawlingOptions = [5, 10, 15, 30, 60]
const crawlingInterval = ref(10)
const lastSavedCrawling = ref('')

function saveCrawlingInterval() {
  // TODO: Firestore나 서버에 설정값 저장
  lastSavedCrawling.value = new Date().toLocaleString('ko-KR')
  alert(`크롤링 주기가 ${crawlingInterval.value}분으로 저장되었습니다.`)
}

// ----------------------
// users + kidinformation
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

      if (!kidsByParent[parentUid]) {
        kidsByParent[parentUid] = []
      }

      kidsByParent[parentUid].push({
        id: docSnap.id,
        name: data.kid || '(이름 없음)',
        age: calcAge(data.birthYear),
        gender: data.gender || '-',
        heightCm: data.heightCm ?? null,
        weightKg: data.weightKg ?? null,
        bloodType: data.bloodType || '',
        allergy: data.allergy || '',
        medicalHistory: data.medicalHistory || '',
        medication: data.medication || '',
        raw: data
      })
    })

    usersWithKids.value = usersSnap.docs.map(docSnap => {
      const u = docSnap.data()
      const uid = docSnap.id

      return {
        uid,
        email: u.email || u.userEmail || '(이메일 없음)',
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
async function loadSuggestions() { //사용자가 작성한  건의사항 로딩하는 메소
  try {
    // Firebase의 suggestions 컬렉션에서 데이터 가져오기
    // 최신 순으로 정렬 (createdAt 내림차순)
    const q = query(
      collection(db, 'suggestions'),
      orderBy('createdAt', 'desc')
    )
    const snapshot = await getDocs(q)

    // 가져온 데이터를 emergencyFeedbacks 배열에 저장
    emergencyFeedbacks.value = snapshot.docs.map(docSnap => {
      const data = docSnap.data()
      return {
        id: docSnap.id, // 문서 ID
        emergencyTitle: data.title || '(제목 없음)', // 건의사항 제목
        content: data.content || '(내용 없음)', // 건의사항 내용
        writer: data.fromEmail || '(작성자 없음)', // 작성자 이메일
        createdAt: data.createdAt ? data.createdAt.toDate() : new Date(), // 작성일
        status: data.status || '접수', // 상태
        adminMemo: data.adminMemo || '', // 관리자 메모
        targetRole: data.targetRole || '', // 대상 역할
        fromUid: data.fromUid || '' // 작성자 UID
      }
    })

    console.log('건의사항 로딩 완료:', emergencyFeedbacks.value.length + '건')
  } catch (e) {
    console.error('suggestions 조회 실패:', e)
    alert('건의사항을 불러오는 중 오류가 발생했습니다.')
  }
}
// ----------------------
// emergencyData
// ----------------------
const emergencyList = ref([])

async function loadEmergencyList() {
  try {
    const q = query(
      collection(db, 'emergencyData'),
      orderBy('createdAt', 'desc')
    )
    const snap = await getDocs(q)

    emergencyList.value = snap.docs.map(d => {
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
        raw: data
      }
    })
  } catch (e) {
    console.error('emergencyData 로딩 실패:', e)
  }
}

// ----------------------
// 건의사항
// ----------------------
const feedbackTab = ref('app')

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
    content: '동영상 속도가 빨라서, 좀 더 천천히 설명된 버전이 있으면 좋겠어요.',
    writer: 'parent04@example.com',
    createdAt: new Date()
  }
])

// ----------------------
// 자동화 실행 + 로그 (FastAPI SSE)
// ----------------------
const AUTOMATION_BASE_URL =
  import.meta.env.VITE_AUTOMATION_BASE_URL || 'http://localhost:8000'

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
    const res = await fetch(`${AUTOMATION_BASE_URL}/run`, {
      method: 'POST'
    })
    if (!res.ok) {
      throw new Error(`실행 요청 실패 (status: ${res.status})`)
    }
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

  if (automationEventSource) {
    automationEventSource.close()
  }

  automationEventSource = new EventSource(
    `${AUTOMATION_BASE_URL}/logs/${automationJobId.value}`
  )

  automationEventSource.onmessage = (event) => {
    automationLogs.value.push(event.data + '\n')
  }

  automationEventSource.onerror = () => {
    // 서버가 닫히면 여기로 한 번 들어온 뒤 연결 종료
    if (automationEventSource) {
      automationEventSource.close()
      automationEventSource = null
    }
    if (automationStatus.value === 'running') {
      // 상태를 강제로 완료로 전환 (서버 쪽에서 done 처리 후 스트림 종료했다고 가정)
      automationStatus.value = 'done'
    }
  }
}

onBeforeUnmount(() => {
  if (automationEventSource) {
    automationEventSource.close()
  }
})

// ----------------------
// 공통 함수
// ----------------------
function goToAdminEmergency() {
  router.push('/AdminEmergency')
}
function goToSuggestionDetail(suggestionId) { //상세 페이지로 가는 메소드 라우트 설정 했습니다
  router.push(`/AdminEmergencysuggestionsdetail/${suggestionId}`)
}

function formatDate(date) {
  if (!date) return ''
  return new Date(date).toLocaleDateString('ko-KR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  })
}

onMounted(() => {
  loadUsersWithKids()
  loadEmergencyList()
  loadSuggestions()
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

/* 상단 탭 */
.admin-tabs .nav-link {
  border-radius: 999px;
  font-weight: 600;
  color: #555;
}
.admin-tabs .nav-link.active {
  background-color: #0d6efd;
  color: #fff;
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
