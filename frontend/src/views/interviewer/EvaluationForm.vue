<template>
  <div class="evaluation-form">
    <el-page-header @back="goBack" :content="pageTitle" style="margin-bottom: 20px;">
      <template #extra>
        <el-tag v-if="isSubmitted" type="success">已提交</el-tag>
        <el-tag v-else-if="isDraft" type="info">草稿</el-tag>
        <el-tag v-else type="warning">待评价</el-tag>
      </template>
    </el-page-header>

    <el-card v-if="candidateInfo" class="candidate-card" shadow="never">
      <div class="candidate-info">
        <div class="info-item">
          <span class="label">候选人：</span>
          <span class="value">{{ candidateInfo.candidateName }}</span>
        </div>
        <div class="info-item">
          <span class="label">应聘岗位：</span>
          <span class="value">{{ candidateInfo.positionName }}</span>
        </div>
        <div class="info-item">
          <span class="label">面试类型：</span>
          <span class="value">{{ candidateInfo.interviewType }}</span>
        </div>
        <div class="info-item">
          <span class="label">面试轮次：</span>
          <span class="value">第{{ candidateInfo.interviewRound }}轮</span>
        </div>
      </div>
    </el-card>

    <div v-for="dim in dimensionScores" :key="dim.dimensionId" class="dimension-card">
      <el-card shadow="never">
        <template #header>
          <div class="dimension-header">
            <span class="dimension-name">{{ dim.dimensionName }}</span>
            <div class="dimension-score">
              <el-rate
                v-model="dim.score"
                :max="dim.maxScore || 5"
                :min="dim.minScore || 1"
                :disabled="isSubmitted"
                show-score
                text-color="#ff9900"
                score-template="{value}分"
              />
            </div>
          </div>
        </template>
        <el-input
          v-model="dim.comment"
          type="textarea"
          :rows="3"
          :disabled="isSubmitted"
          placeholder="请输入该维度的评价说明..."
          maxlength="500"
          show-word-limit
        />
      </el-card>
    </div>

    <el-card class="overall-card" shadow="never">
      <template #header>
        <span class="section-title">综合评价</span>
      </template>
      <el-input
        v-model="overallComment"
        type="textarea"
        :rows="5"
        :disabled="isSubmitted"
        placeholder="请输入对候选人的综合评价..."
        maxlength="2000"
        show-word-limit
      />
    </el-card>

    <el-card class="hire-card" shadow="never">
      <template #header>
        <span class="section-title">录用建议</span>
      </template>
      <el-radio-group v-model="hireSuggestion" :disabled="isSubmitted">
        <el-radio value="STRONG_RECOMMEND">
          <span style="color: #67c23a; font-weight: bold;">强烈推荐</span>
        </el-radio>
        <el-radio value="RECOMMEND">
          <span style="color: #409eff; font-weight: bold;">推荐</span>
        </el-radio>
        <el-radio value="PENDING">
          <span style="color: #e6a23c; font-weight: bold;">待定</span>
        </el-radio>
        <el-radio value="NOT_RECOMMEND">
          <span style="color: #f56c6c; font-weight: bold;">不推荐</span>
        </el-radio>
      </el-radio-group>
    </el-card>

    <div class="action-bar" v-if="!isSubmitted">
      <el-button size="large" @click="handleSaveDraft" :loading="savingDraft">
        保存草稿
      </el-button>
      <el-button type="primary" size="large" @click="handleSubmit" :loading="submitting">
        提交评价
      </el-button>
    </div>

    <div class="action-bar" v-else>
      <el-button size="large" @click="goBack">
        返回列表
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getEvaluationDetail, saveDraft, submitEvaluation } from '@/api/interviewer'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()

const taskId = computed(() => route.params.taskId)
const loading = ref(false)
const savingDraft = ref(false)
const submitting = ref(false)

const candidateInfo = ref(null)
const dimensionScores = ref([])
const overallComment = ref('')
const hireSuggestion = ref('')
const currentStatus = ref('')

