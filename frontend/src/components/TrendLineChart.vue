<template>
  <div class="trend-line-chart" :class="{ 'fullscreen-mode': mode === 'fullscreen' }">
    <div class="card-header" v-if="mode === 'thumbnail'">
      <el-icon><TrendCharts /></el-icon>
      <span>实时车流趋势</span>
    </div>
    <div class="card-body">
      <!-- 优化的模式切换器 -->
      <div class="mode-switcher" v-if="showCategorySelector">
        <div 
          class="switch-btn"
          :class="{ 'active': displayMode === 'total' }"
          @click="switchMode('total')"
        >
          <div class="btn-bg"></div>
          <span class="btn-text">总流量</span>
        </div>
        <div 
          class="switch-btn"
          :class="{ 'active': displayMode === 'category' }"
          @click="switchMode('category')"
        >
          <div class="btn-bg"></div>
          <span class="btn-text">按车牌划分</span>
        </div>
        <div class="switch-indicator" :style="indicatorStyle"></div>
      </div>
      
      <div class="chart-title" v-if="mode === 'fullscreen'">
        <h2><el-icon><TrendCharts /></el-icon> 实时车流趋势监控</h2>
      </div>
      <div ref="chartRef" class="chart-container"></div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { TrendCharts } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { getTrendByCategory } from '@/api/etc'
import gsap from 'gsap'

const props = defineProps({
  mode: { type: String, default: 'thumbnail' },
  data: { type: Array, default: () => [] },
  showCategorySelector: { type: Boolean, default: false }
})

const chartRef = ref(null)
// 从localStorage恢复用户的选择,默认为'total'
const savedMode = localStorage.getItem('trendChart_displayMode') || 'total'
const displayMode = ref(savedMode)
const categoryData = ref(null)
let chartInstance = null

// 滑动指示器样式
const indicatorStyle = computed(() => ({
  transform: displayMode.value === 'total' ? 'translateX(0)' : 'translateX(100%)'
}))

// 切换模式
const switchMode = (mode) => {
  if (displayMode.value === mode) return
  displayMode.value = mode
  
  // 保存用户选择到localStorage
  localStorage.setItem('trendChart_displayMode', mode)
  
  // 动画过渡 - 与Dashboard保持一致的150ms延迟
  if (chartInstance) {
    chartInstance.clear()
  }
  
  setTimeout(() => {
    renderChart()
    // 确保图表大小正确
    if (chartInstance) {
      chartInstance.resize()
    }
  }, 150)
}

// 获取分类数据
const fetchCategoryData = async () => {
  try {
    const res = await getTrendByCategory()
    if (res.code === 200 && res.data) {
      categoryData.value = res.data
    }
  } catch (error) {
    console.error('获取分类趋势数据失败：', error)
  }
}

const renderChart = () => {
  if (!chartRef.value) return
  
  if (!chartInstance) {
    chartInstance = echarts.init(chartRef.value)
  }
  
  const isFullscreen = props.mode === 'fullscreen'
  
  if (displayMode.value === 'total') {
    // 总流量模式 - 单条线
    renderTotalTrend(isFullscreen)
  } else {
    // 按车牌划分模式 - 三条线
    renderCategoryTrend(isFullscreen)
  }
}

