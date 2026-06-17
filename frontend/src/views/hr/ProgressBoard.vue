<template>
  <div class="progress-board">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>评价进度看板</span>
          <div class="filter-bar">
            <el-select
              v-model="selectedPosition"
              placeholder="选择岗位"
              clearable
              style="width: 200px;"
              @change="loadCandidates"
            >
              <el-option
                v-for="pos in positions"
                :key="pos.id"
                :label="pos.positionName"
                :value="pos.id"
              />
            </el-select>
          </div>
        </div>
      </template>

      <div class="stats-row">
        <el-statistic title="总候选人数" :value="stats.totalCandidates" />
        <el-statistic title="全部完成" :value="stats.allCompleted">
          <template #suffix>
            <span style="font-size: 14px;">人</span>
          </template>
        </el-statistic>
        <el-statistic title="进行中" :value="stats.inProgress">
          <template #suffix>
            <span style="font-size: 14px;">人</span>
          </template>
        </el-statistic>
        <el-statistic title="待评价" :value="stats.pending">
          <template #suffix>
            <span style="font-size: 14px;">人</span>
          </template>
        </el-statistic>
      </div>

      <el-table :data="candidateList" v-loading="loading" style="width: 100%">
        <el-table-column prop="candidateName" label="候选人" width="120">
          <template #default="{ row }">
            <span class="candidate-name">{{ row.candidateName }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="positionName" label="应聘岗位" width="180" />
        <el-table-column label="基本信息" width="200">
          <template #default="{ row }">
            <span>{{ row.gender || '-' }} / {{ row.age || '-' }}岁 / {{ row.education || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="workYears" label="工作年限" width="100">
          <template #default="{ row }">
            {{ row.workYears ? row.workYears + '年' : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="评价进度" min-width="250">
          <template #default="{ row }">
            <div class="progress-wrapper">
              <el-progress
                :percentage="calculateProgress(row)"
                :status="getProgressStatus(row)"
                :stroke-width="12"
              />
              <span class="progress-text">
                {{ row.submittedCount || 0 }}/{{ row.totalTasks || 0 }} 已评价
              </span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="整体状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.overallStatus)" size="small">
              {{ getStatusText(row.overallStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              size="small"
              link
              @click="viewDetail(row)"
            >
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && candidateList.length === 0" description="暂无候选人数据" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getPositionList, getCandidateProgress } from '@/api/hr'
import { ElMessage } from 'element-plus'

const router = useRouter()

const positions = ref([])
const candidateList = ref([])
const selectedPosition = ref(null)
const loading = ref(false)

const stats = reactive({
  totalCandidates: 0,
  allCompleted: 0,
  inProgress: 0,
  pending: 0
})

const loadPositions = async () => {
  try {
    const res = await getPositionList()
    if (res.code === 200) {
      positions.value = res.data || []
    }
  } catch (e) {
    console.error(e)
  }
}

const loadCandidates = async () => {
  try {
    loading.value = true
    const res = await getCandidateProgress(selectedPosition.value)
    if (res.code === 200) {
      candidateList.value = res.data || []
      calculateStats()
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const calculateStats = () => {
  const list = candidateList.value
  stats.totalCandidates = list.length
  stats.allCompleted = list.filter(c => c.overallStatus === 'ALL_COMPLETED').length
  stats.inProgress = list.filter(c => c.overallStatus === 'PARTIAL').length
  stats.pending = list.filter(c => c.overallStatus === 'PENDING').length
}

const calculateProgress = (row) => {
  if (!row.totalTasks) return 0
  return Math.round(((row.submittedCount || 0) / row.totalTasks) * 100)
}

const getProgressStatus = (row) => {
  if (row.overallStatus === 'ALL_COMPLETED') return 'success'
  if (row.overallStatus === 'PARTIAL') return ''
  return 'warning'
}

const getStatusType = (status) => {
  const map = {
    PENDING: 'warning',
    PARTIAL: 'primary',
    ALL_COMPLETED: 'success'
  }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = {
    PENDING: '待评价',
    PARTIAL: '部分提交',
    ALL_COMPLETED: '全部完成'
  }
  return map[status] || status
}

const viewDetail = (row) => {
  router.push(`/hr/candidate/${row.candidateId}/evaluation`)
}

onMounted(() => {
  loadPositions()
  loadCandidates()
})
</script>

<style scoped>
.progress-board {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
  font-size: 16px;
}

.filter-bar {
  display: flex;
  gap: 10px;
}

.stats-row {
  display: flex;
  gap: 40px;
  padding: 20px 0;
  margin-bottom: 20px;
  border-bottom: 1px solid #ebeef5;
}

.candidate-name {
  font-weight: 500;
  color: #303133;
}

.progress-wrapper {
  display: flex;
  align-items: center;
  gap: 15px;
}

.progress-wrapper .el-progress {
  flex: 1;
  min-width: 150px;
}

.progress-text {
  font-size: 12px;
  color: #909399;
  white-space: nowrap;
}
</style>
