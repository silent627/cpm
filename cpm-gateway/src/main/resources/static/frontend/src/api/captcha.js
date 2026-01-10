import request from './request'

// 生成验证码
export const generateCaptcha = () => {
  return request({
    url: '/captcha/generate',
    method: 'get'
  })
}

