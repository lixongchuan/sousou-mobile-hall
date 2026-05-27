import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {
  // ==================== 1. State (全局状态) ====================
  const phone = ref(localStorage.getItem('sousou_phone') || '')
  
  const savedBalance = localStorage.getItem('sousou_balance')
  const balance = ref(savedBalance && savedBalance !== 'null' ? parseFloat(savedBalance) : 0)
  
  const packageType = ref(localStorage.getItem('sousou_packageType') || '')

  // 套餐内三大核心余量状态 (初始化时从缓存读取，防止刷新丢失)
  const remainingFlow = ref(parseInt(localStorage.getItem('sousou_remainingFlow')) || 0)     // MB
  const remainingTalkTime = ref(parseInt(localStorage.getItem('sousou_remainingTalkTime')) || 0) // 分钟
  const remainingSms = ref(parseInt(localStorage.getItem('sousou_remainingSms')) || 0)       // 条

  // ==================== 2. Getters (计算属性) ====================
  const isLogin = computed(() => !!phone.value)

  // ==================== 3. Actions (业务方法) ====================
  
  /**
   * 保存登录/注册后的用户信息及初始套餐额度
   */
  function setUserInfo(data) {
    phone.value = data.cardNumber
    balance.value = data.balance
    
    const pack = data.currentPackage
    if (pack) {
      packageType.value = pack.packageType
      // 多态解析：安全提取具体套餐的额度，不存在的设为 0
      remainingFlow.value = pack.flow !== undefined ? pack.flow : 0
      remainingTalkTime.value = pack.talkTime !== undefined ? pack.talkTime : 0
      remainingSms.value = pack.smsCount !== undefined ? pack.smsCount : 0
    } else {
      packageType.value = ''
      remainingFlow.value = 0
      remainingTalkTime.value = 0
      remainingSms.value = 0
    }
    
    // 写入本地持久化缓存
    saveToLocalStorage()
  }

  /**
   * 充值或消费后更新余额
   */
  function updateBalance(newBalance) {
    balance.value = newBalance
    localStorage.setItem('sousou_balance', String(newBalance))
  }

  /**
   * 订购“流量加油包”成功后，前端直接累加流量余量
   * @param {number} mbAmount - 充入的流量(MB)，如 1024, 3072, 5120
   * @param {number} cost - 扣除的费用
   */
  function purchaseDataPack(mbAmount, cost) {
    balance.value -= cost // 扣减话费
    remainingFlow.value += mbAmount // 累加流量
    
    localStorage.setItem('sousou_balance', String(balance.value))
    localStorage.setItem('sousou_remainingFlow', String(remainingFlow.value))
  }

  /**
   * 模拟消费或查询后，手动同步最新的套餐余量
   */
  function syncQuotas(flow, talkTime, sms) {
    remainingFlow.value = flow
    remainingTalkTime.value = talkTime
    remainingSms.value = sms
    
    localStorage.setItem('sousou_remainingFlow', String(flow))
    localStorage.setItem('sousou_remainingTalkTime', String(talkTime))
    localStorage.setItem('sousou_remainingSms', String(sms))
  }

  /**
   * 变更套餐后调用
   */
  function updatePackage(newType, newPackData) {
    packageType.value = newType
    // 变更后，重置余量为新套餐的初始额度
    remainingFlow.value = newPackData.flow || 0
    remainingTalkTime.value = newPackData.talkTime || 0
    remainingSms.value = newPackData.smsCount || 0
    
    saveToLocalStorage()
  }

  /**
   * 退出登录 / 办理退网时清空一切
   */
  function clearUserInfo() {
    phone.value = ''
    balance.value = 0
    packageType.value = ''
    remainingFlow.value = 0
    remainingTalkTime.value = 0
    remainingSms.value = 0
    
    localStorage.clear() // 一键清空所有本地缓存
  }

  // 内部辅助方法：统一保存到本地
  function saveToLocalStorage() {
    localStorage.setItem('sousou_phone', phone.value)
    localStorage.setItem('sousou_balance', String(balance.value))
    localStorage.setItem('sousou_packageType', packageType.value)
    localStorage.setItem('sousou_remainingFlow', String(remainingFlow.value))
    localStorage.setItem('sousou_remainingTalkTime', String(remainingTalkTime.value))
    localStorage.setItem('sousou_remainingSms', String(remainingSms.value))
  }

  return {
    phone,
    balance,
    packageType,
    remainingFlow,
    remainingTalkTime,
    remainingSms,
    isLogin,
    setUserInfo,
    updateBalance,
    purchaseDataPack,
    syncQuotas,
    updatePackage,
    clearUserInfo
  }
})