<template>
  <div class="auth-page">
    <div class="auth-card">
      <!-- 左侧品牌 -->
      <div class="brand-side">
        <svg class="brand-logo" viewBox="0 0 200 120" xmlns="http://www.w3.org/2000/svg">
          <defs>
            <linearGradient id="lG1" x1="0%" y1="0%" x2="100%" y2="0%">
              <stop offset="0%" stop-color="#00FFD1"/>
              <stop offset="45%" stop-color="#FFB800"/>
              <stop offset="100%" stop-color="#FF3D00" stop-opacity="0.35"/>
            </linearGradient>
            <linearGradient id="lG2" x1="0%" y1="0%" x2="100%" y2="0%">
              <stop offset="0%" stop-color="#00FFD1" stop-opacity="0.5"/>
              <stop offset="45%" stop-color="#FFB800" stop-opacity="0.35"/>
              <stop offset="100%" stop-color="#FF3D00" stop-opacity="0.08"/>
            </linearGradient>
            <mask id="lM">
              <linearGradient id="lMG" x1="0" y1="0" x2="1" y2="0">
                <stop offset="0" stop-color="white"/><stop offset="0.6" stop-color="white"/>
                <stop offset="1" stop-color="black"/>
              </linearGradient>
              <rect width="200" height="120" fill="url(#lMG)"/>
            </mask>
          </defs>
          <path d="M 28,42 C 50,92 105,8 125,52 C 145,96 175,12 188,40"
                stroke="url(#lG1)" stroke-width="3.2" fill="none" stroke-linecap="round" mask="url(#lM)"/>
          <path d="M 28,64 C 50,16 105,100 125,58 C 145,16 175,98 188,72"
                stroke="url(#lG2)" stroke-width="2.2" fill="none" stroke-linecap="round" mask="url(#lM)"/>
          <circle cx="28" cy="53" r="3.8" fill="#fff" opacity="0.9"/>
          <circle cx="28" cy="53" r="1.6" fill="#0D9488"/>
        </svg>
        <h2>嗖嗖移动</h2>
        <p>欢迎登录Web营业厅</p>
      </div>

      <!-- 右侧表单 -->
      <div class="form-side">
        <h3>账号登录</h3>
        <el-form ref="loginFormRef" :model="loginForm" :rules="rules" size="large">
          <el-form-item prop="phone">
            <el-input v-model="loginForm.phone" placeholder="请输入手机号" prefix-icon="Iphone" />
          </el-form-item>
          <el-form-item prop="password">
            <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" prefix-icon="Lock" show-password />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" style="width:100%;height:46px;font-weight:700;font-size:15px;border-radius:10px"
                       @click="handleLogin" :loading="loading">
              登 录
            </el-button>
          </el-form-item>
          <div class="bottom-link">
            还没有账号？<el-link type="primary" @click="$router.push('/register')">立即开户</el-link>
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '../stores/user'
import { login } from '../api/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const loginFormRef = ref(null)
const loading = ref(false)
const loginForm = reactive({ phone: '', password: '' })
const rules = reactive({
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ]
})

const handleLogin = () => {
  loginFormRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      const res = await login(loginForm)
      userStore.setUserInfo(res.data)
      ElMessage.success('登录成功！')
      const redirect = route.query.redirect || '/user/dashboard'
      router.push(redirect)
    } catch (error) {
      console.error('登录失败', error)
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.auth-page {
  min-height: 100vh; display: flex; align-items: center; justify-content: center;
  padding: 24px; background: var(--bg-color);
}
.auth-card {
  display: flex; width: 840px; background: #fff;
  border-radius: 18px; box-shadow: var(--shadow-lg); overflow: hidden;
  border: 1px solid var(--border-light);
}

.brand-side {
  flex: 1; background: #0F172A;
  padding: 52px 36px; display: flex; flex-direction: column;
  align-items: center; justify-content: center; text-align: center;
}
.brand-logo { width: 140px; height: auto; margin-bottom: 24px; }
.brand-side h2 {
  font-size: 24px; font-weight: 800; color: #fff; margin-bottom: 6px;
}
.brand-side p { font-size: 14px; color: rgba(255,255,255,0.55); }

.form-side { flex: 1.3; padding: 52px 44px; }
.form-side h3 { font-size: 24px; font-weight: 800; color: var(--text-primary); margin-bottom: 32px; }
.bottom-link {
  text-align: center; margin-top: 12px;
  font-size: 14px; color: var(--text-secondary);
}

@media(max-width:720px) {
  .auth-card { flex-direction: column; width: 100%; max-width: 420px; }
  .brand-side { padding: 36px 24px; }
  .brand-logo { width: 100px; }
  .form-side { padding: 32px 24px; }
}
</style>