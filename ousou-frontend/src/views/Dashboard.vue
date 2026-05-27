<template>
  <div class="dashboard" v-loading="loading">

    <!-- 顶部指标 -->
    <div class="stat-grid">
      <div class="stat-card">
        <div class="stat-dot" style="background:#0D9488"></div>
        <div class="stat-body">
          <span class="stat-label">账户余额</span>
          <span class="stat-val" :class="{ danger: userStore.balance < 20 }">¥ {{ userStore.balance.toFixed(2) }}</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-dot" style="background:#E8590C"></div>
        <div class="stat-body">
          <span class="stat-label">本月消费</span>
          <span class="stat-val">¥ {{ totalCost.toFixed(2) }}</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-dot" style="background:#D97706"></div>
        <div class="stat-body">
          <span class="stat-label">当前套餐</span>
          <span class="stat-val sm">{{ currentPackageName }}</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-dot" style="background:#64748B"></div>
        <div class="stat-body">
          <span class="stat-label">手机号</span>
          <span class="stat-val sm">{{ userStore.phone }}</span>
        </div>
      </div>
    </div>

    <!-- 告警条 -->
    <div v-if="alerts.length" class="alert-bar">
      <div v-for="(a, i) in alerts" :key="i" class="alert-chip" :class="a.level">
        <svg width="14" height="14" viewBox="0 0 14 14" fill="none"><path d="M7 1.5l5.5 10H1.5L7 1.5z" stroke="currentColor" stroke-width="1.2" stroke-linejoin="round"/><path d="M7 5v2.5M7 9.5v.5" stroke="currentColor" stroke-width="1.2" stroke-linecap="round"/></svg>
        <span>{{ a.msg }}</span>
      </div>
    </div>

    <!-- 套餐资源总览 + 消费构成 (通过等高拉伸防止塌陷) -->
    <el-row :gutter="16" class="chart-row" type="flex">
      <el-col :span="14">
        <div class="panel">
          <div class="panel-head">
            <h3>套餐资源总览</h3>
            <span class="pkg-tag">{{ currentPackageName }}</span>
          </div>
          <div class="rings-row" :class="{ 'single': resourceItems.length === 1, 'double': resourceItems.length === 2 }">
            <div class="ring-block" v-for="res in resourceItems" :key="res.type">
              <div class="ring" :style="ringStyle(res.pct, res.color)">
                <div class="ring-inner">
                  <span class="ring-num">{{ res.type === 'flow' ? fmtMB(res.used) : res.used }}</span>
                  <span class="ring-unit">{{ res.label }}已用</span>
                </div>
              </div>
              <div class="ring-meta">
                <span>剩余 <b>{{ res.type === 'flow' ? fmtMB(res.remain) : res.remain }}</b> {{ res.type === 'flow' ? '' : res.unit }}</span>
                <span class="ring-total">总额 {{ res.type === 'flow' ? fmtMB(res.total) : res.total }} {{ res.type === 'flow' ? '' : res.unit }}</span>
              </div>
              <span class="ring-status" :class="res.statusClass">{{ res.statusText }}</span>
            </div>
          </div>
        </div>
      </el-col>
      <el-col :span="10">
        <!-- 面板加持 height: 100% -->
        <div class="panel full-h">
          <div class="panel-head"><h3>消费构成</h3></div>
          <div ref="costChartRef" class="chart-area"></div>
        </div>
      </el-col>
    </el-row>

    <!-- 加油包 + 快捷操作 -->
    <el-row :gutter="16" class="action-row">
      <el-col :span="14">
        <div class="panel">
          <div class="panel-head"><h3>流量加油包</h3></div>
          <div class="addon-row">
            <div class="addon-card" @click="handleBuyAddon(1, '1GB流量包', 10)">
              <span class="addon-badge orange">特惠</span>
              <div class="addon-amount">1 GB</div>
              <div class="addon-desc">全国通用流量</div>
              <div class="addon-price">¥10</div>
            </div>
            <div class="addon-card" @click="handleBuyAddon(3, '3GB流量包', 20)">
              <span class="addon-badge teal">热门</span>
              <div class="addon-amount">3 GB</div>
              <div class="addon-desc">全国通用流量</div>
              <div class="addon-price">¥20</div>
            </div>
            <div class="addon-card" @click="handleBuyAddon(5, '5GB流量包', 30)">
              <span class="addon-badge red">超值</span>
              <div class="addon-amount">5 GB</div>
              <div class="addon-desc">全国通用流量</div>
              <div class="addon-price">¥30</div>
            </div>
          </div>
        </div>
      </el-col>
      <el-col :span="10">
        <div class="shortcut-col">
          <div class="shortcut-card" @click="$router.push('/user/recharge')">
            <div class="sc-icon" style="background:#ECFDF8;color:#0D9488"><el-icon :size="22"><Wallet /></el-icon></div>
            <div class="sc-info"><h4>话费充值</h4><p>余额不足时及时充值</p></div>
            <svg class="sc-arrow" width="16" height="16" viewBox="0 0 16 16" fill="none"><path d="M6 4l4 4-4 4" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/></svg>
          </div>
          <div class="shortcut-card" @click="$router.push('/user/consume')">
            <div class="sc-icon" style="background:#FFF4ED;color:#E8590C"><el-icon :size="22"><Phone /></el-icon></div>
            <div class="sc-info"><h4>模拟消费</h4><p>体验通话、上网、短信扣费</p></div>
            <svg class="sc-arrow" width="16" height="16" viewBox="0 0 16 16" fill="none"><path d="M6 4l4 4-4 4" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/></svg>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useUserStore } from '../stores/user'
