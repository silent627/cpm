<template>
  <div class="household-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>户籍管理</span>
          <div v-if="isAdmin">
            <el-button type="primary" :icon="Download" @click="handleExport">导出Excel</el-button>
            <el-button type="success" @click="handleAdd">新增户籍</el-button>
          </div>
        </div>
      </template>
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="户籍编号">
          <el-input v-model="searchForm.householdNo" placeholder="请输入户籍编号" clearable />
        </el-form-item>
        <el-form-item label="户主姓名">
          <el-input v-model="searchForm.headName" placeholder="请输入户主姓名" clearable />
        </el-form-item>
        <el-form-item label="户籍地址">
          <el-input v-model="searchForm.address" placeholder="请输入户籍地址" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 150px">
            <el-option label="全部" :value="null" />
            <el-option label="正常" :value="1" />
            <el-option label="迁出" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="tableData" border style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" show-overflow-tooltip />
        <el-table-column prop="householdNo" label="户籍编号" show-overflow-tooltip />
        <el-table-column prop="headName" label="户主姓名" show-overflow-tooltip />
        <el-table-column prop="headIdCard" label="户主身份证号" width="180" show-overflow-tooltip />
        <el-table-column prop="address" label="户籍地址" show-overflow-tooltip />
        <el-table-column prop="householdType" label="户别" width="100" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.householdType === 1 ? '家庭户' : '集体户' }}
          </template>
        </el-table-column>
        <el-table-column prop="memberCount" label="户人数" width="100" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100" show-overflow-tooltip>
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '正常' : '迁出' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" show-overflow-tooltip />
        <el-table-column label="操作" :width="isAdmin ? 360 : 150" fixed="right">
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
                type="info" 
                size="small" 
                :icon="User" 
                @click="handleMembers(row)"
                link
                class="action-btn"
              >
                成员
              </el-button>
              <template v-if="isAdmin">
                <el-button 
                  type="warning" 
                  size="small" 
                  :icon="Edit" 
                  @click="handleEdit(row)"
                  link
                  class="action-btn"
                >
                  编辑
                </el-button>
                <el-button 
                  type="success" 
                  size="small" 
                  :icon="Right" 
                  @click="handleMoveOut(row)" 
                  v-if="row.status === 1"
                  link
                  class="action-btn"
                >
                  迁出
                </el-button>
                <el-button 
                  type="danger" 
                  size="small" 
                  :icon="Delete" 
                  @click="handleDelete(row)" 
                  v-if="row.status === 1"
                  link
                  class="action-btn"
                >
                  删除
                </el-button>
              </template>
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

    <!-- 新增/编辑户籍对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑户籍' : '新增户籍'" width="800px">
      <el-form :model="householdForm" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="户籍编号" v-if="!isEdit">
              <el-input v-model="householdForm.householdNo" placeholder="不填则自动生成" clearable />
            </el-form-item>
            <el-form-item label="户主姓名">
              <el-input v-model="householdForm.headName" placeholder="请输入户主姓名" clearable />
            </el-form-item>
            <el-form-item label="户主身份证号">
              <el-input v-model="householdForm.headIdCard" placeholder="请输入户主身份证号" clearable />
            </el-form-item>
            <el-form-item label="户籍地址" required>
              <div class="address-selector-wrapper">
                <RegionCascade
                  ref="regionCascadeRef"
                  v-model="addressCodes"
                  :placeholder="['请选择省份', '请选择市', '请选择区县', '请选择乡镇街道']"
                  @change="handleAddressChange"
                />
                <el-input
                  v-model="householdForm.address"
                  placeholder="请先选择行政区划，然后输入详细地址（如：XX街道XX号）"
                  clearable
                  class="address-detail-input"
                >
                  <template #prefix>
                    <el-icon><Location /></el-icon>
                  </template>
                </el-input>
              </div>
            </el-form-item>
            <el-form-item label="户别">
              <el-select v-model="householdForm.householdType" style="width: 100%">
                <el-option label="家庭户" :value="1" />
                <el-option label="集体户" :value="2" />
              </el-select>
            </el-form-item>
            <el-form-item label="户人数">
              <el-input-number v-model="householdForm.memberCount" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话">
              <el-input v-model="householdForm.contactPhone" placeholder="请输入联系电话" clearable />
            </el-form-item>
            <el-form-item label="迁入日期">
              <el-date-picker
                v-model="householdForm.moveInDate"
                type="datetime"
                placeholder="选择日期时间"
                format="YYYY-MM-DD HH:mm:ss"
                value-format="YYYY-MM-DD HH:mm:ss"
                style="width: 100%"
                clearable
              />
            </el-form-item>
            <el-form-item label="迁入原因">
              <el-input v-model="householdForm.moveInReason" placeholder="请输入迁入原因" clearable />
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="householdForm.status" style="width: 100%">
                <el-option label="正常" :value="1" />
                <el-option label="迁出" :value="0" />
              </el-select>
            </el-form-item>
            <el-form-item label="备注">
              <el-input v-model="householdForm.remark" type="textarea" :rows="3" placeholder="请输入备注" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 户籍详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="户籍详情" width="800px">
      <el-descriptions :column="2" border v-if="householdDetail">
        <el-descriptions-item label="ID">{{ householdDetail.id }}</el-descriptions-item>
        <el-descriptions-item label="户籍编号">{{ householdDetail.householdNo }}</el-descriptions-item>
        <el-descriptions-item label="户主姓名">{{ householdDetail.headName }}</el-descriptions-item>
        <el-descriptions-item label="户主身份证号">{{ householdDetail.headIdCard }}</el-descriptions-item>
        <el-descriptions-item label="户籍地址" :span="2">{{ householdDetail.address }}</el-descriptions-item>
        <el-descriptions-item label="户别">
          {{ householdDetail.householdType === 1 ? '家庭户' : '集体户' }}
        </el-descriptions-item>
        <el-descriptions-item label="户人数">{{ householdDetail.memberCount }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ householdDetail.contactPhone }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="householdDetail.status === 1 ? 'success' : 'danger'">
            {{ householdDetail.status === 1 ? '正常' : '迁出' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="迁入日期">{{ householdDetail.moveInDate }}</el-descriptions-item>
        <el-descriptions-item label="迁入原因">{{ householdDetail.moveInReason }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ householdDetail.createTime }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ householdDetail.updateTime }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 户籍成员管理对话框 -->
    <el-dialog v-model="membersDialogVisible" title="户籍成员管理" width="900px">
      <HouseholdMemberManage :household-id="currentHouseholdId" @refresh="loadData" />
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted, computed, watch, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import { getHouseholdList, getHouseholdById, createHousehold, updateHousehold, deleteHousehold, moveOutHousehold, exportHouseholds } from '../../api/household'
import { ElMessage, ElMessageBox } from 'element-plus'
import { View, Edit, User, Delete, Right, Download, Location } from '@element-plus/icons-vue'
import HouseholdMemberManage from './HouseholdMemberManage.vue'
import RegionCascade from '../../components/RegionCascade.vue'
import { createPagination, createPaginationHandlers } from '../../utils/pagination'
import { createExportHandler } from '../../utils/export'
import { createSearchHandler, createResetHandler } from '../../utils/search'

export default {
  name: 'HouseholdList',
  components: {
    HouseholdMemberManage,
    RegionCascade,
    View,
    Edit,
    User,
    Delete,
    Right,
    Download,
    Location
  },
  setup() {
    const route = useRoute()
    const userInfo = ref(null)
    
    // 获取用户信息
    const loadUserInfo = () => {
      const userInfoStr = localStorage.getItem('userInfo')
      if (userInfoStr) {
        try {
          userInfo.value = JSON.parse(userInfoStr)
        } catch (e) {
          // 静默处理解析失败
        }
      }
    }
    
    const isAdmin = computed(() => {
      return userInfo.value?.role === 'ADMIN'
    })
    
    const tableData = ref([])
    const searchForm = reactive({
      householdNo: '',
      headName: '',
      address: '',
      status: null
    })
    const pagination = createPagination({ current: 1, size: 10, total: 0 })
    const dialogVisible = ref(false)
    const isEdit = ref(false)
    const householdForm = reactive({
      headId: null,
      headName: '',
      headIdCard: '',
      householdNo: '',
      address: '',
      householdType: 1,
      memberCount: 0,
      contactPhone: '',
      moveInDate: '',
      moveInReason: '',
      status: 1,
      remark: ''
    })
    const detailDialogVisible = ref(false)
    const householdDetail = ref(null)
    const membersDialogVisible = ref(false)
    const currentHouseholdId = ref(null)
    
    // 级联选择器的值
    const addressCodes = ref([])
    const regionCascadeRef = ref(null)
    
    // 处理户籍地址级联选择变化
    const handleAddressChange = (data) => {
      if (data && data.names && data.names.length > 0) {
        // 将级联选择的路径作为地址前缀（只使用已选择的级别）
        const prefix = data.names.join('')
        const currentAddress = householdForm.address || ''
        
        // 如果地址为空，直接设置前缀
        if (!currentAddress) {
          householdForm.address = prefix
        } else {
          // 提取当前地址中的详细地址部分（去除级联选择的前缀）
          let detailPart = currentAddress
          
          // 如果当前地址以级联选择的前缀开头，则提取后面的详细地址
          if (currentAddress.startsWith(prefix)) {
            detailPart = currentAddress.substring(prefix.length).trim()
          } else {
            // 如果不以当前前缀开头，尝试提取可能的详细地址部分
            let lastMatchIndex = -1
            for (let i = data.names.length - 1; i >= 0; i--) {
              const name = data.names[i]
              const index = currentAddress.lastIndexOf(name)
              if (index !== -1 && index > lastMatchIndex) {
                lastMatchIndex = index + name.length
                break
              }
            }
            
            if (lastMatchIndex > 0) {
              detailPart = currentAddress.substring(lastMatchIndex).trim()
            } else {
              const hasAnyMatch = data.names.some(name => currentAddress.includes(name))
              if (!hasAnyMatch) {
                detailPart = currentAddress
              }
            }
          }
          
          // 如果详细地址部分以级联选择的前缀开头，则去除
          if (detailPart.startsWith(prefix)) {
            detailPart = detailPart.substring(prefix.length).trim()
          }
          
          // 组合最终地址
          householdForm.address = prefix + (detailPart ? detailPart : '')
        }
      }
    }

    const loadData = async () => {
      try {
        const params = {
          current: pagination.current,
          size: pagination.size,
          ...searchForm
        }
        const res = await getHouseholdList(params)
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
      addressCodes.value = []
      Object.assign(householdForm, {
        headId: null,
        headName: '',
        headIdCard: '',
        householdNo: '',
        address: '',
        householdType: 1,
        memberCount: 0,
        contactPhone: '',
        moveInDate: '',
        moveInReason: '',
        status: 1,
        remark: ''
      })
      dialogVisible.value = true
    }

    const handleEdit = async (row) => {
      isEdit.value = true
      addressCodes.value = []
      
      // 先保存原始地址，用于解析
      const originalAddress = row.address || ''
      
      Object.assign(householdForm, {
        ...row,
        moveInDate: row.moveInDate ? row.moveInDate.replace('T', ' ').substring(0, 19) : ''
      })
      
      dialogVisible.value = true
      
      // 使用 nextTick 确保 DOM 更新完成，然后立即开始解析
      await nextTick()
      
      // 解析地址
      if (originalAddress && regionCascadeRef.value) {
        try {
          const result = await regionCascadeRef.value.parseAddress(originalAddress)
          if (result && result.detail !== undefined) {
            // 将详细地址部分更新到表单
            householdForm.address = result.detail
          } else if (result && result.codes.length === 0) {
            // 如果无法解析，保持原地址
            householdForm.address = originalAddress
          }
        } catch (error) {
          console.error('解析地址失败:', error)
          // 解析失败时，保持原地址
          householdForm.address = originalAddress
        }
      }
    }

    const handleSubmit = async () => {
      try {
        if (isEdit.value) {
          const res = await updateHousehold(householdForm.id, householdForm)
          if (res.code === 200) {
            ElMessage.success('更新成功')
            dialogVisible.value = false
            loadData()
          }
        } else {
          const res = await createHousehold(householdForm)
          if (res.code === 200) {
            ElMessage.success('创建成功')
            dialogVisible.value = false
            loadData()
          }
        }
      } catch (error) {
        ElMessage.error(error.message || '操作失败')
      }
    }

    const handleView = async (row) => {
      try {
        const res = await getHouseholdById(row.id)
        if (res.code === 200) {
          householdDetail.value = res.data
          detailDialogVisible.value = true
        }
      } catch (error) {
        ElMessage.error('获取详情失败')
      }
    }

    const handleDelete = async (row) => {
      try {
        await ElMessageBox.confirm('确定要删除该户籍吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        const res = await deleteHousehold(row.id)
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

    const handleMoveOut = async (row) => {
      try {
        const { value } = await ElMessageBox.prompt('请输入迁出原因', '户籍迁出', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          inputPlaceholder: '请输入迁出原因'
        })
        const res = await moveOutHousehold(row.id, { reason: value })
        if (res.code === 200) {
          ElMessage.success('迁出成功')
          loadData()
        }
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error('迁出失败')
        }
      }
    }

    const handleMembers = (row) => {
      currentHouseholdId.value = row.id
      membersDialogVisible.value = true
    }

    // 使用工具函数创建导出处理函数
    const handleExport = createExportHandler(
      exportHouseholds,
      () => ({
        householdNo: searchForm.householdNo || undefined,
        headName: searchForm.headName || undefined,
        address: searchForm.address || undefined,
        status: searchForm.status !== null ? searchForm.status : undefined
      }),
      '户籍列表.xlsx',
      '导出成功'
    )

    watch(() => route.path, () => {
      loadUserInfo()
    }, { immediate: true })

    onMounted(() => {
      loadUserInfo()
      loadData()
    })

    return {
      tableData,
      searchForm,
      pagination,
      dialogVisible,
      isEdit,
      householdForm,
      detailDialogVisible,
      householdDetail,
      membersDialogVisible,
      currentHouseholdId,
      handleSearch,
      handleReset,
      handleSizeChange,
      handleCurrentChange,
      addressCodes,
      regionCascadeRef,
      handleAddressChange,
      handleAdd,
      handleEdit,
      handleSubmit,
      handleView,
      handleDelete,
      handleMoveOut,
      handleMembers,
      handleExport,
      isAdmin,
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

.address-selector-wrapper {
  width: 100%;
}

.address-detail-input {
  margin-top: 12px;
  transition: all 0.3s ease;
}

.address-detail-input :deep(.el-input__wrapper) {
  border-radius: 6px;
  transition: all 0.3s ease;
}

.address-detail-input :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #c0c4cc inset;
}

.address-detail-input :deep(.el-input.is-focus .el-input__wrapper) {
  box-shadow: 0 0 0 1px #409eff inset;
}

.address-detail-input :deep(.el-input__prefix) {
  color: #909399;
  padding-left: 12px;
}
</style>

