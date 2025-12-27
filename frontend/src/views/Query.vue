<template>
  <div class="query-container">
    <!-- 粒子动画背景 -->
    <canvas id="particleCanvas" class="particle-bg"></canvas>
    
    <!-- 顶部导航栏 -->
    <header class="query-header glass-card">
      <el-button type="primary" class="back-btn" @click="goBack" circle>
        <el-icon><ArrowLeft /></el-icon>
      </el-button>
      <h1 class="page-title gradient-text">交互式数据查询</h1>
      <div class="header-placeholder"></div>
    </header>

    <!-- 主内容区 -->
    <main class="main-content">
        <!-- 查询条件卡片 -->
        <div class="search-section glass-card">
          <el-form :model="queryForm" inline class="search-form">
            <el-form-item label="车牌号">
              <el-input 
                v-model="queryForm.plateNumber" 
                placeholder="请输入车牌号（支持模糊查询）"
                clearable
                prefix-icon="Search"
                style="width: 300px"
                @keyup.enter="handleSearch"
                class="glass-input"
              />
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" class="action-btn" @click="handleSearch" :loading="loading">
                <el-icon><Search /></el-icon>
                查询
              </el-button>
              <el-button class="action-btn glass" @click="handleReset">
                <el-icon><RefreshLeft /></el-icon>
                重置
              </el-button>
            </el-form-item>
          </el-form>
        </div>

        <!-- 快捷筛选卡片 -->
        <div class="quick-filter glass-card">
          <h3 class="filter-title">
            <el-icon><Filter /></el-icon>
            快捷筛选
          </h3>
          <div class="filter-tags">
            <div 
              v-for="tag in quickFilters" 
              :key="tag.value"
              class="filter-chip"
              :class="tag.type"
              @click="applyQuickFilter(tag.value)"
            >
              {{ tag.label }}
            </div>
          </div>
        </div>

        <!-- 数据表格卡片 -->
        <div class="table-section glass-card">
          <div class="table-header">
            <h3 class="table-title">
              <el-icon><List /></el-icon>
              查询结果
              <span class="result-count">（共 {{ total }} 条记录）</span>
            </h3>
            <div class="table-actions">
              <el-button type="success" size="small" class="action-btn success" @click="exportData">
                <el-icon><Download /></el-icon>
                导出数据
              </el-button>
            </div>
          </div>

          <el-table 
            :data="tableData" 
            v-loading="loading"
            stripe
            border
            height="500"
            class="glass-table"
          >
            <el-table-column type="index" label="序号" width="60" align="center" />
            <el-table-column prop="plateNumber" label="车牌号" width="140" align="center">
              <template #default="{ row }">
                <div class="plate-tag">{{ row.plateNumber }}</div>
              </template>
            </el-table-column>
            <el-table-column prop="bayonetName" label="卡口名称" min-width="180" />
            <el-table-column prop="districtName" label="行政区" width="120" align="center" />
            <el-table-column prop="directionType" label="方向" width="100" align="center" />
            <el-table-column prop="plateType" label="号牌种类" width="120" align="center" />
            <el-table-column prop="vehicleModel" label="车辆型号" min-width="150" />
            <el-table-column prop="passTime" label="通行时间" width="180" align="center" />
          </el-table>

          <!-- 分页组件 -->
          <div class="pagination-wrapper">
            <el-pagination
              v-model:current-page="queryForm.page"
              v-model:page-size="queryForm.size"
              :page-sizes="[10, 20, 50, 100]"
              :total="total"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleSizeChange"
              @current-change="handlePageChange"
              background
              popper-class="glass-pagination-popper"
            />
          </div>
        </div>
    </main>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  ArrowLeft, Search, RefreshLeft, List, Download, Filter
} from '@element-plus/icons-vue'
import { getEtcList } from '@/api/etc'

const router = useRouter()

// 查询表单
const queryForm = reactive({
  plateNumber: '',
  page: 1,
  size: 10
})

