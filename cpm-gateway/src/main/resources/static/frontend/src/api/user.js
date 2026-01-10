import request from './request'

export const register = (data) => {
  return request({
    url: '/user/register',
    method: 'post',
    data
  })
}

export const getUserList = (params) => {
  return request({
    url: '/user/list',
    method: 'get',
    params
  })
}

export const getUserInfo = () => {
  return request({
    url: '/user/info',
    method: 'get'
  })
}

export const updateUser = (data) => {
  return request({
    url: '/user/update',
    method: 'put',
    data
  })
}

// 发送修改密码验证码
export const sendChangePasswordCode = () => {
  return request({
    url: '/user/change-password/send-code',
    method: 'post'
  })
}

export const changePassword = (data) => {
  return request({
    url: '/user/change-password',
    method: 'post',
    data
  })
}

export const getUserById = (id) => {
  return request({
    url: `/user/${id}`,
    method: 'get'
  })
}

export const updateUserStatus = (id, data) => {
  return request({
    url: `/user/status/${id}`,
    method: 'put',
    data
  })
}

export const updateUserById = (id, data) => {
  return request({
    url: `/user/update/${id}`,
    method: 'put',
    data
  })
}

export const deleteUser = (id) => {
  return request({
    url: `/user/${id}`,
    method: 'delete'
  })
}

// 导出用户列表
export const exportUsers = (params) => {
  return request({
    url: '/user/export',
    method: 'get',
    params,
    responseType: 'blob' // 重要：指定响应类型为blob
  })
}

// 批量删除用户
export const batchDeleteUsers = (ids) => {
  return request({
    url: '/user/batch/delete',
    method: 'post',
    data: { ids }
  })
}

