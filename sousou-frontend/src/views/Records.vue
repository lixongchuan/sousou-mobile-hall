<template>
  <div class="records-container">
    <!-- 汇总统计 -->
    <el-row :gutter="16" style="margin-bottom: 16px;">
      <el-col :span="8">
        <div class="summary-card" style="border-left: 4px solid #0F3D6B;">
          <span class="label">通话总消费</span>
          <span class="value">¥ {{ summaryCost.call.toFixed(2) }}</span>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="summary-card" style="border-left: 4px solid #FF6A00;">
          <span class="label">上网总消费</span>
          <span class="value">¥ {{ summaryCost.net.toFixed(2) }}</span>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="summary-card" style="border-left: 4px solid #00B578;">
          <span class="label">短信总消费</span>
          <span class="value">¥ {{ summaryCost.sms.toFixed(2) }}</span>
        </div>
      </el-col>
    </el-row>

    <el-card shadow="never">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <span class="card-title">历史消费详单</span>
          
          <!-- ⭐ 升级：仿主流悬浮渐显“刷新详单”按钮 (内置原生SVG，免安装额外依赖) -->
          <el-button type="primary" size="small" @click="fetchRecords" :loading="loading" text class="hover-refresh-btn">
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1024 1024" width="14" height="14" class="refresh-icon">
              <path d="M512 64a448 448 0 0 1 418.176 612.352 32 32 0 1 1-59.584-23.744A384 384 0 1 0 160 512c0 120.32 55.232 227.648 141.44 298.56L256 832a32 32 0 0 1 0 64h160a32 32 0 0 1 32-32V704a32 32 0 0 1-64 0l.064 45.312C296.832 682.496 224 513.216 224 416A288 288 0 0 1 512 64z"/>
            </svg>
            <span class="btn-text">刷新详单</span>
          </el-button>
        </div>
      </template>

      <!-- ⭐ 升级：绑定 pagedRecords 响应式分页计算属性 -->
      <el-table :data="pagedRecords" border style="width: 100%" v-loading="loading" empty-text="暂无消费记录" size="default">
        <el-table-column align="center" type="index" :index="indexMethod" label="序号" width="80" />
        <el-table-column align="center" prop="type" label="消费类型" width="180">
          <template #default="scope">
            <el-tag :type="getTypeTag(scope.row.type)" effect="light">{{ scope.row.type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column align="center" prop="amount" label="消费量" />
        <el-table-column align="center" prop="cost" label="扣费金额 (元)">
          <template #default="scope">
            <span :style="{ color: scope.row.cost > 0 ? '#F53F3F' : '#00B578', fontWeight: 'bold' }">
              {{ scope.row.cost > 0 ? `-¥${scope.row.cost.toFixed(2)}` : '套餐内免费' }}
            </span>
          </template>
        </el-table-column>
      </el-table>

      <!-- ⭐ 升级：底部分页引擎栏 -->
      <div class="pagination-wrapper" v-if="records.length > 0">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="records.length"
          layout="total, prev, pager, next"
          background
          size="small"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '../stores/user'
import { getRecords } from '../api/user'

const userStore = useUserStore()
const loading = ref(false)
const records = ref([])

// ⭐ 新增：分页状态变量 (每页 15 行)
const currentPage = ref(1)
const pageSize = ref(15)

const fetchRecords = async () => {
  loading.value = true
  try {
    const res = await getRecords({ phone: userStore.phone })
    records.value = res.data || []
    currentPage.value = 1 // 刷新数据时，重置回到第一页
  } catch (error) { console.error(error) } finally { loading.value = false }
}

// ⭐ 新增：计算属性，截取当前页要展示的 15 条流水
const pagedRecords = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return records.value.slice(start, end)
})

// ⭐ 新增：跨页序号累加计算方法 (保证翻到第二页时序号从 16 开始)
const indexMethod = (index) => {
  return (currentPage.value - 1) * pageSize.value + index + 1
}

// 前端汇总计算
const summaryCost = computed(() => {
  const sum = { call: 0, net: 0, sms: 0 }
  records.value.forEach(r => {
    if (r.type === '通话') sum.call += r.cost
    else if (r.type === '上网') sum.net += r.cost
    else if (r.type === '发短信') sum.sms += r.cost
  })
  return sum
})

const getTypeTag = (type) => {
  const map = { '通话': '', '上网': 'warning', '发短信': 'success' }
  return map[type] || 'info'
}

onMounted(() => { fetchRecords() })
</script>

<style scoped>
.summary-card {
  background: #fff; padding: 20px; border-radius: 4px;
  display: flex; flex-direction: column;
}
.summary-card .label { font-size: 13px; color: var(--text-secondary); margin-bottom: 8px; }
.summary-card .value { font-size: 22px; font-weight: 700; color: var(--text-primary); }
.card-title { font-weight: 600; color: var(--text-primary); }

/* ⭐ 新增：底部分页包装器样式 */
.pagination-wrapper {
  margin-top: 18px;
  display: flex;
  justify-content: flex-end;
}

/* ⭐ 新增：高仿真悬浮刷新按钮动画 */
.hover-refresh-btn {
  display: inline-flex;
  align-items: center;
  padding: 6px 10px;
  border-radius: 20px;
  transition: all 0.3s ease;
  overflow: hidden;
}

.refresh-icon {
  fill: currentColor;
  transition: transform 0.6s cubic-bezier(0.16, 1, 0.3, 1);
}

.hover-refresh-btn .btn-text {
  max-width: 0;
  opacity: 0;
  overflow: hidden;
  white-space: nowrap;
  display: inline-block;
  transition: max-width 0.4s cubic-bezier(0.16, 1, 0.3, 1), opacity 0.3s ease;
}

/* 悬浮状态 */
.hover-refresh-btn:hover .btn-text {
  max-width: 60px; /* 滑动展开 */
  opacity: 1;
  margin-left: 6px;
}

.hover-refresh-btn:hover .refresh-icon {
  transform: rotate(180deg); /* 刷新图标优雅旋转180度 */
}
</style>