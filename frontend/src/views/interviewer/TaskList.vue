<template>
  <div class="task-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>待评价列表</span>
          <el-radio-group v-model="statusFilter" size="default" @change="loadTasks">
            <el-radio-button value="">全部</el-radio-button>
            <el-radio-button value="PENDING">待评价</el-radio-button>
            <el-radio-button value="DRAFT">草稿</el-radio-button>
            <el-radio-button value="SUBMITTED">已评价</el-radio-button>
          </el-radio-group>
        </div>
      </template>

      <el-table :data="taskList" v-loading="loading" style="width: 100%">
        <el-table-column prop="candidateName" label="候选人" width="120" />
        <el-table-column prop="gender" label="性别" width="80">
          <template #default="{ row }">
            {{ row.gender || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="age" label="年龄" width="80">
          <template #default="{ row }">
            {{ row.age || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="positionName" label="应聘岗位" width="200" />
        <el-table-column prop="department" label="部门" width="120" />
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
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status !== 'SUBMITTED'"
              type="primary"
              size="small"
              @click="goToEvaluation(row)"
            >
              {{ row.status === 'DRAFT' ? '继续填写' : '去评价' }}
            </el-button>
            <el-button
              v-else
              type="primary"
              size="small"
              plain
              @click="viewEvaluation(row)"
            >
              查看
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && taskList.length === 0" description="暂无任务数据" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getTaskList } from '@/api/interviewer'
import dayjs from 'dayjs'
import { ElMessage } from 'element-plus'

const router = useRouter()

const statusFilter = ref('')
const taskList = ref([])
const loading = ref(false)

const loadTasks = async () => {
  try {
    loading.value = true
    const res = await getTaskList(statusFilter.value)
    if (res.code === 200) {
      taskList.value = res.data || []
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadTasks()
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

const goToEvaluation = (row) => {
  router.push(`/interviewer/evaluation/${row.taskId}`)
}

const viewEvaluation = (row) => {
  router.push(`/interviewer/evaluation/${row.taskId}`)
}
</script>

<style scoped>
.task-list {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
