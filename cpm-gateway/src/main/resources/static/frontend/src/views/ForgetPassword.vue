<template>
  <div class="forget-password-container">
    <div class="forget-password-box">
      <h2 class="forget-password-title">忘记密码</h2>
      <el-steps :active="currentStep" finish-status="success" style="margin-bottom: 30px">
        <el-step title="验证邮箱" />
        <el-step title="重置密码" />
      </el-steps>

      <!-- 第一步：验证邮箱 -->
      <el-form v-if="currentStep === 0" :model="emailForm" :rules="emailRules" ref="emailFormRef" class="forget-password-form">
        <el-form-item prop="email">
          <el-input
            v-model="emailForm.email"
            placeholder="请输入注册邮箱"
            prefix-icon="Message"
            size="large"
            clearable
          />
        </el-form-item>
        <el-form-item prop="code">
          <div style="display: flex; gap: 10px;">
            <el-input
              v-model="emailForm.code"
              placeholder="请输入验证码"
              size="large"
              style="flex: 1"
            />
            <el-button
              type="primary"
              size="large"
              :disabled="countdown > 0"
              @click="sendCode"
              :loading="sendingCode"
            >
              {{ countdown > 0 ? `${countdown}秒后重试` : '发送验证码' }}
            </el-button>
          </div>
          <div style="font-size: 12px; color: #909399; margin-top: 5px;">
            验证码有效期为5分钟，请及时输入
          </div>
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            @click="handleNext"
            style="width: 100%"
          >
            下一步
          </el-button>
        </el-form-item>
        <el-form-item>
          <el-button
            type="text"
            @click="goToLogin"
            style="width: 100%; text-align: center"
          >
            返回登录
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 第二步：重置密码 -->
      <el-form v-if="currentStep === 1" :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" class="forget-password-form">
        <el-form-item label="邮箱">
          <el-input v-model="emailForm.email" disabled size="large" />
        </el-form-item>
        <el-form-item prop="newPassword">
          <el-input
            v-model="passwordForm.newPassword"
            type="password"
            placeholder="请输入新密码"
            prefix-icon="Lock"
            size="large"
            show-password
          />
          <div v-if="passwordForm.newPassword" style="font-size: 12px; color: #909399; margin-top: 5px;">
            {{ getPasswordStrengthText(passwordForm.newPassword) }}
          </div>
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input
            v-model="passwordForm.confirmPassword"
            type="password"
            placeholder="请确认新密码"
            prefix-icon="Lock"
            size="large"
            show-password
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            @click="handleReset"
            style="width: 100%"
          >
            重置密码
          </el-button>
        </el-form-item>
        <el-form-item>
          <el-button
            type="text"
            @click="currentStep = 0"
            style="width: 100%; text-align: center"
          >
            上一步
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script>
import { ref, reactive, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { sendForgetPasswordCode, verifyForgetPasswordCode, resetPassword } from '../api/auth'
import { ElMessage } from 'element-plus'
import { Message, Lock } from '@element-plus/icons-vue'

export default {
  name: 'ForgetPassword',
  components: { Message, Lock },
  setup() {
    const router = useRouter()
    const currentStep = ref(0)
    const loading = ref(false)
    const sendingCode = ref(false)
    const countdown = ref(0)
    const emailFormRef = ref(null)
    const passwordFormRef = ref(null)

    const emailForm = reactive({
      email: '',
      code: ''
    })

    const passwordForm = reactive({
      newPassword: '',
      confirmPassword: ''
    })

    let countdownTimer = null

    // 验证邮箱
    const validateEmail = (rule, value, callback) => {
      if (!value || value.trim() === '') {
        callback(new Error('请输入邮箱'))
      } else if (!/^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/.test(value)) {
        callback(new Error('邮箱格式不正确'))
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

    const emailRules = {
      email: [{ validator: validateEmail, trigger: 'blur' }],
      code: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
    }

    const passwordRules = {
      newPassword: [{ validator: validatePassword, trigger: 'blur' }],
      confirmPassword: [{ validator: validateConfirmPassword, trigger: 'blur' }]
    }

    // 发送验证码
    const sendCode = async () => {
      if (!emailFormRef.value) return
      
      await emailFormRef.value.validateField('email', async (valid) => {
        if (valid) {
          sendingCode.value = true
          try {
            const res = await sendForgetPasswordCode(emailForm.email)
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
      })
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

    // 下一步（验证验证码）
    const handleNext = async () => {
      if (!emailFormRef.value) return
      
      await emailFormRef.value.validate(async (valid) => {
        if (valid) {
          // 验证验证码是否正确
          loading.value = true
          try {
            const res = await verifyForgetPasswordCode({
              email: emailForm.email,
              code: emailForm.code
            })
            if (res.code === 200) {
              ElMessage.success('验证码正确')
              currentStep.value = 1
            }
          } catch (error) {
            ElMessage.error(error.message || '验证码错误或已过期')
          } finally {
            loading.value = false
          }
        }
      })
    }

    // 重置密码
    const handleReset = async () => {
      if (!passwordFormRef.value) return
      
      await passwordFormRef.value.validate(async (valid) => {
        if (valid) {
          loading.value = true
          try {
            const res = await resetPassword({
              email: emailForm.email,
              code: emailForm.code,
              newPassword: passwordForm.newPassword
            })
            if (res.code === 200) {
              ElMessage.success('密码重置成功，请使用新密码登录')
              router.push('/login')
            }
          } catch (error) {
            ElMessage.error(error.message || '重置密码失败')
          } finally {
            loading.value = false
          }
        }
      })
    }

    // 返回登录
    const goToLogin = () => {
      // 清除倒计时
      if (countdownTimer) {
        clearInterval(countdownTimer)
        countdownTimer = null
      }
      router.push('/login')
    }

    onBeforeUnmount(() => {
      // 组件销毁时清除倒计时
      if (countdownTimer) {
        clearInterval(countdownTimer)
      }
    })

    return {
      currentStep,
      loading,
      sendingCode,
      countdown,
      emailFormRef,
      passwordFormRef,
      emailForm,
      passwordForm,
      emailRules,
      passwordRules,
      sendCode,
      handleNext,
      handleReset,
      goToLogin,
      getPasswordStrengthText
    }
  }
}
</script>

<style scoped>
.forget-password-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.forget-password-box {
  width: 450px;
  padding: 40px;
  background: white;
  border-radius: 10px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
}

.forget-password-title {
  text-align: center;
  margin-bottom: 30px;
  color: #333;
  font-size: 24px;
}

.forget-password-form {
  margin-top: 20px;
}
</style>

