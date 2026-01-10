import request from './request'

export const getResidentList = (params) => {
  return request({
    url: '/resident/list',
    method: 'get',
    params
  })
}

export const getResidentById = (id) => {
  return request({
    url: `/resident/${id}`,
    method: 'get'
  })
}

export const createResident = (data) => {
  return request({
    url: '/resident/create',
    method: 'post',
    data
  })
}

export const updateResident = (data) => {
  return request({
    url: `/resident/update/${data.id}`,
    method: 'put',
    data
  })
}

export const getResidentInfo = () => {
  return request({
    url: '/resident/info',
    method: 'get'
  })
}

export const getResidentByIdCard = (idCard) => {
  return request({
    url: `/resident/idCard/${idCard}`,
    method: 'get'
  })
}

export const deleteResident = (id) => {
  return request({
    url: `/resident/${id}`,
    method: 'delete'
  })
}

// 导出居民列表
export const exportResidents = (params) => {
  return request({
    url: '/resident/export',
    method: 'get',
    params,
    responseType: 'blob' // 重要：指定响应类型为blob
  })
}

// 批量删除居民
export const batchDeleteResidents = (ids) => {
  return request({
    url: '/resident/batch/delete',
    method: 'post',
    data: { ids }
  })
}

// 下载导入模板
export const downloadResidentTemplate = () => {
  return request({
    url: '/resident/import/template',
    method: 'get',
    responseType: 'blob'
  })
}

// 导入居民信息
export const importResidents = (formData) => {
  return request({
    url: '/resident/import',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

