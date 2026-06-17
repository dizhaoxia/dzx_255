import request from '@/utils/request'

export function getPositionList() {
  return request({
    url: '/hr/positions',
    method: 'get'
  })
}

export function getCandidateProgress(positionId) {
  return request({
    url: '/hr/candidates/progress',
    method: 'get',
    params: { positionId }
  })
}

export function getCandidateEvaluation(candidateId) {
  return request({
    url: `/hr/candidate/${candidateId}/evaluation`,
    method: 'get'
  })
}

export function getEvaluationTemplate() {
  return request({
    url: '/hr/evaluation-template',
    method: 'get'
  })
}

export function exportCsv(candidateId) {
  return request({
    url: `/hr/candidate/${candidateId}/export-csv`,
    method: 'get',
    responseType: 'blob'
  })
}

export function getCandidateCompare(candidateIds) {
  return request({
    url: '/hr/candidates/compare',
    method: 'post',
    data: { candidateIds }
  })
}

export function getFunnelStats(positionId) {
  return request({
    url: '/hr/funnel-stats',
    method: 'get',
    params: { positionId }
  })
}

export function getTemplateList() {
  return request({
    url: '/hr/templates',
    method: 'get'
  })
}
export function getTemplateDetail(templateId) {
  return request({
    url: `/hr/template/${templateId}`,
    method: 'get'
  })
}
export function saveTemplate(data) {
  return request({
    url: '/hr/template',
    method: 'post',
    data
  })
}
export function toggleTemplateStatus(templateId, status) {
  return request({
    url: `/hr/template/${templateId}/status`,
    method: 'put',
    params: { status }
  })
}
export function bindPositionTemplate(positionId, templateId) {
  return request({
    url: '/hr/template/bind',
    method: 'post',
    data: { positionId, templateId }
  })
}
export function getTemplateByPosition(positionId) {
  return request({
    url: '/hr/template/by-position',
    method: 'get',
    params: { positionId }
  })
}

export function getCandidateDiscrepancy(candidateId) {
  return request({
    url: `/hr/candidate/${candidateId}/discrepancy`,
    method: 'get'
  })
}

export function getCandidateCompare(candidateIds) {
  return request({
    url: '/hr/candidates/compare',
    method: 'post',
    data: { candidateIds }
  })
}

export function getFunnelStats(positionId) {
  return request({
    url: '/hr/funnel-stats',
    method: 'get',
    params: { positionId }
  })
}
