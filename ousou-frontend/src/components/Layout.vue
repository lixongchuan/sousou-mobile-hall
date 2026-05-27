<template>
  <el-container style="height: 100vh">
    <el-aside width="220px" style="background-color: #001529">
      <div class="logo-container">
        <svg class="logo-icon-svg" viewBox="0 0 120 120" width="40" height="40" xmlns="http://www.w3.org/2000/svg">
          <defs>
            <linearGradient id="iG1" x1="0%" y1="0%" x2="100%" y2="0%">
              <stop offset="0%" stop-color="#00FFD1"/>
              <stop offset="40%" stop-color="#FFB800"/>
              <stop offset="85%" stop-color="#FF3D00" stop-opacity=".3"/>
              <stop offset="100%" stop-color="#FF3D00" stop-opacity="0"/>
            </linearGradient>
            <linearGradient id="iG2" x1="0%" y1="0%" x2="100%" y2="0%">
              <stop offset="0%" stop-color="#00FFD1" stop-opacity=".5"/>
              <stop offset="40%" stop-color="#FFB800" stop-opacity=".4"/>
              <stop offset="85%" stop-color="#FF3D00" stop-opacity=".1"/>
              <stop offset="100%" stop-color="#FF3D00" stop-opacity="0"/>
            </linearGradient>
            <filter id="iGlow" x="-30%" y="-30%" width="160%" height="160%">
              <feGaussianBlur stdDeviation="2.5" result="b"/>
              <feMerge><feMergeNode in="b"/><feMergeNode in="SourceGraphic"/></feMerge>
            </filter>
          </defs>
          <path d="M 18,48 C 35,90 70,8 85,52 C 100,96 115,12 108,42"
                stroke="url(#iG1)" stroke-width="4" fill="none"
                stroke-linecap="round" filter="url(#iGlow)"/>
          <path d="M 18,62 C 35,22 70,100 85,58 C 100,18 115,96 108,70"
                stroke="url(#iG2)" stroke-width="2.8" fill="none"
                stroke-linecap="round"/>
          <circle cx="18" cy="55" r="3.5" fill="#00FFD1" opacity=".9"/>
          <circle cx="18" cy="55" r="1.5" fill="#fff"/>
        </svg>
        <h2>嗖嗖营业厅</h2>
      </div>
      <el-menu
        :default-active="$route.path"
        router
        background-color="#001529"
        text-color="rgba(255,255,255,0.65)"
        active-text-color="#FFFFFF"
      >
        <el-menu-item index="/user/dashboard">
          <el-icon><Odometer /></el-icon>
          <span>业务大盘</span>
        </el-menu-item>
        <el-menu-item index="/user/consume">
          <el-icon><Phone /></el-icon>
          <span>模拟消费</span>
        </el-menu-item>
        <el-menu-item index="/user/recharge">
          <el-icon><Wallet /></el-icon>
          <span>话费充值</span>
        </el-menu-item>
        <el-menu-item index="/user/change-package">
          <el-icon><Switch /></el-icon>
          <span>套餐变更</span>
        </el-menu-item>
        <el-menu-item index="/user/records">
          <el-icon><Document /></el-icon>
          <span>消费详单</span>
        </el-menu-item>
        <el-menu-item index="/user/settings">
          <el-icon><Setting /></el-icon>
          <span>系统设置</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="app-header">
        <div class="breadcrumb-area">
          <span class="current-page">{{ $route.meta.title }}</span>
        </div>
        <div class="user-info">
          <el-tag type="success" effect="plain" size="large" style="border-radius: 20px;">{{ userStore.phone }}</el-tag>
          <el-tag color="#FF6A00" style="color: white; border:none; border-radius: 20px; margin-left: 15px;">{{ userStore.balance.toFixed(2) }} 元</el-tag>
          <el-button type="info" text style="margin-left: 20px;" @click="handleLogout">
            <el-icon><SwitchButton /></el-icon> 退出
          </el-button>
        </div>
      </el-header>

      <el-main class="app-main">
        <router-view v-if="userStore.isLogin" />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { useUserStore } from '../stores/user'
import { useRouter } from 'vue-router'
import { Odometer, Phone, Wallet, Switch, Document, Setting, SwitchButton } from '@element-plus/icons-vue'
import { ElMessageBox } from 'element-plus'

const userStore = useUserStore()
const router = useRouter()

const handleLogout = () => {
  ElMessageBox.confirm('确定要退出当前账号吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    userStore.clearUserInfo()
    router.push('/login')
  }).catch(() => {})
}
</script>

<style scoped>
.logo-container {
  height: 64px;
  display: flex;
  align-items: center;
  padding: 0 20px;
  background-color: #001529;
  border-bottom: 1px solid rgba(255,255,255,0.1);
}
.logo-icon-svg {
  margin-right: 12px;
  flex-shrink: 0;
}
.logo-container h2 {
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  white-space: nowrap;
}

.app-header {
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  z-index: 9;
}
.breadcrumb-area {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}
.user-info {
  display: flex;
  align-items: center;
}
.el-menu {
  border-right: none;
}
.app-main {
  background-color: var(--bg-color);
  padding: 24px;
}
/* 侧边栏菜单高亮样式覆盖 */
.el-menu-item.is-active {
  background-color: #0F3D6B !important;
}
</style>