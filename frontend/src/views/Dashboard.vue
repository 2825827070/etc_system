<template>
  <div class="dashboard-container">
    <canvas id="particleCanvas" class="particle-bg"></canvas>
    <!-- 顶部标题栏 -->
    <header class="dashboard-header">
      <div class="header-left">
        <el-button class="back-btn" @click="goBack" circle>
          <el-icon><ArrowLeft /></el-icon>
        </el-button>
      </div>
      <h1 class="dashboard-title">大数据存储平台交通监控</h1>
      <div class="header-right">
        <div class="current-time">{{ currentTime }}</div>
      </div>
    </header>



    <!-- 主展示区域 -->
    <div class="dashboard-grid" ref="gridRef">
      <!-- 左侧面板 -->
      <div class="side-column left-column">
        <div 
          v-for="(panelId, index) in leftPanels" 
          :key="'left-' + index"
          class="panel-slot"
          :data-panel-id="panelId"
          :data-slot="'left-' + index"
          @click="handlePanelClick(panelId, 'left', index)"
        >
          <div class="panel-card" :class="{ 'clickable': true }">
            <div class="expand-hint">
              <el-icon><FullScreen /></el-icon>
            </div>
            <component 
              :is="getPanelComponent(panelId)" 
              mode="thumbnail"
              v-bind="getPanelProps(panelId)"
            />
          </div>
        </div>
      </div>

      <!-- 中心展示区 -->
      <div class="center-column">
        <div 
          class="panel-slot center-slot"
          :data-panel-id="centerPanel"
          data-slot="center"
        >
          <div class="panel-card center-card">
            <div class="center-title">
              <el-icon><Monitor /></el-icon>
              <span>{{ getCenterTitle() }}</span>
            </div>
            <div class="center-content">
              <component 
                :is="getPanelComponent(centerPanel)" 
                :key="centerPanel"
                mode="fullscreen"
                v-bind="getPanelProps(centerPanel)"
              />
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧面板 -->
      <div class="side-column right-column">
        <div 
          v-for="(panelId, index) in rightPanels" 
          :key="'right-' + index"
          class="panel-slot"
          :data-panel-id="panelId"
          :data-slot="'right-' + index"
          @click="handlePanelClick(panelId, 'right', index)"
        >
          <div class="panel-card" :class="{ 'clickable': true }">
            <div class="expand-hint">
              <el-icon><FullScreen /></el-icon>
            </div>
            <component 
              :is="getPanelComponent(panelId)" 
              mode="thumbnail"
              v-bind="getPanelProps(panelId)"
            />
          </div>
        </div>
      </div>
    </div>

    <!-- 底部刷新状态 -->
    <div class="refresh-bar">
      <el-icon :class="{ 'rotating': isRefreshing }"><Refresh /></el-icon>
      <span>{{ refreshCountdown }}秒后自动刷新数据</span>
    </div>

    <!-- 动画遮罩层 - 始终挂载但隐藏 -->
    <Teleport to="body">
      <div class="swap-overlay">
        <div class="flying-panel from-panel" ref="fromPanelRef"></div>
        <div class="flying-panel to-panel" ref="toPanelRef"></div>
      </div>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed, markRaw, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft, Refresh, FullScreen, Monitor } from '@element-plus/icons-vue'
import { useStorage } from '@vueuse/core'
import gsap from 'gsap'
import { getTotalFlow, getDistrictStats, getTrend, getFakeVehicleAlerts, getEtcList } from '@/api/etc'

// Import panel components
import TotalFlowCard from '@/components/TotalFlowCard.vue'
import DistrictPieChart from '@/components/DistrictPieChart.vue'
import CityBarChart from '@/components/CityBarChart.vue'
import FakeVehicleAlerts from '@/components/FakeVehicleAlerts.vue'
import TrendLineChart from '@/components/TrendLineChart.vue'
import LatestRecords from '@/components/LatestRecords.vue'
import TrafficFlowMap from '@/components/TrafficFlowMap.vue'

