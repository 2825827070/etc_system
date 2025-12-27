<template>
  <div class="district-pie-chart" :class="{ 'fullscreen-mode': mode === 'fullscreen' }">
    <div class="card-header" v-if="mode === 'thumbnail'">
      <el-icon><PieChart /></el-icon>
      <span>行政区车流占比</span>
    </div>
    <div class="card-body">
      <div ref="chartRef" class="chart-container"></div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted, onUnmounted } from 'vue'
import { PieChart } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import gsap from 'gsap'

const props = defineProps({
  mode: { type: String, default: 'thumbnail' },
  data: { type: Array, default: () => [] }
})

const chartRef = ref(null)
let chartInstance = null

const colors = [
  '#667eea', '#764ba2', '#f093fb', '#f5576c',
  '#4facfe', '#00f2fe', '#43e97b', '#38f9d7',
  '#fa709a', '#fee140', '#30cfd0', '#330867', '#a8edea'
]

const renderChart = () => {
  if (!chartRef.value || !props.data.length) return
  
  if (!chartInstance) {
    chartInstance = echarts.init(chartRef.value)
  }
  
  const isFullscreen = props.mode === 'fullscreen'
  
  const option = {
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)',
      backgroundColor: 'rgba(10, 14, 39, 0.9)',
      borderColor: '#667eea',
      textStyle: { color: '#fff' }
    },
    legend: {
      show: isFullscreen,
      orient: 'vertical',
      right: '5%',
      top: 'center',
      textStyle: { color: '#fff', fontSize: 14 }
    },
    series: [{
      type: 'pie',
      radius: isFullscreen ? ['35%', '65%'] : ['40%', '75%'],
      center: isFullscreen ? ['40%', '50%'] : ['50%', '50%'],
      avoidLabelOverlap: false,
      itemStyle: {
        borderRadius: 8,
        borderColor: '#0a0e27',
        borderWidth: 2
      },
      label: { 
        show: true,
        formatter: isFullscreen ? '{b}\n{d}%' : '{b}',
        color: '#fff',
        fontSize: isFullscreen ? 14 : 11
      },
      emphasis: {
        label: {
          show: true,
          fontSize: isFullscreen ? 18 : 14,
          fontWeight: 'bold',
          color: '#fff'
        },
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.5)'
        }
      },
      data: props.data.map((item, index) => ({
        value: item.count || item.value,
        name: item.districtName || item.name,
        itemStyle: { color: colors[index % colors.length] }
      }))
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
.district-pie-chart {
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
