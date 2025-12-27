<template>
  <div class="traffic-flow-map">
    <div ref="chartDom" class="map-container"></div>
    
    <!-- 刷新状态 -->
    <div class="refresh-status">
      <el-icon :class="{'rotating': isLoading}"><Refresh /></el-icon>
      <span>{{ lastUpdate }}</span>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import { Refresh } from '@element-plus/icons-vue'
import xuzhouGeoJSON from '@/assets/maps/xuzhou.json'
import axios from 'axios'

const chartDom = ref(null)
let chartInstance = null
let refreshTimer = null

const isLoading = ref(false)
const lastUpdate = ref('--:--:--')

// 初始化地图
const initChart = () => {
  if (!chartDom.value) return
  
  chartInstance = echarts.init(chartDom.value, 'dark')
  echarts.registerMap('xuzhou', xuzhouGeoJSON)
  
  fetchAndUpdate()
}

// 获取数据并更新地图
const fetchAndUpdate = async () => {
  isLoading.value = true
  
  try {
    // 加载区域流量数据
    const trafficRes = await axios.get('http://localhost:8080/api/flow/district-traffic')
    const trafficData = trafficRes.data.data || { districts: [] }
    
    updateChart(trafficData)
    
    lastUpdate.value = new Date().toLocaleTimeString('zh-CN')
  } catch (error) {
    console.error('数据加载失败:', error)
  } finally {
    isLoading.value = false
  }
}

// 更新图表
const updateChart = (trafficData) => {
  if (!chartInstance) return
  
  // 准备区域数据
  const districtData = trafficData.districts.map(d => ({
    name: d.name,
    value: d.volume
  }))
  
  const maxVolume = Math.max(...districtData.map(d => d.value), 10000)
  
  const option = {
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'item',
      backgroundColor: 'rgba(0, 0, 0, 0.9)',
      borderColor: '#00d4ff',
      borderWidth: 2,
      textStyle: { color: '#fff', fontSize: 13 },
      formatter: (params) => {
        if (params.componentSubType === 'map') {
          const volume = params.value || 0
          const percent = maxVolume > 0 ? ((volume / maxVolume) * 100).toFixed(1) : 0
          return `
            <div style="padding: 5px;">
              <div style="color: #00d4ff; font-weight: bold; font-size: 14px;">${params.name}</div>
              <div style="margin-top: 5px;">流量: <span style="color: #ffa500; font-weight: bold;">${volume}</span> 辆次</div>
              <div>热度: <span style="color: #00ff88;">${percent}%</span></div>
            </div>
          `
        }
        return params.name
      }
    },
    visualMap: {
      min: 0,
      max: maxVolume,
      left: 'left',
      top: 'bottom',
      text: ['高', '低'],
      textStyle: { color: '#fff' },
      inRange: {
        color: ['#0088ff', '#00d4ff', '#ffff00', '#ff6600', '#ff0000']
      },
      calculable: true
    },
    geo: {
      map: 'xuzhou',
      roam: true,
      itemStyle: {
        areaColor: '#0a1e3e',
        borderColor: '#00d4ff',
        borderWidth: 1.5
      },
      emphasis: {
        itemStyle: {
          areaColor: '#1a3e5e'
        },
        label: {
          show: true,
          color: '#fff'
        }
      }
    },
    series: [
      // 地图热力图
      {
        name: '区域流量',
        type: 'map',
        map: 'xuzhou',
        geoIndex: 0,
        data: districtData
      }
    ]
  }
  
  chartInstance.setOption(option, true)
}

// 生命周期
onMounted(() => {
  initChart()
  
  // 5秒自动刷新
  refreshTimer = setInterval(() => {
    fetchAndUpdate()
  }, 5000)
  
  // 响应式
  window.addEventListener('resize', () => {
    chartInstance?.resize()
  })
})

onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }
  if (chartInstance) {
    chartInstance.dispose()
  }
})
</script>

<style scoped>
.traffic-flow-map {
  position: relative;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #0a0e27 0%, #1a1e3e 100%);
  border-radius: 8px;
  overflow: hidden;
}

.map-container {
  width: 100%;
  height: 100%;
}

.refresh-status {
  position: absolute;
  top: 20px;
  right: 20px;
  background: rgba(0, 0, 0, 0.7);
  backdrop-filter: blur(10px);
  padding: 10px 15px;
  border-radius: 8px;
  border: 1px solid #00d4ff;
  color: #00d4ff;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  z-index: 1000;
}

.rotating {
  animation: rotate 1s linear infinite;
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
</style>
