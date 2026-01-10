/**
 * 分页处理工具函数
 * 用于统一处理分页逻辑
 */

import { reactive } from 'vue'

/**
 * 创建分页对象
 * @param {Object} options - 配置选项
 * @param {number} options.current - 当前页码，默认1
 * @param {number} options.size - 每页大小，默认10
 * @param {number} options.total - 总记录数，默认0
 * @returns {Object} - 分页响应式对象
 */
export const createPagination = (options = {}) => {
  return reactive({
    current: options.current || 1,
    size: options.size || 10,
    total: options.total || 0
  })
}

/**
 * 创建分页处理函数
 * @param {Object} pagination - 分页对象
 * @param {Function} loadData - 数据加载函数
 * @returns {Object} - { handleSizeChange, handleCurrentChange }
 */
export const createPaginationHandlers = (pagination, loadData) => {
  const handleSizeChange = () => {
    pagination.current = 1
    loadData()
  }
  
  const handleCurrentChange = () => {
    loadData()
  }
  
  return {
    handleSizeChange,
    handleCurrentChange
  }
}

/**
 * 重置分页到第一页
 * @param {Object} pagination - 分页对象
 */
export const resetPagination = (pagination) => {
  if (pagination) {
    pagination.current = 1
  }
}

