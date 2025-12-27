<template>
  <div class="home-container">
    <!-- 粒子动画背景 -->
    <canvas id="particleCanvas" class="particle-bg"></canvas>
    
    <!-- 顶部导航栏 -->
    <nav class="top-nav" :class="{ 'scrolled': isScrolled }">
      <div class="nav-left">
        <el-icon :size="28" color="#667eea"><Monitor /></el-icon>
        <span class="project-name">ETC智能监控平台</span>
      </div>
      <div class="nav-center">
        <a href="#" @click.prevent="scrollToSection('features')">功能特性</a>
        <a href="#" @click.prevent="scrollToSection('tech')">技术栈</a>
        <a href="#" @click.prevent="scrollToSection('architecture')">系统架构</a>
        <a href="#" @click.prevent="navigateTo('/dashboard')">数据大屏</a>
      </div>
      <div class="nav-right">
        <a href="https://github.com/zongzuoscc/etc_system" target="_blank" class="github-link">
          <el-icon :size="20"><Link /></el-icon>
          <span>GitHub</span>
        </a>
      </div>
    </nav>

    <!-- Hero Section -->
    <section class="hero-section">
      <div class="hero-content" data-scroll>
        <h1 class="hero-title">
          <span class="gradient-text">高速公路ETC</span>
          <br>
          智能监控与决策平台
        </h1>
        <p class="hero-subtitle">NEXT-GENERATION TRAFFIC INTELLIGENCE</p>
        <p class="hero-description">
          基于 Kafka 实时流处理与机器学习的大数据分析系统，为您提供秒级交通态势感知。
        </p>
        
        <div class="hero-actions">
          <button class="action-btn primary" @click="navigateTo('/dashboard')">
            <el-icon><TrendCharts /></el-icon>
            <span>数据大屏</span>
          </button>
          <button class="action-btn glass" @click="navigateTo('/query')">
            <el-icon><Search /></el-icon>
            <span>交互式查询</span>
          </button>
          <button class="action-btn glass" @click="navigateTo('/analysis')">
            <el-icon><DataAnalysis /></el-icon>
            <span>预测分析</span>
          </button>
        </div>
      </div>
      
      <div class="scroll-indicator">
        <div class="mouse">
          <div class="wheel"></div>
        </div>
        <p>SCROLL TO EXPLORE</p>
      </div>
    </section>

    <!-- 团队介绍 Section -->
    <section class="team-section">
      <div class="section-header" data-scroll>
        <h2>核心研发团队</h2>
        <p>Core Development Team</p>
      </div>
      <div class="team-grid">
        <div class="team-card" v-for="(member, i) in teamMembers" :key="i" data-scroll :style="{ transitionDelay: `${i*100}ms` }">
          <div class="member-avatar">
            <div class="avatar-circle">
               <span class="avatar-text">{{ member.name.charAt(0) }}</span>
            </div>
          </div>
          <div class="member-info">
            <h3>{{ member.name }}</h3>
            <span class="role-badge">{{ member.role }}</span>
          </div>
        </div>
      </div>
    </section>

    <!-- Google Antigravity Style Features Section -->
    <section id="features" class="features-scroll-section">
      <div class="container">
        <div class="grid-row">
          <!-- 左侧描述列表 -->
          <div class="grid-col text-col">
            <div class="features-copy-wrapper">
              <ul class="feature-list">
                <li 
                  v-for="(feature, index) in features" 
                  :key="index"
                  class="feature-item" 
                  :class="{ 'active': activeFeatureIndex === index }"
                  @mouseenter="manualSetActive(index)"
                  :ref="el => { if(el) featureRefs[index] = el }"
                >
                  <span class="feature-title">{{ feature.title }}</span>
                  <div class="description-wrapper" :style="{ height: activeFeatureIndex === index ? 'auto' : '0px' }">
                    <p class="feature-description">{{ feature.description }}</p>
                    <button class="explore-link" @click="navigateTo(feature.path)">
                      立即体验 <el-icon><ArrowRight /></el-icon>
                    </button>
                  </div>
                </li>
              </ul>
            </div>
          </div>
          
          <!-- 右侧固定视觉展示区域 -->
          <div class="grid-col visual-col">
            <div class="pin-spacer">
              <div class="visual-content-wrapper">
                <transition-group name="fade-scale">
                  <div 
                    v-for="(feature, index) in features" 
                    :key="feature.title"
                    class="feature-visual"
                    v-show="activeFeatureIndex === index"
                  >
                    <!-- 模拟UI展示 -->
                    <div class="mockup-window" :class="feature.mockupClass">
                      <div class="mockup-header">
                        <span class="dot red"></span>
                        <span class="dot yellow"></span>
                        <span class="dot green"></span>
                        <div class="address-bar">etc-system://{{ feature.path }}</div>
                      </div>
                      <div class="mockup-body">
                        <!-- 图片模式 -->
                        <div v-if="feature.image" class="mockup-image-container">
                             <img :src="feature.image" class="feature-demo-img" alt="Feature Demo" />
                        </div>
                        
                        <!-- 代码模式 (Fallback) -->
                        <div v-else class="mockup-placeholder">
                          <el-icon :size="64" class="mockup-icon"><component :is="feature.icon" /></el-icon>
                          <h3>{{ feature.title }}</h3>
                          <div class="mockup-lines">
                            <div class="line w-75"></div>
                            <div class="line w-50"></div>
                            <div class="line w-90"></div>
                          </div>
                          <!-- 动态图表模拟 -->
                          <div class="mock-chart" v-if="index === 0">
                            <div class="bar" style="height: 40%"></div>
                            <div class="bar" style="height: 70%"></div>
                            <div class="bar" style="height: 50%"></div>
                            <div class="bar" style="height: 85%"></div>
                            <div class="bar" style="height: 60%"></div>
                          </div>
                          <div class="mock-search" v-if="index === 1">
                            <div class="search-input">输入车牌号...</div>
                            <div class="search-btn">查询</div>
                          </div>
                          <div class="mock-graph" v-if="index === 2">
                            <div class="graph-line"></div>
                            <div class="graph-dot"></div>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </transition-group>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 技术栈 Section -->
    <section id="tech" class="tech-section">
      <div class="section-header" data-scroll>
        <h2>全栈技术架构</h2>
        <p>Enterprise Grade Tech Stack</p>
      </div>
      <div class="tech-grid">
        <div class="tech-card" v-for="(stack, i) in techStacks" :key="i" data-scroll :style="{ transitionDelay: `${i*100}ms` }">
          <div class="tech-icon-wrapper" :style="{ background: stack.bg }">
            <el-icon :size="24" color="#fff"><component :is="stack.icon" /></el-icon>
          </div>
          <h3>{{ stack.category }}</h3>
          <div class="tech-tags">
            <span v-for="tag in stack.items" :key="tag">{{ tag }}</span>
          </div>
        </div>
      </div>
    </section>

    <!-- 架构图可视化 -->
    <section id="architecture" class="arch-section">
      <div class="section-header" data-scroll>
        <h2>系统数据流向</h2>
        <p>Data Pipeline Architecture</p>
      </div>
      <div class="arch-container" data-scroll>
        <div class="arch-flow">
          <div class="arch-node source">
            <div class="node-icon"><el-icon><Van /></el-icon></div>
            <span>ETC数据源</span>
          </div>
          <div class="arch-line"></div>
          <div class="arch-node kafka">
            <div class="node-icon">K</div>
            <span>Kafka集群</span>
          </div>
          <div class="arch-line"></div>
          <div class="arch-node processing">
            <div class="node-icon"><el-icon><Cpu /></el-icon></div>
            <span>实时计算</span>
          </div>
          <div class="arch-line"></div>
          <div class="arch-node storage">
            <div class="node-icon"><el-icon><Coin /></el-icon></div>
            <span>MySQL/Redis</span>
          </div>
          <div class="arch-line"></div>
          <div class="arch-node frontend">
            <div class="node-icon"><el-icon><Monitor /></el-icon></div>
            <span>Vue大屏</span>
          </div>
        </div>
      </div>
    </section>

    <!-- 页脚 -->
    <footer class="footer">
      <div class="footer-content">
        <div class="footer-left">
          <h3>ETC智能监控平台</h3>
          <p>基于大数据的下一代交通监控系统</p>
        </div>
        <div class="footer-right">
          <p>&copy; 2025 CUMT</p>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { 
  Monitor, Link, TrendCharts, Search, DataAnalysis, ArrowRight,
  Van, Cpu, Coin, Platform, Share, Histogram, UserFilled
} from '@element-plus/icons-vue'
import { useIntersectionObserver } from '@vueuse/core'

