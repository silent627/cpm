import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'

const service = axios.create({
  baseURL: '/api',
  timeout: 10000
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    // 如果是FormData，不设置Content-Type，让浏览器自动设置
    if (config.data instanceof FormData) {
      delete config.headers['Content-Type']
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    // 如果是blob类型（导出Excel），直接返回
    if (response.config.responseType === 'blob') {
      return response
    }
    
    const res = response.data
    if (res.code === 200) {
      return res
    } else {
      // 对于登录接口和文件删除接口，不在这里显示错误，让组件自己处理
      const isLoginRequest = response.config.url && response.config.url.includes('/auth/login')
      const isFileDeleteRequest = response.config.url && 
                                  (response.config.url.includes('/upload/file') || response.config.url.includes('/upload/')) && 
                                  response.config.method === 'delete'
      
      // 处理401未授权错误
      if (res.code === 401) {
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
        router.push('/login')
        // 401错误不显示消息，直接跳转登录页
        return Promise.reject(new Error(res.message || '未授权'))
      }
      
      // 对于文件删除接口，即使返回错误，也不抛出异常，让调用方自己决定如何处理
      if (isFileDeleteRequest) {
        // 返回错误响应，但不显示错误消息
        return res
      }
      
      // 对于其他业务错误，不在这里显示错误消息，让组件自己处理，避免重复显示
      // 只对登录接口特殊处理（登录失败时可能需要特殊提示）
      if (isLoginRequest) {
        // 登录接口的错误由组件自己处理，不在这里显示
      }
      
      // 返回错误，让组件自己决定是否显示错误消息
      return Promise.reject(new Error(res.message || '请求失败'))
    }
  },
  error => {
    // 对于登录接口和文件删除接口，不在这里显示错误，让组件自己处理
    const isLoginRequest = error.config && error.config.url && error.config.url.includes('/auth/login')
    const isFileDeleteRequest = error.config && error.config.url && 
                                (error.config.url.includes('/upload/file') || error.config.url.includes('/upload/')) && 
                                error.config.method === 'delete'
    
    // 处理401未授权错误
    if (error.response && error.response.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      router.push('/login')
      // 401错误不显示消息，直接跳转登录页
      return Promise.reject(new Error('未授权，请重新登录'))
    }
    
    // 对于文件删除接口，静默处理，不抛出错误，让调用方自己决定如何处理
    if (isFileDeleteRequest) {
      // 返回一个已解决的Promise，避免触发错误处理
      return Promise.resolve({ code: 500, message: '文件删除失败（已静默处理）', data: null })
    }
    
    // 处理429限流错误
    if (error.response && error.response.status === 429) {
      const errorMessage = error.response?.data?.message || '请求过于频繁，请稍后再试'
      return Promise.reject(new Error(errorMessage))
    }
    
    // 对于网络错误（没有response的情况），显示网络错误提示
    // 对于有response的业务错误，不在这里显示，让组件自己处理，避免重复显示
    if (!error.response) {
      // 网络错误（如超时、连接失败等），显示错误提示
      ElMessage.error('网络错误，请检查网络连接')
      return Promise.reject(new Error('网络错误'))
    }
    
    // 对于登录接口，返回更友好的错误信息，但不显示（由组件处理）
    if (isLoginRequest && error.response?.data?.message) {
      return Promise.reject(new Error(error.response.data.message))
    }
    
    // 对于其他有response的错误，不在这里显示，让组件自己处理
    const errorMessage = error.response?.data?.message || error.message || '请求失败'
    return Promise.reject(new Error(errorMessage))
  }
)

export default service