const renderTotalTrend = (isFullscreen) => {
  if (!props.data || props.data.length === 0) {
    chartInstance.clear()
    return
  }
  
  const sortedData = [...props.data].sort((a, b) => 
    (a.time_str || '').localeCompare(b.time_str || '')
  )
  
  const filteredData = sortedData.length > 1 ? sortedData.slice(0, -1) : sortedData
  
  const xData = filteredData.map(item => item.time_str || '未知')
  const yData = filteredData.map(item => item.vehicle_count || 0)
  
  const option = {
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(10, 14, 39, 0.9)',
      borderColor: '#667eea',
      textStyle: { color: '#fff' }
    },
    grid: {
      left: '5%',
      right: '5%',
      bottom: '10%',
      top: isFullscreen ? '15%' : '15%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: xData,
      axisLine: { lineStyle: { color: 'rgba(255,255,255,0.2)' } },
      axisLabel: { color: '#fff', fontSize: isFullscreen ? 14 : 10 }
    },
    yAxis: {
      type: 'value',
      axisLine: { lineStyle: { color: 'rgba(255,255,255,0.2)' } },
      axisLabel: { color: '#fff', fontSize: isFullscreen ? 14 : 10 },
      splitLine: { lineStyle: { color: 'rgba(255,255,255,0.05)' } }
    },
    series: [{
      name: '总车流',
      type: 'line',
      smooth: true,
      symbol: isFullscreen ? 'circle' : 'none',
      symbolSize: 8,
      lineStyle: {
        width: isFullscreen ? 3 : 2,
        color: {
          type: 'linear', x: 0, y: 0, x2: 1, y2: 0,
          colorStops: [
            { offset: 0, color: '#667eea' },
            { offset: 1, color: '#00d4ff' }
          ]
        }
      },
      areaStyle: {
        color: {
          type: 'linear', x: 0, y: 0, x2: 0, y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(102, 126, 234, 0.4)' },
            { offset: 1, color: 'rgba(0, 212, 255, 0.05)' }
          ]
        }
      },
      data: yData,
      animationDuration: 1000,
      animationEasing: 'cubicOut'
    }]
  }
  
  chartInstance.setOption(option, true)
  chartInstance.resize()
}

const renderCategoryTrend = (isFullscreen) => {
  if (!categoryData.value || !categoryData.value.xData) {
    chartInstance.clear()
    return
  }
  
  const xData = categoryData.value.xData
  
  const option = {
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(10, 14, 39, 0.9)',
      borderColor: '#667eea',
      textStyle: { color: '#fff' },
      formatter: (params) => {
        let result = `<div style="padding: 5px;">
          <div style="color: #00d4ff; font-weight: bold; margin-bottom: 8px;">${params[0].name}</div>`
        params.forEach(param => {
          result += `<div style="display: flex; align-items: center; margin: 4px 0;">
            <span style="display: inline-block; width: 10px; height: 10px; border-radius: 50%; background: ${param.color}; margin-right: 8px;"></span>
            <span>${param.seriesName}: <span style="color: #ffd700; font-weight: bold;">${param.value}</span></span>
          </div>`
        })
        result += '</div>'
        return result
      }
    },
    legend: {
      data: ['蓝牌车', '黄牌车', '绿牌车'],
      textStyle: { color: '#fff' },
      top: '5%',
      right: '5%',
      itemGap: 20
    },
    grid: {
      left: '5%',
      right: '5%',
      bottom: '10%',
      top: isFullscreen ? '18%' : '18%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: xData,
      axisLine: { lineStyle: { color: 'rgba(255,255,255,0.2)' } },
      axisLabel: { color: '#fff', fontSize: isFullscreen ? 14 : 10 }
    },
    yAxis: {
      type: 'value',
      axisLine: { lineStyle: { color: 'rgba(255,255,255,0.2)' } },
      axisLabel: { color: '#fff', fontSize: isFullscreen ? 14 : 10 },
      splitLine: { lineStyle: { color: 'rgba(255,255,255,0.05)' } }
    },
    series: [
      {
        name: '蓝牌车',
        type: 'line',
        smooth: true,
        symbol: isFullscreen ? 'circle' : 'none',
        symbolSize: 6,
        lineStyle: {
          width: isFullscreen ? 3 : 2,
          color: '#4A90E2'
        },
        areaStyle: {
          color: {
            type: 'linear', x: 0, y: 0, x2: 0, y2: 1,
            colorStops: [
              { offset: 0, color: 'rgba(74, 144, 226, 0.3)' },
              { offset: 1, color: 'rgba(74, 144, 226, 0.05)' }
            ]
          }
        },
        data: categoryData.value.blue || [],
        animationDuration: 1000,
        animationEasing: 'cubicOut'
      },
      {
        name: '黄牌车',
        type: 'line',
        smooth: true,
        symbol: isFullscreen ? 'circle' : 'none',
        symbolSize: 6,
        lineStyle: {
          width: isFullscreen ? 3 : 2,
          color: '#F5A623'
        },
        areaStyle: {
          color: {
            type: 'linear', x: 0, y: 0, x2: 0, y2: 1,
            colorStops: [
              { offset: 0, color: 'rgba(245, 166, 35, 0.3)' },
              { offset: 1, color: 'rgba(245, 166, 35, 0.05)' }
            ]
          }
        },
        data: categoryData.value.yellow || [],
        animationDuration: 1000,
        animationEasing: 'cubicOut',
        animationDelay: 100
      },
      {
        name: '绿牌车',
        type: 'line',
        smooth: true,
        symbol: isFullscreen ? 'circle' : 'none',
        symbolSize: 6,
        lineStyle: {
          width: isFullscreen ? 3 : 2,
          color: '#7ED321'
        },
        areaStyle: {
          color: {
            type: 'linear', x: 0, y: 0, x2: 0, y2: 1,
            colorStops: [
              { offset: 0, color: 'rgba(126, 211, 33, 0.3)' },
              { offset: 1, color: 'rgba(126, 211, 33, 0.05)' }
            ]
          }
        },
        data: categoryData.value.green || [],
        animationDuration: 1000,
        animationEasing: 'cubicOut',
        animationDelay: 200
      }
    ]
  }
  
  chartInstance.setOption(option, true)
  chartInstance.resize()
}

