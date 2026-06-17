<template>
  <el-container class="main-container">
    <el-aside width="220px" class="sidebar">
      <div class="logo">
        <h2>面试评价系统</h2>
      </div>
      <el-menu
        :default-active="activeMenu"
        class="menu"
        router
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
      >
        <el-menu-item index="/dashboard">
          <el-icon><House /></el-icon>
          <span>首页</span>
        </el-menu-item>

        <template v-if="isInterviewer">
          <el-menu-item index="/interviewer/tasks">
            <el-icon><List /></el-icon>
            <span>待评价列表</span>
          </el-menu-item>
        </template>

        <template v-if="isHr">
          <el-sub-menu index="hr">
            <template #title>
              <el-icon><OfficeBuilding /></el-icon>
              <span>HR管理</span>
            </template>
            <el-menu-item index="/hr/template">
              <el-icon><Document /></el-icon>
              <span>评价模板</span>
            </el-menu-item>
            <el-menu-item index="/hr/progress">
              <el-icon><DataAnalysis /></el-icon>
              <span>进度看板</span>
            </el-menu-item>
          </el-sub-menu>
        </template>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="header-left">
          <span class="page-title">{{ pageTitle }}</span>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-icon><UserFilled /></el-icon>
              {{ userStore.userInfo.realName }}
              <span class="role-badge">{{ userRoleText }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { ElMessageBox, ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)

const pageTitle = computed(() => route.meta.title || '')

const isInterviewer = computed(() => userStore.userInfo.role === 'INTERVIEWER')
const isHr = computed(() => userStore.userInfo.role === 'HR')

const userRoleText = computed(() => {
  const roleMap = {
    'HR': 'HR',
    'INTERVIEWER': '面试官'
  }
  return roleMap[userStore.userInfo.role] || ''
})

const handleCommand = async (command) => {
  if (command === 'logout') {
    try {
      await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      await userStore.doLogout()
      ElMessage.success('退出成功')
      router.push('/login')
    } catch (e) {
    }
  }
}
</script>

<style scoped>
.main-container {
  height: 100vh;
}

.sidebar {
  background-color: #304156;
  color: #fff;
  overflow: hidden;
}

.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  background-color: #2b3648;
  color: #fff;
  font-size: 18px;
  font-weight: bold;
}

.logo h2 {
  margin: 0;
  font-size: 18px;
}

.menu {
  border-right: none;
}

.header {
  background-color: #fff;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

.header-left {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  color: #606266;
  font-size: 14px;
  gap: 5px;
}

.role-badge {
  padding: 2px 8px;
  background-color: #ecf5ff;
  color: #409eff;
  border-radius: 4px;
  font-size: 12px;
  margin: 0 5px;
}

.main-content {
  background-color: #f0f2f5;
  padding: 20px;
}
</style>
