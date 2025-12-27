<template>
  <div class="fake-vehicle-alerts" :class="{ 'fullscreen-mode': mode === 'fullscreen' }">
    <div class="card-header" v-if="mode === 'thumbnail'">
      <el-icon><Warning /></el-icon>
      <span>套牌车告警 ({{ alerts.length }})</span>
    </div>
    <div class="card-body">
      <div class="alerts-header" v-if="mode === 'fullscreen'">
        <h2><el-icon><Warning /></el-icon> 套牌车告警监控</h2>
        <span class="alert-count">共 {{ alerts.length }} 条告警</span>
      </div>
      <div class="alert-list" v-if="alerts.length > 0">
        <div 
          v-for="alert in displayAlerts" 
          :key="alert.id" 
          class="alert-item"
        >
          <div class="alert-row">
            <span class="alert-plate">{{ alert.plateNumber }}</span>
            <span class="alert-level" :class="(alert.alertLevel || 'HIGH').toLowerCase()">
              {{ alert.alertLevel || 'HIGH' }}
            </span>
          </div>
          <div class="alert-row">
            <span class="alert-speed">🚗 {{ alert.actualSpeed?.toFixed(1) || 0 }} km/h</span>
            <span class="alert-time">{{ formatAlertTime(alert.createTime) }}</span>
          </div>
          <div class="alert-location" v-if="mode === 'fullscreen'">
            📍 {{ alert.endBayonet || alert.startBayonet || '未知站点' }}
          </div>
        </div>
      </div>
      <div class="no-alerts" v-else>
        <el-icon><CircleCheck /></el-icon>
        <span>暂无告警</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Warning, CircleCheck } from '@element-plus/icons-vue'

const props = defineProps({
  mode: { type: String, default: 'thumbnail' },
  alerts: { type: Array, default: () => [] }
})

const displayAlerts = computed(() => {
  return props.mode === 'fullscreen' ? props.alerts : props.alerts.slice(0, 5)
})

const formatAlertTime = (timeStr) => {
  if (!timeStr) return ''
  const time = new Date(timeStr)
  return time.toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    hour12: false
  })
}
</script>

<style scoped>
.fake-vehicle-alerts {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.card-header {
  background: linear-gradient(90deg, transparent, rgba(245, 87, 108, 0.2), transparent);
  border-left: 3px solid #f5576c;
  padding: 10px 14px;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 600;
  color: #f5576c;
  flex-shrink: 0;
}

.card-body {
  flex: 1 1 0;
  padding: 12px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.fullscreen-mode .card-body {
  padding: 20px;
}

.alerts-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 12px;
  margin-bottom: 12px;
  border-bottom: 1px solid rgba(245, 87, 108, 0.3);
  flex-shrink: 0;
}

.alerts-header h2 {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #f5576c;
  font-size: 20px;
  margin: 0;
}

.alert-count {
  color: rgba(255, 255, 255, 0.6);
  font-size: 14px;
}

.alert-list {
  flex: 1 1 0;
  overflow-y: auto;
  overflow-x: hidden;
  padding-right: 8px;
}

.alert-list::-webkit-scrollbar {
  width: 8px;
}

.alert-list::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 4px;
}

.alert-list::-webkit-scrollbar-thumb {
  background: rgba(245, 87, 108, 0.4);
  border-radius: 4px;
}

.alert-list::-webkit-scrollbar-thumb:hover {
  background: rgba(245, 87, 108, 0.6);
}

.alert-item {
  padding: 10px 12px;
  background: rgba(245, 87, 108, 0.08);
  border-radius: 6px;
  margin-bottom: 8px;
  border-left: 3px solid transparent;
  transition: all 0.2s ease;
}

.fullscreen-mode .alert-item {
  padding: 14px 18px;
  margin-bottom: 10px;
}

.alert-item:hover {
  background: rgba(245, 87, 108, 0.15);
  border-left-color: #f5576c;
}

.alert-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.alert-row:last-child {
  margin-bottom: 0;
}

.alert-plate {
  font-size: 14px;
  font-weight: 700;
  color: #f5576c;
}

.fullscreen-mode .alert-plate {
  font-size: 16px;
}

.alert-level {
  font-size: 10px;
  padding: 2px 8px;
  border-radius: 4px;
  font-weight: 600;
}

.alert-level.high {
  background: rgba(245, 87, 108, 0.3);
  color: #f5576c;
}

.alert-level.medium {
  background: rgba(250, 112, 154, 0.3);
  color: #fa709a;
}

.alert-level.low {
  background: rgba(254, 225, 64, 0.3);
  color: #fee140;
}

.alert-speed {
  font-size: 12px;
  color: #00d4ff;
}

.alert-time {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.5);
  font-family: 'Courier New', monospace;
}

.fullscreen-mode .alert-speed,
.fullscreen-mode .alert-time {
  font-size: 13px;
}

.alert-location {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.5);
  margin-top: 6px;
}

.fullscreen-mode .alert-location {
  font-size: 12px;
}

.no-alerts {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: rgba(255, 255, 255, 0.4);
  gap: 10px;
}

.no-alerts .el-icon {
  font-size: 36px;
  color: #43e97b;
}
</style>
