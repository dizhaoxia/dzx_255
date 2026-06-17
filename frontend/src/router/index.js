import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    redirect: '/dashboard'
  },
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'interviewer/tasks',
        name: 'InterviewerTasks',
        component: () => import('@/views/interviewer/TaskList.vue'),
        meta: { title: '待评价列表', roles: ['INTERVIEWER'] }
      },
      {
        path: 'interviewer/evaluation/:taskId',
        name: 'InterviewerEvaluation',
        component: () => import('@/views/interviewer/EvaluationForm.vue'),
        meta: { title: '评价表单', roles: ['INTERVIEWER'] }
      },
      {
        path: 'hr/template',
        name: 'HrTemplate',
        component: () => import('@/views/hr/EvaluationTemplate.vue'),
        meta: { title: '评价模板', roles: ['HR'] }
      },
      {
        path: 'hr/progress',
        name: 'HrProgress',
        component: () => import('@/views/hr/ProgressBoard.vue'),
        meta: { title: '评价进度看板', roles: ['HR'] }
      },
      {
        path: 'hr/candidate/:candidateId/evaluation',
        name: 'HrCandidateEvaluation',
        component: () => import('@/views/hr/CandidateEvaluation.vue'),
        meta: { title: '评价详情', roles: ['HR'] }
      },
      {
        path: 'hr/compare',
        name: 'HrCandidateCompare',
        component: () => import('@/views/hr/CandidateCompare.vue'),
        meta: { title: '候选人横向对比', roles: ['HR'] }
      },
      {
        path: 'hr/funnel',
        name: 'HrFunnel',
        component: () => import('@/views/hr/FunnelDashboard.vue'),
        meta: { title: '招聘漏斗统计', roles: ['HR'] }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  const isLoggedIn = userStore.isLoggedIn

  if (to.path === '/login') {
    if (isLoggedIn) {
      next('/dashboard')
    } else {
      next()
    }
    return
  }

  if (!isLoggedIn) {
    next('/login')
    return
  }

  if (to.meta.roles && to.meta.roles.length > 0) {
    const userRole = userStore.userInfo.role
    if (!to.meta.roles.includes(userRole)) {
      next('/dashboard')
      return
    }
  }

  next()
})

export default router
