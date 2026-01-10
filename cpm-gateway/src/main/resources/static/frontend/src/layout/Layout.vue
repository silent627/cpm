<template>
  <el-container class="layout-container">
    <el-aside width="200px" class="sidebar">
      <div class="logo">
        <h3>人口管理系统</h3>
      </div>
      <el-menu
        :default-active="activeMenu"
        router
        class="sidebar-menu"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
      >
        <el-menu-item index="/dashboard">
          <el-icon><House /></el-icon>
          <span>首页</span>
        </el-menu-item>
        <el-menu-item v-if="isAdmin" index="/users">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
        <el-menu-item index="/residents">
          <el-icon><UserFilled /></el-icon>
          <span>居民管理</span>
        </el-menu-item>
        <el-menu-item index="/households">
          <el-icon><OfficeBuilding /></el-icon>
          <span>户籍管理</span>
        </el-menu-item>
        <el-menu-item v-if="isAdmin" index="/admins">
          <el-icon><Setting /></el-icon>
          <span>管理员管理</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <div class="header-left">
          <span>社区人口管理系统</span>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <div class="user-avatar">
                <img 
                  v-if="hasAvatar && getAvatarUrl(userInfo.avatar)" 
                  :src="getAvatarUrl(userInfo.avatar)" 
                  alt="头像" 
                  class="avatar-img"
                  @error="handleAvatarError"
                  @load="(e) => { if (e.target) e.target.style.display = 'block' }"
                />
                <div v-else class="default-avatar">
                  <span class="default-avatar-text">{{ getAvatarInitial() || '用' }}</span>
                </div>
              </div>
              <span class="user-name">{{ userInfo?.realName || userInfo?.username || '用户' }}</span>
              <el-icon class="el-icon--right"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人信息</el-dropdown-item>
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

<script>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { logout } from '../api/auth'
import { getUserInfo } from '../api/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { House, User, UserFilled, OfficeBuilding, Setting, ArrowDown } from '@element-plus/icons-vue'
import { isValidImageUrl, getImageUrl, sanitizeImageField, getAvatarInitial as getAvatarInitialUtil } from '../utils/image'

export default {
  name: 'Layout',
  setup() {
    const router = useRouter()
    const route = useRoute()
    const userInfo = ref({ avatar: '' }) // 初始化为空对象，确保默认头像能显示

    const activeMenu = computed(() => route.path)
    
    // 计算是否显示用户头像（使用工具函数验证）
    const hasAvatar = computed(() => {
      if (!userInfo.value) return false
      return isValidImageUrl(userInfo.value.avatar)
    })
    
    // 获取用户信息
    const loadUserInfo = async () => {
      // 先从localStorage读取，确保界面能快速显示
      const userInfoStr = localStorage.getItem('userInfo')
      if (userInfoStr) {
        try {
          const parsedInfo = JSON.parse(userInfoStr)
          // 使用工具函数清理avatar字段
          parsedInfo.avatar = sanitizeImageField(parsedInfo.avatar)
          userInfo.value = parsedInfo
        } catch (e) {
          // 如果解析失败，至少设置一个空对象，确保界面能显示默认头像
          userInfo.value = { avatar: '' }
        }
      } else {
        // 如果没有localStorage数据，初始化一个空对象，确保界面能显示默认头像
        userInfo.value = { avatar: '' }
      }
      
      // 然后从API获取最新信息（异步更新）
      try {
        const res = await getUserInfo()
        if (res.code === 200 && res.data) {
          // 使用工具函数清理avatar字段
          res.data.avatar = sanitizeImageField(res.data.avatar)
          userInfo.value = res.data
          localStorage.setItem('userInfo', JSON.stringify(res.data))
        }
      } catch (error) {
        // API失败不影响界面显示，使用localStorage中的数据
      }
    }
    
    const isAdmin = computed(() => {
      return userInfo.value?.role === 'ADMIN'
    })

    // 监听路由变化（但不重新加载用户信息，避免头像闪烁）
    // 只在首次加载时获取用户信息，路由切换时不重新加载
    watch(() => route.path, () => {
      // 路由切换时不重新加载，保持用户信息稳定
      // 如果需要更新，可以通过事件触发
    })

    // 监听storage事件，当localStorage中的userInfo更新时，同步更新
    const handleStorageChange = (e) => {
      if (e.key === 'userInfo') {
        if (e.newValue) {
          try {
            const parsedInfo = JSON.parse(e.newValue)
            // 使用工具函数清理avatar字段
            parsedInfo.avatar = sanitizeImageField(parsedInfo.avatar)
            userInfo.value = parsedInfo
          } catch (error) {
            // 静默处理解析失败
          }
        } else {
          userInfo.value = null
        }
      }
    }

    // 监听自定义事件（用于同窗口内的更新）
    const handleUserInfoUpdate = () => {
      loadUserInfo()
    }

    onMounted(() => {
      // 首次加载时获取用户信息
      loadUserInfo()
      // 监听storage事件（跨标签页）
      window.addEventListener('storage', handleStorageChange)
      // 监听自定义事件（同标签页）
      window.addEventListener('userInfoUpdated', handleUserInfoUpdate)
    })

    onUnmounted(() => {
      window.removeEventListener('storage', handleStorageChange)
      window.removeEventListener('userInfoUpdated', handleUserInfoUpdate)
    })

    const handleCommand = async (command) => {
      if (command === 'profile') {
        router.push('/profile')
      } else if (command === 'logout') {
        try {
          await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          })
          await logout()
          localStorage.removeItem('token')
          localStorage.removeItem('userInfo')
          ElMessage.success('退出成功')
          router.push('/login')
        } catch (error) {
          // 用户取消
        }
      }
    }

    // 获取头像完整URL（使用工具函数）
    const getAvatarUrl = getImageUrl
    
    // 头像加载失败时的处理（使用工具函数）
    const handleAvatarError = (event) => {
      if (!event || !event.target) return
      event.target.style.display = 'none'
      if (userInfo.value) {
        userInfo.value.avatar = ''
      }
    }
    
    // 获取默认头像的初始字母（使用工具函数）
    const getAvatarInitial = () => {
      return getAvatarInitialUtil(userInfo.value, '用')
    }

    return {
      activeMenu,
      userInfo,
      isAdmin,
      hasAvatar,
      handleCommand,
      getAvatarUrl,
      getAvatarInitial,
      handleAvatarError,
      House,
      User,
      UserFilled,
      OfficeBuilding,
      Setting,
      ArrowDown
    }
  }
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

.sidebar {
  background-color: #304156;
  overflow: hidden;
}

.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  background-color: #2b3a4a;
  color: white;
}

.logo h3 {
  margin: 0;
  font-size: 18px;
}

.sidebar-menu {
  border: none;
  height: calc(100vh - 60px);
}

.header {
  background-color: #fff;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
}

.header-left {
  font-size: 18px;
  font-weight: bold;
  color: #333;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 5px 10px;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.user-info:hover {
  background-color: #f5f7fa;
}

.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f0f2f5;
  flex-shrink: 0;
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.default-avatar {
  width: 32px;
  height: 32px;
  min-width: 32px;
  min-height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  overflow: hidden;
  background: linear-gradient(135deg, #409EFF 0%, #66B1FF 50%, #85C1FF 100%);
  position: relative;
  flex-shrink: 0;
}

.default-avatar-text {
  color: white;
  font-size: 14px;
  font-weight: bold;
  line-height: 1;
  user-select: none;
}

.user-name {
  font-size: 14px;
  color: #333;
}

.main-content {
  background-color: #f0f2f5;
  padding: 20px;
}
</style>

