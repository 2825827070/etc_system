<template>
  <div class="latest-records" :class="{ 'fullscreen-mode': mode === 'fullscreen' }">
    <div class="card-header" v-if="mode === 'thumbnail'">
      <el-icon><List /></el-icon>
      <span>最新通行记录</span>
    </div>
    <div class="card-body">
      <div class="records-header" v-if="mode === 'fullscreen'">
        <h2><el-icon><List /></el-icon> 最新通行记录</h2>
      </div>
      <div class="record-list">
        <TransitionGroup name="record-slide">
          <div 
            v-for="item in displayRecords" 
            :key="item.id" 
            class="record-item"
          >
            <div class="record-time">{{ formatTime(item.passTime) }}</div>
            <div class="record-info">
              <span class="plate-number">{{ item.plateNumber }}</span>
              <span class="district">{{ item.districtName }}</span>
            </div>
            <div class="record-extra" v-if="mode === 'fullscreen'">
              <span class="checkpoint">{{ item.bayonetName || '未知站点' }}</span>
              <span class="speed" v-if="item.speed">{{ item.speed }} km/h</span>
            </div>
          </div>
        </TransitionGroup>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { List } from '@element-plus/icons-vue'
import gsap from 'gsap'

const props = defineProps({
  mode: { type: String, default: 'thumbnail' },
  records: { type: Array, default: () => [] }
})

const displayRecords = computed(() => {
  return props.mode === 'fullscreen' ? props.records : props.records.slice(0, 10)
})

const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const time = new Date(timeStr)
  return time.toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false
  })
}

onMounted(() => {
  // CSS TransitionGroup handles animations
})
</script>

<style scoped>
.latest-records {
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
  overflow: hidden;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.fullscreen-mode .card-body {
  padding: 20px 24px;
}

.records-header {
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid rgba(102, 126, 234, 0.3);
}

.records-header h2 {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #00d4ff;
  font-size: 24px;
  margin: 0;
}

.record-list {
  flex: 1;
  overflow-y: auto;
  padding-right: 8px;
  min-height: 0;
}

.fullscreen-mode .record-list {
  padding-right: 12px;
}

.record-list::-webkit-scrollbar {
  width: 4px;
}

.record-list::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 2px;
}

.record-list::-webkit-scrollbar-thumb {
  background: rgba(102, 126, 234, 0.5);
  border-radius: 2px;
}

.record-item {
  padding: 10px 8px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
  transition: all 0.3s ease;
}

.fullscreen-mode .record-item {
  padding: 14px 16px;
  background: rgba(102, 126, 234, 0.05);
  border-radius: 6px;
  margin-bottom: 8px;
}

.record-item:hover {
  background: rgba(102, 126, 234, 0.1);
  border-left: 2px solid #667eea;
  padding-left: 6px;
  transform: translateX(5px);
}

.record-time {
  font-size: 10px;
  color: rgba(255, 255, 255, 0.4);
  margin-bottom: 4px;
  font-family: 'Courier New', monospace;
}

.fullscreen-mode .record-time {
  font-size: 12px;
}

.record-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.plate-number {
  font-size: 14px;
  font-weight: 600;
  color: #00d4ff;
  text-shadow: 0 0 10px rgba(0, 212, 255, 0.3);
}

.fullscreen-mode .plate-number {
  font-size: 16px;
}

.district {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.6);
}

.fullscreen-mode .district {
  font-size: 13px;
}

.record-extra {
  margin-top: 8px;
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.5);
}

.speed {
  color: #43e97b;
}

/* Animations */
.record-slide-enter-active {
  transition: all 0.4s ease;
}

.record-slide-leave-active {
  transition: all 0.3s ease;
}

.record-slide-enter-from {
  opacity: 0;
  transform: translateX(-30px);
}

.record-slide-leave-to {
  opacity: 0;
  transform: translateX(30px);
}
</style>
