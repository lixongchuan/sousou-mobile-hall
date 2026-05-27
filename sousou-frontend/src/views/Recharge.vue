<template>
  <div class="recharge-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>话费充值</span>
          <el-tag type="danger">当前余额: {{ userStore.balance.toFixed(2) }} 元</el-tag>
        </div>
      </template>

      <el-form ref="rechargeFormRef" :model="rechargeForm" :rules="rules" label-width="100px" style="max-width: 500px;">
        <el-form-item label="充值金额" prop="amount">
          <el-input-number 
            v-model="rechargeForm.amount" 
            :precision="2" 
            :step="10" 
            :min="0.01" 
            :max="10000"
            controls-position="right"
            placeholder="请输入充值金额"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="success" @click="handleRecharge" :loading="loading">确认充值</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useUserStore } from '../stores/user'
import { recharge } from '../api/user'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const rechargeFormRef = ref(null)
const loading = ref(false)

const rechargeForm = reactive({
  amount: null
})

const rules = reactive({
  amount: [
    { required: true, message: '请输入充值金额', trigger: 'blur' },
    { type: 'number', message: '金额必须为数字', trigger: 'blur' }
  ]
})

const handleRecharge = () => {
  rechargeFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    loading.value = true
    try {
      const res = await recharge({ 
        phone: userStore.phone, 
        amount: rechargeForm.amount 
      })
      
      ElMessage.success(res.msg)
      
      // 严谨点：充值成功后，提取返回的新余额更新全局状态
      // 正则提取数字，兼容 "当前最新余额为：132.0元。" 这样的字符串
      const match = res.msg.match(/([\d.]+)元/)
      if (match && match[1]) {
        userStore.updateBalance(parseFloat(match[1]))
      } else {
        // 如果正则没提取到，主动查一次接口保底
        const quotaRes = await getQuota({ phone: userStore.phone })
        userStore.updateBalance(quotaRes.data.balance)
      }
      
      rechargeForm.amount = null // 清空输入框
    } catch (error) {
      console.error('充值失败', error)
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}
</style>