import { getQuota, getRecords, buyDataPack } from '../api/user'
import { Wallet, Phone } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as echarts from 'echarts'

const userStore = useUserStore()
const loading = ref(false)
const quotaData = ref({})
const records = ref([])

const costChartRef = ref(null)
let costChart = null

/* ---- 基础数据 ---- */
const packageMap = { TalkativePackage: '话唠套餐', NetizenPackage: '网虫套餐', SupermanPackage: '超人套餐' }
const currentPackageName = computed(() => packageMap[quotaData.value.packageName] || '未知')
const totalCost = computed(() => records.value.reduce((s, r) => s + r.cost, 0))

const parseNum = (str) => parseInt(String(str).replace(/[^0-9]/ig, '')) || 0

const fmtMB = (mb) => {
  if (!mb && mb !== 0) return '0 MB'
  return mb >= 1024 ? (mb / 1024).toFixed(1) + ' GB' : mb + ' MB'
}

/* ---- 核心改进 1：动态累加用户历史上买过多少额外的流量加油包 ---- */
const boughtFlowMB = computed(() => {
  return records.value
    .filter(r => r.type === '购买流量包')
    .reduce((sum, r) => sum + (r.amount || 0), 0)
})

/* ---- 根据套餐类型动态构建资源列表 (改用动态总额计算) ---- */
const resourceItems = computed(() => {
  const pkg = quotaData.value.packageName
  const items = []

  if (pkg === 'TalkativePackage') {
    items.push({ type: 'talk', total: 500, remain: parseNum(quotaData.value.remainingTalkTime), unit: '分钟', label: '通话' })
    items.push({ type: 'sms', total: 30, remain: parseNum(quotaData.value.remainingSms), unit: '条', label: '短信' })
  }
  else if (pkg === 'NetizenPackage') {
    // 基础流量 + 购买的加油包流量 = 动态总流量
    const dynamicTotal = 3072 + boughtFlowMB.value 
    items.push({ type: 'flow', total: dynamicTotal, remain: parseNum(quotaData.value.remainingFlow), unit: 'MB', label: '流量' })
  }
  else if (pkg === 'SupermanPackage') {
    items.push({ type: 'talk', total: 200, remain: parseNum(quotaData.value.remainingTalkTime), unit: '分钟', label: '通话' })
    items.push({ type: 'sms', total: 50, remain: parseNum(quotaData.value.remainingSms), unit: '条', label: '短信' })
    
    const dynamicTotal = 1024 + boughtFlowMB.value
    items.push({ type: 'flow', total: dynamicTotal, remain: parseNum(quotaData.value.remainingFlow), unit: 'MB', label: '流量' })
  }

  return items.map(item => {
    const used = Math.max(0, item.total - item.remain)
    const pct = item.total > 0 ? Math.min(100, (used / item.total) * 100) : 0
    let color, statusClass, statusText
    if (pct > 80) { color = '#DC2626'; statusClass = 'danger'; statusText = '紧张' }
    else if (pct > 50) { color = '#D97706'; statusClass = 'warn'; statusText = '注意' }
    else { color = '#0D9488'; statusClass = 'ok'; statusText = '充足' }
    return { ...item, used, pct, color, statusClass, statusText }
  })
})

