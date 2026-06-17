<template>
  <div class="candidate-evaluation">
    <el-page-header @back="goBack" content="评价详情" style="margin-bottom: 20px;">
      <template #extra>
        <el-button type="primary" @click="handleExport" :loading="exporting">
          <el-icon><Download /></el-icon>
          导出CSV报告
        </el-button>
      </template>
    </el-page-header>

    <el-card v-if="summary" class="summary-card">
      <div class="summary-header">
        <div class="candidate-info">
          <h2>{{ summary.candidateName }}</h2>
          <p>应聘岗位：{{ summary.positionName }}</p>
        </div>
        <div class="score-group">
          <div class="overall-score">
            <div class="score-number">{{ summary.totalAverage?.toFixed(2) || '0.00' }}</div>
            <div class="score-label">总平均分</div>
            <div class="score-count">{{ summary.evaluationCount }}位面试官评价</div>
          </div>
          <div class="overall-score weighted" v-if="summary.weightedTotalScore != null">
            <div class="score-number">{{ summary.weightedTotalScore?.toFixed(2) || '0.00' }}</div>
            <div class="score-label">加权总分</div>
            <div class="score-count">按模板权重计算</div>
          </div>
        </div>
      </div>
    </el-card>

    <el-card v-if="summary && summary.weightedDimensionScores" class="dimensions-card">
      <template #header>
        <span class="section-title">各维度加权得分</span>
      </template>
      <div class="dimension-averages">
        <div
          v-for="(score, code) in summary.weightedDimensionScores"
          :key="code"
          class="dim-average-item"
        >
          <div class="dim-name">{{ getDimensionName(code) }}</div>
          <el-progress
            type="dashboard"
            :percentage="Math.min(100, (score / 5) * 100)"
            :format="() => score?.toFixed(2) || '0'"
            :width="100"
            :stroke-width="8"
            color="#67c23a"
          />
        </div>
      </div>
    </el-card>

    <el-card v-if="summary" class="dimensions-card">
      <template #header>
        <span class="section-title">各维度平均分</span>
      </template>
      <div class="dimension-averages">
        <div
          v-for="(avg, code) in summary.dimensionAverages"
          :key="code"
          class="dim-average-item"
        >
          <div class="dim-name">{{ getDimensionName(code) }}</div>
          <el-progress
            type="dashboard"
            :percentage="calculatePercentage(avg)"
            :format="() => avg?.toFixed(1) || '0'"
            :width="100"
            :stroke-width="8"
          />
        </div>
      </div>
    </el-card>

    <div v-if="summary && summary.interviewerEvaluations" class="evaluations-section">
      <h3 class="section-title">各面试官评价</h3>
      <div
        v-for="(evalItem, index) in summary.interviewerEvaluations"
        :key="evalItem.taskId"
        class="evaluation-card"
      >
        <el-card shadow="never">
          <template #header>
            <div class="eval-header">
              <div class="eval-info">
                <el-avatar :size="40" style="background-color: #409eff;">
                  {{ evalItem.interviewerName?.charAt(0) }}
                </el-avatar>
                <div class="eval-meta">
                  <div class="interviewer-name">{{ evalItem.interviewerName }}</div>
                  <div class="eval-sub">
                    <span>第{{ evalItem.interviewRound }}轮</span>
                    <span class="dot">·</span>
                    <span>{{ evalItem.interviewType || '面试' }}</span>
                    <span class="dot">·</span>
                    <span>{{ formatDate(evalItem.submitTime) }}</span>
                  </div>
                </div>
              </div>
              <div class="eval-score">
                <span class="score-value">{{ evalItem.totalScore?.toFixed(1) || '-' }}</span>
                <span class="score-label">分</span>
              </div>
            </div>
          </template>

          <div class="dim-scores">
            <div
              v-for="dimScore in evalItem.dimensionScores"
              :key="dimScore.dimensionId"
              class="dim-score-row"
            >
              <span class="dim-name">{{ dimScore.dimensionName }}</span>
              <el-rate
                :value="dimScore.score"
                :max="dimScore.maxScore || 5"
                disabled
                show-score
                text-color="#ff9900"
                score-template="{value}分"
              />
            </div>
          </div>

          <el-divider />

          <div class="eval-comments">
            <div v-for="dimScore in evalItem.dimensionScores" :key="dimScore.dimensionId" class="comment-item">
              <div class="comment-label">{{ dimScore.dimensionName }}评语：</div>
              <div class="comment-content">
                {{ dimScore.comment || '暂无评语' }}
              </div>
            </div>
          </div>

          <el-divider />

          <div class="eval-summary">
            <div class="summary-row">
              <span class="label">综合评价：</span>
              <span class="value">{{ evalItem.overallComment || '暂无' }}</span>
            </div>
            <div class="summary-row">
              <span class="label">录用建议：</span>
              <el-tag :type="getHireSuggestionType(evalItem.hireSuggestion)">
                {{ getHireSuggestionText(evalItem.hireSuggestion) }}
              </el-tag>
            </div>
          </div>
        </el-card>
      </div>
    </div>

    <el-empty v-if="summary && summary.evaluationCount === 0" description="暂无评价数据" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getCandidateEvaluation, exportCsv } from '@/api/hr'
