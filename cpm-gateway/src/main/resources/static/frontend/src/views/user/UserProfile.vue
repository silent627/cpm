<template>
  <div class="user-profile">
    <el-card>
      <template #header>
        <span>个人信息</span>
      </template>
      <el-tabs v-model="activeTab">
        <el-tab-pane label="基本信息" name="info">
          <el-form :model="userForm" :rules="userRules" label-width="120px" style="max-width: 600px" ref="userFormRef">
            <el-form-item label="用户名">
              <el-input v-model="userForm.username" disabled />
            </el-form-item>
            <el-form-item label="真实姓名">
              <el-input v-model="userForm.realName" clearable />
            </el-form-item>
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="userForm.phone" clearable />
            </el-form-item>
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="userForm.email" clearable />
              <div v-if="!userForm.email || userForm.email.trim() === ''" style="font-size: 12px; color: #E6A23C; margin-top: 5px;">
                提示：设置邮箱后才可以修改密码
              </div>
            </el-form-item>
            <el-form-item label="角色">
              <el-tag :type="userForm.role === 'ADMIN' ? 'danger' : 'primary'">
                {{ userForm.role === 'ADMIN' ? '管理员' : '普通用户' }}
              </el-tag>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleUpdateInfo">保存</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        <el-tab-pane label="修改密码" name="password">
          <el-radio-group v-model="passwordChangeMode" @change="handlePasswordModeChange" style="margin-bottom: 20px">
            <el-radio label="oldPassword">通过旧密码修改</el-radio>
            <el-radio label="emailCode">通过邮箱验证码修改</el-radio>
          </el-radio-group>
          
          <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="120px" style="max-width: 600px">
            <!-- 通过邮箱验证码修改 -->
            <template v-if="passwordChangeMode === 'emailCode'">
              <el-form-item label="邮箱验证码" prop="emailCode">
                <div style="display: flex; gap: 10px;">
                  <el-input
                    v-model="passwordForm.emailCode"
                    placeholder="请输入邮箱验证码"
                    style="flex: 1"
                  />
                  <el-button
                    type="primary"
                    :disabled="countdown > 0"
                    @click="sendEmailCode"
                    :loading="sendingCode"
                  >
                    {{ countdown > 0 ? `${countdown}秒后重试` : '发送验证码' }}
                  </el-button>
                </div>
                <div style="font-size: 12px; color: #909399; margin-top: 5px;">
                  验证码将发送到您的邮箱：{{ userForm.email || '未设置' }}
                </div>
                <div style="font-size: 12px; color: #909399; margin-top: 3px;">
                  验证码有效期为5分钟，请及时输入
                </div>
              </el-form-item>
            </template>
            
            <!-- 通过旧密码修改 -->
            <template v-if="passwordChangeMode === 'oldPassword'">
              <el-form-item label="旧密码" prop="oldPassword">
                <el-input v-model="passwordForm.oldPassword" type="password" placeholder="请输入旧密码" show-password />
              </el-form-item>
            </template>
            
            <!-- 新密码和确认密码（两种方式都需要） -->
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码" show-password />
              <div v-if="passwordForm.newPassword" style="font-size: 12px; color: #909399; margin-top: 5px;">
                {{ getPasswordStrengthText(passwordForm.newPassword) }}
              </div>
            </el-form-item>
            <el-form-item label="确认新密码" prop="confirmPassword">
              <el-input v-model="passwordForm.confirmPassword" type="password" placeholder="请再次输入新密码" show-password />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleChangePassword">修改密码</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script>
