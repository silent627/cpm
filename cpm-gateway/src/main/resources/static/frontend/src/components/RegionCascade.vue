<template>
  <div class="region-cascade">
    <div class="cascade-container">
      <el-select
        v-for="(level, index) in levels"
        :key="index"
        v-model="selectedValues[index]"
        :placeholder="level.placeholder"
        clearable
        filterable
        :loading="level.loading"
        :disabled="index > 0 && (!selectedValues[index - 1] || level.disabled)"
        @change="handleChange(index, $event)"
        @clear="handleClear(index)"
        class="cascade-select"
      >
        <el-option
          v-for="item in level.options"
          :key="item.code"
          :label="item.name"
          :value="item.code"
        />
      </el-select>
    </div>
  </div>
</template>

<script>
import { ref, reactive, watch, onMounted } from 'vue'
import { getProvinces, getChildren } from '../api/region'
import { ElMessage } from 'element-plus'

export default {
  name: 'RegionCascade',
  props: {
    modelValue: {
      type: Array,
      default: () => []
    },
    placeholder: {
      type: Array,
      default: () => ['请选择省份', '请选择市', '请选择区县', '请选择乡镇街道']
    }
  },
  emits: ['update:modelValue', 'change'],
  expose: ['parseAddress'],
  setup(props, { emit, expose }) {
    const selectedValues = ref([null, null, null, null])
    // 用于防止重复显示错误消息
    let lastErrorTime = 0
    const ERROR_MESSAGE_INTERVAL = 2000 // 2秒内不重复显示相同错误
    
    const levels = reactive([
      {
        placeholder: props.placeholder[0] || '请选择省份',
        options: [],
        loading: false,
        disabled: false
      },
      {
        placeholder: props.placeholder[1] || '请选择市',
        options: [],
        loading: false,
        disabled: true
      },
      {
        placeholder: props.placeholder[2] || '请选择区县',
        options: [],
        loading: false,
        disabled: true
      },
      {
        placeholder: props.placeholder[3] || '请选择乡镇街道',
        options: [],
        loading: false,
        disabled: true
      }
    ])

    // 加载省份
    const loadProvinces = async () => {
      levels[0].loading = true
      try {
        const res = await getProvinces()
        if (res.code === 200) {
          const provinces = res.data || []
          levels[0].options = provinces
          // 如果省份有数据，启用第二级（市）
          if (provinces.length > 0 && levels.length > 1) {
            levels[1].disabled = false
          }
        } else {
          // 如果是限流错误，显示特定提示；其他错误显示通用提示
          const errorMsg = res.message || '加载省份失败'
          const now = Date.now()
          if (now - lastErrorTime > ERROR_MESSAGE_INTERVAL) {
            if (errorMsg.includes('请求过于频繁') || errorMsg.includes('限流')) {
              ElMessage.warning('请求过于频繁，请稍后再试')
            } else {
              ElMessage.error(errorMsg)
            }
            lastErrorTime = now
          }
        }
      } catch (error) {
        // 如果是限流错误，显示特定提示；其他错误显示通用提示
        const errorMsg = error.message || '网络错误'
        const now = Date.now()
        if (now - lastErrorTime > ERROR_MESSAGE_INTERVAL) {
          if (errorMsg.includes('请求过于频繁') || errorMsg.includes('限流') || error.response?.status === 429) {
            ElMessage.warning('请求过于频繁，请稍后再试')
          } else {
            ElMessage.error('加载省份失败: ' + errorMsg)
          }
          lastErrorTime = now
        }
      } finally {
        levels[0].loading = false
      }
    }

    // 加载下级区划
    const loadChildren = async (parentCode, levelIndex) => {
      if (!parentCode) return

      levels[levelIndex].loading = true
      try {
        const res = await getChildren(parentCode)
        if (res.code === 200) {
          const data = res.data || {}
          const items = data.items || []
          levels[levelIndex].options = items
          
          // 如果有数据，启用当前级别；如果没有数据，禁用当前级别
          levels[levelIndex].disabled = items.length === 0
          
          // 如果没有下级或没有数据，禁用并清空后续级别
          if (!data.hasChildren || items.length === 0) {
            for (let i = levelIndex + 1; i < levels.length; i++) {
              selectedValues.value[i] = null
              levels[i].options = []
              levels[i].disabled = true
            }
          } else {
            // 如果有下级，启用下一级
            if (levelIndex + 1 < levels.length) {
              levels[levelIndex + 1].disabled = false
            }
          }
        } else {
          // 如果是限流错误，显示特定提示；其他错误显示通用提示
          const errorMsg = res.message || '加载下级区划失败'
          const now = Date.now()
          if (now - lastErrorTime > ERROR_MESSAGE_INTERVAL) {
            if (errorMsg.includes('请求过于频繁') || errorMsg.includes('限流')) {
              ElMessage.warning('请求过于频繁，请稍后再试')
            } else {
              ElMessage.error(errorMsg)
            }
            lastErrorTime = now
          }
          // 加载失败时，禁用当前级别
          levels[levelIndex].disabled = true
        }
      } catch (error) {
        // 如果是限流错误，显示特定提示；其他错误显示通用提示
        const errorMsg = error.message || '网络错误'
        const now = Date.now()
        if (now - lastErrorTime > ERROR_MESSAGE_INTERVAL) {
          if (errorMsg.includes('请求过于频繁') || errorMsg.includes('限流') || error.response?.status === 429) {
            ElMessage.warning('请求过于频繁，请稍后再试')
          } else {
            ElMessage.error('加载下级区划失败: ' + errorMsg)
          }
          lastErrorTime = now
        }
        // 加载失败时，禁用当前级别
        levels[levelIndex].disabled = true
      } finally {
        levels[levelIndex].loading = false
      }
    }

    // 处理选择变化
    const handleChange = async (index, value) => {
      // 清空后续级别的选择
      for (let i = index + 1; i < selectedValues.value.length; i++) {
        selectedValues.value[i] = null
        levels[i].options = []
        levels[i].disabled = true
      }

      // 如果有选择值，加载下一级
      if (value && index < levels.length - 1) {
        // 先启用下一级（加载中状态）
        levels[index + 1].disabled = false
        await loadChildren(value, index + 1)
      } else if (!value) {
        // 如果清空了当前选择，禁用后续所有级别
        for (let i = index + 1; i < levels.length; i++) {
          levels[i].disabled = true
        }
      }

      // 更新v-model
      const values = [...selectedValues.value]
      emit('update:modelValue', values)
      
      // 触发change事件，传递选中的值数组和对应的名称数组
      const selectedNames = []
      const selectedCodes = []
      for (let i = 0; i <= index; i++) {
        if (selectedValues.value[i]) {
          const option = levels[i].options.find(opt => opt.code === selectedValues.value[i])
          if (option) {
            selectedNames.push(option.name)
            selectedCodes.push(option.code)
          }
        }
      }
      emit('change', {
        codes: selectedCodes,
        names: selectedNames,
        fullPath: selectedNames.join(''),
        selectedValues: values
      })
    }

    // 处理清空
    const handleClear = (index) => {
      // 清空当前及后续级别的选择
      for (let i = index; i < selectedValues.value.length; i++) {
        selectedValues.value[i] = null
        if (i > index) {
          levels[i].options = []
          levels[i].disabled = true
        }
      }
      
      // 更新v-model
      const values = [...selectedValues.value]
      emit('update:modelValue', values)
      
      // 重新计算已选择的名称和代码
      const selectedNames = []
      const selectedCodes = []
      for (let i = 0; i < index; i++) {
        if (selectedValues.value[i]) {
          const option = levels[i].options.find(opt => opt.code === selectedValues.value[i])
          if (option) {
            selectedNames.push(option.name)
            selectedCodes.push(option.code)
          }
        }
      }
      
      emit('change', {
        codes: selectedCodes,
        names: selectedNames,
        fullPath: selectedNames.join(''),
        selectedValues: values
      })
    }

    // 根据codes数组加载数据
    const loadByCodes = async (codes) => {
      if (!codes || codes.length === 0) {
        return
      }

      try {
        // 加载省份
        await loadProvinces()
        
        // 如果第一个code存在，选中它并加载下一级
        if (codes[0]) {
          selectedValues.value[0] = codes[0]
          // 添加延迟，避免请求过于频繁
          await new Promise(resolve => setTimeout(resolve, 100))
          await loadChildren(codes[0], 1)
          
          // 如果第二个code存在，选中它并加载下一级
          if (codes[1] && !levels[1].disabled) {
            selectedValues.value[1] = codes[1]
            // 添加延迟，避免请求过于频繁
            await new Promise(resolve => setTimeout(resolve, 100))
            await loadChildren(codes[1], 2)
            
            // 如果第三个code存在，选中它并加载下一级
            if (codes[2] && !levels[2].disabled) {
              selectedValues.value[2] = codes[2]
              // 添加延迟，避免请求过于频繁
              await new Promise(resolve => setTimeout(resolve, 100))
              await loadChildren(codes[2], 3)
              
              // 如果第四个code存在，选中它
              if (codes[3] && !levels[3].disabled) {
                selectedValues.value[3] = codes[3]
              }
            }
          }
        }
      } catch (error) {
        // loadByCodes 中的错误已经在 loadProvinces 和 loadChildren 中处理，这里不需要重复提示
        console.error('加载区划数据失败:', error)
      }
    }

    // 监听modelValue变化
    watch(() => props.modelValue, (newVal) => {
      if (newVal && Array.isArray(newVal) && newVal.length > 0) {
        // 只有当值不同时才更新
        const isDifferent = selectedValues.value.some((val, index) => val !== newVal[index])
        if (isDifferent) {
          selectedValues.value = [...newVal]
          loadByCodes(newVal)
        }
      } else if (newVal && Array.isArray(newVal) && newVal.length === 0) {
        // 如果传入空数组，清空所有选择
        selectedValues.value = [null, null, null, null]
        // 重置后续级别的选项和禁用状态
        for (let i = 1; i < levels.length; i++) {
          levels[i].options = []
          levels[i].disabled = true
        }
      }
    }, { immediate: false })

    // 快速匹配函数：优先匹配长名称，避免遍历所有选项
    const findBestMatch = (options, address) => {
      if (!options || options.length === 0 || !address) {
        return null
      }
      
      // 按名称长度降序排序，优先匹配长名称（避免"北京市"匹配到"北京"）
      const sortedOptions = [...options].sort((a, b) => b.name.length - a.name.length)
      
      for (const option of sortedOptions) {
        if (address.startsWith(option.name)) {
          return option
        }
      }
      return null
    }

    // 解析地址字符串，提取省市区县街道信息
    const parseAddress = async (address) => {
      if (!address || typeof address !== 'string') {
        return { codes: [], names: [], detail: '' }
      }

      const codes = []
      const names = []
      let remainingAddress = address.trim()
      let detail = ''

      try {
        // 确保省份已加载（如果已加载则不需要等待）
        if (levels[0].options.length === 0) {
          await loadProvinces()
        }

        // 解析省份（使用优化后的匹配算法）
        const provinceMatch = findBestMatch(levels[0].options, remainingAddress)
        if (!provinceMatch) {
          // 无法匹配省份，整个地址作为详细地址
          return { codes: [], names: [], detail: address }
        }

        codes.push(provinceMatch.code)
        names.push(provinceMatch.name)
        remainingAddress = remainingAddress.substring(provinceMatch.name.length).trim()

        // 加载并解析市
        if (remainingAddress) {
          // 如果已加载且code匹配，直接使用；否则加载
          const needLoadCity = !levels[1].options.length || 
                               !levels[1].options.some(opt => opt.code === codes[0])
          if (needLoadCity) {
            await loadChildren(codes[0], 1)
          }
          
          const cityMatch = findBestMatch(levels[1].options, remainingAddress)
          if (cityMatch) {
            codes.push(cityMatch.code)
            names.push(cityMatch.name)
            remainingAddress = remainingAddress.substring(cityMatch.name.length).trim()

            // 解析区县
            if (remainingAddress) {
              const needLoadDistrict = !levels[2].options.length || 
                                       !levels[2].options.some(opt => opt.code === codes[1])
              if (needLoadDistrict) {
                await loadChildren(codes[1], 2)
              }
              
              const districtMatch = findBestMatch(levels[2].options, remainingAddress)
              if (districtMatch) {
                codes.push(districtMatch.code)
                names.push(districtMatch.name)
                remainingAddress = remainingAddress.substring(districtMatch.name.length).trim()

                // 解析乡镇街道
                if (remainingAddress) {
                  const needLoadStreet = !levels[3].options.length || 
                                         !levels[3].options.some(opt => opt.code === codes[2])
                  if (needLoadStreet) {
                    await loadChildren(codes[2], 3)
                  }
                  
                  const streetMatch = findBestMatch(levels[3].options, remainingAddress)
                  if (streetMatch) {
                    codes.push(streetMatch.code)
                    names.push(streetMatch.name)
                    remainingAddress = remainingAddress.substring(streetMatch.name.length).trim()
                  }
                }
              }
            }
          }
        }

        // 剩余部分作为详细地址
        detail = remainingAddress

        // 更新级联选择器的值
        const newValues = [...codes, null, null, null].slice(0, 4)
        selectedValues.value = newValues
        emit('update:modelValue', newValues)

        // 触发change事件
        emit('change', {
          codes: codes,
          names: names,
          fullPath: names.join(''),
          selectedValues: newValues
        })

        return { codes, names, detail }
      } catch (error) {
        console.error('解析地址失败:', error)
        return { codes: [], names: [], detail: address }
      }
    }

    // 组件挂载时加载省份
    onMounted(() => {
      loadProvinces()
    })

    // 暴露解析方法供父组件调用
    expose({
      parseAddress
    })

    return {
      selectedValues,
      levels,
      handleChange,
      handleClear,
      parseAddress
    }
  }
}
</script>

<style scoped>
.region-cascade {
  width: 100%;
}

.cascade-container {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.cascade-select {
  flex: 1;
  min-width: 120px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .cascade-container {
    flex-direction: column;
  }
  
  .cascade-select {
    width: 100%;
  }
}
</style>

