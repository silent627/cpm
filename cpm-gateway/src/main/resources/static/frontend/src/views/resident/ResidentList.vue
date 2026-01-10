<template>
  <div class="resident-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>居民管理</span>
          <div v-if="isAdmin" class="header-buttons">
            <el-button type="info" @click="handleDownloadTemplate">下载导入模板</el-button>
            <el-upload
              :http-request="handleImport"
              :show-file-list="false"
              accept=".xlsx,.xls"
              class="upload-button"
            >
              <el-button type="warning" :icon="Download">导入Excel</el-button>
            </el-upload>
            <el-button type="danger" :icon="Delete" @click="handleBatchDelete" :disabled="selectedRows.length === 0">批量删除</el-button>
            <el-button type="primary" :icon="Download" @click="handleExport">导出Excel</el-button>
            <el-button type="success" @click="handleAdd">新增居民</el-button>
          </div>
        </div>
      </template>
      <el-form :model="searchForm" class="search-form">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="真实姓名">
              <el-input v-model="searchForm.realName" placeholder="请输入真实姓名" clearable />
            </el-form-item>
            <el-form-item label="身份证号">
              <el-input v-model="searchForm.idCard" placeholder="请输入身份证号" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="现居住地址">
              <div class="address-selector-wrapper">
                <RegionCascade
                  ref="searchRegionCascadeRef"
                  v-model="searchAddressCodes"
                  :placeholder="['请选择省份', '请选择市', '请选择区县', '请选择乡镇街道']"
                  @change="handleSearchAddressChange"
                />
                <el-input
                  v-model="searchForm.currentAddress"
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
          </el-col>
        </el-row>
        <el-form-item class="search-actions">
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="tableData" border style="width: 100%" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" show-overflow-tooltip />
        <el-table-column prop="realName" label="真实姓名" show-overflow-tooltip />
        <el-table-column prop="idCard" label="身份证号" width="180" show-overflow-tooltip />
        <el-table-column prop="gender" label="性别" width="80" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.gender === 1 ? '男' : '女' }}
          </template>
        </el-table-column>
        <el-table-column prop="birthDate" label="出生日期" show-overflow-tooltip />
        <el-table-column prop="nationality" label="民族" show-overflow-tooltip />
        <el-table-column prop="currentAddress" label="现居住地址" show-overflow-tooltip />
        <el-table-column prop="occupation" label="职业" show-overflow-tooltip />
        <el-table-column prop="education" label="文化程度" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="180" show-overflow-tooltip />
        <el-table-column label="操作" :width="isAdmin ? 250 : 100" fixed="right">
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
                  type="danger" 
                  size="small" 
                  :icon="Delete" 
                  @click="handleDelete(row)"
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

    <!-- 新增/编辑居民对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑居民' : '新增居民'" width="800px">
      <el-form :model="residentForm" label-width="120px" :rules="residentRules" ref="residentFormRef">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="用户名" prop="username" v-if="!isEdit">
              <el-input v-model="residentForm.username" placeholder="请输入用户名" clearable />
            </el-form-item>
            <el-form-item label="密码" prop="password" v-if="!isEdit">
              <el-input v-model="residentForm.password" type="password" placeholder="请输入密码" show-password />
            </el-form-item>
            <el-form-item label="真实姓名" prop="realName">
              <el-input v-model="residentForm.realName" placeholder="请输入真实姓名" clearable />
            </el-form-item>
            <el-form-item label="身份证号" prop="idCard" v-if="!isEdit">
              <el-input v-model="residentForm.idCard" placeholder="请输入身份证号" clearable />
            </el-form-item>
            <el-form-item label="性别">
              <el-radio-group v-model="residentForm.gender">
                <el-radio :label="1">男</el-radio>
                <el-radio :label="0">女</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="出生日期">
              <el-date-picker
                v-model="residentForm.birthDate"
                type="date"
                placeholder="选择日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
            <el-form-item label="民族">
              <el-input v-model="residentForm.nationality" placeholder="请输入民族" clearable />
            </el-form-item>
            <el-form-item label="头像">
              <el-upload
                class="avatar-uploader"
                :http-request="handleAvatarUpload"
                :show-file-list="false"
                :before-upload="beforeAvatarUpload"
              >
                <img v-if="residentForm.avatar && residentForm.avatar.trim() !== ''" :src="getImageUrl(residentForm.avatar)" class="avatar" @error="handleImageError" />
                <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
              </el-upload>
            </el-form-item>
            <el-form-item label="身份证照片">
              <el-upload
                class="id-card-uploader"
                :http-request="handleIdCardUpload"
                :show-file-list="false"
                :before-upload="beforeIdCardUpload"
              >
                <img v-if="residentForm.idCardPhoto && residentForm.idCardPhoto.trim() !== ''" :src="getImageUrl(residentForm.idCardPhoto)" class="id-card-image" @error="handleImageError" />
                <el-icon v-else class="id-card-uploader-icon"><Plus /></el-icon>
              </el-upload>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="户籍地址">
              <div class="address-selector-wrapper">
                <RegionCascade
                  ref="registeredRegionCascadeRef"
                  v-model="registeredAddressCodes"
                  :placeholder="['请选择省份', '请选择市', '请选择区县', '请选择乡镇街道']"
                  @change="handleRegisteredAddressChange"
                />
                <el-input
                  v-model="residentForm.registeredAddress"
                  placeholder="请先选择行政区划，然后输入详细地址（如：XX街道XX号）"
                  clearable
                  class="address-detail-input"
                  @blur="handleRegisteredAddressBlur"
                >
                  <template #prefix>
                    <el-icon><Location /></el-icon>
                  </template>
                </el-input>
              </div>
            </el-form-item>
            <el-form-item label="现居住地址">
              <div class="address-selector-wrapper">
                <RegionCascade
                  ref="currentRegionCascadeRef"
                  v-model="currentAddressCodes"
                  :placeholder="['请选择省份', '请选择市', '请选择区县', '请选择乡镇街道']"
                  @change="handleCurrentAddressChange"
                />
                <el-input
                  v-model="residentForm.currentAddress"
                  placeholder="请先选择行政区划，然后输入详细地址（如：XX街道XX号）"
                  clearable
                  class="address-detail-input"
                  @blur="handleCurrentAddressBlur"
                >
                  <template #prefix>
                    <el-icon><Location /></el-icon>
                  </template>
                </el-input>
              </div>
            </el-form-item>
            <el-form-item label="职业">
              <el-input v-model="residentForm.occupation" placeholder="请输入职业" clearable />
            </el-form-item>
            <el-form-item label="文化程度">
              <el-select v-model="residentForm.education" placeholder="请选择" style="width: 100%">
                <el-option label="小学" value="小学" />
                <el-option label="初中" value="初中" />
                <el-option label="高中" value="高中" />
                <el-option label="大专" value="大专" />
                <el-option label="本科" value="本科" />
                <el-option label="硕士" value="硕士" />
                <el-option label="博士" value="博士" />
              </el-select>
            </el-form-item>
            <el-form-item label="婚姻状况">
              <el-select v-model="residentForm.maritalStatus" placeholder="请选择" style="width: 100%">
                <el-option label="未婚" :value="0" />
                <el-option label="已婚" :value="1" />
                <el-option label="离异" :value="2" />
                <el-option label="丧偶" :value="3" />
              </el-select>
            </el-form-item>
            <el-form-item label="联系电话">
              <el-input v-model="residentForm.contactPhone" placeholder="请输入联系电话" clearable />
            </el-form-item>
            <el-form-item label="紧急联系人">
              <el-input v-model="residentForm.emergencyContact" placeholder="请输入紧急联系人" clearable />
            </el-form-item>
            <el-form-item label="紧急联系人电话">
              <el-input v-model="residentForm.emergencyPhone" placeholder="请输入紧急联系人电话" clearable />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注">
          <el-input v-model="residentForm.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 居民详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="居民详情" width="900px">
      <el-tabs v-if="residentDetail">
        <el-tab-pane label="基本信息">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="ID">{{ residentDetail.id }}</el-descriptions-item>
            <el-descriptions-item label="真实姓名">{{ residentDetail.realName }}</el-descriptions-item>
            <el-descriptions-item label="身份证号">{{ residentDetail.idCard }}</el-descriptions-item>
            <el-descriptions-item label="性别">
              {{ residentDetail.gender === 1 ? '男' : '女' }}
            </el-descriptions-item>
            <el-descriptions-item label="出生日期">{{ residentDetail.birthDate }}</el-descriptions-item>
            <el-descriptions-item label="民族">{{ residentDetail.nationality }}</el-descriptions-item>
            <el-descriptions-item label="户籍地址" :span="2">{{ residentDetail.registeredAddress }}</el-descriptions-item>
            <el-descriptions-item label="现居住地址" :span="2">{{ residentDetail.currentAddress }}</el-descriptions-item>
            <el-descriptions-item label="职业">{{ residentDetail.occupation }}</el-descriptions-item>
            <el-descriptions-item label="文化程度">{{ residentDetail.education }}</el-descriptions-item>
            <el-descriptions-item label="婚姻状况">
              {{ residentDetail.maritalStatus === 0 ? '未婚' : residentDetail.maritalStatus === 1 ? '已婚' : residentDetail.maritalStatus === 2 ? '离异' : '丧偶' }}
            </el-descriptions-item>
            <el-descriptions-item label="联系电话">{{ residentDetail.contactPhone }}</el-descriptions-item>
            <el-descriptions-item label="紧急联系人">{{ residentDetail.emergencyContact }}</el-descriptions-item>
            <el-descriptions-item label="紧急联系人电话">{{ residentDetail.emergencyPhone }}</el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ residentDetail.createTime }}</el-descriptions-item>
            <el-descriptions-item label="更新时间">{{ residentDetail.updateTime }}</el-descriptions-item>
          </el-descriptions>
        </el-tab-pane>
        <el-tab-pane label="证件照管理">
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
                  <img v-if="residentDetail && residentDetail.avatar && residentDetail.avatar.trim() !== ''" :src="getImageUrl(residentDetail.avatar)" class="avatar" @error="handleImageError" />
                  <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
                </el-upload>
                <div class="photo-actions" v-if="residentDetail && residentDetail.avatar && residentDetail.avatar.trim() !== ''">
                  <el-button type="danger" size="small" @click="handleDeleteAvatar">删除</el-button>
                </div>
              </div>
            </div>
            <div class="photo-item">
              <div class="photo-label">身份证照片</div>
              <div class="photo-content">
                <el-upload
                  class="id-card-uploader"
                  :http-request="handleDetailIdCardUpload"
                  :show-file-list="false"
                  :before-upload="beforeIdCardUpload"
                >
                  <img v-if="residentDetail && residentDetail.idCardPhoto && residentDetail.idCardPhoto.trim() !== ''" :src="getImageUrl(residentDetail.idCardPhoto)" class="id-card-image" @error="handleImageError" />
                  <el-icon v-else class="id-card-uploader-icon"><Plus /></el-icon>
                </el-upload>
                <div class="photo-actions" v-if="residentDetail && residentDetail.idCardPhoto && residentDetail.idCardPhoto.trim() !== ''">
                  <el-button type="danger" size="small" @click="handleDeleteIdCardPhoto">删除</el-button>
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>
        </el-tabs>
      </el-dialog>

    <!-- 导入结果对话框 -->
    <el-dialog v-model="importResultDialogVisible" title="导入结果" width="700px">
      <div v-if="importResult">
        <el-alert :title="`成功导入 ${importResult.successCount} 条，失败 ${importResult.failCount} 条`" 
                  :type="importResult.failCount > 0 ? 'warning' : 'success'" 
                  :closable="false" 
                  style="margin-bottom: 20px" />
        <div v-if="importResult.errors && importResult.errors.length > 0">
          <h4>错误详情：</h4>
          <el-table :data="importResult.errors" border max-height="300">
            <el-table-column prop="row" label="行号" width="80" />
            <el-table-column prop="message" label="错误信息" />
          </el-table>
        </div>
      </div>
      <template #footer>
        <el-button type="primary" @click="importResultDialogVisible = false">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted, computed, watch, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import { getResidentList, getResidentById, createResident, updateResident, deleteResident, exportResidents, batchDeleteResidents, downloadResidentTemplate, importResidents } from '../../api/resident'
