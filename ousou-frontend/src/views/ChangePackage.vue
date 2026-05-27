<template>
  <div class="change-pkg-page">
    <div class="page-header">
      <h2>套餐在线变更</h2>
      <p>变更套餐将立即扣除新套餐月租并重置额度</p>
    </div>

    <!-- 当前状态 -->
    <div class="current-panel">
      <div class="cp-row">
        <div class="cp-block">
          <span class="cp-label">当前套餐</span>
          <span class="cp-value">{{ currentPackageName || '加载中...' }}</span>
        </div>
        <div class="cp-block">
          <span class="cp-label">账户余额</span>
          <span class="cp-value">¥ {{ userStore.balance.toFixed(2) }}</span>
        </div>
      </div>
    </div>

    <!-- 选择新套餐 -->
    <div class="select-panel">
      <h3>选择目标套餐</h3>
      <div class="pkg-options">
        <div
          v-for="pkg in packageList"
          :key="pkg.code"
          class="pkg-option"
          :class="{
            active: form.type === pkg.code,
            disabled: pkg.code === currentPackageCode,
            current: pkg.code === currentPackageCode
          }"
          @click="pkg.code !== currentPackageCode && (form.type = pkg.code)"
        >
          <div class="pkg-option-head">
            <div class="pkg-radio" :class="{ checked: form.type === pkg.code, current: pkg.code === currentPackageCode }">
              <div class="pkg-radio-dot" v-if="form.type === pkg.code || pkg.code === currentPackageCode"></div>
            </div>
            <span class="pkg-option-name">{{ pkg.name }}</span>
            <span class="pkg-current-tag" v-if="pkg.code === currentPackageCode">当前</span>
          </div>
          <div class="pkg-option-price">¥{{ pkg.price }}<span>/月</span></div>
          <ul class="pkg-option-detail">
            <li v-for="(d, i) in pkg.details" :key="i">{{ d }}</li>
          </ul>
        </div>
      </div>

      <div class="action-row">
        <button
          class="submit-btn"
          :class="{ disabled: !form.type || form.type === currentPackageCode || loading }"
          :disabled="!form.type || form.type === currentPackageCode || loading"
          @click="handleChange"
        >
          <span v-if="!loading">确认变更</span>
          <span v-else>处理中...</span>
        </button>
        <p class="action-hint" v-if="form.type && form.type !== currentPackageCode">
          变更后将立即扣除 <b>¥{{ targetPrice }}</b> 月租费用
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useUserStore } from '../stores/user'
import { changePackage, getQuota } from '../api/user'
import { ElMessageBox, ElMessage } from 'element-plus'

const userStore = useUserStore()
const loading = ref(false)
const currentPackageCode = ref('') // 从后端实时获取的当前套餐代码
const form = reactive({ type: '' })

/* ---- 套餐数据定义 ---- */
// 后端 packageName → 前端 code 的映射
const backendToCode = {
  TalkativePackage: 'TALK',
  NetizenPackage: 'NET',
  SupermanPackage: 'SUPER'
}
const codeToName = {
  TALK: '话唠套餐',
  NET: '网虫套餐',
  SUPER: '超人套餐'
}

const packageList = [
  {
    code: 'TALK',
    name: '话唠套餐',
    price: 58,
    details: ['500分钟通话', '30条短信', '超出通话0.2元/分', '超出短信0.1元/条']
  },
  {
    code: 'NET',
    name: '网虫套餐',
    price: 68,
    details: ['3GB全国流量', '超出流量0.1元/MB']
  },
  {
    code: 'SUPER',
    name: '超人套餐',
    price: 78,
    details: ['200分钟通话', '50条短信', '1GB全国流量', '超出按各标准计费']
  }
]

const currentPackageName = computed(() => codeToName[currentPackageCode.value] || '未知')
const targetPrice = computed(() => {
  const pkg = packageList.find(p => p.code === form.type)
  return pkg ? pkg.price : 0
})

/* ---- 挂载时主动获取当前套餐 ---- */
onMounted(async () => {
  try {
    const res = await getQuota({ phone: userStore.phone })
    const packageName = res.data?.packageName
    if (packageName && backendToCode[packageName]) {
      currentPackageCode.value = backendToCode[packageName]
      // 同步回 store，确保其他页面也能拿到
      if (userStore.updatePackageType) {
        userStore.updatePackageType(packageName)
      }
    }
  } catch (e) {
    // 降级：尝试从 store 读取
    if (userStore.packageType && backendToCode[userStore.packageType]) {
      currentPackageCode.value = backendToCode[userStore.packageType]
    }
  }
})

