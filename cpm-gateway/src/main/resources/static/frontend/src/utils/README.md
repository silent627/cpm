# 前端工具函数库

本目录包含前端项目中可复用的工具函数，用于统一处理常见功能。

## 目录结构

- `image.js` - 图片处理工具（URL验证、转换、错误处理等）
- `validation.js` - 表单验证工具（手机号、邮箱、身份证、密码等）
- `pagination.js` - 分页处理工具
- `export.js` - Excel导出工具
- `batchDelete.js` - 批量删除工具
- `search.js` - 搜索和重置工具

## 使用示例

### 1. 图片处理 (image.js)

```javascript
import { isValidImageUrl, getImageUrl, sanitizeImageFields, validateImageFile } from '@/utils/image'

// 验证图片URL
if (isValidImageUrl(userForm.avatar)) {
  // 显示图片
}

// 获取完整图片URL
const imageUrl = getImageUrl('/uploads/avatar.jpg')

// 清理图片字段
const cleanData = sanitizeImageFields(data, ['avatar', 'idCardPhoto'])

// 验证上传文件
const isValid = validateImageFile(file, {
  maxSize: 10,
  onError: (msg) => ElMessage.error(msg)
})
```

### 2. 表单验证 (validation.js)

```javascript
import { 
  createPhoneValidator, 
  createEmailValidator, 
  createIdCardValidator,
  createPasswordValidator,
  getPasswordStrengthText
} from '@/utils/validation'

// 在表单规则中使用
const userRules = {
  phone: [{ validator: createPhoneValidator({ required: false }), trigger: 'blur' }],
  email: [{ validator: createEmailValidator({ required: false }), trigger: 'blur' }],
  idCard: [{ validator: createIdCardValidator(), trigger: 'blur' }],
  password: [{ validator: createPasswordValidator({ minStrength: 1 }), trigger: 'blur' }]
}

// 显示密码强度
const strengthText = getPasswordStrengthText(password)
```

### 3. 分页处理 (pagination.js)

```javascript
import { createPagination, createPaginationHandlers, resetPagination } from '@/utils/pagination'

// 创建分页对象
const pagination = createPagination({ current: 1, size: 10, total: 0 })

// 创建分页处理函数
const { handleSizeChange, handleCurrentChange } = createPaginationHandlers(pagination, loadData)

// 重置分页
resetPagination(pagination)
```

### 4. Excel导出 (export.js)

```javascript
import { createExportHandler, handleExportResponse } from '@/utils/export'
import { exportUsers } from '@/api/user'

// 方式1：使用createExportHandler（推荐）
const handleExport = createExportHandler(
  exportUsers,
  () => ({ username: searchForm.username }),
  '用户列表.xlsx',
  '导出成功'
)

// 方式2：手动处理
const handleExport = async () => {
  try {
    const response = await exportUsers(params)
    await handleExportResponse(response, '用户列表.xlsx')
  } catch (error) {
    console.error('导出失败:', error)
  }
}
```

### 5. 批量删除 (batchDelete.js)

```javascript
import { createBatchDeleteHandler, executeBatchDelete } from '@/utils/batchDelete'
import { batchDeleteUsers } from '@/api/user'

// 方式1：使用createBatchDeleteHandler（推荐）
const handleBatchDelete = createBatchDeleteHandler({
  selectedRows,
  deleteApi: batchDeleteUsers,
  loadData,
  entityName: '用户'
})

// 方式2：手动调用
const handleBatchDelete = () => {
  executeBatchDelete({
    selectedRows: selectedRows.value,
    deleteApi: batchDeleteUsers,
    loadData,
    entityName: '用户'
  })
}
```

### 6. 搜索和重置 (search.js)

```javascript
import { createSearchHandler, createResetHandler } from '@/utils/search'

// 创建搜索处理函数
const handleSearch = createSearchHandler(pagination, loadData)

// 创建重置处理函数
const handleReset = createResetHandler(
  searchForm,
  pagination,
  loadData,
  () => {
    // 自定义重置逻辑（如重置级联选择器）
    addressCodes.value = []
  }
)
```

## 完整示例：用户列表组件

```javascript
import { ref, reactive, onMounted } from 'vue'
import { getUserList, exportUsers, batchDeleteUsers } from '@/api/user'
import { createPagination, createPaginationHandlers } from '@/utils/pagination'
import { createExportHandler } from '@/utils/export'
import { createBatchDeleteHandler } from '@/utils/batchDelete'
import { createSearchHandler, createResetHandler } from '@/utils/search'
import { createPhoneValidator, createEmailValidator } from '@/utils/validation'

export default {
  setup() {
    const searchForm = reactive({ username: '', role: '' })
    const selectedRows = ref([])
    
    // 分页
    const pagination = createPagination()
    const { handleSizeChange, handleCurrentChange } = createPaginationHandlers(pagination, loadData)
    
    // 数据加载
    const loadData = async () => {
      const res = await getUserList({
        ...searchForm,
        current: pagination.current,
        size: pagination.size
      })
      if (res.code === 200) {
        tableData.value = res.data.records
        pagination.total = res.data.total
      }
    }
    
    // 搜索和重置
    const handleSearch = createSearchHandler(pagination, loadData)
    const handleReset = createResetHandler(searchForm, pagination, loadData)
    
    // 导出
    const handleExport = createExportHandler(
      exportUsers,
      () => ({ username: searchForm.username, role: searchForm.role }),
      '用户列表.xlsx'
    )
    
    // 批量删除
    const handleBatchDelete = createBatchDeleteHandler({
      get selectedRows() { return selectedRows.value },
      deleteApi: batchDeleteUsers,
      loadData,
      entityName: '用户'
    })
    
    // 表单验证规则
    const userRules = {
      phone: [{ validator: createPhoneValidator({ required: false }), trigger: 'blur' }],
      email: [{ validator: createEmailValidator({ required: false }), trigger: 'blur' }]
    }
    
    onMounted(() => {
      loadData()
    })
    
    return {
      searchForm,
      pagination,
      selectedRows,
      handleSearch,
      handleReset,
      handleSizeChange,
      handleCurrentChange,
      handleExport,
      handleBatchDelete,
      userRules
    }
  }
}
```

## 注意事项

1. 所有工具函数都是纯函数，不会修改传入的参数（除非明确说明）
2. 工具函数支持配置选项，可以根据需要自定义行为
3. 建议在组件中使用工具函数，而不是直接复制代码
4. 如果发现新的可复用功能，请添加到相应的工具文件中

