import request from '@/utils/request'

export function login(username, password) {
  const formData = new FormData()
  formData.append('username', username)
  formData.append('password', password)
  return request({
    url: '/auth/login',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    }
  })
}

export function getUserInfo() {
  return request({
    url: '/auth/userinfo',
    method: 'get'
  })
}

export function logout() {
  return request({
    url: '/auth/logout',
    method: 'post'
  })
}
