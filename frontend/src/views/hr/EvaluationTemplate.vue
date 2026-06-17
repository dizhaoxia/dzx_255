<template>
  <div class="evaluation-template">
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>模板列表</span>
              <el-button type="primary" size="small" @click="handleAddTemplate">
                <el-icon><Plus /></el-icon>
                新增模板
              </el-button>
            </div>
          </template>
          <div class="template-list">
            <div
              v-for="tpl in templateList"
              :key="tpl.id"
              class="template-item"
              :class="{ active: currentTemplate?.id === tpl.id }"
              @click="handleSelectTemplate(tpl)"
            >
              <div class="template-item-header">
                <span class="template-name">{{ tpl.templateName }}</span>
                <el-tag :type="tpl.status === 'ACTIVE' ? 'success' : 'info'" size="small">
                  {{ tpl.status === 'ACTIVE' ? '启用' : '停用' }}
                </el-tag>
              </div>
              <div class="template-desc">{{ tpl.description || '暂无描述' }}</div>
              <div class="template-actions">
                <el-button size="small" link type="primary" @click.stop="handleSelectTemplate(tpl)">
                  编辑
                </el-button>
                <el-button
                  size="small"
                  link
                  :type="tpl.status === 'ACTIVE' ? 'warning' : 'success'"
                  @click.stop="handleToggleStatus(tpl)"
                >
                  {{ tpl.status === 'ACTIVE' ? '停用' : '启用' }}
                </el-button>
              </div>
            </div>
            <el-empty v-if="!loading && templateList.length === 0" description="暂无模板" />
          </div>
        </el-card>
      </el-col>

      <el-col :span="16">
        <el-card v-loading="saving">
          <template #header>
            <div class="card-header">
              <span>{{ currentTemplate?.id ? '编辑模板' : '新建模板' }}</span>
            </div>
          </template>

          <div v-if="currentTemplate" class="edit-area">
            <el-form :model="currentTemplate" label-width="100px" label-position="left">
              <el-form-item label="模板名称" required>
                <el-input v-model="currentTemplate.templateName" placeholder="请输入模板名称" />
              </el-form-item>
              <el-form-item label="模板描述">
                <el-input
                  v-model="currentTemplate.description"
                  type="textarea"
                  :rows="2"
                  placeholder="请输入模板描述"
                />
              </el-form-item>

              <el-divider content-position="left">评分维度</el-divider>

              <div class="dimensions-section">
                <div class="dimensions-header">
                  <el-button type="primary" size="small" @click="handleAddDimension">
                    <el-icon><Plus /></el-icon>
                    新增维度
                  </el-button>
                  <div class="weight-total" :class="{ error: totalWeight !== 100 }">
                    权重合计：<strong>{{ totalWeight }}%</strong>
                    <el-tooltip v-if="totalWeight !== 100" content="权重总和必须等于 100%" placement="top">
                      <el-icon class="warning-icon"><WarningFilled /></el-icon>
                    </el-tooltip>
                  </div>
                </div>

                <el-table :data="currentTemplate.dimensions" border style="width: 100%; margin-top: 12px;">
                  <el-table-column label="序号" type="index" width="60" align="center" />
                  <el-table-column label="维度名称" min-width="120">
                    <template #default="{ row }">
                      <el-input v-model="row.dimensionName" size="small" placeholder="维度名称" />
                    </template>
                  </el-table-column>
                  <el-table-column label="编码" width="120">
                    <template #default="{ row }">
                      <el-input v-model="row.dimensionCode" size="small" placeholder="编码" />
                    </template>
                  </el-table-column>
                  <el-table-column label="描述" min-width="160">
                    <template #default="{ row }">
                      <el-input v-model="row.dimensionDesc" size="small" placeholder="维度描述" />
                    </template>
                  </el-table-column>
                  <el-table-column label="权重(%)" width="100">
                    <template #default="{ row }">
                      <el-input-number
                        v-model="row.weight"
                        :min="0"
                        :max="100"
                        :step="5"
                        size="small"
                        controls-position="right"
                      />
                    </template>
                  </el-table-column>
                  <el-table-column label="满分" width="90">
                    <template #default="{ row }">
                      <el-input-number
                        v-model="row.maxScore"
                        :min="1"
                        :max="100"
                        size="small"
                        controls-position="right"
                      />
                    </template>
                  </el-table-column>
                  <el-table-column label="最低分" width="90">
                    <template #default="{ row }">
                      <el-input-number
                        v-model="row.minScore"
                        :min="0"
                        :max="100"
                        size="small"
                        controls-position="right"
                      />
                    </template>
                  </el-table-column>
                  <el-table-column label="排序" width="90">
                    <template #default="{ row }">
                      <el-input-number
                        v-model="row.sortOrder"
                        :min="0"
                        size="small"
                        controls-position="right"
                      />
                    </template>
                  </el-table-column>
                  <el-table-column label="操作" width="70" align="center">
                    <template #default="{ $index }">
                      <el-button
                        type="danger"
                        size="small"
                        link
                        @click="handleRemoveDimension($index)"
                      >
                        删除
                      </el-button>
                    </template>
                  </el-table-column>
                </el-table>
              </div>

              <el-divider content-position="left">绑定岗位</el-divider>

              <el-form-item label="绑定岗位">
                <el-select
                  v-model="boundPositionId"
                  placeholder="选择要绑定的岗位（可选）"
                  clearable
                  style="width: 300px;"
                >
                  <el-option
                    v-for="pos in positions"
                    :key="pos.id"
                    :label="pos.positionName"
                    :value="pos.id"
                  />
                </el-select>
              </el-form-item>

              <el-form-item>
                <el-button type="primary" :loading="saving" @click="handleSave">
                  保存模板
                </el-button>
                <el-button @click="handleReset">重置</el-button>
              </el-form-item>
            </el-form>
          </div>

          <el-empty v-else description="请选择左侧模板进行编辑，或点击新增按钮创建新模板" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, reactive, onMounted } from 'vue'
