<template>
  <div class="analysis-container">
    <!-- 粒子动画背景 -->
    <canvas id="particleCanvas" class="particle-bg"></canvas>

    <!-- 顶部标题栏 -->
    <header class="analysis-header">
      <div class="header-left">
        <el-button class="back-btn" @click="goBack" circle>
          <el-icon><ArrowLeft /></el-icon>
        </el-button>
      </div>
      <h1 class="analysis-title">离线流量分析与预测</h1>
      <div class="header-right">
        <div class="current-time">{{ currentTime }}</div>
      </div>
    </header>

    <!-- 主内容区域 - 固定布局 -->
    <div class="analysis-grid">
      <!-- 左侧控制面板 -->
      <div class="control-panel panel-card">
        <div class="panel-header">
          <el-icon :size="20"><Setting /></el-icon>
          <span>控制面板</span>
        </div>
        
        <div class="panel-body">
          <!-- 运行预测按钮 -->
          <div class="control-section">
            <h3 class="section-title">预测控制</h3>
            <el-button 
              type="primary" 
              :loading="isRunning"
              @click="handleRunPrediction"
              class="run-btn"
              :disabled="isLoading"
            >
              <el-icon v-if="!isRunning"><VideoPlay /></el-icon>
              <span>{{ isRunning ? '预测计算中...' : '运行预测分析' }}</span>
            </el-button>
            <p class="btn-hint">点击运行离线预测算法，生成未来车流趋势</p>
          </div>

          <!-- 区域筛选 -->
          <div class="control-section">
            <h3 class="section-title">区域筛选</h3>
            <el-select 
              v-model="selectedDistrict" 
              placeholder="选择区域" 
              @change="handleDistrictChange"
              class="district-select"
              :disabled="isLoading || isRunning"
            >
              <el-option label="全部区域" value="all" />
              <el-option 
                v-for="district in availableDistricts" 
                :key="district" 
                :label="district" 
                :value="district" 
              />
            </el-select>
          </div>

          <!-- 数据状态 -->
          <div class="control-section">
            <h3 class="section-title">数据状态</h3>
            <div class="status-info">
              <div class="status-item">
                <span class="status-label">数据点:</span>
                <span class="status-value">{{ cityTrendData.length }}</span>
              </div>
              <div class="status-item">
                <span class="status-label">预测时段:</span>
                <span class="status-value">{{ predictedTimeRange }}</span>
              </div>
              <div class="status-item">
                <span class="status-label">区域数据:</span>
                <span class="status-value">{{ districtTrendData.length }} 条</span>
              </div>
            </div>
          </div>

          <!-- 刷新数据 -->
          <div class="control-section">
            <el-button 
              @click="loadInitialData" 
              :loading="isLoading"
              :disabled="isRunning"
              class="refresh-btn"
            >
              <el-icon><Refresh /></el-icon>
              <span>刷新数据</span>
            </el-button>
          </div>
        </div>
      </div>

      <!-- 中心图表区 -->
      <div class="chart-panel panel-card">
        <div class="panel-header">
          <el-icon :size="20"><TrendCharts /></el-icon>
          <span>全市未来车流预测趋势</span>
        </div>
        
        <div class="chart-container" v-loading="isLoading || isRunning">
          <div 
            ref="cityChartRef" 
            class="echarts-wrapper"
            v-show="cityTrendData.length > 0"
          ></div>
          <el-empty 
            v-show="cityTrendData.length === 0 && !isLoading && !isRunning"
            description="暂无预测数据，请点击'运行预测分析'"
            :image-size="120"
          />
        </div>
      </div>

      <!-- 右侧数据面板 -->
      <div class="data-panel panel-card">
        <div class="panel-header">
          <el-icon :size="20"><DataAnalysis /></el-icon>
          <span>{{ selectedDistrict === 'all' ? '各区域预测详情' : `${selectedDistrict} 预测详情` }}</span>
        </div>
        
        <div class="data-table-container" v-loading="isLoading || isRunning">
          <el-table 
            :data="displayDistrictData" 
            stripe
            height="100%"
            class="district-table"
            v-if="districtTrendData.length > 0"
          >
            <el-table-column prop="districtName" label="区域" width="100" fixed />
            <el-table-column prop="predictTime" label="预测时间" width="180">
              <template #default="{ row }">
                {{ formatTime(row.predictTime) }}
              </template>
            </el-table-column>
            <el-table-column prop="predictedVolume" label="预测车流" width="100">
              <template #default="{ row }">
                <span class="volume-badge">{{ row.predictedVolume }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="计算时间" min-width="180">
              <template #default="{ row }">
                {{ formatTime(row.createTime) }}
              </template>
            </el-table-column>
          </el-table>
          <el-empty 
            v-else-if="!isLoading && !isRunning"
            description="暂无区域数据"
            :image-size="100"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { 
  ArrowLeft, Setting, VideoPlay, Refresh, 
  TrendCharts, DataAnalysis 
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { 
  runPrediction, 
  getCityPredictionTrend, 
  getDistrictPredictionTrend 
} from '@/api/etc'

const router = useRouter()
const currentTime = ref('')
const isRunning = ref(false)
const isLoading = ref(false)
const selectedDistrict = ref('all')

// 数据状态
const cityTrendData = ref([])
const districtTrendData = ref([])
const cityChartRef = ref(null)
let cityChart = null
let timeTimer = null

// 粒子动画主要逻辑
let particleAnim = null

class ParticleAnimation {
  constructor(canvas) {
    this.canvas = canvas
    this.ctx = canvas.getContext('2d')
    this.particles = []
    this.mouse = { x: null, y: null, radius: 100 }
    
    this.resize()
    this.init()
    
    window.addEventListener('resize', () => this.resize())
    window.addEventListener('mousemove', (e) => {
      this.mouse.x = e.clientX
      this.mouse.y = e.clientY
    })
    window.addEventListener('mouseleave', () => {
        this.mouse.x = null
        this.mouse.y = null
    })
  }

  resize() {
    this.canvas.width = window.innerWidth
    this.canvas.height = window.innerHeight
    this.init()
  }

  init() {
    this.particles = []
    const particleCount = Math.min((window.innerWidth * window.innerHeight) / 15000, 80)
    for (let i = 0; i < particleCount; i++) {
        let size = (Math.random() * 2) + 0.5
        let x = Math.random() * this.canvas.width
        let y = Math.random() * this.canvas.height
        let directionX = (Math.random() * 0.5) - 0.25
        let directionY = (Math.random() * 0.5) - 0.25
        
        this.particles.push({
            x, y, directionX, directionY, size
        })
    }
  }

  draw() {
    this.ctx.clearRect(0, 0, this.canvas.width, this.canvas.height)
    
    for (let i = 0; i < this.particles.length; i++) {
        let p = this.particles[i]
        
        p.x += p.directionX
        p.y += p.directionY
        
        if (p.x > this.canvas.width || p.x < 0) p.directionX = -p.directionX
        if (p.y > this.canvas.height || p.y < 0) p.directionY = -p.directionY
        
        if (this.mouse.x != null) {
            let dx = this.mouse.x - p.x
            let dy = this.mouse.y - p.y
            let distance = Math.sqrt(dx*dx + dy*dy)
            
            if (distance < this.mouse.radius) {
                const force = (this.mouse.radius - distance) / this.mouse.radius
                const forceX = dx / distance * force * 5
                const forceY = dy / distance * force * 5
                p.x -= forceX
                p.y -= forceY
            }
        }
        
        this.ctx.beginPath()
        this.ctx.arc(p.x, p.y, p.size, 0, Math.PI * 2)
        this.ctx.fillStyle = '#667eea'
        this.ctx.fill()

        this.connect(p, this.particles.slice(i + 1))
    }
  }

  connect(p1, others) {
      for (let p2 of others) {
          let distance = ((p1.x - p2.x) * (p1.x - p2.x)) + ((p1.y - p2.y) * (p1.y - p2.y))
          if (distance < (this.canvas.width/7) * (this.canvas.height/7)) {
              let opacityValue = 1 - (distance / 20000)
              if (opacityValue > 0) {
                  this.ctx.strokeStyle = `rgba(102, 126, 234, ${opacityValue * 0.2})`
                  this.ctx.lineWidth = 1
                  this.ctx.beginPath()
                  this.ctx.moveTo(p1.x, p1.y)
                  this.ctx.lineTo(p2.x, p2.y)
                  this.ctx.stroke()
              }
          }
      }
  }

  animate() {
    this.draw()
    requestAnimationFrame(() => this.animate())
  }
}

// 可用区域列表（从数据中提取）
const availableDistricts = computed(() => {
  const districts = new Set()
  districtTrendData.value.forEach(item => {
    if (item.districtName) {
      districts.add(item.districtName)
    }
  })
  return Array.from(districts).sort()
})

// 预测时段范围
const predictedTimeRange = computed(() => {
  if (cityTrendData.value.length === 0) return '无'
  const times = cityTrendData.value.map(item => new Date(item.predict_time))
  const earliest = new Date(Math.min(...times))
  const latest = new Date(Math.max(...times))
  return `${earliest.getHours()}时 - ${latest.getHours()}时`
})

// 显示的区域数据（根据筛选条件）
const displayDistrictData = computed(() => {
  if (selectedDistrict.value === 'all') {
    return districtTrendData.value
  }
  return districtTrendData.value.filter(
    item => item.districtName === selectedDistrict.value
  )
})

// 方法
const goBack = () => router.push('/')

const updateTime = () => {
  const now = new Date()
  currentTime.value = now.toLocaleString('zh-CN', {
    hour12: false,
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

const formatTime = (timeStr) => {
  if (!timeStr) return '-'
  const date = new Date(timeStr)
  return date.toLocaleString('zh-CN', { hour12: false })
}

// 运行预测
const handleRunPrediction = async () => {
  try {
    isRunning.value = true
    ElMessage.info('正在运行预测算法，请稍候...')
    
    const res = await runPrediction()
    
    if (res.code === 200) {
      ElMessage.success('预测完成！正在加载最新数据...')
      cityTrendData.value = res.data || []
      await loadDistrictData()
      await nextTick()
      renderCityChart()
    } else {
      ElMessage.error(res.message || '预测失败')
    }
  } catch (error) {
    console.error('运行预测失败：', error)
    ElMessage.error('预测失败：' + (error.message || '未知错误'))
  } finally {
    isRunning.value = false
  }
}

// 加载全市趋势数据
const loadCityTrend = async () => {
  try {
    const res = await getCityPredictionTrend()
    if (res.code === 200) {
      cityTrendData.value = res.data || []
    }
  } catch (error) {
    console.error('获取全市趋势失败：', error)
  }
}

// 加载区域趋势数据
const loadDistrictData = async (districtName = null) => {
  try {
    const res = await getDistrictPredictionTrend(districtName)
    if (res.code === 200) {
      districtTrendData.value = res.data || []
    }
  } catch (error) {
    console.error('获取区域数据失败：', error)
  }
}

// 初始化数据加载
const loadInitialData = async () => {
  isLoading.value = true
  try {
    await Promise.all([
      loadCityTrend(),
      loadDistrictData()
    ])
    await nextTick()
    renderCityChart()
  } catch (error) {
    console.error('加载数据失败：', error)
    ElMessage.error('数据加载失败')
  } finally {
    isLoading.value = false
  }
}

// 区域筛选变化
const handleDistrictChange = async () => {
  // 表格会自动根据computed属性更新，无需额外操作
}

// 渲染全市趋势图表
const renderCityChart = () => {
  if (!cityChartRef.value || cityTrendData.value.length === 0) return
  
  if (!cityChart) {
    cityChart = echarts.init(cityChartRef.value)
  }
  
  // 【调试】输出原始数据
  console.log('📊 [Analysis] 原始数据:', cityTrendData.value)
  console.log('📊 [Analysis] 数据条数:', cityTrendData.value.length)
  
  const times = cityTrendData.value.map(item => {
    const date = new Date(item.predict_time)
    return date.toLocaleString('zh-CN', { 
      month: '2-digit', 
      day: '2-digit', 
      hour: '2-digit', 
      minute: '2-digit' 
    })
  })
  
  const volumes = cityTrendData.value.map(item => item.total_volume)
  
  // 【调试】输出处理后的数据
  console.log('📊 [Analysis] 时间轴:', times)
  console.log('📊 [Analysis] 数值:', volumes)
  
  const option = {
    backgroundColor: 'transparent',
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '10%',
      containLabel: true
    },
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(0, 0, 0, 0.8)',
      borderColor: '#00d4ff',
      borderWidth: 1,
      textStyle: { color: '#fff' },
      formatter: (params) => {
        const param = params[0]
        return `
          <div style="padding: 5px;">
            <div style="color: #00d4ff; font-weight: bold;">${param.name}</div>
            <div style="margin-top: 5px;">预测车流: <span style="color: #ffd700; font-weight: bold;">${param.value}</span></div>
          </div>
        `
      }
    },
    xAxis: {
      type: 'category',
      data: times,
      axisLine: { lineStyle: { color: '#667eea' } },
      axisLabel: { 
        color: '#8b9dc3',
        rotate: 45,
        fontSize: 11
      }
    },
    yAxis: {
      type: 'value',
      name: '车流量',
      nameTextStyle: { color: '#8b9dc3' },
      axisLine: { lineStyle: { color: '#667eea' } },
      axisLabel: { color: '#8b9dc3' },
      splitLine: { 
        lineStyle: { 
          color: 'rgba(102, 126, 234, 0.2)',
          type: 'dashed'
        } 
      }
    },
    series: [
      {
        name: '预测车流',
        type: 'line',
        smooth: true,
        data: volumes,
        lineStyle: {
          color: '#00d4ff',
          width: 3,
          shadowColor: 'rgba(0, 212, 255, 0.5)',
          shadowBlur: 10
        },
        itemStyle: { 
          color: '#00d4ff',
          borderColor: '#fff',
          borderWidth: 2
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(0, 212, 255, 0.5)' },
            { offset: 1, color: 'rgba(0, 212, 255, 0.05)' }
          ])
        },
        emphasis: {
          itemStyle: {
            scale: true,
            scaleSize: 8
          }
        }
      }
    ]
  }
  
  cityChart.setOption(option)
}

// 响应式调整
const handleResize = () => {
  if (cityChart) {
    cityChart.resize()
  }
}

// 生命周期
onMounted(async () => {
  updateTime()
  timeTimer = setInterval(updateTime, 1000)
  
  await loadInitialData()
  
  window.addEventListener('resize', handleResize)
  
  // 初始化粒子
  const canvas = document.getElementById('particleCanvas')
  if (canvas) {
    particleAnim = new ParticleAnimation(canvas)
    particleAnim.animate()
  }
})

onUnmounted(() => {
  if (timeTimer) clearInterval(timeTimer)
  if (cityChart) {
    cityChart.dispose()
    cityChart = null
  }
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
/* 容器 - 固定视口高度，不滚动 */
.analysis-container {
  background-color: #0b0d17;
  height: 100vh;
  width: 100vw;
  color: #fff;
  padding: 12px 16px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  box-sizing: border-box;
  position: relative;
}

/* 粒子背景 - FIXED positioning */
.particle-bg {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 0;
  pointer-events: none;
}
/* 顶部标题栏 - 固定高度 */
.analysis-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 50px;
  flex-shrink: 0;
  margin-bottom: 12px;
}

.back-btn {
  background: rgba(102, 126, 234, 0.2);
  border: 1px solid rgba(102, 126, 234, 0.5);
  color: #fff;
}

.back-btn:hover {
  background: rgba(102, 126, 234, 0.4);
  border-color: #667eea;
}

.analysis-title {
  font-size: 26px;
  font-weight: 700;
  background: linear-gradient(90deg, #667eea 0%, #00d4ff 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin: 0;
  letter-spacing: 2px;
}

.current-time {
  font-size: 15px;
  font-weight: 600;
  color: #00d4ff;
  font-family: 'Courier New', monospace;
}

/* 主网格布局 - 固定高度 */
.analysis-grid {
  flex: 1;
  display: grid;
  grid-template-columns: 320px 1fr 400px;
  gap: 12px;
  height: calc(100vh - 90px);
  min-height: 500px;
  overflow: hidden;
}

/* 通用面板卡片样式 */
.panel-card {
  background: rgba(11, 13, 23, 0.85);
  backdrop-filter: blur(8px);
  border: 1px solid rgba(102, 126, 234, 0.4);
  border-radius: 12px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  height: 100%;
  box-sizing: border-box;
  box-shadow: 0 0 15px rgba(102, 126, 234, 0.1);
  transition: all 0.3s ease;
}

.panel-card:hover {
  border-color: rgba(102, 126, 234, 0.8);
  box-shadow: 0 0 20px rgba(102, 126, 234, 0.2);
}

.panel-header {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 18px;
  background: linear-gradient(90deg, rgba(0, 212, 255, 0.15), transparent);
  border-bottom: 1px solid rgba(102, 126, 234, 0.3);
  color: #00d4ff;
  font-size: 15px;
  font-weight: 600;
  flex-shrink: 0;
}

/* 左侧控制面板 */
.control-panel {
  overflow-y: auto;
}

.panel-body {
  padding: 16px;
  flex: 1;
  overflow-y: auto;
}

.control-section {
  margin-bottom: 24px;
}

.section-title {
  font-size: 14px;
  color: #8b9dc3;
  margin-bottom: 12px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 1px;
}

.run-btn {
  width: 100%;
  height: 48px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  font-size: 15px;
  font-weight: 600;
  border-radius: 8px;
  transition: all 0.3s;
}

.run-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 16px rgba(102, 126, 234, 0.4);
}

.btn-hint {
  font-size: 12px;
  color: #667eea;
  margin-top: 8px;
  line-height: 1.5;
}

.district-select {
  width: 100%;
}

:deep(.el-select .el-input__wrapper) {
  background: rgba(102, 126, 234, 0.1);
  border-color: rgba(102, 126, 234, 0.3);
  box-shadow: none;
}

:deep(.el-select .el-input__wrapper:hover) {
  border-color: rgba(102, 126, 234, 0.5);
}

.status-info {
  background: rgba(102, 126, 234, 0.1);
  border-radius: 8px;
  padding: 12px;
}

.status-item {
  display: flex;
  justify-content: space-between;
  padding: 6px 0;
  border-bottom: 1px solid rgba(102, 126, 234, 0.2);
}

.status-item:last-child {
  border-bottom: none;
}

.status-label {
  color: #8b9dc3;
  font-size: 13px;
}

.status-value {
  color: #00d4ff;
  font-weight: 600;
  font-size: 13px;
}

.refresh-btn {
  width: 100%;
  background: rgba(0, 212, 255, 0.1);
  border: 1px solid rgba(0, 212, 255, 0.3);
  color: #00d4ff;
}

.refresh-btn:hover:not(:disabled) {
  background: rgba(0, 212, 255, 0.2);
  border-color: #00d4ff;
}

/* 中心图表区 */
.chart-panel {
  min-width: 0;
  border: 2px solid rgba(0, 212, 255, 0.5);
  box-shadow: 0 0 25px rgba(0, 212, 255, 0.15);
}

.chart-container {
  flex: 1;
  padding: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  min-height: 0;
}

.echarts-wrapper {
  width: 100%;
  height: 100%;
}

/* 右侧数据面板 */
.data-panel {
  overflow: hidden;
}

.data-table-container {
  flex: 1;
  padding: 16px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

/* 表格深度样式定制 - 强制深色主题 */
.district-table {
  flex: 1;
}

/* 表格整体背景 */
:deep(.district-table) {
  background-color: rgba(10, 14, 39, 0.95) !important;
  --el-table-bg-color: rgba(10, 14, 39, 0.95) !important;
  --el-table-tr-bg-color: rgba(10, 14, 39, 0.95) !important;
  --el-table-text-color: #ffffff !important;
  --el-table-header-text-color: #00d4ff !important;
  --el-table-header-bg-color: rgba(102, 126, 234, 0.3) !important;
  --el-table-row-hover-bg-color: rgba(102, 126, 234, 0.2) !important;
  --el-table-border-color: rgba(102, 126, 234, 0.2) !important;
}

:deep(.district-table .el-table__inner-wrapper) {
  background-color: rgba(10, 14, 39, 0.95) !important;
}

:deep(.district-table .el-table__body-wrapper) {
  background-color: rgba(10, 14, 39, 0.95) !important;
}

/* 表头样式 - 深色背景 + 亮蓝色文字 */
:deep(.district-table .el-table__header-wrapper) {
  background-color: rgba(102, 126, 234, 0.3) !important;
}

:deep(.district-table th.el-table__cell) {
  background-color: rgba(102, 126, 234, 0.3) !important;
  color: #00d4ff !important;
  font-weight: 600 !important;
  border-bottom: 1px solid rgba(102, 126, 234, 0.4) !important;
}

/* 表格单元格 - 深色背景 + 白色文字 */
:deep(.district-table td.el-table__cell) {
  background-color: rgba(10, 14, 39, 0.95) !important;
  color: #ffffff !important;
  border-bottom: 1px solid rgba(102, 126, 234, 0.2) !important;
}

/* 表格行背景 */
:deep(.district-table .el-table__row) {
  background-color: rgba(10, 14, 39, 0.95) !important;
}

/* 斑马纹 - 稍亮的深色背景 */
:deep(.district-table .el-table__row--striped) {
  background-color: rgba(26, 30, 62, 0.95) !important;
}

:deep(.district-table .el-table__row--striped td.el-table__cell) {
  background-color: rgba(26, 30, 62, 0.95) !important;
}

/* 鼠标悬停行 - 蓝色高亮 */
:deep(.district-table .el-table__body tr:hover) {
  background-color: rgba(102, 126, 234, 0.25) !important;
}

:deep(.district-table .el-table__body tr:hover > td.el-table__cell) {
  background-color: rgba(102, 126, 234, 0.25) !important;
}

/* 固定列背景 */
:deep(.district-table .el-table__fixed),
:deep(.district-table .el-table__fixed-right) {
  background-color: rgba(10, 14, 39, 0.95) !important;
}

:deep(.district-table .el-table__fixed-header-wrapper th.el-table__cell) {
  background-color: rgba(102, 126, 234, 0.3) !important;
}

/* 空状态 */
:deep(.district-table .el-table__empty-block) {
  background-color: rgba(10, 14, 39, 0.95) !important;
}

:deep(.district-table .el-table__empty-text) {
  color: #8b9dc3 !important;
}

.volume-badge {
  display: inline-block;
  padding: 4px 12px;
  background: linear-gradient(135deg, #00d4ff 0%, #667eea 100%);
  border-radius: 12px;
  font-weight: 600;
  font-size: 13px;
  color: #0a0e27;
}

/* 滚动条样式 */
::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

::-webkit-scrollbar-track {
  background: rgba(102, 126, 234, 0.1);
}

::-webkit-scrollbar-thumb {
  background: rgba(102, 126, 234, 0.4);
  border-radius: 3px;
}

::-webkit-scrollbar-thumb:hover {
  background: rgba(102, 126, 234, 0.6);
}

/* 响应式 */
@media (max-width: 1500px) {
  .analysis-grid {
    grid-template-columns: 280px 1fr 360px;
  }
}

@media (max-width: 1200px) {
  .analysis-grid {
    grid-template-columns: 260px 1fr 320px;
    gap: 10px;
  }
  
  .analysis-title {
    font-size: 22px;
  }
}
</style>
