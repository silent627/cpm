/**
 * 搜索和重置工具函数
 * 用于统一处理搜索表单的逻辑
 */

/**
 * 创建搜索处理函数
 * @param {Object} pagination - 分页对象
 * @param {Function} loadData - 数据加载函数
 * @returns {Function} - 搜索处理函数
 */
export const createSearchHandler = (pagination, loadData) => {
  return () => {
    if (pagination) {
      pagination.current = 1
    }
    if (loadData) {
      loadData()
    }
  }
}

/**
 * 创建重置处理函数
 * @param {Object} searchForm - 搜索表单对象
 * @param {Object} pagination - 分页对象（可选）
 * @param {Function} loadData - 数据加载函数（可选）
 * @param {Function} customReset - 自定义重置逻辑（可选）
 * @returns {Function} - 重置处理函数
 */
export const createResetHandler = (searchForm, pagination = null, loadData = null, customReset = null) => {
  return () => {
    // 重置搜索表单的所有字段
    Object.keys(searchForm).forEach(key => {
      if (Array.isArray(searchForm[key])) {
        searchForm[key] = []
      } else if (typeof searchForm[key] === 'object' && searchForm[key] !== null) {
        // 如果是对象，递归重置
        Object.keys(searchForm[key]).forEach(subKey => {
          if (Array.isArray(searchForm[key][subKey])) {
            searchForm[key][subKey] = []
          } else {
            searchForm[key][subKey] = ''
          }
        })
      } else {
        searchForm[key] = ''
      }
    })
    
    // 执行自定义重置逻辑
    if (customReset && typeof customReset === 'function') {
      customReset()
    }
    
    // 重置分页并重新加载数据
    if (pagination) {
      pagination.current = 1
    }
    if (loadData) {
      loadData()
    }
  }
}

