<template>
  <div class="login-container">
    <div class="login-box">
      <h2 class="login-title">社区人口管理系统</h2>
      <el-form :model="loginForm" :rules="rules" ref="loginFormRef" class="login-form">
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="用户名"
            prefix-icon="User"
            size="large"
            clearable
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="密码"
            prefix-icon="Lock"
            size="large"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        <el-form-item prop="captchaCode" v-if="showCaptcha">
          <div style="display: flex; gap: 10px; align-items: flex-start;">
            <el-input
              v-model="loginForm.captchaCode"
              placeholder="验证码"
              size="large"
              style="flex: 1"
              @keyup.enter="handleLogin"
            />
            <div style="cursor: pointer; border: 1px solid #dcdfe6; border-radius: 4px; overflow: hidden;" @click="refreshCaptcha">
              <img :src="captchaImage" alt="验证码" style="height: 40px; display: block;" />
            </div>
          </div>
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            @click="handleLogin"
            style="width: 100%"
          >
            登录
          </el-button>
        </el-form-item>
        <el-form-item>
          <el-button
            type="text"
            @click="goToForgetPassword"
            style="width: 100%; text-align: right; padding-right: 0"
          >
            忘记密码？
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { login } from '../api/auth'
import { generateCaptcha } from '../api/captcha'
import { ElMessage } from 'element-plus'

export default {
  name: 'Login',
  setup() {
    const router = useRouter()
    const loginFormRef = ref(null)
    const loading = ref(false)
    const showCaptcha = ref(false)
    const captchaImage = ref('')
    const captchaKey = ref('')
    const loginForm = reactive({
      username: '',
      password: '',
      captchaCode: ''
    })

    const rules = {
      username: [
        { required: true, message: '请输入用户名', trigger: 'blur' }
      ],
      password: [
        { required: true, message: '请输入密码', trigger: 'blur' }
      ],
      captchaCode: [
        { required: true, message: '请输入验证码', trigger: 'blur', validator: (rule, value, callback) => {
          if (showCaptcha.value && (!value || value.trim() === '')) {
            callback(new Error('请输入验证码'))
          } else {
            callback()
          }
        }}
      ]
    }

    // 加载验证码
    const loadCaptcha = async () => {
      try {
        const res = await generateCaptcha()
        if (res.code === 200) {
          captchaImage.value = res.data.image
          captchaKey.value = res.data.key
        }
      } catch (error) {
        console.error('加载验证码失败', error)
      }
    }

    // 刷新验证码
    const refreshCaptcha = () => {
      loadCaptcha()
      loginForm.captchaCode = ''
    }

    const goToForgetPassword = () => {
      router.push('/forget-password')
    }

    const handleLogin = async () => {
      if (!loginFormRef.value) return
      
      await loginFormRef.value.validate(async (valid) => {
        if (valid) {
          loading.value = true
          try {
            const loginData = {
              username: loginForm.username,
              password: loginForm.password
            }
            // 如果需要验证码，添加验证码信息
            if (showCaptcha.value) {
              loginData.captchaKey = captchaKey.value
              loginData.captchaCode = loginForm.captchaCode
            }
            
            const res = await login(loginData)
            if (res.code === 200) {
              localStorage.setItem('token', res.data.token)
              localStorage.setItem('userInfo', JSON.stringify(res.data))
              ElMessage.success('登录成功')
              router.push('/dashboard')
            }
          } catch (error) {
            const errorMsg = error.message || '登录失败'
            ElMessage.error(errorMsg)
            
            // 如果错误信息包含"验证码"或"失败次数"，显示验证码
            if (errorMsg.includes('验证码') || errorMsg.includes('失败次数')) {
              showCaptcha.value = true
              await loadCaptcha()
            } else {
              // 刷新验证码
              refreshCaptcha()
            }
          } finally {
            loading.value = false
          }
        }
      })
    }

    onMounted(() => {
      // 初始加载验证码（可选，也可以等失败后再显示）
    })

    return {
      loginFormRef,
      loginForm,
      rules,
      loading,
      handleLogin,
      showCaptcha,
      captchaImage,
      refreshCaptcha,
      goToForgetPassword
    }
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-box {
  width: 400px;
  padding: 40px;
  background: white;
  border-radius: 10px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
}

.login-title {
  text-align: center;
  margin-bottom: 30px;
  color: #333;
  font-size: 24px;
}

.login-form {
  margin-top: 20px;
}

.login-tip {
  margin-top: 20px;
  text-align: center;
  color: #999;
  font-size: 12px;
}

.login-tip p {
  margin: 5px 0;
}
</style>

