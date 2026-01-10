<template>
  <div class="dashboard">
    <h2>系统概览</h2>
    
    <!-- 统计卡片 -->
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="6">
        <el-card>
          <div class="stat-item">
            <div class="stat-value">{{ stats.userCount || 0 }}</div>
            <div class="stat-label">用户总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <div class="stat-item">
            <div class="stat-value">{{ stats.residentCount || 0 }}</div>
            <div class="stat-label">居民总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <div class="stat-item">
            <div class="stat-value">{{ stats.householdCount || 0 }}</div>
            <div class="stat-label">户籍总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <div class="stat-item">
            <div class="stat-value">{{ stats.adminCount || 0 }}</div>
            <div class="stat-label">管理员总数</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" style="margin-top: 20px">
      <!-- 居民性别统计 -->
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>居民性别统计</span>
          </template>
          <div ref="genderChartRef" style="width: 100%; height: 300px;"></div>
        </el-card>
      </el-col>
      
      <!-- 户籍类型统计 -->
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>户籍类型统计</span>
          </template>
          <div ref="householdTypeChartRef" style="width: 100%; height: 300px;"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <!-- 居民年龄分布 -->
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>居民年龄分布</span>
          </template>
          <div ref="ageChartRef" style="width: 100%; height: 300px;"></div>
        </el-card>
      </el-col>
      
      <!-- 户籍迁入迁出趋势 -->
      <el-col :span="12">
        <el-card>
          <template #header>
            <div style="display: flex; justify-content: space-between; align-items: center;">
              <span>户籍迁入迁出趋势</span>
              <el-radio-group v-model="trendType" size="small" @change="loadMoveTrend">
                <el-radio-button label="month">月度</el-radio-button>
                <el-radio-button label="year">年度</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div ref="moveTrendChartRef" style="width: 100%; height: 300px;"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <!-- 月度数据统计 -->
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>月度数据统计</span>
          </template>
          <div ref="monthlyChartRef" style="width: 100%; height: 300px;"></div>
        </el-card>
      </el-col>
      
      <!-- 年度数据统计 -->
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>年度数据统计</span>
          </template>
          <div ref="yearlyChartRef" style="width: 100%; height: 300px;"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { ref, onMounted, nextTick, onBeforeUnmount } from 'vue'
import * as echarts from 'echarts'
import { getUserList } from '../api/user'
import { getResidentList } from '../api/resident'
import { getHouseholdList } from '../api/household'
import { getAdminList } from '../api/admin'
import {
  getResidentAgeDistribution,
  getResidentGenderStatistics,
  getHouseholdTypeStatistics,
  getHouseholdMoveTrend,
  getMonthlyStatistics,
  getYearlyStatistics
} from '../api/statistics'
import { ElMessage } from 'element-plus'