const handleResize = () => {
  chartInstance?.resize()
}

// 智能数据更新:只在总流量模式下自动刷新,按车牌划分模式使用自己的数据
watch(() => props.data, () => {
  if (displayMode.value === 'total') {
    setTimeout(() => {
      if (chartInstance) {
        chartInstance.resize()
      }
      renderChart()
    }, 150)
  }
}, { deep: true })

// mode变化时只调整图表大小,不重新渲染(保持用户选择的displayMode)
watch(() => props.mode, () => {
  setTimeout(() => {
    if (chartInstance) {
      chartInstance.resize()
    }
  }, 150)
})

onMounted(async () => {
  // 如果启用了分类选择器,总是需要获取分类数据(以便用户切换)
  if (props.showCategorySelector) {
    await fetchCategoryData()
  }
  
  setTimeout(renderChart, 50)
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  chartInstance?.dispose()
})
</script>

<style scoped>
.trend-line-chart {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.card-header {
  background: linear-gradient(90deg, transparent, rgba(102, 126, 234, 0.2), transparent);
  border-left: 3px solid #667eea;
  padding: 10px 14px;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 600;
  color: #00d4ff;
}

.card-body {
  flex: 1;
  padding: 16px;
  min-height: 0;
  display: flex;
  flex-direction: column;
}

/* 科技感模式切换器 */
.mode-switcher {
  position: relative;
  display: flex;
  gap: 0;
  background: rgba(10, 14, 39, 0.6);
  border: 1px solid rgba(102, 126, 234, 0.3);
  border-radius: 8px;
  padding: 4px;
  margin-bottom: 16px;
  width: fit-content;
  align-self: flex-end;
}

.switch-btn {
  position: relative;
  padding: 8px 20px;
  cursor: pointer;
  z-index: 2;
  transition: all 0.3s ease;
  border-radius: 6px;
}

.btn-text {
  position: relative;
  font-size: 13px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.5);
  transition: color 0.3s ease;
  white-space: nowrap;
}

.switch-btn.active .btn-text {
  color: #00d4ff;
  text-shadow: 0 0 8px rgba(0, 212, 255, 0.5);
}

.switch-indicator {
  position: absolute;
  top: 4px;
  left: 4px;
  width: calc(50% - 4px);
  height: calc(100% - 8px);
  background: linear-gradient(135deg, 
    rgba(102, 126, 234, 0.4) 0%, 
    rgba(0, 212, 255, 0.4) 100%);
  border-radius: 6px;
  transition: transform 0.3s ease;
  z-index: 1;
  box-shadow: 
    0 0 15px rgba(0, 212, 255, 0.3),
    inset 0 1px 1px rgba(255, 255, 255, 0.1);
}

.switch-btn:hover:not(.active) .btn-text {
  color: rgba(255, 255, 255, 0.8);
}

.chart-title {
  margin-bottom: 20px;
}

.chart-title h2 {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #00d4ff;
  font-size: 24px;
  margin: 0;
}

.chart-container {
  flex: 1;
  width: 100%;
  min-height: 150px;
}

.fullscreen-mode .chart-container {
  min-height: 400px;
}

/* 响应式调整 */
@media (max-width: 1200px) {
  .switch-btn {
    padding: 6px 16px;
  }
  
  .btn-text {
    font-size: 12px;
  }
}
</style>
