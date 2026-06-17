<template>
  <div class="candidate-compare">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>候选人横向对比</span>
          <div class="filter-bar">
            <el-button
              :type="sortType === 'total' ? 'primary' : 'default'"
              @click="handleSortByTotal"
            >
              {{ sortType === 'total' ? '按总分降序' : '按总分排序' }}
            </el-button>
            <el-select
              v-model="sortDimension"
              placeholder="按维度排序"
              clearable
              style="width: 180px;"
              @change="handleSortByDimension"
            >
              <el-option
                v-for="dim in dimensionOptions"
                :key="dim.code"
                :label="dim.name"
                :value="dim.code"
              />
            </el-select>
          </div>
        </div>
      </template>

      <div v-loading="loading">
        <div class="charts-row" v-if="compareList.length > 0">
          <div class="chart-card">
            <div class="chart-title">雷达图 - 各维度加权得分对比</div>
            <v-chart class="chart" :option="radarOption" autoresize />
          </div>
          <div class="chart-card">
            <div class="chart-title">
              <span>柱状图 - </span>
              <el-radio-group v-model="barChartMode" size="small">
                <el-radio-button value="total">加权总分</el-radio-button>
                <el-radio-button value="dimension">各维度对比</el-radio-button>
              </el-radio-group>
            </div>
            <v-chart class="chart" :option="barOption" autoresize />
          </div>
        </div>

        <el-table
          :data="sortedList"
          v-if="compareList.length > 0"
          style="width: 100%; margin-top: 20px;"
          border
        >
          <el-table-column prop="candidateName" label="候选人" width="120" fixed="left">
            <template #default="{ row }">
              <span class="candidate-name">{{ row.candidateName }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="positionName" label="应聘岗位" width="150" />
          <el-table-column
            v-for="dim in dimensionOptions"
            :key="dim.code"
            :prop="`weightedDimensionScores.${dim.code}`"
            :label="dim.name"
            width="110"
            align="center"
          >
            <template #default="{ row }">
              <span>{{ getWeightedScore(row, dim.code) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="totalWeightedScore" label="加权总分" width="110" align="center" fixed="right">
            <template #default="{ row }">
              <span class="total-score">{{ row.totalWeightedScore?.toFixed(2) || '0.00' }}</span>
            </template>
          </el-table-column>
        </el-table>

        <el-empty v-if="!loading && compareList.length === 0" description="暂无对比数据，请从进度看板选择候选人进行对比" />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { getCandidateCompare, getEvaluationTemplate } from '@/api/hr'
import { ElMessage } from 'element-plus'

const route = useRoute()

const loading = ref(false)
const compareList = ref([])
const dimensions = ref([])
const sortType = ref('')
const sortDimension = ref('')
const sortAsc = ref(false)
const barChartMode = ref('total')

const dimensionOptions = computed(() => {
  return dimensions.value.map(d => ({
    code: d.dimensionCode,
    name: d.dimensionName
  }))
})

const sortedList = computed(() => {
  let list = [...compareList.value]
  if (sortType.value === 'total') {
    list.sort((a, b) => {
      const scoreA = a.totalWeightedScore || 0
      const scoreB = b.totalWeightedScore || 0
      return sortAsc.value ? scoreA - scoreB : scoreB - scoreA
    })
  } else if (sortDimension.value) {
    list.sort((a, b) => {
      const scoreA = a.weightedDimensionScores?.[sortDimension.value] || 0
      const scoreB = b.weightedDimensionScores?.[sortDimension.value] || 0
      return sortAsc.value ? scoreA - scoreB : scoreB - scoreA
    })
  }
  return list
})

const getWeightedScore = (row, code) => {
  return row.weightedDimensionScores?.[code]?.toFixed(2) || '0.00'
}

const radarOption = computed(() => {
  const indicators = dimensionOptions.value.map(dim => ({
    name: dim.name,
    max: 100
  }))
  const series = compareList.value.map((candidate, index) => ({
    value: dimensionOptions.value.map(dim =>
      Number((candidate.weightedDimensionScores?.[dim.code] || 0).toFixed(2))
    ),
    name: candidate.candidateName
  }))
  return {
    tooltip: {
      trigger: 'item'
    },
    legend: {
      data: compareList.value.map(c => c.candidateName),
      bottom: 0
    },
    radar: {
      indicator: indicators,
      center: ['50%', '50%'],
      radius: '60%'
    },
    series: [
      {
        type: 'radar',
        data: series
      }
    ]
  }
})

const barOption = computed(() => {
  const candidateNames = compareList.value.map(c => c.candidateName)
  if (barChartMode.value === 'total') {
    return {
      tooltip: {
        trigger: 'axis',
        axisPointer: { type: 'shadow' }
      },
      legend: {
        bottom: 0
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '15%',
        top: '10%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        data: candidateNames
      },
      yAxis: {
        type: 'value',
        name: '加权总分',
        max: 100
      },
      series: [
        {
          name: '加权总分',
          type: 'bar',
          data: compareList.value.map(c => Number((c.totalWeightedScore || 0).toFixed(2))),
          itemStyle: {
            color: '#409eff'
          },
          label: {
            show: true,
            position: 'top'
          }
        }
      ]
    }
  } else {
    const colors = ['#409eff', '#67c23a', '#e6a23c', '#f56c6c', '#909399', '#8e44ad']
    const series = dimensionOptions.value.map((dim, idx) => ({
      name: dim.name,
      type: 'bar',
      data: compareList.value.map(c =>
        Number((c.weightedDimensionScores?.[dim.code] || 0).toFixed(2))
      ),
      itemStyle: {
        color: colors[idx % colors.length]
      }
    }))
    return {
      tooltip: {
        trigger: 'axis',
        axisPointer: { type: 'shadow' }
      },
      legend: {
        bottom: 0
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '20%',
        top: '10%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        data: candidateNames
      },
      yAxis: {
        type: 'value',
        name: '加权得分',
        max: 100
      },
      series: series
    }
  }
})

const handleSortByTotal = () => {
  if (sortType.value === 'total') {
    sortAsc.value = !sortAsc.value
  } else {
    sortType.value = 'total'
    sortDimension.value = ''
    sortAsc.value = false
  }
}

const handleSortByDimension = () => {
  sortType.value = 'dimension'
  sortAsc.value = false
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

const loadCompareData = async (candidateIds) => {
  if (!candidateIds || candidateIds.length === 0) {
    return
  }
  try {
    loading.value = true
    const res = await getCandidateCompare(candidateIds)
    if (res.code === 200) {
      compareList.value = res.data || []
    } else {
      ElMessage.error(res.message || '获取对比数据失败')
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('获取对比数据失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadDimensions()
  const ids = route.query.candidateIds
  if (ids) {
    const candidateIds = Array.isArray(ids) ? ids.map(Number) : ids.split(',').map(Number)
    loadCompareData(candidateIds)
  }
})

watch(() => route.query.candidateIds, (newIds) => {
  if (newIds) {
    const candidateIds = Array.isArray(newIds) ? newIds.map(Number) : newIds.split(',').map(Number)
    loadCompareData(candidateIds)
  }
})
</script>

<style scoped>
.candidate-compare {
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

.charts-row {
  display: flex;
  gap: 20px;
  margin-bottom: 10px;
}

.chart-card {
  flex: 1;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 15px;
  background-color: #fff;
}

.chart-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.chart {
  height: 360px;
  width: 100%;
}

.candidate-name {
  font-weight: 500;
  color: #303133;
}

.total-score {
  font-weight: bold;
  color: #409eff;
  font-size: 14px;
}
</style>
