<template>
  <div class="admin-section px-4 pb-4">
    <h4 class="fw-bold mb-3">👥 사용자 정보 (users + kidinformation)</h4>

    <div class="card shadow-sm border-0 rounded-4">
      <div class="card-body">
        <div class="d-flex justify-content-between align-items-center mb-3">
          <h5 class="fw-semibold mb-0">사용자 목록</h5>
          <span class="badge bg-secondary-subtle text-dark small">
            총 {{ usersWithKids.length }}명
          </span>
        </div>

        <div class="table-responsive">
          <table class="table table-sm align-middle mb-0">
            <thead class="table-light">
              <tr>
                <th>No</th>
                <th>이메일</th>
                <th>이름</th>
                <th>전화번호</th>
                <th>자녀 수</th>
                <th>가입일</th>
                <th>관리</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(u, idx) in usersWithKids" :key="u.uid">
                <td>{{ idx + 1 }}</td>
                <td>{{ u.email }}</td>
                <td>{{ u.name }}</td>
                <td>{{ u.phone || '-' }}</td>
                <td>{{ u.kids?.length || 0 }}</td>
                <td>{{ formatDate(u.createdAt) }}</td>
                <td>
                  <button class="btn btn-outline-primary btn-sm" @click="goToUserEdit(u.uid)">수정</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "AdminManageUser",
  data() {
    return {
      usersWithKids: []
    }
  },
  methods: {
    formatDate(d) {
      return new Date(d).toLocaleDateString()
    },
    goToUserEdit(uid) {
      this.$router.push(`/AdminUserEdit/${uid}`)
    }
  }
}
</script>
