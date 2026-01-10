import request from './request'

// 获取居民年龄分布统计
export const getResidentAgeDistribution = () => {
  return request({
    url: '/statistics/resident/age-distribution',
    method: 'get'
  })
}

// 获取居民性别统计
export const getResidentGenderStatistics = () => {
  return request({
    url: '/statistics/resident/gender',
    method: 'get'
  })
}

// 获取户籍类型统计
export const getHouseholdTypeStatistics = () => {
  return request({
    url: '/statistics/household/type',
    method: 'get'
  })
}

// 获取户籍迁入迁出趋势
export const getHouseholdMoveTrend = (type = 'month') => {
  return request({
    url: '/statistics/household/move-trend',
    method: 'get',
    params: { type }
  })
}

// 获取月度数据统计
export const getMonthlyStatistics = () => {
  return request({
    url: '/statistics/monthly',
    method: 'get'
  })
}

// 获取年度数据统计
export const getYearlyStatistics = () => {
  return request({
    url: '/statistics/yearly',
    method: 'get'
  })
}

