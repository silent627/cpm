/**
 * 表单验证工具函数
 * 用于统一处理表单验证规则
 */

/**
 * 验证手机号（11位数字，1开头）
 * @param {string} phone - 手机号
 * @returns {boolean} - 是否有效
 */
export const isValidPhone = (phone) => {
  if (!phone || typeof phone !== 'string') return false
  return /^1[3-9]\d{9}$/.test(phone.trim())
}

/**
 * 验证邮箱格式
 * @param {string} email - 邮箱地址
 * @returns {boolean} - 是否有效
 */
export const isValidEmail = (email) => {
  if (!email || typeof email !== 'string') return false
  return /^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/.test(email.trim())
}

/**
 * 验证身份证号（18位数字）
 * @param {string} idCard - 身份证号
 * @returns {boolean} - 是否有效
 */
export const isValidIdCard = (idCard) => {
  if (!idCard || typeof idCard !== 'string') return false
  return /^\d{18}$/.test(idCard.trim())
}

/**
 * 验证密码长度（只检查长度，不强制其他规则）
 * @param {string} password - 密码
 * @returns {boolean} - 是否有效（长度>=8位）
 */
export const validatePasswordLength = (password) => {
  return password && typeof password === 'string' && password.length >= 8
}

/**
 * 验证密码强度（仅用于提示，不强制）
 * @param {string} password - 密码
 * @returns {Object} - { valid: boolean, strength: number, message: string }
 *                     strength: 0-弱密码, 1-中等密码, 2-强密码
 */
export const validatePasswordStrength = (password) => {
  if (!password || typeof password !== 'string') {
    return { valid: false, strength: 0, message: '密码不能为空' }
  }
  
  if (password.length < 8) {
    return { valid: false, strength: 0, message: '密码长度至少8位' }
  }
  
  const hasLetter = /[A-Za-z]/.test(password)
  const hasNumber = /\d/.test(password)
  const hasUpper = /[A-Z]/.test(password)
  const hasLower = /[a-z]/.test(password)
  const hasSpecial = /[@$!%*?&]/.test(password)
  
  // 强密码：包含大小写字母、数字和特殊字符
  if (hasUpper && hasLower && hasNumber && hasSpecial) {
    return { valid: true, strength: 2, message: '强密码' }
  }
  
  // 中等密码：至少包含字母和数字
  if (hasLetter && hasNumber) {
    return { valid: true, strength: 1, message: '中等密码' }
  }
  
  // 弱密码（仅作为提示，不强制）
  return { valid: true, strength: 0, message: '弱密码（建议包含大小写字母、数字和特殊字符）' }
}

/**
 * 创建Element Plus表单验证器
 */

/**
 * 创建手机号验证器
 * @param {Object} options - 配置选项
 * @param {boolean} options.required - 是否必填，默认false
 * @param {string} options.message - 自定义错误消息
 * @returns {Function} - Element Plus验证器函数
 */
export const createPhoneValidator = (options = {}) => {
  const { required = false, message } = options
  return (rule, value, callback) => {
    if (!value || value.trim() === '') {
      if (required) {
        callback(new Error(message || '请输入手机号'))
      } else {
        callback()
      }
    } else if (!isValidPhone(value)) {
      callback(new Error(message || '手机号格式不正确，应为11位数字且以1开头'))
    } else {
      callback()
    }
  }
}

/**
 * 创建邮箱验证器
 * @param {Object} options - 配置选项
 * @param {boolean} options.required - 是否必填，默认false
 * @param {string} options.message - 自定义错误消息
 * @returns {Function} - Element Plus验证器函数
 */
export const createEmailValidator = (options = {}) => {
  const { required = false, message } = options
  return (rule, value, callback) => {
    if (!value || value.trim() === '') {
      if (required) {
        callback(new Error(message || '请输入邮箱'))
      } else {
        callback()
      }
    } else if (!isValidEmail(value)) {
      callback(new Error(message || '邮箱格式不正确'))
    } else {
      callback()
    }
  }
}

/**
 * 创建身份证号验证器
 * @param {Object} options - 配置选项
 * @param {boolean} options.required - 是否必填，默认true
 * @param {string} options.message - 自定义错误消息
 * @returns {Function} - Element Plus验证器函数
 */
export const createIdCardValidator = (options = {}) => {
  const { required = true, message } = options
  return (rule, value, callback) => {
    if (!value || value.trim() === '') {
      if (required) {
        callback(new Error(message || '请输入身份证号'))
      } else {
        callback()
      }
    } else if (!isValidIdCard(value)) {
      callback(new Error(message || '身份证号必须为18位数字'))
    } else {
      callback()
    }
  }
}

/**
 * 创建密码验证器（只检查长度，不强制其他规则）
 * @param {Object} options - 配置选项
 * @param {boolean} options.required - 是否必填，默认true
 * @param {string} options.message - 自定义错误消息
 * @returns {Function} - Element Plus验证器函数
 */
export const createPasswordValidator = (options = {}) => {
  const { required = true, message } = options
  return (rule, value, callback) => {
    if (!value || value.trim() === '') {
      if (required) {
        callback(new Error(message || '请输入密码'))
      } else {
        callback()
      }
    } else if (!validatePasswordLength(value)) {
      callback(new Error(message || '密码长度至少8位'))
    } else {
      // 只检查长度，其他规则仅作为提醒（不强制）
      callback()
    }
  }
}

/**
 * 获取密码强度文本（用于显示，仅作为提醒）
 * @param {string} password - 密码
 * @returns {string} - 强度文本
 */
export const getPasswordStrengthText = (password) => {
  if (!password) return ''
  if (password.length < 8) {
    return '密码长度至少8位'
  }
  const result = validatePasswordStrength(password)
  const strengthTexts = ['弱密码', '中等密码', '强密码']
  return strengthTexts[result.strength] + (result.strength === 0 ? '（建议包含大小写字母、数字和特殊字符）' : '')
}