import {
  getTemplateList,
  getTemplateDetail,
  saveTemplate,
  toggleTemplateStatus,
  bindPositionTemplate,
  getPositionList
} from '@/api/hr'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, WarningFilled } from '@element-plus/icons-vue'

const templateList = ref([])
const positions = ref([])
const currentTemplate = ref(null)
const boundPositionId = ref(null)
const loading = ref(false)
const saving = ref(false)

const totalWeight = computed(() => {
  if (!currentTemplate.value?.dimensions) return 0
  return currentTemplate.value.dimensions.reduce(
    (sum, dim) => sum + (Number(dim.weight) || 0),
    0
  )
})

const createEmptyTemplate = () => ({
  id: null,
  templateName: '',
  description: '',
  status: 'ACTIVE',
  dimensions: []
})

const createEmptyDimension = () => ({
  dimensionId: null,
  dimensionName: '',
  dimensionCode: '',
  dimensionDesc: '',
  weight: 0,
  maxScore: 5,
  minScore: 0,
  sortOrder: currentTemplate.value?.dimensions?.length || 0
})

const loadTemplates = async () => {
  try {
    loading.value = true
    const res = await getTemplateList()
    if (res.code === 200) {
      templateList.value = res.data || []
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
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

const handleAddTemplate = () => {
  currentTemplate.value = createEmptyTemplate()
  boundPositionId.value = null
}

const handleSelectTemplate = async (tpl) => {
  try {
    loading.value = true
    const res = await getTemplateDetail(tpl.id)
    if (res.code === 200) {
      currentTemplate.value = res.data || createEmptyTemplate()
      currentTemplate.value.dimensions = currentTemplate.value.dimensions || []
      boundPositionId.value = null
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const handleToggleStatus = async (tpl) => {
  const action = tpl.status === 'ACTIVE' ? '停用' : '启用'
  try {
    await ElMessageBox.confirm(
      `确定要${action}模板「${tpl.templateName}」吗？`,
      '提示',
      { type: 'warning' }
    )
    const newStatus = tpl.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'
    const res = await toggleTemplateStatus(tpl.id, newStatus)
    if (res.code === 200) {
      ElMessage.success(`${action}成功`)
      loadTemplates()
    }
  } catch (e) {
    if (e !== 'cancel') {
      console.error(e)
    }
  }
}

const handleAddDimension = () => {
  if (!currentTemplate.value) return
  if (!currentTemplate.value.dimensions) {
    currentTemplate.value.dimensions = []
  }
  currentTemplate.value.dimensions.push(createEmptyDimension())
}

const handleRemoveDimension = (index) => {
  currentTemplate.value.dimensions.splice(index, 1)
}

const handleSave = async () => {
  if (!currentTemplate.value?.templateName?.trim()) {
    ElMessage.warning('请输入模板名称')
    return
  }
  if (!currentTemplate.value.dimensions || currentTemplate.value.dimensions.length === 0) {
    ElMessage.warning('请至少添加一个评分维度')
    return
  }
  if (totalWeight.value !== 100) {
    ElMessage.warning(`权重总和必须等于 100%，当前为 ${totalWeight.value}%`)
    return
  }
  for (const dim of currentTemplate.value.dimensions) {
    if (!dim.dimensionName?.trim()) {
      ElMessage.warning('请填写所有维度的名称')
      return
    }
    if (!dim.dimensionCode?.trim()) {
      ElMessage.warning('请填写所有维度的编码')
      return
    }
    if (Number(dim.minScore) >= Number(dim.maxScore)) {
      ElMessage.warning(`维度「${dim.dimensionName}」的最低分必须小于满分`)
      return
    }
  }

  try {
    saving.value = true
    const res = await saveTemplate(currentTemplate.value)
    if (res.code === 200) {
      const savedId = res.data?.id
      ElMessage.success('保存成功')

      if (boundPositionId.value && savedId) {
        const bindRes = await bindPositionTemplate(boundPositionId.value, savedId)
        if (bindRes.code === 200) {
          ElMessage.success('岗位绑定成功')
        }
      }

      await loadTemplates()
      if (savedId) {
        handleSelectTemplate({ id: savedId })
      }
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

const handleReset = () => {
  if (currentTemplate.value?.id) {
    handleSelectTemplate({ id: currentTemplate.value.id })
  } else {
    currentTemplate.value = createEmptyTemplate()
    boundPositionId.value = null
  }
}

onMounted(() => {
  loadTemplates()
  loadPositions()
})
</script>

<style scoped>
.evaluation-template {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
  font-size: 16px;
}

.template-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-height: 70vh;
  overflow-y: auto;
}

.template-item {
  padding: 14px;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
}

.template-item:hover {
  border-color: #c6e2ff;
  background-color: #f5faff;
}

.template-item.active {
  border-color: #409eff;
  background-color: #ecf5ff;
}

.template-item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.template-name {
  font-weight: 600;
  color: #303133;
}

.template-desc {
  font-size: 13px;
  color: #909399;
  margin-bottom: 8px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.template-actions {
  display: flex;
  gap: 12px;
}

.edit-area {
  padding: 0 10px;
}

.dimensions-section {
  margin-bottom: 10px;
}

.dimensions-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.weight-total {
  font-size: 14px;
  color: #606266;
  display: flex;
  align-items: center;
  gap: 6px;
}

.weight-total strong {
  color: #409eff;
  font-size: 18px;
}

.weight-total.error strong {
  color: #f56c6c;
}

.warning-icon {
  color: #f56c6c;
  font-size: 16px;
}
</style>
