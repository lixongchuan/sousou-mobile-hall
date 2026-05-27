<template>
  <div class="auth-page">
    <div class="auth-card">
      <!-- 左侧品牌 -->
      <div class="brand-side">
        <svg class="brand-logo" viewBox="0 0 200 120" xmlns="http://www.w3.org/2000/svg">
          <defs>
            <linearGradient id="rG1" x1="0%" y1="0%" x2="100%" y2="0%">
              <stop offset="0%" stop-color="#00FFD1"/>
              <stop offset="45%" stop-color="#FFB800"/>
              <stop offset="100%" stop-color="#FF3D00" stop-opacity="0.35"/>
            </linearGradient>
            <linearGradient id="rG2" x1="0%" y1="0%" x2="100%" y2="0%">
              <stop offset="0%" stop-color="#00FFD1" stop-opacity="0.5"/>
              <stop offset="45%" stop-color="#FFB800" stop-opacity="0.35"/>
              <stop offset="100%" stop-color="#FF3D00" stop-opacity="0.08"/>
            </linearGradient>
            <mask id="rM">
              <linearGradient id="rMG" x1="0" y1="0" x2="1" y2="0">
                <stop offset="0" stop-color="white"/><stop offset="0.6" stop-color="white"/>
                <stop offset="1" stop-color="black"/>
              </linearGradient>
              <rect width="200" height="120" fill="url(#rMG)"/>
            </mask>
          </defs>
          <path d="M 28,42 C 50,92 105,8 125,52 C 145,96 175,12 188,40"
                stroke="url(#rG1)" stroke-width="3.2" fill="none" stroke-linecap="round" mask="url(#rM)"/>
          <path d="M 28,64 C 50,16 105,100 125,58 C 145,16 175,98 188,72"
                stroke="url(#rG2)" stroke-width="2.2" fill="none" stroke-linecap="round" mask="url(#rM)"/>
          <circle cx="28" cy="53" r="3.8" fill="#fff" opacity="0.9"/>
          <circle cx="28" cy="53" r="1.6" fill="#0D9488"/>
        </svg>
        <h2>嗖嗖移动</h2>
        <p>开通您的专属号码</p>
      </div>

      <!-- 右侧表单 -->
      <div class="form-side">
        <h3>新号开户</h3>
        <el-form ref="registerFormRef" :model="registerForm" :rules="rules" size="large" label-position="top">
          <el-form-item label="选择套餐" prop="type">
            <el-radio-group v-model="registerForm.type" class="radio-col">
              <el-radio label="TALK" border>话唠套餐 - 58元/月</el-radio>
              <el-radio label="NET" border>网虫套餐 - 68元/月</el-radio>
              <el-radio label="SUPER" border>超人套餐 - 78元/月</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="设置密码" prop="password">
            <el-input v-model="registerForm.password" type="password" placeholder="至少6位" show-password />
          </el-form-item>
          <el-form-item label="确认密码" prop="checkPassword">
            <el-input v-model="registerForm.checkPassword" type="password" placeholder="请再次输入密码" show-password />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" style="width:100%;height:46px;font-weight:700;font-size:15px;border-radius:10px;margin-top:8px"
                       @click="handleRegister" :loading="loading">
              同意协议并开户
            </el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { register } from '../api/user'
import { ElMessageBox, ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const registerFormRef = ref(null)
const loading = ref(false)

const registerForm = reactive({
  type: route.query.type || '',
  password: '',
  checkPassword: ''
})

const validateCheckPassword = (rule, value, callback) => {
  if (value !== registerForm.password) callback(new Error('两次输入的密码不一致'))
  else callback()
}

const rules = reactive({
  type: [{ required: true, message: '请选择套餐类型', trigger: 'change' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  checkPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: validateCheckPassword, trigger: 'blur' }
  ]
})

const handleRegister = () => {
  registerFormRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      const res = await register({ password: registerForm.password, type: registerForm.type })
      const cardNumber = res.data.cardNumber
      ElMessageBox.alert(
        `<div style="text-align:center;padding:8px 0">
          <p style="font-size:14px;color:#64748B;margin-bottom:16px">请牢记您的手机号</p>
          <p style="font-size:32px;font-weight:800;letter-spacing:3px;color:#0F172A;margin:0">${cardNumber}</p>
          <p style="font-size:12px;color:#DC2626;margin-top:14px;font-weight:500">此号码为系统随机分配，丢失将无法登录</p>
        </div>`,
        '开户成功',
        {
          dangerouslyUseHTMLString: true,
          confirmButtonText: '我已记下，去登录',
          showClose: false,
          closeOnClickModal: false,
          closeOnPressEscape: false,
          type: 'success',
          callback: () => { router.push('/login') }
        }
      )
    } catch (error) {
      console.error('注册失败', error)
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

/* 品牌侧 */
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

/* 表单侧 */
.form-side { flex: 1.3; padding: 52px 44px; }
.form-side h3 { font-size: 24px; font-weight: 800; color: var(--text-primary); margin-bottom: 32px; }

.radio-col { display: flex; flex-direction: column; gap: 10px; width: 100%; }
:deep(.radio-col .el-radio) {
  margin-right: 0; width: 100%; padding: 14px 18px;
  border-radius: 10px !important;
}
:deep(.radio-col .el-radio.is-checked) {
  border-color: var(--primary-color) !important;
  background: var(--primary-light) !important;
}

@media(max-width:720px) {
  .auth-card { flex-direction: column; width: 100%; max-width: 420px; }
  .brand-side { padding: 36px 24px; }
  .brand-logo { width: 100px; }
  .form-side { padding: 32px 24px; }
}
</style>