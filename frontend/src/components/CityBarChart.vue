<template>
  <div class="city-bar-chart" :class="{ 'fullscreen-mode': mode === 'fullscreen' }">
    <div class="card-header" v-if="mode === 'thumbnail'">
      <el-icon><Histogram /></el-icon>
      <span>各地市车流统计</span>
    </div>
    <div class="card-body">
      <div ref="chartRef" class="chart-container"></div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted, onUnmounted } from 'vue'
import { Histogram } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import gsap from 'gsap'

const props = defineProps({
  mode: { type: String, default: 'thumbnail' },
  data: { type: Array, default: () => [] }
})

const chartRef = ref(null)
let chartInstance = null

const renderChart = () => {
  if (!chartRef.value || !props.data.length) return
  
  if (!chartInstance) {
    chartInstance = echarts.init(chartRef.value)
  }
  
  const isFullscreen = props.mode === 'fullscreen'
  
  // Sort data
  const sortedData = [...props.data].sort((a, b) => {
    const aVal = a.count || a.value || 0
    const bVal = b.count || b.value || 0
    return aVal - bVal
  })
  
  const option = {
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      backgroundColor: 'rgba(10, 14, 39, 0.9)',
      borderColor: '#667eea',
      textStyle: { color: '#fff' }
    },
    grid: {
      left: '5%',
      right: isFullscreen ? '10%' : '5%',
      bottom: '5%',
      top: '5%',
      containLabel: true
    },
    xAxis: {
      type: 'value',
      axisLine: { lineStyle: { color: 'rgba(255,255,255,0.2)' } },
      axisLabel: { color: '#fff', fontSize: isFullscreen ? 14 : 10 },
      splitLine: { lineStyle: { color: 'rgba(255,255,255,0.05)' } }
    },
    yAxis: {
      type: 'category',
      data: sortedData.map(item => (item.districtName || item.name).replace('市', '')),
      axisLine: { lineStyle: { color: 'rgba(255,255,255,0.2)' } },
      axisLabel: { color: '#fff', fontSize: isFullscreen ? 14 : 10 }
    },
    series: [{
      type: 'bar',
      data: sortedData.map(item => ({
        value: item.count || item.value,
        itemStyle: {
          color: {
            type: 'linear',
            x: 0, y: 0, x2: 1, y2: 0,
            colorStops: [
              { offset: 0, color: '#667eea' },
              { offset: 1, color: '#00d4ff' }
            ]
          }
        }
      })),
      barWidth: isFullscreen ? '50%' : '60%',
      label: {
        show: true,
        position: 'right',
        color: '#fff',
        fontSize: isFullscreen ? 14 : 10
      }
    }]
  }
  
  chartInstance.setOption(option, true)
  chartInstance.resize()
}

const handleResize = () => {
  chartInstance?.resize()
}

watch(() => [props.data, props.mode], () => {
  setTimeout(() => {
    if (chartInstance) {
      chartInstance.resize()
    }
    renderChart()
  }, 150)
}, { deep: true })

onMounted(() => {
  setTimeout(renderChart, 50)
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  chartInstance?.dispose()
})
</script>

<style scoped>
.city-bar-chart {
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
}

.chart-container {
  width: 100%;
  height: 100%;
  min-height: 150px;
}

.fullscreen-mode .chart-container {
  min-height: 400px;
}
</style>
