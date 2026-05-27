import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'

// 路由规则定义
const routes = [
  // === 游客视图 (无侧边栏) ===
  {
    path: '/',
    component: () => import('../views/Home.vue'),
    meta: { title: '嗖嗖移动-首页' }
  },
  {
    path: '/login',
    component: () => import('../views/Login.vue'),
    meta: { title: '登录', guestOnly: true } // guestOnly: 仅游客可访问
  },
  {
    path: '/register',
    component: () => import('../views/Register.vue'),
    meta: { title: '开户注册', guestOnly: true }
  },

  // === 用户视图 (带 Layout 侧边栏) ===
  {
    path: '/user',
    component: () => import('../components/Layout.vue'), // 父路由为布局组件
    meta: { requiresAuth: true }, // 需要登录才能访问此层级
    redirect: '/user/dashboard', // 默认重定向到仪表盘
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/Dashboard.vue'),
        meta: { title: '个人控制台' }
      },
      {
        path: 'consume',
        name: 'Consume',
        component: () => import('../views/Consume.vue'),
        meta: { title: '模拟消费' }
      },
      {
        path: 'recharge',
        name: 'Recharge',
        component: () => import('../views/Recharge.vue'),
        meta: { title: '话费充值' }
      },
      {
        path: 'change-package',
        name: 'ChangePackage',
        component: () => import('../views/ChangePackage.vue'),
        meta: { title: '套餐变更' }
      },
      {
        path: 'records',
        name: 'Records',
        component: () => import('../views/Records.vue'),
        meta: { title: '消费详单' }
      },
      {
        path: 'settings',
        name: 'Settings',
        component: () => import('../views/Settings.vue'),
        meta: { title: '系统设置' }
      }
    ]
  },
  // 404 兜底
  {
    path: '/:pathMatch(.*)*',
    redirect: '/'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// --- 全局前置守卫 ---
router.beforeEach((to, from, next) => {
  // 设置页面标题
  if (to.meta.title) {
    document.title = `${to.meta.title} - 嗖嗖移动营业厅`
  }

  const userStore = useUserStore()

  // 1. 如果路由需要登录权限
  if (to.matched.some(record => record.meta.requiresAuth)) {
    if (!userStore.isLogin) {
      // 未登录，重定向到登录页，并携带原目标路径
      next({ path: '/login', query: { redirect: to.fullPath } })
    } else {
      next() // 已登录，放行
    }
  } 
  // 2. 如果路由仅限游客访问 (如登录页、注册页)
  else if (to.matched.some(record => record.meta.guestOnly)) {
    if (userStore.isLogin) {
      next({ path: '/user/dashboard' }) // 已登录用户访问登录页，重定向到控制台
    } else {
      next()
    }
  } 
  // 3. 其他情况直接放行
  else {
    next()
  }
})

export default router