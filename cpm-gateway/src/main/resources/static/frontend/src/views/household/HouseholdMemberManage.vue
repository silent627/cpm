<template>
  <div class="household-member-manage">
    <div style="margin-bottom: 20px">
      <el-button type="primary" @click="handleAddMember">添加成员</el-button>
    </div>
    <el-table :data="memberList" border style="width: 100%" v-loading="loading">
      <el-table-column prop="realName" label="姓名" width="120" show-overflow-tooltip />
      <el-table-column prop="idCard" label="身份证号" width="180" show-overflow-tooltip />
      <el-table-column prop="gender" label="性别" width="80" show-overflow-tooltip>
        <template #default="{ row }">
          {{ row.gender === 1 ? '男' : '女' }}
        </template>
      </el-table-column>
      <el-table-column prop="relationship" label="与户主关系" width="120" show-overflow-tooltip />
      <el-table-column prop="birthDate" label="出生日期" width="120" show-overflow-tooltip />
      <el-table-column prop="nationality" label="民族" width="100" show-overflow-tooltip />
      <el-table-column prop="currentAddress" label="现居住地址" show-overflow-tooltip min-width="200" />
      <el-table-column prop="occupation" label="职业" width="120" show-overflow-tooltip />
      <el-table-column prop="education" label="文化程度" width="120" show-overflow-tooltip />
      <el-table-column prop="createTime" label="创建时间" width="180" show-overflow-tooltip />
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="{ row }">
          <el-button type="danger" size="small" @click="handleRemove(row)">移除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      v-model:current-page="pagination.current"
      v-model:page-size="pagination.size"
      :total="pagination.total"
      :page-sizes="[10, 20, 50]"
      layout="total, sizes, prev, pager, next"
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
      style="margin-top: 20px"
    />

    <!-- 添加成员对话框 -->
    <el-dialog v-model="addMemberDialogVisible" title="添加成员" width="500px">
      <el-form :model="memberForm" label-width="120px">
        <el-form-item label="居民ID" required>
          <el-input-number v-model="memberForm.residentId" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="与户主关系">
          <el-select v-model="memberForm.relationship" placeholder="请选择" style="width: 100%">
            <el-option label="户主" value="户主" />
            <el-option label="配偶" value="配偶" />
            <el-option label="子女" value="子女" />
            <el-option label="父母" value="父母" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addMemberDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitAdd">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted, watch } from 'vue'
import { getMembersByHouseholdId, addMember, removeMember } from '../../api/householdMember'
import { ElMessage, ElMessageBox } from 'element-plus'

export default {
  name: 'HouseholdMemberManage',
  props: {
    householdId: {
      type: Number,
      required: true
    }
  },
  emits: ['refresh'],
  setup(props, { emit }) {
    const memberList = ref([])
    const loading = ref(false)
    const pagination = reactive({
      current: 1,
      size: 10,
      total: 0
    })
    const addMemberDialogVisible = ref(false)
    const memberForm = reactive({
      residentId: null,
      relationship: ''
    })

    const loadData = async () => {
      if (!props.householdId) return
      loading.value = true
      try {
        const params = {
          current: pagination.current,
          size: pagination.size
        }
        const res = await getMembersByHouseholdId(props.householdId, params)
        if (res.code === 200) {
          memberList.value = res.data.records || []
          pagination.total = res.data.total || 0
        }
      } catch (error) {
        ElMessage.error('加载成员列表失败')
      } finally {
        loading.value = false
      }
    }

    const handleSizeChange = () => {
      loadData()
    }

    const handleCurrentChange = () => {
      loadData()
    }

    const handleAddMember = () => {
      memberForm.residentId = null
      memberForm.relationship = ''
      addMemberDialogVisible.value = true
    }

    const handleSubmitAdd = async () => {
      if (!memberForm.residentId) {
        ElMessage.warning('请输入居民ID')
        return
      }
      try {
        const res = await addMember({
          householdId: props.householdId,
          residentId: memberForm.residentId,
          relationship: memberForm.relationship
        })
        if (res.code === 200) {
          ElMessage.success('添加成功')
          addMemberDialogVisible.value = false
          loadData()
          emit('refresh')
        }
      } catch (error) {
        ElMessage.error(error.message || '添加失败')
      }
    }

    const handleRemove = async (row) => {
      try {
        await ElMessageBox.confirm('确定要移除该成员吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        const res = await removeMember({
          householdId: props.householdId,
          residentId: row.residentId
        })
        if (res.code === 200) {
          ElMessage.success('移除成功')
          loadData()
          emit('refresh')
        }
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error('移除失败')
        }
      }
    }

    watch(() => props.householdId, () => {
      if (props.householdId) {
        loadData()
      }
    }, { immediate: true })

    onMounted(() => {
      if (props.householdId) {
        loadData()
      }
    })

    return {
      memberList,
      loading,
      pagination,
      addMemberDialogVisible,
      memberForm,
      handleSizeChange,
      handleCurrentChange,
      handleAddMember,
      handleSubmitAdd,
      handleRemove
    }
  }
}
</script>

<style scoped>
.household-member-manage {
  padding: 10px;
}
</style>

