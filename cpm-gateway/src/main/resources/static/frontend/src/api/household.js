import request from './request'

export const getHouseholdList = (params) => {
  return request({
    url: '/household/list',
    method: 'get',
    params
  })
}

export const getHouseholdById = (id) => {
  return request({
    url: `/household/${id}`,
    method: 'get'
  })
}

export const createHousehold = (data) => {
  return request({
    url: '/household/create',
    method: 'post',
    data
  })
}

export const updateHousehold = (id, data) => {
  return request({
    url: `/household/update/${id}`,
    method: 'put',
    data
  })
}

export const deleteHousehold = (id) => {
  return request({
    url: `/household/${id}`,
    method: 'delete'
  })
}

export const getHouseholdByNo = (householdNo) => {
  return request({
    url: `/household/no/${householdNo}`,
    method: 'get'
  })
}

export const moveOutHousehold = (id, data) => {
  return request({
    url: `/household/move-out/${id}`,
    method: 'post',
    data
  })
}

// 导出户籍列表
export const exportHouseholds = (params) => {
  return request({
    url: '/household/export',
    method: 'get',
    params,
    responseType: 'blob' // 重要：指定响应类型为blob
  })
}

