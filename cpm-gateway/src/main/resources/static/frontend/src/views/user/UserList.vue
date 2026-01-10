<template>
  <div class="user-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>用户管理</span>
          <div>
            <el-button type="danger" :icon="Delete" @click="handleBatchDelete" :disabled="selectedRows.length === 0">批量删除</el-button>
            <el-button type="primary" :icon="Download" @click="handleExport">导出Excel</el-button>
            <el-button type="success" @click="handleAdd">新增用户</el-button>
          </div>
        </div>
      </template>
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="用户名">
          <el-input v-model="searchForm.username" placeholder="请输入用户名" clearable />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="searchForm.role" placeholder="请选择角色" clearable style="width: 150px">
            <el-option label="全部" value="" />
            <el-option label="管理员" value="ADMIN" />
            <el-option label="普通用户" value="USER" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="tableData" border style="width: 100%" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" show-overflow-tooltip />
        <el-table-column prop="username" label="用户名" show-overflow-tooltip />
        <el-table-column prop="realName" label="真实姓名" show-overflow-tooltip />
        <el-table-column prop="phone" label="手机号" show-overflow-tooltip />
        <el-table-column prop="email" label="邮箱" show-overflow-tooltip />
        <el-table-column prop="role" label="角色" show-overflow-tooltip>
          <template #default="{ row }">
            <el-tag :type="row.role === 'ADMIN' ? 'danger' : 'primary'">
              {{ row.role === 'ADMIN' ? '管理员' : '普通用户' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" show-overflow-tooltip>
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" show-overflow-tooltip />
        <el-table-column label="操作" width="300" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button 
                type="primary" 
                size="small" 
                :icon="View" 
                @click="handleView(row)"
                link
                class="action-btn"
              >
                查看
              </el-button>
              <el-button 
                type="success" 
                size="small" 
                :icon="Edit" 
                @click="handleEdit(row)"
                link
                class="action-btn"
              >
                编辑
              </el-button>
              <el-button 
                :type="row.status === 1 ? 'warning' : 'success'" 
                size="small" 
                :icon="row.status === 1 ? SwitchButton : CircleCheck" 
                @click="handleEditStatus(row)"
                link
                class="action-btn"
              >
                {{ row.status === 1 ? '禁用' : '启用' }}
              </el-button>
              <el-button 
                type="danger" 
                size="small" 
                :icon="Delete" 
                @click="handleDelete(row)"
                link
                class="action-btn"
              >
                删除
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="pagination.current"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>

    <!-- 新增/编辑用户对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑用户' : '新增用户'" width="500px">
      <el-form :model="userForm" :rules="userRules" label-width="100px" ref="userFormRef">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username" placeholder="请输入用户名" clearable :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="密码" prop="password" :required="!isEdit">
          <el-input v-model="userForm.password" type="password" placeholder="请输入密码" show-password />
          <div v-if="userForm.password" style="font-size: 12px; color: #909399; margin-top: 5px;">
            {{ getPasswordStrengthText(userForm.password) }}
          </div>
        </el-form-item>
        <el-form-item label="真实姓名">
          <el-input v-model="userForm.realName" placeholder="请输入真实姓名" clearable />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="userForm.phone" placeholder="请输入手机号" clearable />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="userForm.email" placeholder="请输入邮箱" clearable />
        </el-form-item>
        <el-form-item label="头像">
          <el-upload
            class="avatar-uploader"
            :http-request="handleAvatarUpload"
            :show-file-list="false"
            :before-upload="beforeAvatarUpload"
          >
            <img v-if="isValidImageUrl(userForm.avatar)" :src="getImageUrl(userForm.avatar)" class="avatar" @error="handleEditAvatarError" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="userForm.role" style="width: 100%">
            <el-option label="普通用户" value="USER" />
            <el-option label="管理员" value="ADMIN" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 用户详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="用户详情" width="700px">
      <el-tabs v-if="userDetail">
        <el-tab-pane label="基本信息">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="ID">{{ userDetail.id }}</el-descriptions-item>
            <el-descriptions-item label="用户名">{{ userDetail.username }}</el-descriptions-item>
            <el-descriptions-item label="真实姓名">{{ userDetail.realName }}</el-descriptions-item>
            <el-descriptions-item label="手机号">{{ userDetail.phone }}</el-descriptions-item>
            <el-descriptions-item label="邮箱">{{ userDetail.email }}</el-descriptions-item>
            <el-descriptions-item label="角色">
              <el-tag :type="userDetail.role === 'ADMIN' ? 'danger' : 'primary'">
                {{ userDetail.role === 'ADMIN' ? '管理员' : '普通用户' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="userDetail.status === 1 ? 'success' : 'danger'">
                {{ userDetail.status === 1 ? '启用' : '禁用' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ userDetail.createTime }}</el-descriptions-item>
            <el-descriptions-item label="更新时间">{{ userDetail.updateTime }}</el-descriptions-item>
          </el-descriptions>
        </el-tab-pane>
        <el-tab-pane label="头像管理">
          <div class="photo-management">
            <div class="photo-item">
              <div class="photo-label">头像</div>
              <div class="photo-content">
                <el-upload
                  class="avatar-uploader"
                  :http-request="handleDetailAvatarUpload"
                  :show-file-list="false"
                  :before-upload="beforeAvatarUpload"
                >
                  <img v-if="userDetail && userDetail.avatar && userDetail.avatar.trim() !== ''" :src="getImageUrl(userDetail.avatar)" class="avatar" @error="handleImageError" />
                  <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
                </el-upload>
                <div class="photo-actions" v-if="userDetail && userDetail.avatar && userDetail.avatar.trim() !== ''">
                  <el-button type="danger" size="small" @click="handleDeleteAvatar">删除</el-button>
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-dialog>

  </div>
</template>

<script>
import { ref, reactive, onMounted, watch } from 'vue'
import { getUserList, getUserById, updateUserStatus, register, updateUserById, deleteUser, exportUsers, getUserInfo, batchDeleteUsers } from '../../api/user'
import { uploadAvatar, deleteFile } from '../../api/upload'
import { ElMessage, ElMessageBox } from 'element-plus'
import { View, SwitchButton, CircleCheck, Edit, Delete, Download, Plus } from '@element-plus/icons-vue'
import { isValidImageUrl, getImageUrl, sanitizeImageField, sanitizeImageFields, createImageErrorHandler, validateImageFile } from '../../utils/image'
import { createPhoneValidator, createEmailValidator, createPasswordValidator, getPasswordStrengthText } from '../../utils/validation'
import { createPagination, createPaginationHandlers } from '../../utils/pagination'
import { createExportHandler } from '../../utils/export'
import { createBatchDeleteHandler } from '../../utils/batchDelete'
import { createSearchHandler, createResetHandler } from '../../utils/search'

export default {
  name: 'UserList',
  components: {
    View,
    SwitchButton,
    CircleCheck,
    Edit,
    Delete,
    Download,
    Plus
  },
  setup() {
    const tableData = ref([])
    const searchForm = reactive({
      username: '',
      role: ''
    })
    const pagination = createPagination({ current: 1, size: 10, total: 0 })
    const dialogVisible = ref(false)
    const isEdit = ref(false)
    const userForm = reactive({
      id: null,
      username: '',
      password: '',
      realName: '',
      phone: '',
      email: '',
      role: 'USER',
      avatar: ''
    })
    const detailDialogVisible = ref(false)
    const userDetail = ref(null)
    const selectedRows = ref([])
    const userFormRef = ref(null)
    
    // 记录原始表单数据，用于取消时恢复
    const originalUserForm = reactive({
      avatar: ''
    })

    // 创建密码验证器（考虑编辑模式）
    const validatePassword = (rule, value, callback) => {
      if (!isEdit.value && (!value || value.trim() === '')) {
        callback(new Error('请输入密码'))
      } else if (value && value.trim() !== '') {
        const validator = createPasswordValidator({ required: false, minStrength: 1 })
        validator(rule, value, callback)
      } else {
        callback()
      }
    }

    const userRules = {
      username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
      password: [{ validator: validatePassword, trigger: 'blur' }],
      phone: [{ validator: createPhoneValidator({ required: false }), trigger: 'blur' }],
      email: [{ validator: createEmailValidator({ required: false }), trigger: 'blur' }]
    }

    const loadData = async () => {
      try {
        const params = {
          current: pagination.current,
          size: pagination.size,
          ...searchForm
        }
        const res = await getUserList(params)
        if (res.code === 200) {
          tableData.value = res.data.records || []
          pagination.total = res.data.total || 0
        }
      } catch (error) {
        ElMessage.error('加载数据失败')
      }
    }

    // 使用工具函数创建搜索和重置处理函数
    const handleSearch = createSearchHandler(pagination, loadData)
    const handleReset = createResetHandler(searchForm, pagination, loadData)
    
    // 使用工具函数创建分页处理函数
    const { handleSizeChange, handleCurrentChange } = createPaginationHandlers(pagination, loadData)

    const handleAdd = () => {
      isEdit.value = false
      Object.assign(userForm, {
        id: null,
        username: '',
        password: '',
        realName: '',
        phone: '',
        email: '',
        role: 'USER',
        avatar: ''
      })
      // 重置原始头像URL
      originalUserForm.avatar = ''
      dialogVisible.value = true
    }

    const handleEdit = async (row) => {
      isEdit.value = true
      try {
        const res = await getUserById(row.id)
        if (res.code === 200) {
          // 使用工具函数清理图片字段
          const sanitizedData = sanitizeImageFields(res.data, ['avatar'])
          
          Object.assign(userForm, {
            id: sanitizedData.id,
            username: sanitizedData.username,
            password: '',
            realName: sanitizedData.realName || '',
            phone: sanitizedData.phone || '',
            email: sanitizedData.email || '',
            role: sanitizedData.role || 'USER',
            avatar: sanitizedData.avatar
          })
          // 保存原始头像URL，用于取消时判断是否需要删除
          originalUserForm.avatar = sanitizedData.avatar || ''
          dialogVisible.value = true
        }
      } catch (error) {
        ElMessage.error('获取用户信息失败')
      }
    }

    const syncCurrentUserInfo = async (userId = null) => {
      try {
        const currentUserInfoStr = localStorage.getItem('userInfo')
        if (currentUserInfoStr) {
          const currentUserInfo = JSON.parse(currentUserInfoStr)
          const targetUserId = userId || userForm.id || userDetail.value?.id
          if (targetUserId && targetUserId === currentUserInfo.id) {
            const res = await getUserInfo()
            if (res.code === 200 && res.data) {
              localStorage.setItem('userInfo', JSON.stringify(res.data))
              window.dispatchEvent(new CustomEvent('userInfoUpdated'))
            }
          }
        }
      } catch (error) {
        // 静默处理同步失败
      }
    }

    const handleSubmit = async () => {
      if (!userFormRef.value) return
      
      await userFormRef.value.validate(async (valid) => {
        if (valid) {
          try {
            if (isEdit.value) {
              const res = await updateUserById(userForm.id, userForm)
              if (res.code === 200) {
                ElMessage.success('更新成功')
                // 更新原始值，避免关闭对话框时误删已保存的图片
                originalUserForm.avatar = userForm.avatar
                dialogVisible.value = false
                loadData()
                // 同步更新当前登录用户信息
                await syncCurrentUserInfo()
              }
            } else {
              const res = await register(userForm)
              if (res.code === 200) {
                ElMessage.success('创建成功')
                // 更新原始值，避免关闭对话框时误删已保存的图片
                originalUserForm.avatar = userForm.avatar
                dialogVisible.value = false
                loadData()
              }
            }
          } catch (error) {
            ElMessage.error(error.message || '操作失败')
          }
        }
      })
    }

    const handleDelete = async (row) => {
      try {
        await ElMessageBox.confirm(
          `确定要删除用户 "${row.username}" 吗？`,
          '提示',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }
        )
        const res = await deleteUser(row.id)
        if (res.code === 200) {
          ElMessage.success('删除成功')
          loadData()
        }
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error('删除失败')
        }
      }
    }

    const handleView = async (row) => {
      try {
        const res = await getUserById(row.id)
        if (res.code === 200 && res.data) {
          // 使用工具函数清理图片字段
          const userData = sanitizeImageFields(res.data, ['avatar'])
          userDetail.value = userData
          detailDialogVisible.value = true
        }
      } catch (error) {
        ElMessage.error('获取详情失败')
      }
    }

    const handleEditStatus = async (row) => {
      try {
        await ElMessageBox.confirm(
          `确定要${row.status === 1 ? '禁用' : '启用'}该用户吗？`,
          '提示',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }
        )
        const res = await updateUserStatus(row.id, { status: row.status === 1 ? 0 : 1 })
        if (res.code === 200) {
          ElMessage.success('状态更新成功')
          loadData()
        }
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error('状态更新失败')
        }
      }
    }

    // 使用工具函数创建导出处理函数
    const handleExport = createExportHandler(
      exportUsers,
      () => ({
        username: searchForm.username || undefined,
        role: searchForm.role || undefined
      }),
      '用户列表.xlsx',
      '导出成功'
    )

    // 使用工具函数创建错误处理函数
    const handleEditAvatarError = createImageErrorHandler(userForm, 'avatar')
    const handleImageError = (event) => {
      createImageErrorHandler(userDetail, 'avatar')(event)
    }
    
    const handleAvatarUpload = async (options) => {
      try {
        const res = await uploadAvatar(options.file)
        if (res.code === 200) {
          userForm.avatar = res.data
          ElMessage.success('头像上传成功')
          if (isEdit.value && userForm.id) {
            await syncCurrentUserInfo()
          }
        } else {
          ElMessage.error(res.message || '头像上传失败')
        }
      } catch (error) {
        ElMessage.error(error.message || '头像上传失败')
      }
    }
    
    const beforeAvatarUpload = (file) => {
      return validateImageFile(file, {
        maxSize: 10,
        onError: (msg) => ElMessage.error(msg)
      })
    }
    
    const handleDetailAvatarUpload = async (options) => {
      try {
        const res = await uploadAvatar(options.file)
        if (res.code === 200) {
          userDetail.value.avatar = res.data
          userForm.avatar = res.data
          await updateUserById(userDetail.value.id, {
            avatar: res.data
          })
          ElMessage.success('头像上传成功')
          await syncCurrentUserInfo(userDetail.value.id)
        } else {
          ElMessage.error(res.message || '头像上传失败')
        }
      } catch (error) {
        ElMessage.error(error.message || '头像上传失败')
      }
    }
    
    const handleDeleteAvatar = async () => {
      try {
        await ElMessageBox.confirm('确定要删除头像吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        const userId = userDetail.value.id
        const avatarUrl = userDetail.value.avatar
        
        await updateUserById(userId, {
          avatar: null
        })
        
        userDetail.value.avatar = ''
        userForm.avatar = ''
        
        if (avatarUrl && avatarUrl.trim() !== '' && avatarUrl !== 'null' && avatarUrl !== 'undefined') {
          deleteFile(avatarUrl).catch(() => {})
        }
        
        ElMessage.success('删除成功')
        await syncCurrentUserInfo(userId)
      } catch (error) {
        if (error !== 'cancel' && error.message !== 'cancel') {
          ElMessage.error('删除失败: ' + (error.message || '未知错误'))
        }
      }
    }

    const handleSelectionChange = (selection) => {
      selectedRows.value = selection
    }

    // 使用工具函数创建批量删除处理函数
    const handleBatchDelete = createBatchDeleteHandler({
      get selectedRows() { return selectedRows.value },
      deleteApi: batchDeleteUsers,
      loadData,
      entityName: '用户'
    })

    watch(detailDialogVisible, (newVal) => {
      if (!newVal) {
        userDetail.value = null
      }
    })

    // 监听编辑/新增对话框关闭，如果用户取消操作，删除新上传但未保存的图片
    watch(dialogVisible, (newVal) => {
      if (!newVal) {
        // 对话框关闭时，检查是否有新上传但未保存的图片
        // 如果当前图片URL与原始值不同，说明是新上传的，需要删除
        const deletePromises = []
        
        // 检查头像
        if (userForm.avatar && 
            userForm.avatar.trim() !== '' && 
            userForm.avatar !== originalUserForm.avatar &&
            isValidImageUrl(userForm.avatar)) {
          deletePromises.push(
            deleteFile(userForm.avatar).catch(() => {
              // 静默处理删除失败，不影响用户体验
            })
          )
        }
        
        // 执行删除操作（不等待结果，静默处理）
        if (deletePromises.length > 0) {
          Promise.all(deletePromises).catch(() => {
            // 静默处理，不显示错误消息
          })
        }
      }
    })

    onMounted(() => {
      loadData()
    })

    return {
      tableData,
      searchForm,
      pagination,
      dialogVisible,
      userForm,
      detailDialogVisible,
      userDetail,
      handleSearch,
      handleReset,
      handleSizeChange,
      handleCurrentChange,
      handleAdd,
      handleEdit,
      handleSubmit,
      handleView,
      handleEditStatus,
      handleDelete,
      handleExport,
      isEdit,
      Download,
      Plus,
      isValidImageUrl,
      getImageUrl,
      handleEditAvatarError,
      handleImageError,
      handleAvatarUpload,
      beforeAvatarUpload,
      handleDetailAvatarUpload,
      handleDeleteAvatar,
      selectedRows,
      handleSelectionChange,
      handleBatchDelete,
      userFormRef,
      userRules,
      getPasswordStrengthText
    }
  }
}
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-form {
  margin-bottom: 20px;
}

.action-buttons {
  display: flex;
  gap: 4px;
  align-items: center;
  justify-content: center;
  flex-wrap: wrap;
}

.action-buttons .action-btn {
  margin: 0;
  padding: 4px 8px;
  font-size: 13px;
  transition: all 0.3s ease;
  border-radius: 4px;
  font-weight: 500;
}

.action-buttons .action-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.12);
}

