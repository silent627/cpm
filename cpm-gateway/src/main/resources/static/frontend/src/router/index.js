import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'
import Login from '../views/Login.vue'
import ForgetPassword from '../views/ForgetPassword.vue'
import Layout from '../layout/Layout.vue'
import Dashboard from '../views/Dashboard.vue'
import UserList from '../views/user/UserList.vue'
import UserProfile from '../views/user/UserProfile.vue'
import ResidentList from '../views/resident/ResidentList.vue'
import HouseholdList from '../views/household/HouseholdList.vue'
import AdminList from '../views/admin/AdminList.vue'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  {
    path: '/forget-password',
    name: 'ForgetPassword',
    component: ForgetPassword
  },
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: Dashboard
      },
      {
        path: 'users',
        name: 'UserList',
        component: UserList
      },
      {
        path: 'profile',
        name: 'UserProfile',
        component: UserProfile
      },
      {
        path: 'residents',
        name: 'ResidentList',
        component: ResidentList
      },
      {
        path: 'households',
        name: 'HouseholdList',
        component: HouseholdList
      },
      {
        path: 'admins',
        name: 'AdminList',
        component: AdminList
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  // 允许访问登录和忘记密码页面（无需登录）
  if (to.path === '/login' || to.path === '/forget-password') {
    next()
  } else {
    if (token) {
      // 检查用户角色权限
      const userInfoStr = localStorage.getItem('userInfo')
      if (userInfoStr) {
        const userInfo = JSON.parse(userInfoStr)
        const isAdmin = userInfo.role === 'ADMIN'
        
        // 普通用户不能访问用户管理和管理员管理
        if (!isAdmin && (to.path === '/users' || to.path === '/admins')) {
          ElMessage.warning('您没有权限访问此页面')
          next('/dashboard')
          return
        }
      }
      next()
    } else {
      next('/login')
    }
  }
})

export default router

