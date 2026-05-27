<template>
  <div class="settings-container">
    <el-row :gutter="20">
      <!-- 模拟月结 -->
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <span>系统时间模拟</span>
          </template>
          <el-alert 
            title="此操作将模拟系统进入下一个月，所有用户套餐额度将被重置并扣除月租，历史详单将被清空。" 
            type="info" 
            :closable="false" 
            style="margin-bottom: 20px;"
          />
          <el-button type="warning" @click="handleNextMonth" :loading="monthLoading">执行模拟月结</el-button>
        </el-card>
      </el-col>

      <!-- 退网注销 -->
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <span style="color: #f56c6c; font-weight: bold;">危险操作区</span>
          </template>
          <el-alert 
            title="退网注销将永久删除您的账号所有数据，退还的话费将无法继续使用。此操作不可逆！" 
            type="error" 
            :closable="false" 
            style="margin-bottom: 20px;"
          />
          <el-button type="danger" @click="handleCancel">办理退网注销</el-button>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useUserStore } from '../stores/user'
import { cancelAccount, nextMonth, getQuota } from '../api/user'
import { ElMessageBox, ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'

const userStore = useUserStore()
const router = useRouter()
const monthLoading = ref(false)

// 模拟月结
const handleNextMonth = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要执行系统月结吗？这将影响系统内所有用户的数据状态。',
      '月结确认',
      { type: 'warning' }
    )
    
    monthLoading.value = true
    try {
      const res = await nextMonth()
      ElMessage.success(res.msg)
      
      // 严谨点：月结后刷新当前登录用户的状态（可能扣费停机）
      const quotaRes = await getQuota({ phone: userStore.phone })
      userStore.updateBalance(quotaRes.data.balance)
    } catch (error) {
      console.error('月结失败', error)
    } finally {
      monthLoading.value = false
    }
  } catch {}
}

// 退网注销 (极高危险：需输入手机号验证)
const handleCancel = () => {
  ElMessageBox.prompt(
    '此操作将永久注销您的账号，退还剩余话费。请输入您的手机号以确认操作：',
    '极其危险的确认',
    {
      confirmButtonText: '确认注销',
      cancelButtonText: '取消',
      type: 'error',
      inputPattern: new RegExp(`^${userStore.phone}$`),
      inputErrorMessage: '输入的手机号与当前登录账号不一致！',
      inputPlaceholder: '请输入当前手机号'
    }
  ).then(async () => {
    try {
      const res = await cancelAccount({ phone: userStore.phone })
      ElMessage.success(res.msg)
      
      // 注销成功后，清理前端状态并强制跳回首页
      userStore.clearUserInfo()
      router.push('/')
    } catch (error) {
      console.error('注销失败', error)
    }
  }).catch(() => {})
}
</script>