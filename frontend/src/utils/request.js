import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'
import router from '@/router'

const service = axios.create({
  baseURL: '/api',
  timeout: 30000,
  withCredentials: true
})

service.interceptors.request.use(
  (config) => {
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

service.interceptors.response.use(
  (response) => {
    const res = response.data
    return res
  },
  (error) => {
    if (error.response) {
      if (error.response.status === 401) {
        const userStore = useUserStore()
        userStore.doLogout()
        ElMessage.error('请先登录')
        router.push('/login')
      } else if (error.response.status === 403) {
        ElMessage.error('权限不足')
      } else {
        ElMessage.error(error.response.data?.message || '请求失败')
      }
    } else {
      ElMessage.error('网络错误，请检查网络连接')
    }
    return Promise.reject(error)
  }
)

export default service
