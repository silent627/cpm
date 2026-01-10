/**
 * 导出功能工具函数
 * 用于统一处理Excel导出逻辑
 */

import { ElMessage } from 'element-plus'

/**
 * 处理Excel导出响应
 * @param {Object} response - axios响应对象
 * @param {string} defaultFileName - 默认文件名
 * @param {string} successMessage - 成功消息，默认'导出成功'
 * @returns {Promise<void>}
 */
export const handleExportResponse = async (response, defaultFileName = '导出数据.xlsx', successMessage = '导出成功') => {
  try {
    const blob = new Blob([response.data], { 
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' 
    })
    
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    
    // 从响应头中获取文件名
    const contentDisposition = response.headers['content-disposition']
    let fileName = defaultFileName
    if (contentDisposition) {
      const fileNameMatch = contentDisposition.match(/filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/)
      if (fileNameMatch && fileNameMatch[1]) {
        fileName = decodeURIComponent(fileNameMatch[1].replace(/['"]/g, ''))
      }
    }
    
    link.setAttribute('download', fileName)
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    
    ElMessage.success(successMessage)
  } catch (error) {
    ElMessage.error('导出失败：' + (error.message || '未知错误'))
    throw error
  }
}

/**
 * 创建导出处理函数
 * @param {Function} exportApi - 导出API函数
 * @param {Function} getParams - 获取导出参数的函数
 * @param {string} defaultFileName - 默认文件名
 * @param {string} successMessage - 成功消息
 * @returns {Function} - 导出处理函数
 */
export const createExportHandler = (exportApi, getParams, defaultFileName = '导出数据.xlsx', successMessage = '导出成功') => {
  return async () => {
    try {
      const params = getParams()
      const response = await exportApi(params)
      await handleExportResponse(response, defaultFileName, successMessage)
    } catch (error) {
      // 错误已在handleExportResponse中处理
      console.error('导出失败:', error)
    }
  }
}

