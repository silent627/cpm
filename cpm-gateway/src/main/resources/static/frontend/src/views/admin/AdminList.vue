<template>
  <div class="admin-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>管理员管理</span>
          <div>
            <el-button type="primary" :icon="Download" @click="handleExport">导出Excel</el-button>
            <el-button type="success" @click="handleAdd">新增管理员</el-button>
          </div>
        </div>
      </template>
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="管理员编号">
          <el-input v-model="searchForm.adminNo" placeholder="请输入管理员编号" clearable />
        </el-form-item>
        <el-form-item label="部门">
          <el-input v-model="searchForm.department" placeholder="请输入部门" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="tableData" border style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" show-overflow-tooltip />
        <el-table-column prop="adminNo" label="管理员编号" show-overflow-tooltip />
        <el-table-column prop="department" label="部门" show-overflow-tooltip />
        <el-table-column prop="position" label="职位" show-overflow-tooltip />
        <el-table-column prop="remark" label="备注" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="180" show-overflow-tooltip />
        <el-table-column label="操作" width="150" fixed="right">
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

    <!-- 新增管理员对话框 -->
    <el-dialog v-model="dialogVisible" title="新增管理员" width="600px">
      <el-form :model="adminForm" label-width="100px">
        <el-form-item label="用户名" required>
          <el-input v-model="adminForm.username" placeholder="请输入用户名" clearable />
        </el-form-item>
        <el-form-item label="密码" required>
          <el-input v-model="adminForm.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="真实姓名">
          <el-input v-model="adminForm.realName" placeholder="请输入真实姓名" clearable />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="adminForm.phone" placeholder="请输入手机号" clearable />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="adminForm.email" placeholder="请输入邮箱" clearable />
        </el-form-item>
        <el-form-item label="管理员编号">
          <el-input v-model="adminForm.adminNo" placeholder="请输入管理员编号" clearable />
        </el-form-item>
        <el-form-item label="部门">
          <el-input v-model="adminForm.department" placeholder="请输入部门" clearable />
        </el-form-item>
        <el-form-item label="职位">
          <el-input v-model="adminForm.position" placeholder="请输入职位" clearable />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="adminForm.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 管理员详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="管理员详情" width="600px">
      <el-descriptions :column="2" border v-if="adminDetail">
        <el-descriptions-item label="ID">{{ adminDetail.id }}</el-descriptions-item>
        <el-descriptions-item label="用户ID">{{ adminDetail.userId }}</el-descriptions-item>
        <el-descriptions-item label="管理员编号">{{ adminDetail.adminNo }}</el-descriptions-item>
        <el-descriptions-item label="部门">{{ adminDetail.department }}</el-descriptions-item>
        <el-descriptions-item label="职位">{{ adminDetail.position }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ adminDetail.remark }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ adminDetail.createTime }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ adminDetail.updateTime }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { getAdminList, getAdminById, createAdmin, exportAdmins } from '../../api/admin'
import { ElMessage } from 'element-plus'
import { View, Download } from '@element-plus/icons-vue'
import { createPagination, createPaginationHandlers } from '../../utils/pagination'
import { createExportHandler } from '../../utils/export'
import { createSearchHandler, createResetHandler } from '../../utils/search'

export default {
  name: 'AdminList',
  components: {
    View,
    Download
  },
  setup() {
    const tableData = ref([])
    const searchForm = reactive({
      adminNo: '',
      department: ''
    })
    const pagination = createPagination({ current: 1, size: 10, total: 0 })
    const dialogVisible = ref(false)
    const adminForm = reactive({
      username: '',
      password: '',
      realName: '',
      phone: '',
      email: '',
      adminNo: '',
      department: '',
      position: '',
      remark: ''
    })
    const detailDialogVisible = ref(false)
    const adminDetail = ref(null)

    const loadData = async () => {
      try {
        const params = {
          current: pagination.current,
          size: pagination.size,
          ...searchForm
        }
        const res = await getAdminList(params)
        if (res.code === 200) {
          tableData.value = res.data.records || []
          pagination.total = res.data.total || 0
        } else {
          ElMessage.error(res.message || '加载数据失败')
        }
      } catch (error) {
        ElMessage.error(error.message || '加载数据失败')
      }
    }

    // 使用工具函数创建搜索和重置处理函数
    const handleSearch = createSearchHandler(pagination, loadData)
    const handleReset = createResetHandler(searchForm, pagination, loadData)
    
    // 使用工具函数创建分页处理函数
    const { handleSizeChange, handleCurrentChange } = createPaginationHandlers(pagination, loadData)

    const handleAdd = () => {
      Object.assign(adminForm, {
        username: '',
        password: '',
        realName: '',
        phone: '',
        email: '',
        adminNo: '',
        department: '',
        position: '',
        remark: ''
      })
      dialogVisible.value = true
    }

    const handleSubmit = async () => {
      try {
        const res = await createAdmin(adminForm)
        if (res.code === 200) {
          ElMessage.success('创建成功')
          dialogVisible.value = false
          loadData()
        }
      } catch (error) {
        ElMessage.error(error.message || '创建失败')
      }
    }

    const handleView = async (row) => {
      try {
        const res = await getAdminById(row.id)
        if (res.code === 200) {
          adminDetail.value = res.data
          detailDialogVisible.value = true
        }
      } catch (error) {
        ElMessage.error('获取详情失败')
      }
    }

    // 使用工具函数创建导出处理函数
    const handleExport = createExportHandler(
      exportAdmins,
      () => ({
          adminNo: searchForm.adminNo || undefined,
          department: searchForm.department || undefined
      }),
      '管理员列表.xlsx',
      '导出成功'
    )

    onMounted(() => {
      loadData()
    })

    return {
      tableData,
      searchForm,
      pagination,
      dialogVisible,
      adminForm,
      detailDialogVisible,
      adminDetail,
      handleSearch,
      handleReset,
      handleSizeChange,
      handleCurrentChange,
      handleAdd,
      handleSubmit,
      handleView,
      handleExport,
      Download
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
</style>

