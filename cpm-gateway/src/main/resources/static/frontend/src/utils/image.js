/**
 * 图片处理工具函数
 * 用于统一处理图片URL验证、转换、错误处理等功能
 */

// 后端API基础地址（可以从环境变量或配置中获取）
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

/**
 * 检查图片URL是否有效
 * @param {string} url - 图片URL
 * @returns {boolean} - 是否有效
 */
export const isValidImageUrl = (url) => {
  if (!url || typeof url !== 'string') return false
  const trimmed = url.trim()
  if (trimmed === '' || trimmed === 'null' || trimmed === 'undefined') return false
  // 确保URL包含有效路径或http协议
  if (!trimmed.includes('/') && !trimmed.startsWith('http')) return false
  return true
}

/**
 * 清理图片字段值，将无效值转换为空字符串
 * @param {any} value - 图片字段值
 * @returns {string} - 清理后的值（空字符串或有效URL）
 */
export const sanitizeImageField = (value) => {
  if (!value || value === null || value === undefined) return ''
  if (value === 'null' || value === 'undefined') return ''
  if (typeof value === 'string' && value.trim() === '') return ''
  return value
}

/**
 * 获取完整的图片URL
 * @param {string} url - 图片URL（相对路径或绝对路径）
 * @returns {string} - 完整的图片URL（带时间戳避免缓存）
 */
export const getImageUrl = (url) => {
  // 先验证URL是否有效
  if (!isValidImageUrl(url)) {
    return ''
  }
  
  // 如果是完整的HTTP/HTTPS URL，直接添加时间戳
  if (url.startsWith('http://') || url.startsWith('https://')) {
    return url + (url.includes('?') ? '&' : '?') + '_t=' + Date.now()
  }
  
  // 如果是相对路径，添加后端地址和时间戳
  return `${API_BASE_URL}${url}${url.includes('?') ? '&' : '?'}_t=${Date.now()}`
}

/**
 * 图片加载错误处理函数
 * @param {Event} event - 错误事件对象
 * @param {Object} options - 配置选项
 * @param {Object} options.dataRef - 数据引用对象（如 userForm, residentForm 等）
 * @param {string} options.fieldName - 字段名称（如 'avatar', 'idCardPhoto'）
 * @param {Function} options.onError - 自定义错误处理回调
 */
export const handleImageError = (event, options = {}) => {
  if (!event || !event.target) return
  
  // 隐藏图片元素，避免显示乱码
  event.target.style.display = 'none'
  
  // 如果提供了数据引用和字段名，清空该字段
  if (options.dataRef && options.fieldName) {
    if (typeof options.dataRef === 'object' && options.dataRef.value) {
      // 处理 ref 对象
      options.dataRef.value[options.fieldName] = ''
    } else if (typeof options.dataRef === 'object') {
      // 处理普通对象
      options.dataRef[options.fieldName] = ''
    }
  }
  
  // 执行自定义错误处理回调
  if (options.onError && typeof options.onError === 'function') {
    options.onError(event, options)
  }
}

/**
 * 创建图片错误处理函数（返回一个绑定了上下文的处理函数）
 * @param {Object} dataRef - 数据引用对象
 * @param {string} fieldName - 字段名称
 * @returns {Function} - 错误处理函数
 */
export const createImageErrorHandler = (dataRef, fieldName) => {
  return (event) => {
    handleImageError(event, { dataRef, fieldName })
  }
}

/**
 * 验证图片文件（上传前验证）
 * @param {File} file - 文件对象
 * @param {Object} options - 配置选项
 * @param {number} options.maxSize - 最大文件大小（MB），默认10MB
 * @param {Array<string>} options.allowedTypes - 允许的文件类型，默认 ['image/*']
 * @param {Function} options.onError - 自定义错误处理回调
 * @returns {boolean} - 是否通过验证
 */
export const validateImageFile = (file, options = {}) => {
  const {
    maxSize = 10,
    allowedTypes = ['image/*'],
    onError
  } = options
  
  // 检查文件类型
  const isAllowedType = allowedTypes.some(type => {
    if (type === 'image/*') {
      return file.type.startsWith('image/')
    }
    return file.type === type
  })
  
  if (!isAllowedType) {
    const errorMsg = '只能上传图片文件!'
    if (onError && typeof onError === 'function') {
      onError(errorMsg)
    } else {
      // 默认使用 Element Plus 的 ElMessage（需要在使用时导入）
      console.error(errorMsg)
    }
    return false
  }
  
  // 检查文件大小
  const fileSizeMB = file.size / 1024 / 1024
  if (fileSizeMB > maxSize) {
    const errorMsg = `图片大小不能超过 ${maxSize}MB!`
    if (onError && typeof onError === 'function') {
      onError(errorMsg)
    } else {
      console.error(errorMsg)
    }
    return false
  }
  
  return true
}

/**
 * 创建图片上传前验证函数（返回一个绑定了配置的验证函数）
 * @param {Object} options - 配置选项
 * @returns {Function} - 验证函数
 */
export const createImageValidator = (options = {}) => {
  return (file) => {
    return validateImageFile(file, options)
  }
}

/**
 * 批量清理对象中的图片字段
 * @param {Object} data - 数据对象
 * @param {Array<string>} imageFields - 图片字段名数组（如 ['avatar', 'idCardPhoto']）
 * @returns {Object} - 清理后的数据对象
 */
export const sanitizeImageFields = (data, imageFields = ['avatar']) => {
  if (!data || typeof data !== 'object') return data
  
  const sanitized = { ...data }
  imageFields.forEach(field => {
    if (field in sanitized) {
      sanitized[field] = sanitizeImageField(sanitized[field])
    }
  })
  
  return sanitized
}

/**
 * 获取默认头像的初始字母
 * @param {Object} userInfo - 用户信息对象
 * @param {string} defaultChar - 默认字符，默认为'用'
 * @returns {string} - 初始字母
 */
export const getAvatarInitial = (userInfo, defaultChar = '用') => {
  try {
    if (!userInfo) return defaultChar
    const name = userInfo.realName || userInfo.username || '用户'
    if (!name || typeof name !== 'string') return defaultChar
    
    // 返回第一个字符，如果是中文返回第一个字，如果是英文返回第一个字母的大写
    const firstChar = name.charAt(0)
    if (!firstChar) return defaultChar
    if (/[a-zA-Z]/.test(firstChar)) {
      return firstChar.toUpperCase()
    }
    return firstChar
  } catch (error) {
    return defaultChar
  }
}