.action-buttons .el-button--primary.action-btn:hover {
  background-color: rgba(64, 158, 255, 0.1);
}

.action-buttons .el-button--warning.action-btn:hover {
  background-color: rgba(230, 162, 60, 0.1);
}

.action-buttons .el-button--info.action-btn:hover {
  background-color: rgba(144, 147, 153, 0.1);
}

.action-buttons .el-button--success.action-btn:hover {
  background-color: rgba(103, 194, 58, 0.1);
}

.action-buttons .el-button--danger.action-btn:hover {
  background-color: rgba(245, 108, 108, 0.1);
}

.avatar-uploader {
  display: flex;
  align-items: center;
}

.avatar-uploader .avatar {
  width: 100px;
  height: 100px;
  display: block;
  border-radius: 4px;
  object-fit: cover;
  border: 1px solid #dcdfe6;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 100px;
  height: 100px;
  line-height: 100px;
  text-align: center;
  border: 1px dashed #d9d9d9;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.avatar-uploader-icon:hover {
  border-color: #409EFF;
}

.photo-management {
  padding: 20px;
}

.photo-item {
  margin-bottom: 30px;
}

.photo-label {
  font-weight: bold;
  margin-bottom: 10px;
  color: #333;
}

.photo-content {
  display: flex;
  align-items: flex-start;
  gap: 20px;
}

.photo-actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
</style>