// 粒子动画逻辑
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
                const forceDirectionX = dx / distance
                const forceDirectionY = dy / distance
                const force = (this.mouse.radius - distance) / this.mouse.radius
                const directionX = forceDirectionX * force * 3
                const directionY = forceDirectionY * force * 3
                p.x -= directionX
                p.y -= directionY
            }
        }
        
        this.ctx.beginPath()
        this.ctx.arc(p.x, p.y, p.size, 0, Math.PI * 2)
        this.ctx.fillStyle = '#667eea'
        this.ctx.fill()

        this.connect(p, this.particles.slice(i + 1))
        
        if (this.mouse.x != null) {
             this.connectMouse(p)
        }
    }
  }

  connect(p1, others) {
      for (let p2 of others) {
          let distance = ((p1.x - p2.x) * (p1.x - p2.x)) + ((p1.y - p2.y) * (p1.y - p2.y))
          if (distance < (this.canvas.width/9) * (this.canvas.height/9)) {
              let opacityValue = 1 - (distance / 20000)
              if (opacityValue > 0) {
                  this.ctx.strokeStyle = `rgba(102, 126, 234, ${opacityValue * 0.15})`
                  this.ctx.lineWidth = 1
                  this.ctx.beginPath()
                  this.ctx.moveTo(p1.x, p1.y)
                  this.ctx.lineTo(p2.x, p2.y)
                  this.ctx.stroke()
              }
          }
      }
  }
  
  connectMouse(p) {
      let dx = this.mouse.x - p.x
      let dy = this.mouse.y - p.y
      let distance = dx*dx + dy*dy
      if (distance < 20000) {
          this.ctx.strokeStyle = `rgba(100, 255, 218, 0.2)`
          this.ctx.lineWidth = 1
          this.ctx.beginPath()
          this.ctx.moveTo(this.mouse.x, this.mouse.y)
          this.ctx.lineTo(p.x, p.y)
          this.ctx.stroke()
      }
  }

  animate() {
    this.draw()
    requestAnimationFrame(() => this.animate())
  }
}

const router = useRouter()
const gridRef = ref(null)
const fromPanelRef = ref(null)
const toPanelRef = ref(null)
const isRefreshing = ref(false)
const isAnimating = ref(false)

// Panel component mapping
const panelComponents = {
  totalFlow: markRaw(TotalFlowCard),
  district: markRaw(DistrictPieChart),
  city: markRaw(CityBarChart),
  alerts: markRaw(FakeVehicleAlerts),
  trend: markRaw(TrendLineChart),
  records: markRaw(LatestRecords),
  map: markRaw(TrafficFlowMap)
}

// Panel titles
const panelTitles = {
  totalFlow: '总车流量统计',
  district: '行政区车流占比',
  city: '各地市车流统计',
  alerts: '套牌车告警监控',
  trend: '实时车流趋势',
  records: '最新通行记录',
  map: '交通流量地图'
}

// Layout state with localStorage persistence
const layoutState = useStorage('dashboard-layout-v3', {
  leftPanels: ['map', 'alerts', 'city'],
  rightPanels: ['district', 'trend', 'records'],
  centerPanel: 'totalFlow'
})

const leftPanels = computed(() => layoutState.value.leftPanels)
const rightPanels = computed(() => layoutState.value.rightPanels)
const centerPanel = computed(() => layoutState.value.centerPanel)

// Data states
const currentTime = ref('')
const refreshCountdown = ref(10)
const totalFlow = ref('0')
const districtData = ref([])
const trendData = ref([])
const fakeVehicleAlerts = ref([])
const realtimeData = ref([])

let countdownTimer = null
let timeTimer = null

// Methods
const goBack = () => router.push('/')

const getCenterTitle = () => panelTitles[centerPanel.value] || '数据展示'

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

const getPanelComponent = (panelId) => {
  return panelComponents[panelId]
}

const getPanelProps = (panelId) => {
  const propsMap = {
    totalFlow: { totalFlow: totalFlow.value },
    district: { data: districtData.value },
    city: { data: districtData.value },
    alerts: { alerts: fakeVehicleAlerts.value },
    trend: { data: trendData.value, showCategorySelector: true },
    records: { records: realtimeData.value },
    map: {}
  }
  return propsMap[panelId] || {}
}

