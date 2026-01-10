import request from './request'

export const getAdminInfo = () => {
  return request({
    url: '/admin/info',
    method: 'get'
  })
}

export const getAdminList = (params) => {
  return request({
    url: '/admin/list',
    method: 'get',
    params
  })
}

export const createAdmin = (data) => {
  return request({
    url: '/admin/create',
    method: 'post',
    data
  })
}

export const updateAdmin = (data) => {
  return request({
    url: '/admin/update',
    method: 'put',
    data
  })
}

export const getAdminById = (id) => {
  return request({
    url: `/admin/${id}`,
    method: 'get'
  })
}

// 导出管理员列表
export const exportAdmins = (params) => {
  return request({
    url: '/admin/export',
    method: 'get',
    params,
    responseType: 'blob' // 重要：指定响应类型为blob
  })
}

