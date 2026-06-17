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
              @change="handlePositionChange"
            >
              <el-option
                v-for="pos in positions"
                :key="pos.id"
                :label="pos.positionName"
                :value="pos.id"
              />
            </el-select>
            <el-button
              type="primary"
              :disabled="selectedCandidates.length < 2 || selectedCandidates.length > 5"
              @click="handleCompare"
            >
              横向对比 ({{ selectedCandidates.length }})
            </el-button>
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

      <el-table
        :data="candidateList"
        v-loading="loading"
        style="width: 100%"
        @selection-change="handleSelectionChange"
        ref="tableRef"
      >
        <el-table-column type="selection" width="55" align="center" />
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
        <el-table-column label="加权总分" width="110" align="center">
          <template #default="{ row }">
            <span class="weighted-score">
              {{ row.weightedTotalScore != null ? Number(row.weightedTotalScore).toFixed(2) : '-' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="评分分歧" width="90" align="center">
          <template #default="{ row }">
            <el-tooltip
              v-if="row.hasScoreDiscrepancy"
              :content="getDiscrepancyTooltip(row)"
              placement="top"
            >
              <el-icon
                class="discrepancy-icon"
                @click="handleShowDiscrepancy(row)"
              >
                <WarningFilled />
              </el-icon>
            </el-tooltip>
            <span v-else class="no-discrepancy">-</span>
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

    <el-dialog
      v-model="discrepancyDialogVisible"
      title="评分分歧详情"
      width="700px"
      destroy-on-close
    >
      <div v-loading="discrepancyLoading" class="discrepancy-content">
        <div v-if="currentDiscrepancyCandidate" class="discrepancy-header">
          <el-icon color="#f56c6c"><WarningFilled /></el-icon>
          <span class="discrepancy-title">
            候选人「{{ currentDiscrepancyCandidate.candidateName }}」的评分分歧
          </span>
        </div>
        <el-table
          v-if="discrepancyDetail && discrepancyDetail.length > 0"
          :data="discrepancyDetail"
          border
          style="width: 100%; margin-top: 15px;"
        >
          <el-table-column prop="dimensionName" label="维度" width="140" />
          <el-table-column label="评分范围" width="150" align="center">
            <template #default="{ row }">
              <span class="score-range">{{ row.minScore }} ~ {{ row.maxScore }}</span>
            </template>
          </el-table-column>
          <el-table-column label="平均分" width="100" align="center">
            <template #default="{ row }">
              <span class="avg-score">{{ row.avgScore?.toFixed(2) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="scoreDiff" label="分差" width="100" align="center">
            <template #default="{ row }">
              <el-tag type="danger" size="small">{{ row.scoreDiff?.toFixed(2) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="各面试官评分">
            <template #default="{ row }">
              <div class="interviewer-scores">
                <span
                  v-for="(s, idx) in row.interviewerScores"
                  :key="idx"
                  class="score-tag"
                >
                  {{ s.interviewerName }}: {{ s.score }}
                </span>
              </div>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-else-if="!discrepancyLoading" description="暂无分歧数据" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getPositionList, getCandidateProgress, getCandidateDiscrepancy } from '@/api/hr'
import { ElMessage } from 'element-plus'
import { WarningFilled } from '@element-plus/icons-vue'

const router = useRouter()

const positions = ref([])
const candidateList = ref([])
const selectedPosition = ref(null)
const loading = ref(false)
const selectedCandidates = ref([])
const tableRef = ref(null)

const discrepancyDialogVisible = ref(false)
const discrepancyLoading = ref(false)
const discrepancyDetail = ref([])
const currentDiscrepancyCandidate = ref(null)

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

const handlePositionChange = () => {
  selectedCandidates.value = []
  if (tableRef.value) {
    tableRef.value.clearSelection()
  }
  loadCandidates()
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

const handleSelectionChange = (selection) => {
  selectedCandidates.value = selection
}

const handleCompare = () => {
  if (selectedCandidates.value.length < 2) {
    ElMessage.warning('请至少选择 2 位候选人')
    return
  }
  if (selectedCandidates.value.length > 5) {
    ElMessage.warning('最多只能选择 5 位候选人')
    return
  }
  const ids = selectedCandidates.value.map(c => c.candidateId).join(',')
  router.push({ path: '/hr/compare', query: { candidateIds: ids } })
}

const getDiscrepancyTooltip = (row) => {
  if (row.discrepancyDimensions && row.discrepancyDimensions.length > 0) {
    return `分歧维度：${row.discrepancyDimensions.join('、')}`
  }
  return '存在评分分歧，点击查看详情'
}

const handleShowDiscrepancy = async (row) => {
  currentDiscrepancyCandidate.value = row
  discrepancyDialogVisible.value = true
  discrepancyDetail.value = []
  try {
    discrepancyLoading.value = true
    const res = await getCandidateDiscrepancy(row.candidateId)
    if (res.code === 200) {
      discrepancyDetail.value = res.data || []
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('获取分歧详情失败')
  } finally {
    discrepancyLoading.value = false
  }
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
  align-items: center;
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

.weighted-score {
  font-weight: 600;
  color: #409eff;
  font-size: 15px;
}

.discrepancy-icon {
  color: #f56c6c;
  font-size: 18px;
  cursor: pointer;
  transition: transform 0.2s;
}

.discrepancy-icon:hover {
  transform: scale(1.2);
}

.no-discrepancy {
  color: #c0c4cc;
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

.discrepancy-content {
  min-height: 100px;
}

.discrepancy-header {
  display: flex;
  align-items: center;
  gap: 8px;
}

.discrepancy-title {
  font-weight: 600;
  color: #303133;
  font-size: 15px;
}

.score-range {
  font-family: 'Courier New', monospace;
  color: #606266;
}

.avg-score {
  font-weight: 600;
  color: #409eff;
}

.interviewer-scores {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.score-tag {
  padding: 3px 8px;
  background-color: #f5f7fa;
  border-radius: 4px;
  font-size: 12px;
  color: #606266;
}
</style>