const router = useRouter()
const isScrolled = ref(false)
const activeFeatureIndex = ref(0)
const featureRefs = ref([])

const teamMembers = [
  { name: '杨一鸣', role: '后端开发与数据库设计' },
  { name: '赵博涵', role: '前端开发与测试' },
  { name: '李端宸', role: '数据生成模拟与分析' }
]

// 功能列表数据
const features = [
  {
    title: '实时数据大屏',
    description: '通过动态可视化图表，实时展示交通流量、车牌分布及关键指标。支持多维度下钻分析，让数据一目了然。',
    path: '/dashboard',
    icon: 'TrendCharts',
    mockupClass: 'theme-blue',
    image: '/dashboard_demo.png' // 图片占位符 (请在public目录下放置dashboard_demo.png)
  },
  {
    title: '交互式数据查询',
    description: '提供强大的多条件组合查询引擎，毫秒级响应海量通行记录检索。支持按车牌、时间段、站点精准定位。',
    path: '/query',
    icon: 'Search',
    mockupClass: 'theme-purple',
    image: '/query_demo.png' // 图片占位符
  },
  {
    title: '智能预测分析',
    description: '集成机器学习模型，基于历史数据对未来车流量进行精准预测。为交通疏导和资源调度提供科学依据。',
    path: '/analysis',
    icon: 'DataAnalysis',
    mockupClass: 'theme-cyan'
  }
]

