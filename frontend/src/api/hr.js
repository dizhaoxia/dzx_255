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