/* ---- 变更逻辑 ---- */
const handleChange = () => {
  if (!form.type || form.type === currentPackageCode.value) return

  const targetName = codeToName[form.type]

  ElMessageBox.confirm(
    `确定要将套餐变更为【${targetName}】吗？系统将立即扣除 ¥${targetPrice.value} 月租费用，原套餐剩余额度将清零。`,
    '变更确认',
    {
      confirmButtonText: '确定变更',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    loading.value = true
    try {
      const res = await changePackage({ phone: userStore.phone, type: form.type })
      ElMessage.success(res.msg || '套餐变更成功')

      // 重新拉取最新状态
      const quotaRes = await getQuota({ phone: userStore.phone })
      userStore.updateBalance(quotaRes.data.balance)
      if (quotaRes.data.packageName) {
        const code = backendToCode[quotaRes.data.packageName]
        currentPackageCode.value = code || ''
        if (userStore.updatePackageType) {
          userStore.updatePackageType(quotaRes.data.packageName)
        }
      }
      form.type = ''
    } catch (error) {
      console.error('变更失败', error)
    } finally {
      loading.value = false
    }
  }).catch(() => {})
}
</script>

<style scoped>
.change-pkg-page { max-width: 760px; margin: 0 auto; }

.page-header { margin-bottom: 24px; }
.page-header h2 { font-size: 22px; font-weight: 800; color: var(--text-primary); margin-bottom: 4px; }
.page-header p { font-size: 14px; color: var(--text-secondary); }

/* 当前状态 */
.current-panel {
  background: #fff; border: 1px solid var(--border-color); border-radius: 14px;
  padding: 20px 24px; margin-bottom: 20px;
}
.cp-row { display: flex; gap: 40px; }
.cp-block { display: flex; flex-direction: column; gap: 2px; }
.cp-label { font-size: 12px; color: var(--text-secondary); }
.cp-value { font-size: 18px; font-weight: 700; color: var(--text-primary); }

/* 选择面板 */
.select-panel {
  background: #fff; border: 1px solid var(--border-color); border-radius: 14px;
  padding: 24px;
}
.select-panel h3 { font-size: 16px; font-weight: 700; color: var(--text-primary); margin-bottom: 18px; }

.pkg-options { display: grid; grid-template-columns: repeat(3, 1fr); gap: 14px; }

.pkg-option {
  border: 1px solid var(--border-color); border-radius: 12px;
  padding: 20px 16px; cursor: pointer; transition: all .25s;
  position: relative;
}
.pkg-option:hover:not(.disabled) { border-color: #0D9488; box-shadow: 0 2px 12px rgba(13,148,136,0.08); }
.pkg-option.active { border-color: #0D9488; background: #F8FFFE; }
.pkg-option.disabled { cursor: default; opacity: .55; }
.pkg-option.current { opacity: 1; border-color: #CBD5E1; background: #F8FAFC; }

.pkg-option-head { display: flex; align-items: center; gap: 8px; margin-bottom: 10px; }
.pkg-radio {
  width: 16px; height: 16px; border-radius: 50%;
  border: 1.5px solid #CBD5E1; flex-shrink: 0;
  display: flex; align-items: center; justify-content: center;
  transition: border-color .2s;
}
.pkg-radio.checked { border-color: #0D9488; }
.pkg-radio.current { border-color: #94A3B8; }
.pkg-radio-dot {
  width: 8px; height: 8px; border-radius: 50%;
  background: #0D9488;
}
.pkg-radio.current .pkg-radio-dot { background: #94A3B8; }

.pkg-option-name { font-size: 15px; font-weight: 600; color: var(--text-primary); }
.pkg-current-tag {
  font-size: 10px; font-weight: 600; color: #64748B;
  background: #F1F5F9; padding: 1px 8px; border-radius: 4px;
  margin-left: auto;
}

.pkg-option-price {
  font-size: 28px; font-weight: 900; color: var(--text-primary);
  margin-bottom: 12px; line-height: 1;
}
.pkg-option-price span { font-size: 13px; font-weight: 400; color: var(--text-secondary); }

.pkg-option-detail { list-style: none; }
.pkg-option-detail li {
  font-size: 13px; color: var(--text-secondary); line-height: 1.8;
  padding-left: 12px; position: relative;
}
.pkg-option-detail li::before {
  content: ''; position: absolute; left: 0; top: 50%;
  width: 4px; height: 4px; border-radius: 50%;
  background: #CBD5E1; transform: translateY(-50%);
}

/* 操作区 */
.action-row { margin-top: 24px; display: flex; align-items: center; gap: 16px; }
.submit-btn {
  height: 44px; padding: 0 32px; border-radius: 10px; border: none;
  background: var(--primary-color); color: #fff;
  font-size: 15px; font-weight: 700; cursor: pointer;
  transition: background .25s, opacity .25s;
}
.submit-btn:hover:not(.disabled) { background: var(--primary-hover); }
.submit-btn.disabled { opacity: .45; cursor: not-allowed; }
.action-hint { font-size: 13px; color: var(--text-secondary); margin: 0; }
.action-hint b { color: #E8590C; font-weight: 700; }

@media (max-width: 640px) {
  .pkg-options { grid-template-columns: 1fr; }
  .cp-row { flex-direction: column; gap: 12px; }
}
</style>