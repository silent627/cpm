/**
 * 批量删除工具函数
 * 用于统一处理批量删除逻辑
 */

import { ElMessage, ElMessageBox } from 'element-plus'

/**
 * 执行批量删除
 * @param {Object} options - 配置选项
 * @param {Array} options.selectedRows - 选中的行数据
 * @param {Function} options.deleteApi - 删除API函数
 * @param {Function} options.loadData - 数据加载函数
 * @param {string} options.entityName - 实体名称（如'用户'、'居民'），用于提示消息
 * @param {string} options.confirmMessage - 确认消息模板，默认'确定要删除选中的 {count} 个{entityName}吗？'
 * @param {string} options.successMessage - 成功消息模板，默认'成功删除 {count} 个{entityName}'
 * @param {string} options.errorMessage - 错误消息，默认'批量删除失败'
 * @returns {Promise<void>}
 */
export const executeBatchDelete = async (options) => {
  const {
    selectedRows,
    deleteApi,
    loadData,
    entityName = '数据',
    confirmMessage,
    successMessage,
    errorMessage = '批量删除失败'
  } = options
  
  if (!selectedRows || selectedRows.length === 0) {
    ElMessage.warning(`请选择要删除的${entityName}`)
    return
  }
  
  try {
    const count = selectedRows.length
    const defaultConfirmMessage = confirmMessage || `确定要删除选中的 ${count} 个${entityName}吗？`
    
    await ElMessageBox.confirm(
      defaultConfirmMessage,
      '批量删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const ids = selectedRows.map(row => row.id)
    const res = await deleteApi(ids)
    
    if (res.code === 200) {
      const deletedCount = res.data || ids.length
      const defaultSuccessMessage = successMessage || `成功删除 ${deletedCount} 个${entityName}`
      ElMessage.success(defaultSuccessMessage)
      selectedRows.length = 0 // 清空选中项
      if (loadData) {
        loadData()
      }
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(errorMessage)
    }
  }
}

/**
 * 创建批量删除处理函数
 * @param {Object} options - 配置选项
 * @returns {Function} - 批量删除处理函数
 */
export const createBatchDeleteHandler = (options) => {
  return () => executeBatchDelete(options)
}