// Smooth swap animation with asymmetric content transition
// - Expanding (small→large): swap content immediately, then animate size
// - Shrinking (large→small): keep old content during animation, swap at the end
const handlePanelClick = async (clickedPanelId, side, index) => {
  if (isAnimating.value) return
  
  // Get the elements BEFORE swap
  const clickedSlot = gridRef.value?.querySelector(`[data-slot="${side}-${index}"]`)
  const centerSlot = gridRef.value?.querySelector('[data-slot="center"]')
  
  if (!clickedSlot || !centerSlot) {
    performSwap(clickedPanelId, side, index)
    return
  }
  
  const oldClickedCard = clickedSlot.querySelector('.panel-card')
  const oldCenterCard = centerSlot.querySelector('.panel-card')
  
  if (!oldClickedCard || !oldCenterCard || !fromPanelRef.value || !toPanelRef.value) {
    performSwap(clickedPanelId, side, index)
    return
  }
  
  // Store OLD positions and OLD content
  const oldClickedRect = oldClickedCard.getBoundingClientRect()
  const oldCenterRect = oldCenterCard.getBoundingClientRect()
  
  // Clone OLD center content (for shrinking animation - will keep this during animation)
  const oldCenterClone = oldCenterCard.cloneNode(true)
  
  // Helper function to copy canvas content
  const copyCanvasContent = (source, clone) => {
    const sourceCanvases = source.querySelectorAll('canvas')
    const cloneCanvases = clone.querySelectorAll('canvas')
    sourceCanvases.forEach((sourceCanvas, i) => {
      const cloneCanvas = cloneCanvases[i]
      if (cloneCanvas) {
        const ctx = cloneCanvas.getContext('2d')
        if (ctx) ctx.drawImage(sourceCanvas, 0, 0)
      }
    })
  }
  
  copyCanvasContent(oldCenterCard, oldCenterClone)
  
  isAnimating.value = true
  
  // FIRST: Perform the data swap so Vue renders new content
  performSwap(clickedPanelId, side, index)
  
  // Wait for Vue to update
  await nextTick()
  await new Promise(resolve => setTimeout(resolve, 50))
  
  // Get NEW elements (now in swapped positions)
  const newCenterCard = centerSlot.querySelector('.panel-card')
  const newClickedCard = clickedSlot.querySelector('.panel-card')
  
  if (!newCenterCard || !newClickedCard) {
    isAnimating.value = false
    return
  }
  
  // Get NEW positions
  const newCenterRect = newCenterCard.getBoundingClientRect()
  const newClickedRect = newClickedCard.getBoundingClientRect()
  
  // Clone NEW center content (for expanding animation - already has fullscreen mode)
  const newCenterClone = newCenterCard.cloneNode(true)
  copyCanvasContent(newCenterCard, newCenterClone)
  
  // Setup flying panels
  fromPanelRef.value.innerHTML = ''
  toPanelRef.value.innerHTML = ''
  
  // fromPanel: small→large (expanding) - use NEW content that's already in fullscreen mode
  fromPanelRef.value.appendChild(newCenterClone)
  
  // toPanel: large→small (shrinking) - use OLD content to maintain continuity
  toPanelRef.value.appendChild(oldCenterClone)
  
  // Hide actual cards during animation
  newCenterCard.style.visibility = 'hidden'
  newClickedCard.style.visibility = 'hidden'
  
  // Style expanding clone (starts small with new full content, scales up)
  newCenterClone.style.cssText = `
    width: ${newCenterRect.width}px;
    height: ${newCenterRect.height}px;
    position: absolute;
    top: 0; left: 0;
    border: none;
    border-radius: 0;
    transform-origin: top left;
    transform: scale(${oldClickedRect.width / newCenterRect.width}, ${oldClickedRect.height / newCenterRect.height});
  `
  
  // Style shrinking clone (starts large with old full content, stays full size in container)
  oldCenterClone.style.cssText = `
    width: ${oldCenterRect.width}px;
    height: ${oldCenterRect.height}px;
    position: absolute;
    top: 0; left: 0;
    border: none;
    border-radius: 0;
    transform-origin: top left;
    transform: scale(1, 1);
  `
  
  // Set flying panels initial positions
  gsap.set(fromPanelRef.value, {
    position: 'fixed',
    left: oldClickedRect.left,
    top: oldClickedRect.top,
    width: oldClickedRect.width,
    height: oldClickedRect.height,
    zIndex: 1001,
    opacity: 1,
    borderRadius: 10,
    background: 'rgba(10, 14, 39, 0.98)',
    border: '2px solid #00d4ff',
    boxShadow: '0 0 30px rgba(0, 212, 255, 0.5)',
    overflow: 'hidden'
  })
  
  gsap.set(toPanelRef.value, {
    position: 'fixed',
    left: oldCenterRect.left,
    top: oldCenterRect.top,
    width: oldCenterRect.width,
    height: oldCenterRect.height,
    zIndex: 1000,
    opacity: 1,
    borderRadius: 12,
    background: 'rgba(10, 14, 39, 0.98)',
    border: '2px solid rgba(102, 126, 234, 0.6)',
    boxShadow: '0 0 30px rgba(102, 126, 234, 0.4)',
    overflow: 'hidden'
  })
  
  // Create animation timeline
  const tl = gsap.timeline({
    onComplete: () => {
      gsap.set([fromPanelRef.value, toPanelRef.value], { opacity: 0 })
      newCenterCard.style.visibility = 'visible'
      newClickedCard.style.visibility = 'visible'
      isAnimating.value = false
    }
  })
  
  // === EXPANDING ANIMATION (small → large) ===
  // Container moves and grows
  tl.to(fromPanelRef.value, {
    left: newCenterRect.left,
    top: newCenterRect.top,
    width: newCenterRect.width,
    height: newCenterRect.height,
    borderRadius: 12,
    duration: 0.6,
    ease: 'power3.inOut'
  }, 0)
  
  // Content scales up to fill container
  tl.to(newCenterClone, {
    scaleX: 1,
    scaleY: 1,
    duration: 0.6,
    ease: 'power3.inOut'
  }, 0)
  
  // === SHRINKING ANIMATION (large → small) ===
  // Container moves and shrinks
  tl.to(toPanelRef.value, {
    left: newClickedRect.left,
    top: newClickedRect.top,
    width: newClickedRect.width,
    height: newClickedRect.height,
    borderRadius: 10,
    duration: 0.6,
    ease: 'power3.inOut'
  }, 0)
  
  // Content stays the same size (clips as container shrinks) - keeps old content visible
  // Scale down only at the very end
  tl.to(oldCenterClone, {
    scaleX: newClickedRect.width / oldCenterRect.width,
    scaleY: newClickedRect.height / oldCenterRect.height,
    duration: 0.6,
    ease: 'power3.inOut'
  }, 0)
  
  // Fade out at the end
  tl.to([fromPanelRef.value, toPanelRef.value], {
    opacity: 0,
    duration: 0.1,
    ease: 'power1.in'
  }, 0.55)
}