const isSubmitted = computed(() => currentStatus.value === 'SUBMITTED')
const isDraft = computed(() => currentStatus.value === 'DRAFT')

const pageTitle = computed(() => {
  if (isSubmitted.value) return '查看评价'
  if (isDraft.value) return '继续填写评价'
  return '填写评价'
})

const loadEvaluationDetail = async () => {
  try {
    loading.value = true
    const res = await getEvaluationDetail(taskId.value)
    if (res.code === 200) {
      const data = res.data
      currentStatus.value = data.status
      candidateInfo.value = data
      dimensionScores.value = data.dimensionScores || []
      overallComment.value = data.overallComment || ''
      hireSuggestion.value = data.hireSuggestion || ''
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadEvaluationDetail()
})

const validateForm = () => {
  for (const dim of dimensionScores.value) {
    if (!dim.score || dim.score < 1) {
      ElMessage.warning(`请为"${dim.dimensionName}"维度打分`)
      return false
    }
  }
  if (!hireSuggestion.value) {
    ElMessage.warning('请选择录用建议')
    return false
  }
  return true
}

const handleSaveDraft = async () => {
  try {
    savingDraft.value = true
    const data = {
      taskId: taskId.value,
      dimensionScores: dimensionScores.value.map(d => ({
        dimensionId: d.dimensionId,
        dimensionCode: d.dimensionCode,
        score: d.score || 0,
        comment: d.comment
      })),
      overallComment: overallComment.value,
      hireSuggestion: hireSuggestion.value
    }
    const res = await saveDraft(data)
    if (res.code === 200) {
      ElMessage.success('草稿保存成功')
      currentStatus.value = 'DRAFT'
    } else {
      ElMessage.error(res.message || '保存失败')
    }
  } catch (e) {
    console.error(e)
  } finally {
    savingDraft.value = false
  }
}

const handleSubmit = async () => {
  if (!validateForm()) return

  try {
    await ElMessageBox.confirm(
      '提交后将无法修改，确定要提交评价吗？',
      '确认提交',
      {
        confirmButtonText: '确定提交',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    submitting.value = true
    const data = {
      taskId: taskId.value,
      dimensionScores: dimensionScores.value.map(d => ({
        dimensionId: d.dimensionId,
        dimensionCode: d.dimensionCode,
        score: d.score,
        comment: d.comment
      })),
      overallComment: overallComment.value,
      hireSuggestion: hireSuggestion.value
    }
    const res = await submitEvaluation(data)
    if (res.code === 200) {
      ElMessage.success('评价提交成功')
      currentStatus.value = 'SUBMITTED'
    } else {
      ElMessage.error(res.message || '提交失败')
    }
  } catch (e) {
    if (e !== 'cancel') {
      console.error(e)
    }
  } finally {
    submitting.value = false
  }
}

const goBack = () => {
  router.push('/interviewer/tasks')
}
</script>

<style scoped>
.evaluation-form {
  max-width: 900px;
  margin: 0 auto;
  padding-bottom: 80px;
}

.candidate-card {
  margin-bottom: 20px;
}

.candidate-info {
  display: flex;
  flex-wrap: wrap;
  gap: 30px;
}

.info-item {
  display: flex;
  align-items: center;
}

.label {
  color: #909399;
  font-size: 14px;
}

.value {
  color: #303133;
  font-size: 14px;
  font-weight: 500;
}

.dimension-card {
  margin-bottom: 16px;
}

.dimension-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.dimension-name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.dimension-score {
  display: flex;
  align-items: center;
}

.overall-card {
  margin-bottom: 16px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.hire-card {
  margin-bottom: 16px;
}

.action-bar {
  position: fixed;
  bottom: 0;
  left: 220px;
  right: 0;
  padding: 16px 40px;
  background: #fff;
  border-top: 1px solid #e6e6e6;
  display: flex;
  justify-content: center;
  gap: 20px;
  z-index: 100;
}

.action-bar .el-button {
  min-width: 150px;
}
</style>