import { getEvaluationTemplate } from '@/api/hr'
import dayjs from 'dayjs'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()

const candidateId = ref(route.params.candidateId)
const summary = ref(null)
const dimensions = ref([])
const loading = ref(false)
const exporting = ref(false)

const loadEvaluation = async () => {
  try {
    loading.value = true
    const res = await getCandidateEvaluation(candidateId.value)
    if (res.code === 200) {
      summary.value = res.data
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const loadDimensions = async () => {
  try {
    const res = await getEvaluationTemplate()
    if (res.code === 200) {
      dimensions.value = res.data || []
    }
  } catch (e) {
    console.error(e)
  }
}

const getDimensionName = (code) => {
  const dim = dimensions.value.find(d => d.dimensionCode === code)
  return dim ? dim.dimensionName : code
}

const calculatePercentage = (score) => {
  if (!score) return 0
  return Math.round((score / 5) * 100)
}

const getHireSuggestionType = (suggestion) => {
  const map = {
    STRONG_RECOMMEND: 'success',
    RECOMMEND: 'primary',
    PENDING: 'warning',
    NOT_RECOMMEND: 'danger'
  }
  return map[suggestion] || 'info'
}

const getHireSuggestionText = (suggestion) => {
  const map = {
    STRONG_RECOMMEND: '强烈推荐',
    RECOMMEND: '推荐',
    PENDING: '待定',
    NOT_RECOMMEND: '不推荐'
  }
  return map[suggestion] || suggestion || '未填写'
}

const formatDate = (date) => {
  return date ? dayjs(date).format('YYYY-MM-DD HH:mm') : '-'
}

const handleExport = async () => {
  try {
    exporting.value = true
    const res = await exportCsv(candidateId.value)
    const blob = new Blob([res], { type: 'text/csv;charset=utf-8;' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', `候选人评价报告_${candidateId.value}.csv`)
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (e) {
    console.error(e)
    ElMessage.error('导出失败')
  } finally {
    exporting.value = false
  }
}

const goBack = () => {
  router.push('/hr/progress')
}

onMounted(() => {
  loadDimensions()
  loadEvaluation()
})
</script>

<style scoped>
.candidate-evaluation {
  max-width: 1000px;
  margin: 0 auto;
  padding-bottom: 40px;
}

.summary-card {
  margin-bottom: 20px;
}

.summary-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.candidate-info h2 {
  margin: 0 0 8px 0;
  font-size: 24px;
  color: #303133;
}

.candidate-info p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.overall-score {
  text-align: center;
  padding: 0 30px;
}

.score-group {
  display: flex;
  gap: 30px;
}

.overall-score.weighted .score-number {
  color: #67c23a;
}

.score-number {
  font-size: 48px;
  font-weight: bold;
  color: #409eff;
  line-height: 1;
}

.score-label {
  font-size: 14px;
  color: #909399;
  margin-top: 5px;
}

.score-count {
  font-size: 12px;
  color: #c0c4cc;
  margin-top: 3px;
}

.dimensions-card {
  margin-bottom: 20px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.dimension-averages {
  display: flex;
  justify-content: space-around;
  flex-wrap: wrap;
  gap: 20px;
}

.dim-average-item {
  text-align: center;
  min-width: 120px;
}

.dim-name {
  font-size: 14px;
  color: #606266;
  margin-bottom: 10px;
}

.evaluations-section {
  margin-top: 20px;
}

.evaluations-section .section-title {
  margin-bottom: 15px;
  padding-left: 10px;
  border-left: 4px solid #409eff;
}

.evaluation-card {
  margin-bottom: 20px;
}

.eval-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.eval-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.eval-meta {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.interviewer-name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.eval-sub {
  font-size: 13px;
  color: #909399;
  display: flex;
  align-items: center;
  gap: 5px;
}

.eval-sub .dot {
  color: #dcdfe6;
}

.eval-score {
  text-align: right;
}

.score-value {
  font-size: 28px;
  font-weight: bold;
  color: #409eff;
}

.score-label {
  font-size: 14px;
  color: #909399;
  margin-left: 2px;
}

.dim-scores {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.dim-score-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.dim-score-row .dim-name {
  font-size: 14px;
  color: #606266;
}

.eval-comments {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.comment-item {
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.comment-label {
  font-size: 13px;
  color: #909399;
  margin-bottom: 6px;
}

.comment-content {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
}

.eval-summary {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.summary-row {
  display: flex;
  align-items: flex-start;
  gap: 10px;
}

.summary-row .label {
  font-size: 14px;
  color: #909399;
  flex-shrink: 0;
}

.summary-row .value {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
  flex: 1;
}
</style>
