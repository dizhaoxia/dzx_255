import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login, getUserInfo, logout } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  const userInfo = ref({})
  const token = ref('')

  const isLoggedIn = computed(() => {
    return !!userInfo.value.id
  })

  const setUserInfo = (info) => {
    userInfo.value = info
  }

  const setToken = (t) => {
    token.value = t
  }

  const doLogin = async (username, password) => {
    const res = await login(username, password)
    if (res.code === 200) {
      const userRes = await getUserInfo()
      if (userRes.code === 200) {
        setUserInfo(userRes.data)
      }
    }
    return res
  }

  const fetchUserInfo = async () => {
    const res = await getUserInfo()
    if (res.code === 200) {
      setUserInfo(res.data)
    }
    return res
  }

  const doLogout = async () => {
    try {
      await logout()
    } catch (e) {
      console.error('Logout error:', e)
    }
    userInfo.value = {}
    token.value = ''
  }

  return {
    userInfo,
    token,
    isLoggedIn,
    setUserInfo,
    setToken,
    doLogin,
    fetchUserInfo,
    doLogout
  }
})
