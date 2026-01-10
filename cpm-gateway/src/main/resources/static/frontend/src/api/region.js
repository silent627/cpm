import request from './request'

/**
 * 获取所有省份
 */
export function getProvinces() {
  return request({
    url: '/region/provinces',
    method: 'get'
  })
}

/**
 * 获取下级行政区划
 * @param {string} parentCode 父级区划代码
 */
export function getChildren(parentCode) {
  return request({
    url: `/region/children/${parentCode}`,
    method: 'get'
  })
}

/**
 * 获取数据统计信息
 */
export function getStats() {
  return request({
    url: '/region/stats',
    method: 'get'
  })
}

