<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon blue">
              <el-icon><User /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.total }}</div>
              <div class="stat-label">待处理任务</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon green">
              <el-icon><CircleCheck /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.completed }}</div>
              <div class="stat-label">已完成</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon orange">
              <el-icon><Document /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.draft }}</div>
              <div class="stat-label">草稿</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="welcome-card" style="margin-top: 20px;">
      <div class="welcome-content">
        <h2>欢迎回来，{{ userStore.userInfo.realName }}！</h2>
        <p>
          <span v-if="isInterviewer">
            您是面试官角色，可以在"待评价列表"中查看分配给您的面试任务，并对候选人进行评价。
          </span>
          <span v-if="isHr">
            您是HR角色，可以查看评价模板、管理候选人面试进度、查看评价详情和导出报告。
          </span>
        </p>
        <div class="quick-actions">
          <el-button type="primary" v-if="isInterviewer" @click="goToTasks">
            <el-icon><List /></el-icon>
            前往待评价列表
          </el-button>
          <el-button type="primary" v-if="isHr" @click="goToProgress">
            <el-icon><DataAnalysis /></el-icon>
            查看进度看板
          </el-button>
        </div>
      </div>
    </el-card>

    <el-card class="recent-card" style="margin-top: 20px;">
      <template #header>
        <div class="card-header">
          <span>最近任务</span>
        </div>
      </template>
      <el-table :data="recentTasks" v-if="recentTasks.length > 0" style="width: 100%">
        <el-table-column prop="candidateName" label="候选人" width="120" />
        <el-table-column prop="positionName" label="应聘岗位" width="180" />
        <el-table-column prop="interviewType" label="面试类型" width="120" />
        <el-table-column prop="interviewRound" label="轮次" width="80">
          <template #default="{ row }">
            第{{ row.interviewRound }}轮
          </template>
        </el-table-column>
        <el-table-column prop="interviewTime" label="面试时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.interviewTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-else description="暂无任务数据" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { getTaskList } from '@/api/interviewer'
import { getCandidateProgress } from '@/api/hr'
import dayjs from 'dayjs'

const router = useRouter()
const userStore = useUserStore()

const isInterviewer = computed(() => userStore.userInfo.role === 'INTERVIEWER')
const isHr = computed(() => userStore.userInfo.role === 'HR')

const stats = reactive({
  total: 0,
  completed: 0,
  draft: 0
})

const recentTasks = ref([])

const loadInterviewerData = async () => {
  try {
    const allRes = await getTaskList('')
    const allTasks = allRes.data || []
    stats.total = allTasks.length
    stats.completed = allTasks.filter(t => t.status === 'SUBMITTED').length
    stats.draft = allTasks.filter(t => t.status === 'DRAFT').length
    recentTasks.value = allTasks.slice(0, 5)
  } catch (e) {
    console.error(e)
  }
}

const loadHrData = async () => {
  try {
    const res = await getCandidateProgress()
    const candidates = res.data || []
    let totalTasks = 0
    let completed = 0
    let draft = 0
    candidates.forEach(c => {
      totalTasks += c.totalTasks || 0
      completed += c.submittedCount || 0
      draft += c.draftCount || 0
    })
    stats.total = totalTasks
    stats.completed = completed
    stats.draft = draft
  } catch (e) {
    console.error(e)
  }
}

onMounted(() => {
  if (isInterviewer.value) {
    loadInterviewerData()
  } else if (isHr.value) {
    loadHrData()
  }
})

const formatDate = (date) => {
  return date ? dayjs(date).format('YYYY-MM-DD HH:mm') : '-'
}

const getStatusType = (status) => {
  const map = {
    PENDING: 'warning',
    DRAFT: 'info',
    SUBMITTED: 'success'
  }
  return map[status] || ''
}

const getStatusText = (status) => {
  const map = {
    PENDING: '待评价',
    DRAFT: '草稿',
    SUBMITTED: '已评价'
  }
  return map[status] || status
}

const goToTasks = () => {
  router.push('/interviewer/tasks')
}

const goToProgress = () => {
  router.push('/hr/progress')
}
</script>

<style scoped>
.dashboard {
  padding: 0;
}

.stat-card {
  margin-bottom: 20px;
}

.stat-content {
  display: flex;
  align-items: center;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: #fff;
  margin-right: 20px;
}

.stat-icon.blue {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stat-icon.green {
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
}

.stat-icon.orange {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.welcome-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
}

.welcome-card :deep(.el-card__body) {
  padding: 40px;
}

.welcome-content h2 {
  margin: 0 0 10px 0;
  font-size: 24px;
}

.welcome-content p {
  margin: 0 0 20px 0;
  font-size: 14px;
  opacity: 0.9;
}

.quick-actions {
  margin-top: 20px;
}

.card-header {
  font-weight: bold;
  font-size: 16px;
}
</style>