import { uploadAvatar, uploadIdCard, deleteFile } from '../../api/upload'
import { ElMessage, ElMessageBox } from 'element-plus'
import { View, Edit, Delete, Download, Plus, Location } from '@element-plus/icons-vue'
import RegionCascade from '../../components/RegionCascade.vue'
import { getImageUrl, sanitizeImageFields, handleImageError as handleImageErrorUtil, isValidImageUrl } from '../../utils/image'
import { createIdCardValidator, createPhoneValidator } from '../../utils/validation'
import { createPagination, createPaginationHandlers } from '../../utils/pagination'
import { createExportHandler } from '../../utils/export'
import { createBatchDeleteHandler } from '../../utils/batchDelete'
import { createSearchHandler, createResetHandler } from '../../utils/search'

export default {
  name: 'ResidentList',
  components: {
    View,
    Edit,
    Delete,
    Download,
    Plus,
    Location,
    RegionCascade
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
      realName: '',
      idCard: '',
      currentAddress: ''
    })
    const pagination = createPagination({ current: 1, size: 10, total: 0 })
    const dialogVisible = ref(false)
    const isEdit = ref(false)
    // 记录原始表单数据，用于取消时恢复
    const originalResidentForm = reactive({
      avatar: '',
      idCardPhoto: ''
    })
    const residentForm = reactive({
      username: '',
      password: '',
      realName: '',
      phone: '',
      email: '',
      idCard: '',
      gender: 1,
      birthDate: '',
      nationality: '汉族',
      registeredAddress: '',
      currentAddress: '',
      occupation: '',
      education: '',
      maritalStatus: 0,
      contactPhone: '',
      emergencyContact: '',
      emergencyPhone: '',
      remark: '',
      avatar: '',
      idCardPhoto: ''
    })
    
    // 级联选择器的值
    const registeredAddressCodes = ref([])
    const currentAddressCodes = ref([])
    const searchAddressCodes = ref([])
    const registeredRegionCascadeRef = ref(null)
    const currentRegionCascadeRef = ref(null)
    const searchRegionCascadeRef = ref(null)
    
    // 处理户籍地址级联选择变化
    const handleRegisteredAddressChange = (data) => {
      if (data && data.names && data.names.length > 0) {
        // 将级联选择的路径作为地址前缀（只使用已选择的级别）
        const prefix = data.names.join('')
        const currentAddress = residentForm.registeredAddress || ''
        
        // 如果地址为空，直接设置前缀
        if (!currentAddress) {
          residentForm.registeredAddress = prefix
        } else {
          // 提取当前地址中的详细地址部分（去除级联选择的前缀）
          // 尝试匹配并移除可能的前缀部分
          let detailPart = currentAddress
          
          // 如果当前地址以级联选择的前缀开头，则提取后面的详细地址
          if (currentAddress.startsWith(prefix)) {
            detailPart = currentAddress.substring(prefix.length).trim()
          } else {
            // 如果不以当前前缀开头，尝试提取可能的详细地址部分
            // 查找最后一个匹配的级联选择名称，保留其后的内容
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
              // 找到了匹配的位置，提取后面的详细地址
              detailPart = currentAddress.substring(lastMatchIndex).trim()
            } else {
              // 没有找到匹配，检查是否包含级联选择中的任何名称
              // 如果包含，尝试提取详细地址；否则保留原地址
              const hasAnyMatch = data.names.some(name => currentAddress.includes(name))
              if (!hasAnyMatch) {
                // 完全不包含级联选择的内容，保留原地址作为详细地址
                detailPart = currentAddress
              }
            }
          }
          
          // 组合新的地址：级联选择前缀 + 详细地址（去除重复的前缀部分）
          // 如果详细地址部分以级联选择的前缀开头，则去除
          if (detailPart.startsWith(prefix)) {
            detailPart = detailPart.substring(prefix.length).trim()
          }
          
          // 组合最终地址
          residentForm.registeredAddress = prefix + (detailPart ? detailPart : '')
        }
      }
    }
    
    // 处理搜索地址级联选择变化
    const handleSearchAddressChange = (data) => {
      if (data && data.names && data.names.length > 0) {
        // 将级联选择的路径作为地址前缀
        const prefix = data.names.join('')
        const currentAddress = searchForm.currentAddress || ''
        
        // 如果地址为空，直接设置前缀
        if (!currentAddress) {
          searchForm.currentAddress = prefix
        } else {
          // 提取当前地址中的详细地址部分
          let detailPart = currentAddress
          
          if (currentAddress.startsWith(prefix)) {
            detailPart = currentAddress.substring(prefix.length).trim()
          } else {
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
          
          if (detailPart.startsWith(prefix)) {
            detailPart = detailPart.substring(prefix.length).trim()
          }
          
          searchForm.currentAddress = prefix + (detailPart ? detailPart : '')
        }
      }
    }
    
    // 处理现居住地址级联选择变化
    const handleCurrentAddressChange = (data) => {
      if (data && data.names && data.names.length > 0) {
        // 将级联选择的路径作为地址前缀（只使用已选择的级别）
        const prefix = data.names.join('')
        const currentAddress = residentForm.currentAddress || ''
        
        // 如果地址为空，直接设置前缀
        if (!currentAddress) {
          residentForm.currentAddress = prefix
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
          residentForm.currentAddress = prefix + (detailPart ? detailPart : '')
        }
      }
    }
    
    // 处理户籍地址输入框失焦事件，确保如果级联选择器有值，地址字段至少包含前缀
    const handleRegisteredAddressBlur = () => {
      if (registeredAddressCodes.value && registeredAddressCodes.value.length > 0) {
        // 如果级联选择器有值，但地址字段为空或只包含空格，则恢复前缀
        const currentAddress = residentForm.registeredAddress || ''
        if (!currentAddress.trim()) {
          // 从级联选择器获取完整路径（通过触发change事件来获取）
          // 这里我们依赖级联选择器的change事件已经更新了地址字段
          // 如果地址字段为空，说明用户清空了输入框，我们需要恢复前缀
          // 但由于我们无法直接访问级联选择器内部的选项，这里暂时不做处理
          // 用户需要重新选择级联选择器或手动输入地址
        }
      }
    }
    
    // 处理现居住地址输入框失焦事件
    const handleCurrentAddressBlur = () => {
      if (currentAddressCodes.value && currentAddressCodes.value.length > 0) {
        const currentAddress = residentForm.currentAddress || ''
        if (!currentAddress.trim()) {
          // 同上，暂时不做处理
        }
      }
    }
    
    // 图片加载错误处理（针对居民表单，需要判断是头像还是身份证照片）
    const handleImageError = (event) => {
      if (!event || !event.target) return
      
      // 隐藏图片元素
      event.target.style.display = 'none'
      
      // 判断是哪个图片加载失败
      const imgClass = event.target.className || ''
      
      // 优先通过class名称判断（更准确）
      if (imgClass.includes('id-card-image')) {
        // 身份证照片加载失败
        handleImageErrorUtil(event, { dataRef: residentDetail, fieldName: 'idCardPhoto' })
        if (residentForm.idCardPhoto) {
          residentForm.idCardPhoto = ''
        }
      } else if (imgClass.includes('avatar')) {
        // 头像加载失败
        handleImageErrorUtil(event, { dataRef: residentDetail, fieldName: 'avatar' })
        if (residentForm.avatar) {
          residentForm.avatar = ''
        }
      } else {
        // 备用判断：通过URL判断
        const imgSrc = event.target.src || ''
        if (residentDetail.value) {
          if (residentDetail.value.idCardPhoto && imgSrc.includes(residentDetail.value.idCardPhoto)) {
            residentDetail.value.idCardPhoto = ''
          } else if (residentDetail.value.avatar && imgSrc.includes(residentDetail.value.avatar)) {
            residentDetail.value.avatar = ''
          }
        }
        if (residentForm.idCardPhoto && imgSrc.includes(residentForm.idCardPhoto)) {
          residentForm.idCardPhoto = ''
        } else if (residentForm.avatar && imgSrc.includes(residentForm.avatar)) {
          residentForm.avatar = ''
        }
      }
    }
    
    // 头像上传（自定义上传方法）
    const handleAvatarUpload = async (options) => {
      try {
        const res = await uploadAvatar(options.file)
        if (res.code === 200) {
          residentForm.avatar = res.data
          ElMessage.success('头像上传成功')
        } else {
          ElMessage.error(res.message || '头像上传失败')
        }
      } catch (error) {
        ElMessage.error(error.message || '头像上传失败')
      }
    }
    
    // 身份证照片上传（自定义上传方法）
    const handleIdCardUpload = async (options) => {
      try {
        const res = await uploadIdCard(options.file)
        if (res.code === 200) {
          residentForm.idCardPhoto = res.data
          ElMessage.success('身份证照片上传成功')
        } else {
          ElMessage.error(res.message || '身份证照片上传失败')
        }
      } catch (error) {
        ElMessage.error(error.message || '身份证照片上传失败')
      }
    }
    
    // 头像上传前验证
    const beforeAvatarUpload = (file) => {
      const isImage = file.type.startsWith('image/')
      const isLt10M = file.size / 1024 / 1024 < 10

      if (!isImage) {
        ElMessage.error('只能上传图片文件!')
        return false
      }
      if (!isLt10M) {
        ElMessage.error('图片大小不能超过 10MB!')
        return false
      }
      return true
    }
    
    // 身份证照片上传前验证
    const beforeIdCardUpload = (file) => {
      const isImage = file.type.startsWith('image/')
      const isLt10M = file.size / 1024 / 1024 < 10

      if (!isImage) {
        ElMessage.error('只能上传图片文件!')
        return false
      }
      if (!isLt10M) {
        ElMessage.error('图片大小不能超过 10MB!')
        return false
      }
      return true
    }
    
    // 详情对话框中的头像上传
    const handleDetailAvatarUpload = async (options) => {
      try {
        const res = await uploadAvatar(options.file)
        if (res.code === 200) {
          residentDetail.value.avatar = res.data
          // 同时更新表单中的头像
          residentForm.avatar = res.data
          // 更新数据库
          await updateResident({
            id: residentDetail.value.id,
            avatar: res.data
          })
          ElMessage.success('头像上传成功')
        } else {
          ElMessage.error(res.message || '头像上传失败')
        }
      } catch (error) {
        ElMessage.error(error.message || '头像上传失败')
      }
    }
    
    // 详情对话框中的身份证照片上传
    const handleDetailIdCardUpload = async (options) => {
      try {
        const res = await uploadIdCard(options.file)
        if (res.code === 200) {
          residentDetail.value.idCardPhoto = res.data
          // 同时更新表单中的身份证照片
          residentForm.idCardPhoto = res.data
          // 更新数据库
          await updateResident({
            id: residentDetail.value.id,
            idCardPhoto: res.data
          })
          ElMessage.success('身份证照片上传成功')
        } else {
          ElMessage.error(res.message || '身份证照片上传失败')
        }
      } catch (error) {
        ElMessage.error(error.message || '身份证照片上传失败')
      }
    }
    
    // 删除头像
    const handleDeleteAvatar = async () => {
      try {
        await ElMessageBox.confirm('确定要删除头像吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        const residentId = residentDetail.value.id
        const avatarUrl = residentDetail.value.avatar
        
        // 先更新数据库，将avatar设置为null
        await updateResident({
          id: residentId,
          avatar: null
        })
        
        // 更新本地显示，设置为空字符串，确保显示默认上传图标
        residentDetail.value.avatar = ''
        residentForm.avatar = ''
        
        // 然后删除文件（如果文件存在），静默处理，不显示任何错误消息
        if (isValidImageUrl(avatarUrl)) {
          // 使用 Promise.resolve().then() 来静默处理删除操作
          deleteFile(avatarUrl).catch(() => {
            // 完全静默处理，不显示任何错误消息，也不记录到控制台
            // 文件删除失败不影响整体流程，因为数据库已经更新
          })
        }
        
        // 只显示成功消息
        ElMessage.success('删除成功')
      } catch (error) {
        // 如果是用户取消操作，不显示任何消息
        if (error !== 'cancel' && error.message !== 'cancel') {
          // 只有在真正出错时才显示错误消息
          ElMessage.error('删除失败: ' + (error.message || '未知错误'))
        }
      }
    }
    
    // 删除身份证照片
    const handleDeleteIdCardPhoto = async () => {
      try {
        await ElMessageBox.confirm('确定要删除身份证照片吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        const residentId = residentDetail.value.id
        const idCardPhotoUrl = residentDetail.value.idCardPhoto
        
        // 先更新数据库，将idCardPhoto设置为null
        await updateResident({
          id: residentId,
          idCardPhoto: null
        })
        
        // 更新本地显示，设置为空字符串，确保显示默认上传图标
        residentDetail.value.idCardPhoto = ''
        residentForm.idCardPhoto = ''
        
        // 然后删除文件（如果文件存在），静默处理，不显示任何错误消息
        if (isValidImageUrl(idCardPhotoUrl)) {
          // 使用 Promise.resolve().then() 来静默处理删除操作
          deleteFile(idCardPhotoUrl).catch(() => {
            // 完全静默处理，不显示任何错误消息，也不记录到控制台
            // 文件删除失败不影响整体流程，因为数据库已经更新
          })
        }
        
        // 只显示成功消息
        ElMessage.success('删除成功')
      } catch (error) {
        // 如果是用户取消操作，不显示任何消息
        if (error !== 'cancel' && error.message !== 'cancel') {
          // 只有在真正出错时才显示错误消息
          ElMessage.error('删除失败: ' + (error.message || '未知错误'))
        }
      }
    }
    const detailDialogVisible = ref(false)
    const residentDetail = ref(null)
    const residentFormRef = ref(null)
    const selectedRows = ref([])
    const importResultDialogVisible = ref(false)
    const importResult = ref(null)

    // 使用工具函数创建验证器
    const validatePassword = (rule, value, callback) => {
      if (!value) {
        callback(new Error('请输入密码'))
      } else {
        const validator = createPasswordValidator({ required: true, minStrength: 1 })
        validator(rule, value, callback)
      }
    }

    const residentRules = {
      username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
      password: [{ required: true, validator: validatePassword, trigger: 'blur' }],
      realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
      idCard: [{ required: true, validator: createIdCardValidator(), trigger: 'blur' }],
      contactPhone: [{ validator: createPhoneValidator({ required: false }), trigger: 'blur' }],
      emergencyPhone: [{ validator: createPhoneValidator({ required: false }), trigger: 'blur' }]
    }

    const loadData = async () => {
      try {
        const params = {
          current: pagination.current,
          size: pagination.size,
          ...searchForm
        }
        const res = await getResidentList(params)
        if (res.code === 200) {
          tableData.value = res.data.records || []
          pagination.total = res.data.total || 0
        }
      } catch (error) {
        ElMessage.error('加载数据失败')
      }
    }

    const handleSearch = () => {
      pagination.current = 1
      loadData()
    }

    const handleReset = () => {
      searchForm.realName = ''
      searchForm.idCard = ''
      searchForm.currentAddress = ''
      searchAddressCodes.value = []
      handleSearch()
    }

    // 使用工具函数创建分页处理函数
    const { handleSizeChange, handleCurrentChange } = createPaginationHandlers(pagination, loadData)

    const handleAdd = () => {
      isEdit.value = false
      registeredAddressCodes.value = []
      currentAddressCodes.value = []
      Object.assign(residentForm, {
        username: '',
        password: '',
        realName: '',
        phone: '',
        email: '',
        idCard: '',
        gender: 1,
        birthDate: '',
        nationality: '汉族',
        registeredAddress: '',
        currentAddress: '',
        occupation: '',
        education: '',
        maritalStatus: 0,
        contactPhone: '',
        emergencyContact: '',
        emergencyPhone: '',
        remark: '',
        avatar: '',
        idCardPhoto: ''
      })
      // 保存原始值（用于取消时判断）
      originalResidentForm.avatar = ''
      originalResidentForm.idCardPhoto = ''
      dialogVisible.value = true
    }

    const handleEdit = async (row) => {
      isEdit.value = true
      registeredAddressCodes.value = []
      currentAddressCodes.value = []
      
      try {
        // 重新获取最新数据，确保包含最新的头像和身份证照片
        const res = await getResidentById(row.id)
        if (res.code === 200 && res.data) {
          // 使用工具函数清理图片字段
          const residentData = sanitizeImageFields(res.data, ['avatar', 'idCardPhoto'])
      
      // 先保存原始地址，用于解析
          const originalRegisteredAddress = residentData.registeredAddress || ''
          const originalCurrentAddress = residentData.currentAddress || ''
      
      Object.assign(residentForm, {
            ...residentData,
            birthDate: residentData.birthDate || '',
            avatar: residentData.avatar || '',
            idCardPhoto: residentData.idCardPhoto || ''
      })
          // 保存原始值（用于取消时判断）
          originalResidentForm.avatar = residentData.avatar || ''
          originalResidentForm.idCardPhoto = residentData.idCardPhoto || ''
      
      dialogVisible.value = true
      
      // 使用 nextTick 确保 DOM 更新完成，然后立即开始解析（不等待固定时间）
      await nextTick()
      
      // 并行解析两个地址，提高响应速度
      const parsePromises = []
      
      // 解析户籍地址
      if (originalRegisteredAddress && registeredRegionCascadeRef.value) {
        parsePromises.push(
          registeredRegionCascadeRef.value.parseAddress(originalRegisteredAddress)
            .then(result => {
              if (result && result.detail !== undefined) {
                residentForm.registeredAddress = result.detail
              } else if (result && result.codes.length === 0) {
                residentForm.registeredAddress = originalRegisteredAddress
              }
            })
            .catch(error => {
              console.error('解析户籍地址失败:', error)
              residentForm.registeredAddress = originalRegisteredAddress
            })
        )
      }
      
      // 解析现居住地址
      if (originalCurrentAddress && currentRegionCascadeRef.value) {
        parsePromises.push(
          currentRegionCascadeRef.value.parseAddress(originalCurrentAddress)
            .then(result => {
              if (result && result.detail !== undefined) {
                residentForm.currentAddress = result.detail
              } else if (result && result.codes.length === 0) {
                residentForm.currentAddress = originalCurrentAddress
              }
            })
            .catch(error => {
              console.error('解析现居住地址失败:', error)
              residentForm.currentAddress = originalCurrentAddress
            })
        )
      }
      
      // 等待所有解析完成（并行执行）
      await Promise.all(parsePromises)
        } else {
          ElMessage.error('获取居民信息失败')
        }
      } catch (error) {
        ElMessage.error('获取居民信息失败: ' + (error.message || '未知错误'))
      }
    }

    const handleSubmit = async () => {
      if (!residentFormRef.value) return
      await residentFormRef.value.validate(async (valid) => {
        if (valid) {
          try {
            // 地址字段已经通过级联选择器的change事件更新，包含了完整地址（级联选择 + 详细地址）
            // 直接提交即可，地址字段会完整保存到数据库
            if (isEdit.value) {
              const res = await updateResident(residentForm)
              if (res.code === 200) {
                ElMessage.success('更新成功')
                // 更新原始值，避免关闭对话框时误删已保存的图片
                originalResidentForm.avatar = residentForm.avatar
                originalResidentForm.idCardPhoto = residentForm.idCardPhoto
                dialogVisible.value = false
                loadData()
              }
            } else {
              const res = await createResident(residentForm)
              if (res.code === 200) {
                ElMessage.success('创建成功')
                // 更新原始值，避免关闭对话框时误删已保存的图片
                originalResidentForm.avatar = residentForm.avatar
                originalResidentForm.idCardPhoto = residentForm.idCardPhoto
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

    const handleView = async (row) => {
      try {
        // 每次打开对话框时都重新获取最新数据，避免使用缓存
        const res = await getResidentById(row.id)
        if (res.code === 200 && res.data) {
          // 使用工具函数清理图片字段
          const residentData = sanitizeImageFields(res.data, ['avatar', 'idCardPhoto'])
          residentDetail.value = residentData
          detailDialogVisible.value = true
        }
      } catch (error) {
        ElMessage.error('获取详情失败')
      }
    }
    
    const handleSelectionChange = (selection) => {
      selectedRows.value = selection
    }

    // 使用工具函数创建批量删除处理函数
    const handleBatchDelete = createBatchDeleteHandler({
      get selectedRows() { return selectedRows.value },
      deleteApi: batchDeleteResidents,
      loadData,
      entityName: '居民'
    })

    const handleDownloadTemplate = async () => {
      try {
        const response = await downloadResidentTemplate()
        const blob = new Blob([response.data], { 
          type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' 
        })
        const url = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.setAttribute('download', '居民信息导入模板.xlsx')
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
        window.URL.revokeObjectURL(url)
        ElMessage.success('模板下载成功')
      } catch (error) {
        ElMessage.error('模板下载失败')
      }
    }

    const handleImport = async (options) => {
      try {
        const formData = new FormData()
        formData.append('file', options.file)
        const res = await importResidents(formData)
        if (res.code === 200) {
          importResult.value = res.data
          importResultDialogVisible.value = true
          loadData()
          if (res.data.successCount > 0) {
            ElMessage.success(`成功导入 ${res.data.successCount} 条记录`)
          }
          if (res.data.failCount > 0) {
            ElMessage.warning(`有 ${res.data.failCount} 条记录导入失败，请查看详情`)
          }
        }
      } catch (error) {
        ElMessage.error('导入失败：' + (error.message || '未知错误'))
      }
    }

    // 监听对话框关闭，清空residentDetail，确保下次打开时重新获取数据
    watch(detailDialogVisible, (newVal) => {
      if (!newVal) {
        // 对话框关闭时，清空residentDetail，避免下次打开时显示旧数据
        residentDetail.value = null
      }
    })

    // 监听编辑/新增对话框关闭，如果用户取消操作，删除新上传但未保存的图片
    watch(dialogVisible, (newVal) => {
      if (!newVal) {
        // 对话框关闭时，检查是否有新上传但未保存的图片
        // 如果当前图片URL与原始值不同，说明是新上传的，需要删除
        const deletePromises = []
        
        // 检查头像
        if (residentForm.avatar && 
            residentForm.avatar.trim() !== '' && 
            residentForm.avatar !== originalResidentForm.avatar &&
            isValidImageUrl(residentForm.avatar)) {
          deletePromises.push(
            deleteFile(residentForm.avatar).catch(() => {
              // 静默处理删除失败，不影响用户体验
            })
          )
        }
        
        // 检查身份证照片
        if (residentForm.idCardPhoto && 
            residentForm.idCardPhoto.trim() !== '' && 
            residentForm.idCardPhoto !== originalResidentForm.idCardPhoto &&
            isValidImageUrl(residentForm.idCardPhoto)) {
          deletePromises.push(
            deleteFile(residentForm.idCardPhoto).catch(() => {
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

    const handleDelete = async (row) => {
      try {
        await ElMessageBox.confirm(
          `确定要删除居民 "${row.realName}" 吗？`,
          '提示',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }
        )
        const res = await deleteResident(row.id)
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

    // 使用工具函数创建导出处理函数
    const handleExport = createExportHandler(
      exportResidents,
      () => ({
        realName: searchForm.realName || undefined,
        idCard: searchForm.idCard || undefined,
        currentAddress: searchForm.currentAddress || undefined
      }),
      '居民列表.xlsx',
      '导出成功'
    )

    // 监听路由变化，重新获取用户信息
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
      residentForm,
      detailDialogVisible,
      residentDetail,
      registeredAddressCodes,
      currentAddressCodes,
      searchAddressCodes,
      registeredRegionCascadeRef,
      currentRegionCascadeRef,
      searchRegionCascadeRef,
      handleRegisteredAddressChange,
      handleCurrentAddressChange,
      handleSearchAddressChange,
      handleRegisteredAddressBlur,
      handleCurrentAddressBlur,
      handleSearch,
      handleReset,
      handleSizeChange,
      handleCurrentChange,
      handleAdd,
      handleEdit,
      handleSubmit,
      handleView,
      handleDelete,
      handleExport,
      residentFormRef,
      residentRules,
      isAdmin,
      Download,
      Plus,
      getImageUrl,
      handleImageError,
      handleAvatarUpload,
      handleIdCardUpload,
      beforeAvatarUpload,
      beforeIdCardUpload,
      handleDetailAvatarUpload,
      handleDetailIdCardUpload,
      handleDeleteAvatar,
      handleDeleteIdCardPhoto,
      selectedRows,
      handleSelectionChange,
      handleBatchDelete,
      handleDownloadTemplate,
      handleImport,
      importResultDialogVisible,
      importResult
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

.header-buttons {
  display: flex;
  align-items: center;
  gap: 10px;
}

.header-buttons .upload-button {
  display: inline-block;
  margin: 0;
}

.search-form {
  margin-bottom: 20px;
  padding: 20px;
  background-color: #fafafa;
  border-radius: 8px;
}

.search-form :deep(.el-form-item) {
  margin-bottom: 18px;
}

.search-form :deep(.el-form-item__label) {
  font-weight: 500;
  color: #606266;
  width: 100px !important;
}

.search-actions {
  margin-top: 10px;
  margin-bottom: 0 !important;
  padding-top: 10px;
  border-top: 1px solid #e4e7ed;
}

.search-actions :deep(.el-form-item__content) {
  margin-left: 0 !important;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
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

.id-card-uploader {
  display: flex;
  align-items: center;
}

.id-card-uploader .id-card-image {
  width: 200px;
  height: 120px;
  display: block;
  border-radius: 4px;
  object-fit: contain;
  border: 1px solid #dcdfe6;
}

.id-card-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 200px;
  height: 120px;
  line-height: 120px;
  text-align: center;
  border: 1px dashed #d9d9d9;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.id-card-uploader-icon:hover {
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

.address-selector-wrapper {
  width: 100%;
}

.address-detail-input {
  margin-top: 10px;
  transition: all 0.3s ease;
}

.address-detail-input :deep(.el-input__wrapper) {
  border-radius: 4px;
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

/* 优化级联选择器样式 */
.search-form :deep(.region-cascade .cascade-container) {
  gap: 6px;
}

.search-form :deep(.region-cascade .cascade-select) {
  min-width: 110px;
}
</style>