export default {
  name: 'Dashboard',
  setup() {
    const stats = ref({
      userCount: 0,
      residentCount: 0,
      householdCount: 0,
      adminCount: 0
    })

    // 图表引用
    const genderChartRef = ref(null)
    const householdTypeChartRef = ref(null)
    const ageChartRef = ref(null)
    const moveTrendChartRef = ref(null)
    const monthlyChartRef = ref(null)
    const yearlyChartRef = ref(null)

    // 图表实例
    let genderChart = null
    let householdTypeChart = null
    let ageChart = null
    let moveTrendChart = null
    let monthlyChart = null
    let yearlyChart = null

    const trendType = ref('month')

    const loadStats = async () => {
      try {
        const [userRes, residentRes, householdRes, adminRes] = await Promise.all([
          getUserList({ current: 1, size: 1 }),
          getResidentList({ current: 1, size: 1 }),
          getHouseholdList({ current: 1, size: 1 }),
          getAdminList({ current: 1, size: 1 })
        ])
        stats.value.userCount = userRes.data?.total || 0
        stats.value.residentCount = residentRes.data?.total || 0
        stats.value.householdCount = householdRes.data?.total || 0
        stats.value.adminCount = adminRes.data?.total || 0
      } catch (error) {
        // 静默处理加载失败
      }
    }

    // 初始化性别统计图表
    const initGenderChart = async () => {
      try {
        const res = await getResidentGenderStatistics()
        if (res.code === 200 && genderChartRef.value) {
          if (!genderChart) {
            genderChart = echarts.init(genderChartRef.value)
          }
          
          const option = {
            tooltip: {
              trigger: 'item',
              formatter: '{a} <br/>{b}: {c} ({d}%)'
            },
            legend: {
              orient: 'vertical',
              left: 'left'
            },
            series: [
              {
                name: '性别统计',
                type: 'pie',
                radius: '50%',
                data: [
                  { value: res.data.male || 0, name: '男' },
                  { value: res.data.female || 0, name: '女' }
                ],
                emphasis: {
                  itemStyle: {
                    shadowBlur: 10,
                    shadowOffsetX: 0,
                    shadowColor: 'rgba(0, 0, 0, 0.5)'
                  }
                }
              }
            ]
          }
          genderChart.setOption(option)
        }
      } catch (error) {
        console.error('加载性别统计失败', error)
      }
    }

    // 初始化户籍类型统计图表
    const initHouseholdTypeChart = async () => {
      try {
        const res = await getHouseholdTypeStatistics()
        if (res.code === 200 && householdTypeChartRef.value) {
          if (!householdTypeChart) {
            householdTypeChart = echarts.init(householdTypeChartRef.value)
          }
          
          const option = {
            tooltip: {
              trigger: 'item',
              formatter: '{a} <br/>{b}: {c} ({d}%)'
            },
            legend: {
              orient: 'vertical',
              left: 'left'
            },
            series: [
              {
                name: '户籍类型',
                type: 'pie',
                radius: '50%',
                data: [
                  { value: res.data.family || 0, name: '家庭户' },
                  { value: res.data.collective || 0, name: '集体户' }
                ],
                emphasis: {
                  itemStyle: {
                    shadowBlur: 10,
                    shadowOffsetX: 0,
                    shadowColor: 'rgba(0, 0, 0, 0.5)'
                  }
                }
              }
            ]
          }
          householdTypeChart.setOption(option)
        }
      } catch (error) {
        console.error('加载户籍类型统计失败', error)
      }
    }

    // 初始化年龄分布图表
    const initAgeChart = async () => {
      try {
        const res = await getResidentAgeDistribution()
        if (res.code === 200 && ageChartRef.value) {
          if (!ageChart) {
            ageChart = echarts.init(ageChartRef.value)
          }
          
          const option = {
            tooltip: {
              trigger: 'axis',
              axisPointer: {
                type: 'shadow'
              }
            },
            grid: {
              left: '3%',
              right: '4%',
              bottom: '3%',
              containLabel: true
            },
            xAxis: {
              type: 'category',
              data: res.data.categories || [],
              axisTick: {
                alignWithLabel: true
              }
            },
            yAxis: {
              type: 'value'
            },
            series: [
              {
                name: '人数',
                type: 'bar',
                barWidth: '60%',
                data: res.data.data || [],
                itemStyle: {
                  color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                    { offset: 0, color: '#83bff6' },
                    { offset: 0.5, color: '#188df0' },
                    { offset: 1, color: '#188df0' }
                  ])
                }
              }
            ]
          }
          ageChart.setOption(option)
        }
      } catch (error) {
        console.error('加载年龄分布失败', error)
      }
    }

    // 加载迁入迁出趋势
    const loadMoveTrend = async () => {
      try {
        const res = await getHouseholdMoveTrend(trendType.value)
        if (res.code === 200 && moveTrendChartRef.value) {
          if (!moveTrendChart) {
            moveTrendChart = echarts.init(moveTrendChartRef.value)
          }
          
          const option = {
            tooltip: {
              trigger: 'axis'
            },
            legend: {
              data: ['迁入', '迁出']
            },
            grid: {
              left: '3%',
              right: '4%',
              bottom: '3%',
              containLabel: true
            },
            xAxis: {
              type: 'category',
              boundaryGap: false,
              data: res.data.categories || []
            },
            yAxis: {
              type: 'value'
            },
            series: [
              {
                name: '迁入',
                type: 'line',
                stack: 'Total',
                data: res.data.moveIn || [],
                smooth: true,
                itemStyle: { color: '#409EFF' }
              },
              {
                name: '迁出',
                type: 'line',
                stack: 'Total',
                data: res.data.moveOut || [],
                smooth: true,
                itemStyle: { color: '#F56C6C' }
              }
            ]
          }
          moveTrendChart.setOption(option)
        }
      } catch (error) {
        console.error('加载迁入迁出趋势失败', error)
      }
    }

    // 初始化月度统计图表
    const initMonthlyChart = async () => {
      try {
        const res = await getMonthlyStatistics()
        if (res.code === 200 && monthlyChartRef.value) {
          if (!monthlyChart) {
            monthlyChart = echarts.init(monthlyChartRef.value)
          }
          
          const option = {
            tooltip: {
              trigger: 'axis'
            },
            legend: {
              data: ['居民', '户籍']
            },
            grid: {
              left: '3%',
              right: '4%',
              bottom: '3%',
              containLabel: true
            },
            xAxis: {
              type: 'category',
              boundaryGap: false,
              data: res.data.categories || []
            },
            yAxis: {
              type: 'value'
            },
            series: [
              {
                name: '居民',
                type: 'line',
                data: res.data.resident || [],
                smooth: true,
                itemStyle: { color: '#409EFF' }
              },
              {
                name: '户籍',
                type: 'line',
                data: res.data.household || [],
                smooth: true,
                itemStyle: { color: '#67C23A' }
              }
            ]
          }
          monthlyChart.setOption(option)
        }
      } catch (error) {
        console.error('加载月度统计失败', error)
      }
    }

    // 初始化年度统计图表
    const initYearlyChart = async () => {
      try {
        const res = await getYearlyStatistics()
        if (res.code === 200 && yearlyChartRef.value) {
          if (!yearlyChart) {
            yearlyChart = echarts.init(yearlyChartRef.value)
          }
          
          const option = {
            tooltip: {
              trigger: 'axis'
            },
            legend: {
              data: ['居民', '户籍']
            },
            grid: {
              left: '3%',
              right: '4%',
              bottom: '3%',
              containLabel: true
            },
            xAxis: {
              type: 'category',
              boundaryGap: false,
              data: res.data.categories || []
            },
            yAxis: {
              type: 'value'
            },
            series: [
              {
                name: '居民',
                type: 'line',
                data: res.data.resident || [],
                smooth: true,
                itemStyle: { color: '#409EFF' }
              },
              {
                name: '户籍',
                type: 'line',
                data: res.data.household || [],
                smooth: true,
                itemStyle: { color: '#67C23A' }
              }
            ]
          }
          yearlyChart.setOption(option)
        }
      } catch (error) {
        console.error('加载年度统计失败', error)
      }
    }

    // 窗口大小改变时调整图表
    const handleResize = () => {
      genderChart?.resize()
      householdTypeChart?.resize()
      ageChart?.resize()
      moveTrendChart?.resize()
      monthlyChart?.resize()
      yearlyChart?.resize()
    }

    onMounted(async () => {
      await loadStats()
      
      // 等待DOM渲染完成后再初始化图表
      await nextTick()
      
      initGenderChart()
      initHouseholdTypeChart()
      initAgeChart()
      loadMoveTrend()
      initMonthlyChart()
      initYearlyChart()
      
      window.addEventListener('resize', handleResize)
    })

    onBeforeUnmount(() => {
      window.removeEventListener('resize', handleResize)
      genderChart?.dispose()
      householdTypeChart?.dispose()
      ageChart?.dispose()
      moveTrendChart?.dispose()
      monthlyChart?.dispose()
      yearlyChart?.dispose()
    })

    return {
      stats,
      genderChartRef,
      householdTypeChartRef,
      ageChartRef,
      moveTrendChartRef,
      monthlyChartRef,
      yearlyChartRef,
      trendType,
      loadMoveTrend
    }
  }
}
</script>

<style scoped>
.dashboard h2 {
  margin-bottom: 20px;
  color: #333;
}

.stat-item {
  text-align: center;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: #409EFF;
  margin-bottom: 10px;
}

.stat-label {
  font-size: 14px;
  color: #666;
}
</style>