// 技术栈数据
const techStacks = [
  {
    category: '前端交互',
    items: ['Vue 3', 'Element Plus', 'ECharts 5', 'GSAP'],
    icon: 'Monitor',
    bg: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
  },
  {
    category: '后端服务',
    items: ['Spring Boot', 'MyBatis Plus', 'RESTful API'],
    icon: 'Platform',
    bg: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)'
  },
  {
    category: '数据处理',
    items: ['Kafka', 'Python Scripts', 'Scikit-learn'],
    icon: 'Cpu',
    bg: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)'
  },
  {
    category: '存储设施',
    items: ['MySQL Cluster', 'MyCat', 'Redis'],
    icon: 'Coin',
    bg: 'linear-gradient(135deg, #fa709a 0%, #fee140 100%)'
  }
]

const navigateTo = (path) => {
  router.push(path)
}

const scrollToSection = (id) => {
  const element = document.getElementById(id)
  if (element) {
    element.scrollIntoView({ behavior: 'smooth' })
  }
}

// 模拟Google Antigravity的Active Item Logic
const manualSetActive = (index) => {
  // 鼠标悬停时也可以切换，提升交互感
  // activeFeatureIndex.value = index
}

// 简单的滚动监听
const handleScroll = () => {
  isScrolled.value = window.scrollY > 50
  
  // 核心功能区的滚动监听逻辑
  // 这种简单的计算方式比IntersectionObserver在Sticky布局中更直观
  const featureList = featureRefs.value
  if (!featureList || featureList.length === 0) return

  // 找到当前视口中间位置对应的元素
  const viewportCenter = window.innerHeight / 2
  
  let closestIndex = 0
  let minDistance = Infinity

  featureRefs.value.forEach((el, index) => {
    if (!el) return
    const rect = el.getBoundingClientRect()
    // 计算元素中心到视口中心的距离
    const elementCenter = rect.top + rect.height / 2
    const distance = Math.abs(elementCenter - viewportCenter)
    
    if (distance < minDistance) {
      minDistance = distance
      closestIndex = index
    }
  })
  
  // 只有当该section在视口内时才更新
  const section = document.getElementById('features')
  if (section) {
    const rect = section.getBoundingClientRect()
    // 如果section在视口范围内
    if (rect.top < window.innerHeight && rect.bottom > 0) {
      activeFeatureIndex.value = closestIndex
    }
  }
}