const performSwap = (clickedPanelId, side, index) => {
  const currentCenter = centerPanel.value
  
  if (side === 'left') {
    layoutState.value.leftPanels[index] = currentCenter
  } else {
    layoutState.value.rightPanels[index] = currentCenter
  }
  
  layoutState.value.centerPanel = clickedPanelId
}

// Data fetching
const fetchTotalFlow = async () => {
  try {
    const res = await getTotalFlow()
    if (res.code === 200) {
      totalFlow.value = res.data.toLocaleString()
    }
  } catch (error) {
    console.error('获取总车流量失败：', error)
  }
}

const fetchDistrictStats = async () => {
  try {
    const res = await getDistrictStats()
    if (res.code === 200 && res.data) {
      districtData.value = res.data
    }
  } catch (error) {
    console.error('获取行政区统计失败：', error)
  }
}

const fetchFakeVehicleAlerts = async () => {
  try {
    const res = await getFakeVehicleAlerts(24)
    if (res.code === 200 && res.data) {
      fakeVehicleAlerts.value = res.data
    }
  } catch (error) {
    console.error('获取套牌车告警失败：', error)
  }
}

const fetchTrend = async () => {
  try {
    const res = await getTrend()
    if (res.code === 200 && res.data) {
      trendData.value = res.data
    }
  } catch (error) {
    console.error('获取实时趋势失败：', error)
  }
}

// 获取最新通行记录（使用分页API获取最新的10条记录）
const fetchLatestRecords = async () => {
  try {
    const res = await getEtcList({ page: 1, size: 10 })
    if (res.code === 200 && res.data && res.data.records) {
      realtimeData.value = res.data.records
    }
  } catch (error) {
    console.error('获取最新通行记录失败：', error)
  }
}