/* ---- 环形进度条样式 ---- */
const ringStyle = (pct, color) => ({
  background: `conic-gradient(${color} 0% ${pct}%, #F1F5F9 ${pct}% 100%)`
})

/* ---- 智能告警 ---- */
const alerts = computed(() => {
  const list = []
  if (userStore.balance < 20) list.push({ level: 'red', msg: '余额不足，请及时充值' })
  resourceItems.value.forEach(res => {
    if (res.pct > 80) {
      if (res.type === 'flow') list.push({ level: 'orange', msg: '流量即将耗尽，建议购买加油包' })
      else if (res.type === 'talk') list.push({ level: 'orange', msg: '通话时长即将耗尽' })
      else if (res.type === 'sms') list.push({ level: 'orange', msg: '短信条数即将耗尽' })
    }
  })
  return list
})

/* ---- 数据加载 ---- */
const fetchData = async () => {
  loading.value = true
  try {
    const [qRes, rRes] = await Promise.all([
      getQuota({ phone: userStore.phone }),
      getRecords({ phone: userStore.phone })
    ])
    quotaData.value = qRes.data
    records.value = rRes.data || []
    if (quotaData.value.balance !== undefined) userStore.updateBalance(quotaData.value.balance)
  } catch (e) {
    console.error('加载数据失败:', e)
  }

  loading.value = false
  await nextTick()
  initCostChart()
  costChart?.resize()
}

/* ---- 仅刷新额度数据 ---- */
const refreshQuota = async () => {
  try {
    const qRes = await getQuota({ phone: userStore.phone })
    quotaData.value = qRes.data
    if (qRes.data.balance !== undefined) userStore.updateBalance(qRes.data.balance)
  } catch (e) {
    console.error('刷新额度失败:', e)
  }
}

/* ---- 消费构成饼图  ---- */
const initCostChart = () => {
  if (!costChartRef.value) return
  if (!costChart) costChart = echarts.init(costChartRef.value)

  // 1. 初始化标准容器，合并“购买流量包”至“上网”分类
  const costMap = { '通话': 0, '上网': 0, '短信': 0, '购买流量包': 0 }
  
  // 2. 遍历合并
  records.value.forEach(r => {
    let type = r.type
    if (type === '发短信') type = '短信'
    
    if (costMap[type] !== undefined) {
      costMap[type] += r.cost
    }
  })

  // 3. 构建数据
  const data = [
    { name: '通话', value: +costMap['通话'].toFixed(2), itemStyle: { color: '#0D9488' } },
    { name: '上网', value: +(costMap['上网'] + costMap['购买流量包']).toFixed(2), itemStyle: { color: '#E8590C' } },
    { name: '短信', value: +costMap['短信'].toFixed(2), itemStyle: { color: '#D97706' } }
  ].filter(d => d.value > 0)

  // 判断当前是否有真实的消费数据
  const hasData = data.length > 0

  costChart.setOption({
    // 如果没有消费，直接关闭 tooltip 悬浮提示，彻底避免显示 ¥1
    tooltip: { 
      show: hasData,
      trigger: 'item', 
      formatter: '{b}: ¥{c} ({d}%)' 
    },
    // 如果没有消费,隐藏底部的分类图例
    legend: { 
      show: hasData,
      bottom: 8, 
      left: 'center', 
      itemGap: 16, 
      textStyle: { fontSize: 12, color: '#475569' } 
    },
    series: [{
      type: 'pie', 
      radius: ['42%', '68%'], 
      center: ['50%', '44%'],
      avoidLabelOverlap: false,
      itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
      
      
      // 当没有数据时，开启静默模式(silent: true)，彻底禁用一切鼠标 hover 交互
      silent: !hasData, 
      label: { 
        show: !hasData, 
        position: 'center', 
        formatter: '暂无消费', 
        fontSize: 15, 
        fontWeight: 'bold', 
        color: '#94A3B8' // 优雅的置灰字色
      },
      emphasis: { 
        label: { 
          show: hasData, // 有数据时，悬浮激活中心文字
          fontSize: 14, 
          fontWeight: 'bold', 
          formatter: '{b}\n¥{c}' 
        } 
      },
      // 没有数据，使用温和的浅灰色 #F1F5F9 绘制一个纯粹的空心圆环占位
      data: hasData ? data : [{ name: '暂无消费', value: 1, itemStyle: { color: '#F1F5F9' } }]
    }]
  }, true)
}

