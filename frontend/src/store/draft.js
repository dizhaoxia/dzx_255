import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useDraftStore = defineStore('draft', () => {
  const drafts = ref({})

  const saveDraft = (taskId, data) => {
    drafts.value[taskId] = {
      ...data,
      savedAt: new Date().toISOString()
    }
  }

  const getDraft = (taskId) => {
    return drafts.value[taskId] || null
  }

  const clearDraft = (taskId) => {
    if (drafts.value[taskId]) {
      delete drafts.value[taskId]
    }
  }

  return {
    drafts,
    saveDraft,
    getDraft,
    clearDraft
  }
})
