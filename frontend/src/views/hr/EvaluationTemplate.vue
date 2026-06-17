<template>
  <div class="evaluation-template">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>评价模板</span>
          <el-tag type="success">系统通用模板</el-tag>
        </div>
      </template>

      <div class="template-desc">
        <el-alert
          title="系统内置通用评价模板，包含5个核心评分维度，HR可直接使用，暂不支持自定义编辑"
          type="info"
          :closable="false"
          show-icon
        />
      </div>

      <div class="dimension-list">
        <div
          v-for="(dim, index) in dimensions"
          :key="dim.id"
          class="dimension-item"
        >
          <div class="dimension-header">
            <div class="dimension-number">{{ index + 1 }}</div>
            <div class="dimension-title">
              <h3>{{ dim.dimensionName }}</h3>
              <el-tag size="small">{{ dim.minScore }}-{{ dim.maxScore }}分</el-tag>
            </div>
          </div>
          <div class="dimension-desc">
            {{ dim.dimensionDesc }}
          </div>
          <div class="dimension-score-preview">
            <el-rate
              :value="dim.maxScore"
              :max="dim.maxScore"
              disabled
              show-score
              text-color="#ff9900"
              score-template="满分{value}分"
            />
          </div>
        </div>
      </div>

      <el-divider />

      <div class="additional-section">
        <h3>附加评价项</h3>
        <div class="section-item">
          <el-icon><Document /></el-icon>
          <span>综合评价：大段文本域，填写候选人的整体表现总结</span>
        </div>
        <div class="section-item">
          <el-icon><Star /></el-icon>
          <span>录用建议：单选（强烈推荐 / 推荐 / 待定 / 不推荐）</span>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getEvaluationTemplate } from '@/api/hr'

const dimensions = ref([])

const loadTemplate = async () => {
  try {
    const res = await getEvaluationTemplate()
    if (res.code === 200) {
      dimensions.value = res.data || []
    }
  } catch (e) {
    console.error(e)
  }
}

onMounted(() => {
  loadTemplate()
})
</script>

<style scoped>
.evaluation-template {
  max-width: 900px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
  font-size: 16px;
}

.template-desc {
  margin-bottom: 30px;
}

.dimension-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.dimension-item {
  padding: 20px;
  background-color: #f5f7fa;
  border-radius: 8px;
  border-left: 4px solid #409eff;
}

.dimension-header {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 12px;
}

.dimension-number {
  width: 36px;
  height: 36px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 16px;
}

.dimension-title {
  display: flex;
  align-items: center;
  gap: 10px;
}

.dimension-title h3 {
  margin: 0;
  font-size: 18px;
  color: #303133;
}

.dimension-desc {
  color: #606266;
  font-size: 14px;
  margin-bottom: 12px;
  margin-left: 51px;
}

.dimension-score-preview {
  margin-left: 51px;
}

.additional-section {
  padding: 0 10px;
}

.additional-section h3 {
  font-size: 16px;
  color: #303133;
  margin-bottom: 15px;
}

.section-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 0;
  color: #606266;
  font-size: 14px;
}

.section-item .el-icon {
  color: #409eff;
  font-size: 18px;
}
</style>
