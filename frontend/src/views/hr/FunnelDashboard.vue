<template>
  <div class="funnel-dashboard">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>招聘漏斗统计</span>
          <div class="filter-bar">
            <el-select
              v-model="selectedPosition"
              placeholder="选择岗位"
              clearable
              style="width: 220px;"
              @change="loadFunnelData"
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

      <div v-loading="loading">
        <div class="funnel-content" v-if="funnelData.length > 0">
          <div class="funnel-chart-wrapper">
            <v-chart class="funnel-chart" :option="funnelOption" autoresize />
          </div>

          <el-table :data="funnelData" style="width: 100%; margin-top: 20px;" border>
            <el-table-column prop="stageName" label="招聘阶段" width="180">
              <template #default="{ row, $index }">
                <div class="stage-cell">
                  <span class="stage-index">{{ $index + 1 }}</span>
                  <span>{{ row.stageName }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="count" label="人数" width="120" align="center">
              <template #default="{ row }">
                <span class="count-text">{{ row.count || 0 }}</span>
              </template>
            </el-table-column>
            <el-table-column label="转化率" width="180" align="center">
              <template #default="{ row, $index }">
                <div v-if="$index === 0" class="conversion-cell">
                  <el-tag type="primary" size="small">100%</el-tag>
                  <span class="conversion-hint">（基准）</span>
                </div>
                <div v-else class="conversion-cell">
                  <el-tag :type="getConversionType(row.conversionRate)" size="small">
                    {{ formatConversionRate(row.conversionRate) }}
                  </el-tag>
                  <span class="conversion-hint">（相对上一阶段）</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="相对首阶段转化率" align="center">
              <template #default="{ row }">
                <el-tag :type="getConversionType(getRelativeRate(row))" size="small">
                  {{ formatConversionRate(getRelativeRate(row)) }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <el-empty v-if="!loading && funnelData.length === 0" description="暂无漏斗数据" />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getPositionList, getFunnelStats } from '@/api/hr'
import { ElMessage } from 'element-plus'

const positions = ref([])
const selectedPosition = ref(null)
const funnelData = ref([])
const loading = ref(false)

const funnelOption = computed(() => {
  const data = funnelData.value.map(item => ({
    value: item.count || 0,
    name: item.stageName
  }))
  return {
    tooltip: {
      trigger: 'item',
      formatter: function(params) {
        const idx = params.dataIndex
        const item = funnelData.value[idx]
        let rateText = ''
        if (idx === 0) {
          rateText = '<br/>转化率：100%'
        } else {
          rateText = `<br/>转化率：${formatConversionRate(item.conversionRate)}`
        }
        return `<strong>${params.name}</strong><br/>人数：${params.value}${rateText}`
      }
    },
    legend: {
      bottom: 0,
      data: funnelData.value.map(item => item.stageName)
    },
    series: [
      {
        name: '招聘漏斗',
        type: 'funnel',
        left: '10%',
        top: 30,
        bottom: 80,
        width: '80%',
        min: 0,
        max: Math.max(...funnelData.value.map(i => i.count || 1)),
        minSize: '20%',
        maxSize: '100%',
        sort: 'descending',
        gap: 2,
        label: {
          show: true,
          position: 'inside',
          formatter: function(params) {
            return `${params.name}\n${params.value}人`
          },
          fontSize: 14,
          fontWeight: 'bold'
        },
        labelLine: {
          length: 10,
          lineStyle: {
            width: 1,
            type: 'solid'
          }
        },
        itemStyle: {
          borderColor: '#fff',
          borderWidth: 1
        },
        emphasis: {
          label: {
            fontSize: 16
          }
        },
        data: data
      }
    ]
  }
})

const formatConversionRate = (rate) => {
  if (rate === null || rate === undefined || isNaN(rate)) return '-'
  return (rate * 100).toFixed(1) + '%'
}

const getConversionType = (rate) => {
  if (rate === null || rate === undefined || isNaN(rate)) return 'info'
  const percent = rate * 100
  if (percent >= 80) return 'success'
  if (percent >= 50) return 'primary'
  if (percent >= 30) return 'warning'
  return 'danger'
}

const getRelativeRate = (row) => {
  const first = funnelData.value[0]?.count || 0
  if (first === 0) return 0
  return (row.count || 0) / first
}

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

const loadFunnelData = async () => {
  try {
    loading.value = true
    const res = await getFunnelStats(selectedPosition.value)
    if (res.code === 200) {
      funnelData.value = res.data || []
    } else {
      ElMessage.error(res.message || '获取漏斗数据失败')
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('获取漏斗数据失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadPositions()
  loadFunnelData()
})
</script>

<style scoped>
.funnel-dashboard {
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

.funnel-content {
  padding: 10px 0;
}

.funnel-chart-wrapper {
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 15px;
  background-color: #fff;
}

.funnel-chart {
  height: 420px;
  width: 100%;
}

.stage-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.stage-index {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  background-color: #409eff;
  color: #fff;
  border-radius: 50%;
  font-size: 12px;
  font-weight: bold;
}

.count-text {
  font-weight: bold;
  font-size: 16px;
  color: #303133;
}

.conversion-cell {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.conversion-hint {
  font-size: 12px;
  color: #909399;
}
</style>