// 数据状态
const loading = ref(false)
const tableData = ref([])
const total = ref(0)

// 快捷筛选选项
const quickFilters = [
  { label: '最近10条', value: '10', type: 'normal' },
  { label: '最近50条', value: '50', type: 'success' },
  { label: '最近100条', value: '100', type: 'warning' },
  { label: '全部数据', value: 'all', type: 'danger' }
]

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

// 返回首页
const goBack = () => {
  router.push('/')
}

// 查询数据
const fetchData = async () => {
  loading.value = true
  try {
    const params = {
      page: queryForm.page,
      size: queryForm.size
    }
    
    if (queryForm.plateNumber) {
      params.plateNumber = queryForm.plateNumber
    }
    
    const res = await getEtcList(params)
    
    if (res.code === 200 && res.data) {
      tableData.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (error) {
    console.error('查询失败：', error)
    ElMessage.error('查询失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 搜索按钮
const handleSearch = () => {
  queryForm.page = 1
  fetchData()
}

// 重置按钮
const handleReset = () => {
  queryForm.plateNumber = ''
  queryForm.page = 1
  queryForm.size = 10
  fetchData()
}

// 分页大小变化
const handleSizeChange = (size) => {
  queryForm.size = size
  queryForm.page = 1
  fetchData()
}

// 页码变化
const handlePageChange = (page) => {
  queryForm.page = page
  fetchData()
}

// 快捷筛选
const applyQuickFilter = (value) => {
  if (value === 'all') {
    queryForm.size = 100
    queryForm.page = 1
  } else {
    queryForm.size = parseInt(value)
    queryForm.page = 1
  }
  handleReset()
}

// 导出数据
const exportData = () => {
  if (tableData.value.length === 0) {
    ElMessage.warning('暂无数据可导出')
    return
  }
  
  // 简单的CSV导出实现
  const headers = ['车牌号', '卡口名称', '行政区', '方向', '号牌种类', '车辆型号', '通行时间']
  const csvContent = [
    headers.join(','),
    ...tableData.value.map(row => 
      [
        row.plateNumber,
        row.bayonetName,
        row.districtName,
        row.directionType,
        row.plateType,
        row.vehicleModel,
        row.passTime
      ].join(',')
    )
  ].join('\n')
  
  const blob = new Blob(['\ufeff' + csvContent], { type: 'text/csv;charset=utf-8;' })
  const link = document.createElement('a')
  const url = URL.createObjectURL(blob)
  link.setAttribute('href', url)
  link.setAttribute('download', `ETC查询结果_${new Date().getTime()}.csv`)
  link.style.visibility = 'hidden'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  
  ElMessage.success('数据导出成功')
}

// 初始化
onMounted(() => {
  fetchData()
  
  const canvas = document.getElementById('particleCanvas')
  if (canvas) {
    particleAnim = new ParticleAnimation(canvas)
    particleAnim.animate()
  }
})
</script>

<style scoped>
.query-container {
  min-height: 100vh;
  background-color: #0b0d17;
  padding: 20px;
  position: relative;
  overflow-x: hidden;
  color: #fff;
  font-family: 'Inter', sans-serif;
}

/* 粒子背景 */
.particle-bg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 0;
  pointer-events: none;
}

.glass-card {
  background: rgba(11, 13, 23, 0.7);
  backdrop-filter: blur(16px);
  border: 1px solid rgba(102, 126, 234, 0.4);
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
  position: relative;
  z-index: 1;
  transition: border-color 0.3s;
}

.glass-card:hover {
  border-color: rgba(0, 212, 255, 0.6);
  box-shadow: 0 0 20px rgba(102, 126, 234, 0.2);
}

.query-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 30px;
  margin-bottom: 24px;
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

.page-title {
  font-size: 28px;
  font-weight: 700;
  margin: 0;
}

.gradient-text {
  background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.header-placeholder {
  width: 40px;
}

/* 搜索区域 */
.search-section {
  padding: 30px;
  margin-bottom: 24px;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  align-items: center;
}

.search-form :deep(.el-form-item__label) {
  color: rgba(255,255,255,0.8);
  font-weight: 500;
}

.glass-input :deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  box-shadow: none;
  border-radius: 8px;
}

.glass-input :deep(.el-input__inner) {
  color: #fff;
}

.glass-input :deep(.el-input__wrapper:hover),
.glass-input :deep(.el-input__wrapper.is-focus) {
    border-color: #667eea;
}

.action-btn {
  padding: 18px 24px;
  border-radius: 8px;
  font-weight: 600;
}

.action-btn.primary {
  background: #2563eb;
  border: none;
}

.action-btn.glass {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  color: #fff;
}
.action-btn.glass:hover {
    background: rgba(255, 255, 255, 0.1);
}

.action-btn.success {
    background: #059669;
    border: none;
}

/* 快捷筛选 */
.quick-filter {
  padding: 24px;
  margin-bottom: 24px;
}

.filter-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  gap: 8px;
  color: rgba(255,255,255,0.8);
}

.filter-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.filter-chip {
    padding: 8px 16px;
    border-radius: 20px;
    background: rgba(255,255,255,0.05);
    border: 1px solid rgba(255,255,255,0.1);
    cursor: pointer;
    font-size: 14px;
    transition: all 0.2s;
}

.filter-chip:hover {
    background: rgba(255,255,255,0.1);
    transform: translateY(-2px);
}

.filter-chip.success { border-color: rgba(5, 150, 105, 0.5); }
.filter-chip.warning { border-color: rgba(217, 119, 6, 0.5); }
.filter-chip.danger { border-color: rgba(220, 38, 38, 0.5); }

/* 表格区域 */
.table-section {
  padding: 24px;
  background: rgba(255, 255, 255, 0.02);
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.table-title {
  font-size: 20px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0;
  color: #fff;
}

.result-count {
  font-size: 14px;
  color: rgba(255,255,255,0.5);
  font-weight: 400;
}

/* 表格深度定制 */
.glass-table {
    --el-table-border-color: rgba(255, 255, 255, 0.05);
    --el-table-header-bg-color: rgba(255, 255, 255, 0.05);
    --el-table-bg-color: transparent;
    --el-table-tr-bg-color: transparent;
    --el-table-row-hover-bg-color: rgba(102, 126, 234, 0.1);
    background: transparent !important;
    color: #fff;
}

:deep(.el-table__inner-wrapper::before) {
    background-color: transparent;
}

:deep(.el-table th.el-table__cell) {
    background: rgba(255, 255, 255, 0.05) !important;
    color: #fff;
    border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

:deep(.el-table td.el-table__cell) {
    background: transparent !important;
    border-bottom: 1px solid rgba(255, 255, 255, 0.05);
    color: rgba(255,255,255,0.8);
}

.plate-tag {
    display: inline-block;
    padding: 2px 8px;
    background: rgba(37, 99, 235, 0.2);
    border: 1px solid rgba(37, 99, 235, 0.4);
    border-radius: 4px;
    color: #60a5fa;
    font-weight: 600;
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

:deep(.el-pagination.is-background .el-pager li:not(.is-disabled)) {
    background-color: rgba(255,255,255,0.05);
    color: #fff;
}

:deep(.el-pagination.is-background .el-pager li:not(.is-disabled).is-active) {
    background-color: #2563eb;
}

:deep(.el-pagination.is-background .btn-next), 
:deep(.el-pagination.is-background .btn-prev) {
    background-color: rgba(255,255,255,0.05);
    color: #fff;
}

/* 响应式 */
@media (max-width: 768px) {
  .query-header {
    flex-direction: column;
    gap: 16px;
  }
  
  .table-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .search-form {
    flex-direction: column;
    align-items: stretch;
  }
  
  .search-form :deep(.el-input) {
    width: 100% !important;
  }
}
</style>