import { ref, reactive, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { getUserInfo, updateUser, changePassword, sendChangePasswordCode } from '../../api/user'
import { ElMessage, ElMessageBox } from 'element-plus'

export default {
  name: 'UserProfile',
  setup() {
    const router = useRouter()
    const activeTab = ref('info')
    const passwordChangeMode = ref('oldPassword') // 默认使用旧密码方式
    const userForm = reactive({
      username: '',
      realName: '',
      phone: '',
      email: '',
      role: ''
    })
    const passwordForm = reactive({
      emailCode: '',
      oldPassword: '',
      newPassword: '',
      confirmPassword: ''
    })
    const passwordFormRef = ref(null)
    const sendingCode = ref(false)
    const countdown = ref(0)
    let countdownTimer = null

    const userFormRef = ref(null)

    // 验证手机号
    const validatePhone = (rule, value, callback) => {
      if (value && value.trim() !== '') {
        if (!/^1[3-9]\d{9}$/.test(value)) {
          callback(new Error('手机号格式不正确，应为11位数字且以1开头'))
        } else {
          callback()
        }
      } else {
        callback()
      }
    }

    // 验证邮箱
    const validateEmail = (rule, value, callback) => {
      if (value && value.trim() !== '') {
        if (!/^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/.test(value)) {
          callback(new Error('邮箱格式不正确'))
        } else {
          callback()
        }
      } else {
        callback()
      }
    }

    // 验证密码（只检查长度，不强制其他规则）
    const validatePassword = (rule, value, callback) => {
      if (!value || value.trim() === '') {
        callback(new Error('请输入新密码'))
      } else if (value.length < 8) {
        callback(new Error('密码长度至少8位'))
      } else {
        // 只检查长度，其他规则仅作为提醒（不强制）
        callback()
      }
    }

    // 获取密码强度文本（仅作为提醒，不强制）
    const getPasswordStrengthText = (password) => {
      if (!password || password.length < 8) {
        return '密码长度至少8位'
      }
      const hasLower = /[a-z]/.test(password)
      const hasUpper = /[A-Z]/.test(password)
      const hasNumber = /\d/.test(password)
      const hasSpecial = /[@$!%*?&]/.test(password)
      
      let strength = 0
      if (hasLower) strength++
      if (hasUpper) strength++
      if (hasNumber) strength++
      if (hasSpecial) strength++
      
      if (strength >= 3) {
        return '强密码'
      } else if (strength >= 2) {
        return '中等密码'
      } else {
        return '弱密码（建议包含大小写字母、数字和特殊字符）'
      }
    }

    const validateConfirmPassword = (rule, value, callback) => {
      if (!value || value.trim() === '') {
        callback(new Error('请确认新密码'))
      } else if (value !== passwordForm.newPassword) {
        callback(new Error('两次输入的密码不一致'))
      } else {
        callback()
      }
    }

    const userRules = {
      phone: [{ validator: validatePhone, trigger: 'blur' }],
      email: [{ validator: validateEmail, trigger: 'blur' }]
    }

    // 验证旧密码（仅在使用旧密码方式时必填）
    const validateOldPassword = (rule, value, callback) => {
      if (passwordChangeMode.value === 'oldPassword') {
        if (!value || value.trim() === '') {
          callback(new Error('请输入旧密码'))
        } else {
          callback()
        }
      } else {
        callback()
      }
    }

    // 验证邮箱验证码（仅在使用邮箱验证码方式时必填）
    const validateEmailCode = (rule, value, callback) => {
      if (passwordChangeMode.value === 'emailCode') {
        if (!value || value.trim() === '') {
          callback(new Error('请输入邮箱验证码'))
        } else {
          callback()
        }
      } else {
        callback()
      }
    }

    const passwordRules = {
      emailCode: [{ validator: validateEmailCode, trigger: 'blur' }],
      oldPassword: [{ validator: validateOldPassword, trigger: 'blur' }],
      newPassword: [{ validator: validatePassword, trigger: 'blur' }],
      confirmPassword: [
        { validator: validateConfirmPassword, trigger: 'blur' }
      ]
    }

    // 发送邮箱验证码
    const sendEmailCode = async () => {
      if (!userForm.email || userForm.email.trim() === '') {
        ElMessage.warning('请先设置邮箱')
        return
      }

      sendingCode.value = true
      try {
        const res = await sendChangePasswordCode()
        if (res.code === 200) {
          ElMessage.success('验证码已发送，请查收邮件')
          startCountdown()
        }
      } catch (error) {
        ElMessage.error(error.message || '发送验证码失败')
      } finally {
        sendingCode.value = false
      }
    }

    // 开始倒计时
    const startCountdown = () => {
      countdown.value = 60
      if (countdownTimer) {
        clearInterval(countdownTimer)
      }
      countdownTimer = setInterval(() => {
        countdown.value--
        if (countdown.value <= 0) {
          clearInterval(countdownTimer)
          countdownTimer = null
        }
      }, 1000)
    }

    // 切换修改密码方式时，清空另一个方式的输入
    const handlePasswordModeChange = () => {
      if (passwordChangeMode.value === 'oldPassword') {
        // 切换到旧密码方式，清空邮箱验证码
        passwordForm.emailCode = ''
        // 清除倒计时
        if (countdownTimer) {
          clearInterval(countdownTimer)
          countdownTimer = null
        }
        countdown.value = 0
      } else if (passwordChangeMode.value === 'emailCode') {
        // 切换到邮箱验证码方式，清空旧密码
        passwordForm.oldPassword = ''
      }
      // 清空表单验证状态
      if (passwordFormRef.value) {
        passwordFormRef.value.clearValidate()
      }
    }

    const loadUserInfo = async () => {
      try {
        const res = await getUserInfo()
        if (res.code === 200) {
          Object.assign(userForm, res.data)
        }
      } catch (error) {
        ElMessage.error('加载用户信息失败')
      }
    }

    const handleUpdateInfo = async () => {
      if (!userFormRef.value) return
      
      await userFormRef.value.validate(async (valid) => {
        if (valid) {
          try {
            const res = await updateUser({
              realName: userForm.realName,
              phone: userForm.phone,
              email: userForm.email
            })
            if (res.code === 200) {
              ElMessage.success('更新成功')
              loadUserInfo()
            }
          } catch (error) {
            ElMessage.error(error.message || '更新失败')
          }
        }
      })
    }

    const handleChangePassword = async () => {
      if (!passwordFormRef.value) return
      await passwordFormRef.value.validate(async (valid) => {
        if (valid) {
          try {
            const params = {
              newPassword: passwordForm.newPassword
            }
            
            // 根据选择的修改方式，添加相应的验证参数
            if (passwordChangeMode.value === 'oldPassword') {
              params.oldPassword = passwordForm.oldPassword
            } else if (passwordChangeMode.value === 'emailCode') {
              params.emailCode = passwordForm.emailCode
            }
            
            const res = await changePassword(params)
            if (res.code === 200) {
              // 清除表单数据
              passwordForm.emailCode = ''
              passwordForm.oldPassword = ''
              passwordForm.newPassword = ''
              passwordForm.confirmPassword = ''
              // 清除倒计时
              if (countdownTimer) {
                clearInterval(countdownTimer)
                countdownTimer = null
              }
              countdown.value = 0
              
              // 显示成功提示并跳转到登录页面
              ElMessageBox.alert('密码修改成功，请重新登录', '提示', {
                confirmButtonText: '确定',
                type: 'success',
                callback: () => {
                  // 清除本地存储的登录信息
                  localStorage.removeItem('token')
                  localStorage.removeItem('userInfo')
                  // 跳转到登录页面
                  router.push('/login')
                }
              })
            }
          } catch (error) {
            ElMessage.error(error.message || '密码修改失败')
          }
        }
      })
    }

    onMounted(() => {
      loadUserInfo()
    })

    onBeforeUnmount(() => {
      if (countdownTimer) {
        clearInterval(countdownTimer)
      }
    })

    return {
      activeTab,
      passwordChangeMode,
      userForm,
      passwordForm,
      passwordFormRef,
      userFormRef,
      userRules,
      passwordRules,
      handleUpdateInfo,
      handleChangePassword,
      getPasswordStrengthText,
      sendEmailCode,
      sendingCode,
      countdown,
      handlePasswordModeChange
    }
  }
}
</script>

<style scoped>
.user-profile {
  padding: 20px;
}
</style>