/* ---- 加油包订购 ---- */
const handleBuyAddon = (size, label, price) => {
  ElMessageBox.confirm(
    `确定购买【${label}】吗？将从余额中扣除 ¥${price}。`,
    '订购确认',
    { confirmButtonText: '确定订购', cancelButtonText: '取消', type: 'info' }
  ).then(async () => {
    try {
      const res = await buyDataPack({ phone: userStore.phone, size })
      ElMessage.success(res.msg || '订购成功')
      
      // 订购成功后，重新获取 records ，然后重绘饼图和资源总览
      const rRes = await getRecords({ phone: userStore.phone })
      records.value = rRes.data || []
      
      await refreshQuota()
      initCostChart()
    } catch (error) {
      const errMsg = error.response?.data?.error || error.message || '订购失败'
      ElMessage.error(errMsg)
    }
  }).catch(() => {})
}

/* ---- resize ---- */
const handleResize = () => { costChart?.resize() }

onMounted(() => {
  window.addEventListener('resize', handleResize)
  fetchData()
})
onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  costChart?.dispose()
})
</script>

<style scoped>
.dashboard { padding: 0 0 32px; }

/* === 指标卡片 === */
.stat-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 14px; margin-bottom: 14px; }
.stat-card {
  background: #fff; border: 1px solid var(--border-color); border-radius: 12px;
  padding: 18px 20px; display: flex; align-items: center; gap: 14px;
  transition: box-shadow .25s;
}
.stat-card:hover { box-shadow: var(--shadow-md); }
.stat-dot { width: 8px; height: 8px; border-radius: 50%; flex-shrink: 0; }
.stat-body { display: flex; flex-direction: column; }
.stat-label { font-size: 13px; color: var(--text-secondary); margin-bottom: 2px; }
.stat-val { font-size: 22px; font-weight: 800; color: var(--text-primary); line-height: 1.2; }
.stat-val.sm { font-size: 17px; }
.stat-val.danger { color: #DC2626; }

/* === 告警条 === */
.alert-bar { display: flex; gap: 10px; margin-bottom: 14px; flex-wrap: wrap; }
.alert-chip {
  display: inline-flex; align-items: center; gap: 6px;
  padding: 7px 14px; border-radius: 8px; font-size: 13px; font-weight: 500;
}
.alert-chip.red { background: #FEF2F2; color: #DC2626; }
.alert-chip.orange { background: #FFF7ED; color: #D97706; }

/* === 通用面板 === */
.panel {
  background: #fff; border: 1px solid var(--border-color); border-radius: 14px;
  padding: 22px;
}
.panel.full-h { display: flex; flex-direction: column; height: 100%; box-sizing: border-box; }
.panel-head {
  display: flex; align-items: center; justify-content: space-between;
  margin-bottom: 18px;
}
.panel-head h3 { font-size: 16px; font-weight: 700; color: var(--text-primary); }
.pkg-tag {
  font-size: 12px; font-weight: 600; color: #0D9488;
  background: #ECFDF8; padding: 3px 10px; border-radius: 6px;
}

/* === 环形进度条 === */
.rings-row {
  display: flex; justify-content: space-around; align-items: flex-start;
  padding: 8px 0; gap: 20px;
}
.rings-row.single { justify-content: center; }
.rings-row.single .ring { width: 150px; height: 150px; }
.rings-row.single .ring-inner { width: 116px; height: 116px; }
.rings-row.single .ring-num { font-size: 26px; }
.rings-row.double .ring { width: 128px; height: 128px; }
.rings-row.double .ring-inner { width: 98px; height: 98px; }

.ring-block { display: flex; flex-direction: column; align-items: center; gap: 10px; }
.ring {
  width: 112px; height: 112px; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  transition: transform .3s;
}
.ring:hover { transform: scale(1.06); }
.ring-inner {
  width: 84px; height: 84px; border-radius: 50%; background: #fff;
  display: flex; flex-direction: column; align-items: center; justify-content: center;
}
.ring-num { font-size: 20px; font-weight: 800; color: var(--text-primary); line-height: 1.2; }
.ring-unit { font-size: 11px; color: var(--text-secondary); margin-top: 2px; }
.ring-meta {
  text-align: center; font-size: 13px; color: var(--text-secondary);
  display: flex; flex-direction: column; gap: 1px;
}
.ring-meta b { color: var(--text-primary); font-weight: 700; }
.ring-total { font-size: 11px; color: var(--text-secondary); opacity: .7; }
.ring-status {
  font-size: 11px; font-weight: 600; padding: 2px 10px; border-radius: 4px;
}
.ring-status.ok { background: #ECFDF8; color: #0D9488; }
.ring-status.warn { background: #FFF7ED; color: #D97706; }
.ring-status.danger { background: #FEF2F2; color: #DC2626; }

/* === 图表区 === */
.chart-row { margin-bottom: 14px; }

.chart-area { width: 100%; height: 280px; }

/* === 加油包 === */
.addon-row { display: grid; grid-template-columns: repeat(3, 1fr); gap: 12px; }
.addon-card {
  position: relative; border: 1px solid var(--border-color); border-radius: 12px;
  padding: 20px 16px; text-align: center; cursor: pointer;
  transition: border-color .25s, box-shadow .25s;
}
.addon-card:hover { border-color: #0D9488; box-shadow: 0 2px 12px rgba(13,148,136,0.08); }
.addon-badge {
  position: absolute; top: 8px; right: 8px;
  font-size: 10px; font-weight: 700; padding: 2px 8px; border-radius: 4px;
  color: #fff;
}
.addon-badge.orange { background: #D97706; }
.addon-badge.teal { background: #0D9488; }
.addon-badge.red { background: #E8590C; }
.addon-amount { font-size: 26px; font-weight: 900; color: var(--text-primary); margin: 6px 0 4px; }
.addon-desc { font-size: 12px; color: var(--text-secondary); margin-bottom: 8px; }
.addon-price { font-size: 16px; font-weight: 700; color: #E8590C; }

/* === 快捷操作 === */
.shortcut-col { display: flex; flex-direction: column; gap: 12px; height: 100%; }
.shortcut-card {
  flex: 1; background: #fff; border: 1px solid var(--border-color); border-radius: 12px;
  padding: 20px; display: flex; align-items: center; gap: 16px;
  cursor: pointer; transition: border-color .25s, box-shadow .25s;
}
.shortcut-card:hover { border-color: #0D9488; box-shadow: var(--shadow-sm); }
.sc-icon {
  width: 46px; height: 46px; border-radius: 12px; flex-shrink: 0;
  display: flex; align-items: center; justify-content: center;
}
.sc-info h4 { font-size: 15px; font-weight: 600; color: var(--text-primary); margin-bottom: 2px; }
.sc-info p { font-size: 12px; color: var(--text-secondary); margin: 0; }
.sc-arrow { margin-left: auto; color: #CBD5E1; flex-shrink: 0; }
.shortcut-card:hover .sc-arrow { color: #0D9488; }

/* === 响应式 === */
@media (max-width: 900px) {
  .stat-grid { grid-template-columns: repeat(2, 1fr); }
  .rings-row { gap: 16px; flex-wrap: wrap; justify-content: center; }
  .ring { width: 90px; height: 90px; }
  .ring-inner { width: 68px; height: 68px; }
  .ring-num { font-size: 16px; }
}
</style>