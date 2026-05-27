<template>
  <div class="quota-container">
    <div class="balance-section">
      <span class="label">当前账户余额</span>
      <div class="value">
        <span class="currency">¥</span>
        <span class="amount">{{ quotaData.balance }}</span>
      </div>
    </div>
    <div class="resources-section">
      <div v-if="isTalk || isSuper" class="resource-item">
        <div class="icon-wrapper talk"><el-icon><Phone /></el-icon></div>
        <div class="info">
          <span class="label">剩余通话</span>
          <span class="data">{{ quotaData.remainingTalkTime }}</span>
        </div>
      </div>
      <div v-if="isTalk || isSuper" class="resource-item">
        <div class="icon-wrapper sms"><el-icon><ChatDotRound /></el-icon></div>
        <div class="info">
          <span class="label">剩余短信</span>
          <span class="data">{{ quotaData.remainingSms }}</span>
        </div>
      </div>
      <div v-if="isNet || isSuper" class="resource-item">
        <div class="icon-wrapper net"><el-icon><Connection /></el-icon></div>
        <div class="info">
          <span class="label">剩余流量</span>
          <span class="data">{{ quotaData.remainingFlow }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
// JS 部分不变
import { computed } from 'vue'
import { Phone, ChatDotRound, Connection } from '@element-plus/icons-vue'

const props = defineProps({
  quotaData: { type: Object, required: true, default: () => ({}) }
})
const isTalk = computed(() => props.quotaData.packageName === 'TalkativePackage')
const isNet = computed(() => props.quotaData.packageName === 'NetizenPackage')
const isSuper = computed(() => props.quotaData.packageName === 'SupermanPackage')
</script>

<style scoped>
.quota-container {
  display: flex;
  background: linear-gradient(135deg, #0F3D6B 0%, #1A5CAA 100%);
  border-radius: 12px;
  padding: 28px;
  color: #fff;
  box-shadow: 0 8px 16px rgba(15, 61, 107, 0.2);
}
.balance-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  border-right: 1px solid rgba(255,255,255,0.2);
  padding-right: 24px;
  margin-right: 24px;
}
.balance-section .label { font-size: 14px; opacity: 0.8; margin-bottom: 8px; }
.balance-section .value { display: flex; align-items: baseline; }
.balance-section .currency { font-size: 20px; margin-right: 4px; }
.balance-section .amount { font-size: 40px; font-weight: 700; line-height: 1; }

.resources-section {
  flex: 2;
  display: flex;
  gap: 24px;
}
.resource-item {
  flex: 1;
  display: flex;
  align-items: center;
  background: rgba(255,255,255,0.1);
  padding: 12px 16px;
  border-radius: 8px;
}
.icon-wrapper {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
  font-size: 20px;
}
.icon-wrapper.talk { background: rgba(0, 181, 120, 0.2); color: #00B578; }
.icon-wrapper.sms { background: rgba(255, 106, 0, 0.2); color: #FF6A00; }
.icon-wrapper.net { background: rgba(22, 119, 255, 0.2); color: #1677FF; }

.info { display: flex; flex-direction: column; }
.info .label { font-size: 12px; opacity: 0.7; margin-bottom: 4px; }
.info .data { font-size: 16px; font-weight: 600; }
</style>