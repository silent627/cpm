import request from './request'

export const addMember = (data) => {
  return request({
    url: '/household-member/add',
    method: 'post',
    data
  })
}

export const removeMember = (params) => {
  return request({
    url: '/household-member/remove',
    method: 'delete',
    params
  })
}

export const getMembersByHouseholdId = (householdId, params) => {
  return request({
    url: `/household-member/list/${householdId}`,
    method: 'get',
    params
  })
}