// 粒子动画与滚动初始化
let particleAnim = null

class ParticleAnimation {
  constructor(canvas) {
    this.canvas = canvas
    this.ctx = canvas.getContext('2d')
    this.particles = []
    this.mouse = { x: null, y: null, radius: 100 } // 减小交互半径
    
    this.resize()
    this.init()
    
    window.addEventListener('resize', () => this.resize())
    window.addEventListener('mousemove', (e) => {
      this.mouse.x = e.clientX // 使用 clientX 配合 fixed 定位
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
    // 性能优化：大幅减少粒子数量，全屏不卡顿
    // 限制最大80个粒子，保证4K屏也不卡
    const particleCount = Math.min((window.innerWidth * window.innerHeight) / 15000, 80)
    
    for (let i = 0; i < particleCount; i++) {
        let size = (Math.random() * 2) + 0.5
        let x = Math.random() * this.canvas.width
        let y = Math.random() * this.canvas.height
        let directionX = (Math.random() * 0.5) - 0.25 // 速度适中
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
        
        // 移动
        p.x += p.directionX
        p.y += p.directionY
        
        // 边界反弹
        if (p.x > this.canvas.width || p.x < 0) p.directionX = -p.directionX
        if (p.y > this.canvas.height || p.y < 0) p.directionY = -p.directionY
        
        // 鼠标排斥逻辑 (柔和推开)
        if (this.mouse.x != null) {
            let dx = this.mouse.x - p.x
            let dy = this.mouse.y - p.y
            let distance = Math.sqrt(dx*dx + dy*dy)
            
            if (distance < this.mouse.radius) {
                // 线性排斥力
                const forceDirectionX = dx / distance
                const forceDirectionY = dy / distance
                const force = (this.mouse.radius - distance) / this.mouse.radius
                const directionX = forceDirectionX * force * 3 // 3是力度
                const directionY = forceDirectionY * force * 3
                
                p.x -= directionX
                p.y -= directionY
            }
        }
        
        // 绘制粒子
        this.ctx.beginPath()
        this.ctx.arc(p.x, p.y, p.size, 0, Math.PI * 2)
        this.ctx.fillStyle = '#667eea'
        this.ctx.fill()

        // 粒子间连线
        this.connect(p, this.particles.slice(i + 1))
        
        // 鼠标与粒子连线 (增加互动灵动感)
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
      if (distance < 20000) { // 鼠标连线距离
          this.ctx.strokeStyle = `rgba(100, 255, 218, 0.2)` // 稍微高亮的颜色
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

onMounted(() => {
  const canvas = document.getElementById('particleCanvas')
  if (canvas) {
    particleAnim = new ParticleAnimation(canvas)
    particleAnim.animate()
  }
  
  window.addEventListener('scroll', handleScroll)
  
  // 简单的淡入动画Observer
  const observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        entry.target.classList.add('visible')
      }
    })
  }, { threshold: 0.1 })

  document.querySelectorAll('[data-scroll]').forEach(el => observer.observe(el))
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<style scoped>
.home-container {
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  background-color: #0b0d17;
  color: #fff;
  min-height: 100vh;
  position: relative;
  /* overflow-x: hidden; 不需要，body处理 */
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

/* 导航栏 */
.top-nav {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 48px;
  z-index: 100;
  transition: all 0.3s ease;
  background: transparent;
}

.top-nav.scrolled {
  background: rgba(11, 13, 23, 0.9);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
  height: 64px;
}

.nav-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.project-name {
  font-size: 18px;
  font-weight: 600;
  letter-spacing: -0.5px;
}

.nav-center {
  display: flex;
  gap: 32px;
}

.nav-center a {
  text-decoration: none;
  color: rgba(255, 255, 255, 0.7);
  font-size: 14px;
  font-weight: 500;
  transition: color 0.2s;
}

.nav-center a:hover {
  color: #fff;
}

.nav-right .github-link {
  display: flex;
  align-items: center;
  gap: 8px;
  text-decoration: none;
  color: white;
  background: rgba(255, 255, 255, 0.1);
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 14px;
  transition: background 0.2s;
}

.nav-right .github-link:hover {
  background: rgba(255, 255, 255, 0.2);
}

/* Hero 区域 */
.hero-section {
  height: 100vh;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  text-align: center;
  padding: 0 20px;
  position: relative;
  z-index: 1;
}

.hero-content {
  max-width: 800px;
  opacity: 0;
  transform: translateY(30px);
  animation: fadeInUp 1s ease forwards 0.2s;
}

.hero-title {
  font-size: 64px;
  line-height: 1.1;
  font-weight: 800;
  margin-bottom: 24px;
  letter-spacing: -2px;
}

.gradient-text {
  background: linear-gradient(90deg, #4facfe 0%, #00f2fe 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.hero-subtitle {
  font-size: 14px;
  letter-spacing: 4px;
  color: rgba(255, 255, 255, 0.5);
  margin-bottom: 16px;
  font-weight: 600;
}

.hero-description {
  font-size: 20px;
  line-height: 1.6;
  color: rgba(255, 255, 255, 0.8);
  margin-bottom: 48px;
}

.hero-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 14px 28px;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  border: none;
}

.action-btn.primary {
  background: #2563eb;
  color: white;
}

.action-btn.primary:hover {
  background: #1d4ed8;
  transform: translateY(-2px);
  box-shadow: 0 10px 20px rgba(37, 99, 235, 0.3);
}

.action-btn.glass {
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  color: white;
}

.action-btn.glass:hover {
  background: rgba(255, 255, 255, 0.1);
  border-color: rgba(255, 255, 255, 0.2);
}

.scroll-indicator {
  position: absolute;
  bottom: 40px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  opacity: 0.6;
}

.scroll-indicator .mouse {
  width: 24px;
  height: 40px;
  border: 2px solid #fff;
  border-radius: 12px;
  position: relative;
}

.scroll-indicator .wheel {
  width: 4px;
  height: 4px;
  background: #fff;
  border-radius: 50%;
  position: absolute;
  top: 8px;
  left: 50%;
  transform: translateX(-50%);
  animation: scrollWheel 1.5s infinite;
}

.scroll-indicator p {
  font-size: 10px;
  letter-spacing: 2px;
}

/* Antigravity Scrollytelling Section */
/* Antigravity Scrollytelling Section */
.features-scroll-section {
  position: relative;
  padding: 100px 0;
  background: transparent;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
}

.grid-row {
  display: flex;
  gap: 60px;
}

.text-col {
  width: 40%;
  padding-top: 10vh; /* 给予一点顶部空间 */
  padding-bottom: 20vh;
}

.visual-col {
  width: 60%;
  position: relative;
}

.pin-spacer {
  position: sticky;
  top: 100px;
  height: 600px; /* 固定高度展示区域 */
  display: flex;
  align-items: center;
  justify-content: center;
}

.feature-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.feature-item {
  padding: 32px 0;
  border-left: 2px solid rgba(255, 255, 255, 0.1);
  padding-left: 32px;
  opacity: 0.3;
  transition: all 0.5s ease;
  cursor: pointer;
}

.feature-item.active {
  opacity: 1;
  border-left-color: #667eea;
}

.feature-title {
  font-size: 32px;
  font-weight: 700;
  display: block;
  margin-bottom: 8px;
  color: #fff;
}

.description-wrapper {
  overflow: hidden;
  transition: height 0.5s ease;
}

.feature-description {
  font-size: 16px;
  line-height: 1.6;
  color: #a1a1aa;
  margin-bottom: 24px;
}

.explore-link {
  background: transparent;
  border: 1px solid rgba(255, 255, 255, 0.2);
  color: #fff;
  padding: 8px 16px;
  border-radius: 20px;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  transition: all 0.2s;
}

.explore-link:hover {
  background: #fff;
  color: #000;
}

/* 视觉Mockup */
.visual-content-wrapper {
  width: 100%;
  height: 100%;
  position: relative;
}

.feature-visual {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.mockup-window {
  width: 100%;
  height: 400px;
  background: #1a1d2d;
  border-radius: 12px;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.5);
  border: 1px solid rgba(255, 255, 255, 0.05);
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.mockup-header {
  height: 40px;
  background: rgba(0, 0, 0, 0.2);
  display: flex;
  align-items: center;
  padding: 0 16px;
  gap: 8px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
}

.dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
}
.red { background: #ff5f56; }
.yellow { background: #ffbd2e; }
.green { background: #27c93f; }

.address-bar {
  flex: 1;
  margin-left: 12px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 4px;
  height: 24px;
  font-size: 10px;
  display: flex;
  align-items: center;
  padding: 0 12px;
  color: rgba(255, 255, 255, 0.3);
  font-family: monospace;
}

.mockup-body {
  flex: 1;
  position: relative;
  overflow: hidden;
  background: #131620;
}

.theme-blue .mockup-icon { color: #60a5fa; }
.theme-purple .mockup-icon { color: #c084fc; }
.theme-cyan .mockup-icon { color: #22d3ee; }

.mockup-placeholder {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  opacity: 0.8;
}

.mockup-placeholder h3 {
  margin-top: 16px;
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 24px;
}

.mockup-lines {
  width: 60%;
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 30px;
}

.line {
  height: 8px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 4px;
}
.w-75 { width: 75%; }
.w-50 { width: 50%; }
.w-90 { width: 90%; }

/* Mock Widgets */
.mock-chart {
  display: flex;
  align-items: flex-end;
  gap: 8px;
  height: 60px;
}
.bar {
  width: 12px;
  background: #3b82f6;
  border-radius: 2px 2px 0 0;
  animation: growBar 2s infinite ease-in-out alternate;
}

.mock-search {
  display: flex;
  gap: 8px;
}
.search-input {
  background: rgba(255, 255, 255, 0.1);
  padding: 8px 12px;
  border-radius: 6px;
  width: 150px;
  font-size: 12px;
  color: #888;
}
.search-btn {
  background: #c084fc;
  color: white;
  padding: 8px 16px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 600;
}

.mock-graph {
  width: 100px;
  height: 50px;
  border-bottom: 2px solid rgba(255, 255, 255, 0.2);
  position: relative;
}
.graph-line {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, transparent 50%, rgba(34, 211, 238, 0.1) 100%);
}
.graph-dot {
  width: 8px;
  height: 8px;
  background: #22d3ee;
  border-radius: 50%;
  position: absolute;
  top: 10px;
  right: 10px;
  box-shadow: 0 0 10px #22d3ee;
  animation: pulseDot 2s infinite;
}

/* 动画过渡 */
.fade-scale-enter-active,
.fade-scale-leave-active {
  transition: all 0.5s ease;
}

.fade-scale-enter-from {
  opacity: 0;
  transform: scale(0.95);
}
.fade-scale-leave-to {
  opacity: 0;
  transform: scale(1.05);
}

/* Tech Stack & Arch (简化以适配新风格) */
.section-header {
  text-align: center;
  margin-bottom: 60px;
}
.section-header h2 { font-size: 36px; margin-bottom: 12px; }
.section-header p { color: #666; letter-spacing: 1px; }

.tech-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 24px;
  max-width: 1200px;
  margin: 0 auto 100px;
  padding: 0 24px;
}

.tech-card {
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.05);
  border-radius: 16px;
  padding: 24px;
  transition: transform 0.3s;
}
.tech-card:hover { transform: translateY(-5px); background: rgba(255, 255, 255, 0.05); }

.tech-icon-wrapper {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16px;
}

.tech-tags span {
  display: inline-block;
  background: rgba(255,255,255,0.1);
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  margin-right: 6px;
  margin-bottom: 6px;
}

/* 架构流简化 */
.arch-container {
  display: flex;
  justify-content: center;
  padding-bottom: 100px;
}
.arch-flow {
  display: flex;
  align-items: center;
  background: rgba(255,255,255,0.02);
  padding: 40px;
  border-radius: 20px;
  border: 1px solid rgba(255,255,255,0.05);
}
.arch-node {
  text-align: center;
  width: 100px;
}
.node-icon {
  width: 48px;
  height: 48px;
  background: #252836;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 12px;
  border: 2px solid #333;
}
.arch-line {
  flex: 1;
  height: 2px;
  background: #333;
  width: 50px;
}

/* Scroll Animation Helper */
[data-scroll] {
  opacity: 0;
  transform: translateY(20px);
  transition: all 0.6s ease;
}
[data-scroll].visible {
  opacity: 1;
  transform: translateY(0);
}

@keyframes fadeInUp {
  to { opacity: 1; transform: translateY(0); }
}
@keyframes scrollWheel {
  0% { top: 8px; opacity: 1; }
  100% { top: 24px; opacity: 0; }
}
@keyframes growBar {
  from { transform: scaleY(0.8); }
  to { transform: scaleY(1.1); }
}
@keyframes pulseDot {
  0% { box-shadow: 0 0 0 0 rgba(34, 211, 238, 0.4); }
  70% { box-shadow: 0 0 0 10px rgba(34, 211, 238, 0); }
  100% { box-shadow: 0 0 0 0 rgba(34, 211, 238, 0); }
}

@media (max-width: 768px) {
  .hero-title { font-size: 40px; }
  .grid-row { flex-direction: column; }
  .text-col, .visual-col { width: 100%; }
  .pin-spacer { height: auto; position: relative; top: 0; margin-top: 40px; }
  .feature-item { padding: 20px 0; border-left: none; border-bottom: 1px solid rgba(255,255,255,0.1); opacity: 1; }
  .arch-flow { flex-direction: column; gap: 20px; }
  .arch-line { width: 2px; height: 30px; }
}

/* 团队展示样式 */
.team-section {
    padding: 100px 0;
    max-width: 1200px;
    margin: 0 auto;
}

.team-grid {
    display: flex;
    justify-content: center;
    gap: 40px;
    flex-wrap: wrap;
    padding: 0 20px;
}

.team-card {
    background: rgba(255, 255, 255, 0.03);
    border: 1px solid rgba(255, 255, 255, 0.05);
    border-radius: 16px;
    padding: 30px;
    width: 280px;
    text-align: center;
    transition: all 0.3s;
    backdrop-filter: blur(10px);
}

.team-card:hover {
    background: rgba(255, 255, 255, 0.05);
    transform: translateY(-10px);
    border-color: #667eea;
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
}

.avatar-circle {
    width: 80px;
    height: 80px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 50%;
    margin: 0 auto 20px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 32px;
    font-weight: 700;
    color: #fff;
    box-shadow: 0 5px 15px rgba(102, 126, 234, 0.4);
}

.member-info h3 {
    margin: 0 0 10px;
    font-size: 20px;
    color: #fff;
}

.role-badge {
    display: inline-block;
    padding: 4px 12px;
    background: rgba(102, 126, 234, 0.1);
    color: #667eea;
    border-radius: 20px;
    font-size: 13px;
    border: 1px solid rgba(102, 126, 234, 0.2);
}

/* 图片Demo样式 */
.mockup-image-container {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    overflow: hidden;
    background: #000;
}

.feature-demo-img {
    width: 100%;
    height: 100%;
    object-fit: cover; /* 或者 contain，视图片尺寸而定 */
    transition: transform 0.5s;
}

.feature-visual:hover .feature-demo-img {
    transform: scale(1.02);
}
</style>
