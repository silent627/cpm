import request from './request'

export const login = (data) => {
  return request({
    url: '/auth/login',
    method: 'post',
    data
  })
}

export const logout = () => {
  return request({
    url: '/auth/logout',
    method: 'post'
  })
}

// 发送忘记密码验证码
export const sendForgetPasswordCode = (email) => {
  return request({
    url: '/auth/forget-password/send-code',
    method: 'post',
    data: { email }
  })
}

// 验证忘记密码验证码
export const verifyForgetPasswordCode = (data) => {
  return request({
    url: '/auth/forget-password/verify-code',
    method: 'post',
    data
  })
}

// 重置密码（忘记密码）
export const resetPassword = (data) => {
  return request({
    url: '/auth/forget-password/reset',
    method: 'post',
    data
  })
}
