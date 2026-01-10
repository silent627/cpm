import request from './request'

// 上传居民头像
export const uploadAvatar = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/upload/avatar',
    method: 'post',
    data: formData
  })
}

// 上传身份证照片
export const uploadIdCard = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/upload/idCard',
    method: 'post',
    data: formData
  })
}

// 通用文件上传
export const uploadFile = (file, subPath = 'files') => {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('subPath', subPath)
  return request({
    url: '/upload/file',
    method: 'post',
    data: formData
  })
}

// 删除文件
export const deleteFile = (url) => {
  return request({
    url: '/upload/file',
    method: 'delete',
    params: { url }
  })
}