const refreshAllData = async () => {
  isRefreshing.value = true
  await Promise.all([
    fetchTotalFlow(),
    fetchDistrictStats(),
    fetchTrend(),
    fetchFakeVehicleAlerts(),
    fetchLatestRecords()
  ])
  isRefreshing.value = false
  refreshCountdown.value = 10
}

const startCountdown = () => {
  countdownTimer = setInterval(() => {
    refreshCountdown.value--
    if (refreshCountdown.value <= 0) {
      refreshAllData()
    }
  }, 1000)
}

// Lifecycle
onMounted(async () => {
  updateTime()
  timeTimer = setInterval(updateTime, 1000)
  
  await refreshAllData()
  startCountdown()
  
  // 初始化粒子
  const canvas = document.getElementById('particleCanvas')
  if (canvas) {
    particleAnim = new ParticleAnimation(canvas)
    particleAnim.animate()
  }
})

onUnmounted(() => {
  if (countdownTimer) clearInterval(countdownTimer)
  if (timeTimer) clearInterval(timeTimer)
})
</script>

<style scoped>
.dashboard-container {
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

.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 50px;
  flex-shrink: 0;
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

.dashboard-title {
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

/* Fixed Grid Layout - Using explicit pixel heights */
.dashboard-grid {
  flex: 1;
  display: grid;
  grid-template-columns: 360px 1fr 360px;
  gap: 12px;
  margin-top: 12px;
  height: calc(100vh - 120px);
  min-height: 500px;
}

.side-column {
  display: flex;
  flex-direction: column;
  gap: 10px;
  height: 100%;
}

/* Fixed height distribution - exactly 1/3 each */
.panel-slot {
  flex: 1 1 0;
  min-height: 0;
  height: calc((100% - 20px) / 3);
}

.center-column {
  height: 100%;
}

.center-slot {
  height: 100% !important;
}

.panel-card {
  height: 100%;
  width: 100%;
  background: rgba(11, 13, 23, 0.85); /* 半透明深色背景，让粒子透出 */
  backdrop-filter: blur(8px);
  border: 1px solid rgba(102, 126, 234, 0.4);
  border-radius: 10px;
  overflow: hidden;
  transition: border-color 0.3s, box-shadow 0.3s;
  position: relative;
  box-sizing: border-box;
}

.panel-card.clickable {
  cursor: pointer;
}

.panel-card.clickable:hover {
  border-color: #00d4ff;
  box-shadow: 0 0 20px rgba(0, 212, 255, 0.3);
}

.panel-card.clickable:hover .expand-hint {
  opacity: 1;
}

.expand-hint {
  position: absolute;
  top: 8px;
  right: 8px;
  background: rgba(0, 212, 255, 0.85);
  color: #0a0e27;
  width: 28px;
  height: 28px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  opacity: 0;
  transition: opacity 0.2s ease;
  z-index: 10;
}

/* Center Card */
.center-card {
  border: 2px solid rgba(0, 212, 255, 0.5);
  border-radius: 12px;
  display: flex;
  flex-direction: column;
}

.center-title {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 18px;
  background: linear-gradient(90deg, rgba(0, 212, 255, 0.15), transparent);
  border-bottom: 1px solid rgba(0, 212, 255, 0.3);
  color: #00d4ff;
  font-size: 16px;
  font-weight: 600;
  flex-shrink: 0;
}

.center-content {
  flex: 1;
  min-height: 0;
  overflow: hidden;
  position: relative;
}

/* Child component needs to fill this container */
.center-content > * {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
}

/* Refresh Bar */
.refresh-bar {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 8px;
  font-size: 13px;
  color: #667eea;
  flex-shrink: 0;
  height: 36px;
}

.rotating {
  animation: rotate 1s linear infinite;
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* Animation Overlay */
.swap-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 9999;
  pointer-events: none;
}

.flying-panel {
  position: fixed;
  opacity: 0;
  pointer-events: none;
}

/* Responsive */
@media (max-width: 1500px) {
  .dashboard-grid {
    grid-template-columns: 320px 1fr 320px;
  }
}

@media (max-width: 1300px) {
  .dashboard-grid {
    grid-template-columns: 280px 1fr 280px;
    gap: 10px;
  }
  
  .dashboard-title {
    font-size: 22px;
  }
}
</style>
