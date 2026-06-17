import request from '@/utils/request'

export function getTaskList(status) {
  return request({
    url: '/interviewer/tasks',
    method: 'get',
    params: { status }
  })
}

export function getEvaluationDetail(taskId) {
  return request({
    url: `/interviewer/evaluation/${taskId}`,
    method: 'get'
  })
}

export function saveDraft(data) {
  return request({
    url: '/interviewer/evaluation/draft',
    method: 'post',
    data
  })
}

export function submitEvaluation(data) {
  return request({
    url: '/interviewer/evaluation/submit',
    method: 'post',
    data
  })
}

export function getDimensions() {
  return request({
    url: '/interviewer/dimensions',
    method: 'get'
  })
}

export function getDimensionTags(positionId) {
  return request({ url: '/interviewer/tags', method: 'get', params: { positionId } })
}

export function getDimensionsByPosition(positionId) {
  return request({ url: '/interviewer/dimensions/by-position', method: 'get', params: { positionId } })
}
