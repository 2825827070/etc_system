<template>
  <div class="total-flow-card" :class="{ 'fullscreen-mode': mode === 'fullscreen' }">
    <div class="card-header" v-if="mode === 'thumbnail'">
      <el-icon><Odometer /></el-icon>
      <span>总车流量</span>
    </div>
    <div class="card-body">
      <div class="flow-display">
        <div class="big-number" ref="numberRef">{{ displayFlow }}</div>
        <div class="label">总流量</div>
      </div>
      <div v-if="mode === 'fullscreen'" class="extra-stats">
        <div class="stat-item">
          <span class="stat-label">小时峰值</span>
          <span class="stat-value">{{ peakFlow }}/小时</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">平均流量</span>
          <span class="stat-value">{{ avgFlow }}/小时</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { Odometer } from '@element-plus/icons-vue'
import gsap from 'gsap'

const props = defineProps({
  mode: { type: String, default: 'thumbnail' },
  totalFlow: { type: [Number, String], default: 0 }
})

const numberRef = ref(null)
const displayFlow = ref('0')
const animatedValue = ref(0)

// 平均流量 = 总流量 / 24小时
const avgFlow = computed(() => {
  const val = typeof props.totalFlow === 'string' 
    ? parseInt(props.totalFlow.replace(/,/g, '')) 
    : props.totalFlow
  return Math.floor(val / 24).toLocaleString()
})

// 峰值流量 = 平均流量 × 1.5 (模拟高峰时段，是每小时峰值，不是总量)
const peakFlow = computed(() => {
  const val = typeof props.totalFlow === 'string' 
    ? parseInt(props.totalFlow.replace(/,/g, '')) 
    : props.totalFlow
  const avg = Math.floor(val / 24)
  return Math.floor(avg * 1.5).toLocaleString()
})

watch(() => props.totalFlow, (newVal) => {
  const target = typeof newVal === 'string' 
    ? parseInt(newVal.replace(/,/g, '')) 
    : newVal
  
  gsap.to(animatedValue, {
    value: target,
    duration: 1.5,
    ease: 'power2.out',
    onUpdate: () => {
      displayFlow.value = Math.floor(animatedValue.value).toLocaleString()
    }
  })
}, { immediate: true })

onMounted(() => {
  // Number animation handled by watch
})
</script>

<style scoped>
.total-flow-card {
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
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 16px;
}

.flow-display {
  text-align: center;
}

.big-number {
  font-size: 48px;
  font-weight: 700;
  background: linear-gradient(135deg, #667eea 0%, #00d4ff 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 8px;
}

.fullscreen-mode .big-number {
  font-size: 96px;
}

.label {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.6);
}

.fullscreen-mode .label {
  font-size: 20px;
}

.extra-stats {
  display: flex;
  gap: 40px;
  margin-top: 30px;
}

.stat-item {
  text-align: center;
  padding: 20px 30px;
  background: rgba(102, 126, 234, 0.1);
  border-radius: 12px;
  border: 1px solid rgba(102, 126, 234, 0.3);
}

.stat-label {
  display: block;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.5);
  margin-bottom: 8px;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: #00d4ff;
}
</style>
