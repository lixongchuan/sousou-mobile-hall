<template>
  <div class="home">
    <!-- 导航 -->
    <header class="nav">
      <div class="nav-inner">
        <div class="logo" @click="$router.push('/')">
          <svg class="logo-mark" viewBox="0 0 28 28" fill="none">
            <defs>
              <linearGradient id="navG" x1="0%" y1="0%" x2="100%" y2="0%">
                <stop offset="0%" stop-color="#00C9A7"/>
                <stop offset="55%" stop-color="#E8A000"/>
                <stop offset="100%" stop-color="#D94420"/>
              </linearGradient>
            </defs>
            <path d="M4,9 C7,20 16,3 19,12 C22,21 26,4 25,9"
                  stroke="url(#navG)" stroke-width="2.4" fill="none" stroke-linecap="round"/>
            <path d="M4,15 C7,5 16,24 19,13 C22,3 26,22 25,17"
                  stroke="url(#navG)" stroke-width="1.5" fill="none" stroke-linecap="round" opacity="0.35"/>
            <circle cx="4" cy="12" r="1.8" fill="#0D9488"/>
          </svg>
          <span class="logo-name">嗖嗖移动</span>
        </div>
        <div class="nav-btns">
          <el-button round @click="$router.push('/login')">登录</el-button>
          <el-button type="primary" round @click="$router.push('/register')">开户注册</el-button>
        </div>
      </div>
    </header>

    <!-- 首屏 -->
    <section class="hero">
      <div class="hero-inner">
        <div class="hero-text">
          <div class="hero-eyebrow">SOUSOU MOBILE</div>
          <h1>极速网络，尽在掌握</h1>
          <p>超低资费，海量流量，无缝连接的数字生活即刻开启。</p>
          <el-button type="primary" size="large" round @click="scrollToPackages">
            查看套餐详情
          </el-button>
        </div>
        <div class="hero-visual">
          <svg viewBox="0 0 340 200" xmlns="http://www.w3.org/2000/svg">
            <defs>
              <linearGradient id="hG1" x1="0%" y1="0%" x2="100%" y2="0%">
                <stop offset="0%" stop-color="#00C9A7"/>
                <stop offset="50%" stop-color="#E8A000"/>
                <stop offset="100%" stop-color="#D94420" stop-opacity="0.3"/>
              </linearGradient>
              <linearGradient id="hG2" x1="0%" y1="0%" x2="100%" y2="0%">
                <stop offset="0%" stop-color="#00C9A7" stop-opacity="0.4"/>
                <stop offset="50%" stop-color="#E8A000" stop-opacity="0.3"/>
                <stop offset="100%" stop-color="#D94420" stop-opacity="0.08"/>
              </linearGradient>
              <mask id="hM">
                <linearGradient id="hMG" x1="0" y1="0" x2="1" y2="0">
                  <stop offset="0" stop-color="white"/>
                  <stop offset="0.6" stop-color="white"/>
                  <stop offset="1" stop-color="black"/>
                </linearGradient>
                <rect width="340" height="200" fill="url(#hMG)"/>
              </mask>
            </defs>
            <path d="M 50,65 C 85,150 185,10 225,80 C 265,150 310,18 300,60"
                  stroke="url(#hG1)" stroke-width="3.5" fill="none" stroke-linecap="round" mask="url(#hM)"/>
            <path d="M 50,105 C 85,25 185,170 225,85 C 265,10 310,158 300,120"
                  stroke="url(#hG2)" stroke-width="2.4" fill="none" stroke-linecap="round" mask="url(#hM)"/>
            <circle cx="50" cy="85" r="4.5" fill="#0D9488"/>
            <circle cx="50" cy="85" r="1.8" fill="#fff"/>
          </svg>
        </div>
      </div>
    </section>

    <!-- 三大特色 -->
    <section class="features">
      <div class="feat-card">
        <div class="feat-dot" style="background:#0D9488"></div>
        <h4>5G 极速网络</h4>
        <p>覆盖广，速度快，告别卡顿</p>
      </div>
      <div class="feat-card">
        <div class="feat-dot" style="background:#D97706"></div>
        <h4>资费透明公开</h4>
        <p>超出精准计费，拒绝隐形消费</p>
      </div>
      <div class="feat-card">
        <div class="feat-dot" style="background:#E8590C"></div>
        <h4>自助便捷服务</h4>
        <p>开户销户一键办理，省时省心</p>
      </div>
    </section>

    <!-- 套餐 -->
    <section class="packages" id="packages">
      <div class="pkg-head">
        <h2>专属套餐推荐</h2>
        <p>选择适合您的通信方案</p>
      </div>
      <div class="pkg-grid" v-loading="loading">
        <div v-for="tariff in tariffs" :key="tariff.name"
             class="pkg-card" :class="{ hot: tariff.name === '超人套餐' }">
          <div v-if="tariff.name === '超人套餐'" class="hot-tag">热门推荐</div>
          <h3>{{ tariff.name }}</h3>
          <div class="pkg-price">
            <span class="sym">¥</span>
            <span class="num">{{ tariff.price.replace('元/月','') }}</span>
            <span class="per">/月</span>
          </div>
          <ul class="pkg-list">
            <li v-for="(line,i) in formatDetail(tariff.detail)" :key="i">
              <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
                <path d="M2.5 7l3.5 3.5L11.5 4" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              <span>{{ line }}</span>
            </li>
          </ul>
          <el-button
            :type="tariff.name === '超人套餐' ? 'primary' : 'default'"
            round class="pkg-btn" @click="goRegister(tariff.name)">
            立即办理
          </el-button>
        </div>
      </div>
    </section>

    <div style="height:80px"></div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getTariffs } from '../api/user'

const router = useRouter()
const tariffs = ref([])
const loading = ref(true)

const formatDetail = (d) => {
  if (!d) return []
  return d.replace(/。/g,'|').split('|').filter(i => i.trim())
}
const goRegister = (name) => {
  const m = { '话唠套餐':'TALK','网虫套餐':'NET','超人套餐':'SUPER' }
  router.push({ path:'/register', query:{ type: m[name] || '' } })
}
const scrollToPackages = () => {
  document.getElementById('packages').scrollIntoView({ behavior:'smooth' })
}
onMounted(async () => {
  try { const r = await getTariffs(); tariffs.value = r.data } catch(e) {} finally { loading.value = false }
})
</script>

<style scoped>
.home { min-height: 100vh; background: var(--bg-color); }

/* 导航 */
.nav {
  position: sticky; top: 0; z-index: 50;
  background: rgba(248,250,252,0.88);
  backdrop-filter: blur(14px);
  border-bottom: 1px solid var(--border-light);
}
.nav-inner {
  max-width: 1120px; margin: 0 auto; padding: 0 24px;
  height: 64px; display: flex; justify-content: space-between; align-items: center;
}
.logo { display: flex; align-items: center; cursor: pointer; gap: 10px; }
.logo-mark { width: 28px; height: 28px; }
.logo-name {
  font-size: 19px; font-weight: 800; letter-spacing: .5px;
  color: var(--text-primary);
}

/* 首屏 */
.hero { background: #fff; border-bottom: 1px solid var(--border-light); }
.hero-inner {
  max-width: 1120px; margin: 0 auto; padding: 80px 24px 72px;
  display: flex; align-items: center; gap: 56px;
}
.hero-text { flex: 1; }
.hero-eyebrow {
  font-size: 11px; letter-spacing: 4px; font-weight: 600;
  color: var(--primary-color); margin-bottom: 14px;
  opacity: .7;
}
.hero-text h1 {
  font-size: clamp(32px, 4.5vw, 48px); font-weight: 800;
  line-height: 1.2; color: var(--text-primary); margin-bottom: 16px;
}
.hero-text p {
  font-size: 16px; color: var(--text-regular);
  max-width: 400px; margin-bottom: 32px; line-height: 1.7;
}
.hero-visual {
  flex: 1; display: flex; justify-content: center; align-items: center;
}
.hero-visual svg { width: 100%; max-width: 340px; height: auto; }

/* 特色 */
.features {
  max-width: 1120px; margin: -36px auto 0; padding: 0 24px;
  display: grid; grid-template-columns: repeat(3,1fr); gap: 20px;
  position: relative; z-index: 2;
}
.feat-card {
  background: #fff; border: 1px solid var(--border-color);
  border-radius: 14px; padding: 28px 22px;
  transition: transform .25s, box-shadow .25s;
}
.feat-card:hover { transform: translateY(-3px); box-shadow: var(--shadow-md); }
.feat-dot {
  width: 8px; height: 8px; border-radius: 50%;
  margin-bottom: 16px;
}
.feat-card h4 { font-size: 16px; font-weight: 700; margin-bottom: 4px; color: var(--text-primary); }
.feat-card p { font-size: 14px; color: var(--text-secondary); line-height: 1.5; }

/* 套餐 */
.packages { max-width: 1120px; margin: 0 auto; padding: 72px 24px 0; }
.pkg-head { text-align: center; margin-bottom: 40px; }
.pkg-head h2 { font-size: 28px; font-weight: 800; color: var(--text-primary); margin-bottom: 6px; }
.pkg-head p { color: var(--text-secondary); font-size: 15px; }

.pkg-grid { display: grid; grid-template-columns: repeat(3,1fr); gap: 24px; }
.pkg-card {
  position: relative; background: #fff;
  border: 1px solid var(--border-color); border-radius: 16px;
  padding: 32px 28px; display: flex; flex-direction: column;
  transition: transform .3s, box-shadow .3s, border-color .3s;
}
.pkg-card:hover { transform: translateY(-4px); box-shadow: var(--shadow-md); }
.pkg-card.hot {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 1px var(--primary-color), var(--shadow-md);
}
.hot-tag {
  position: absolute; top: -12px; left: 24px;
  background: var(--primary-color); color: #fff;
  padding: 4px 14px; border-radius: 6px;
  font-size: 12px; font-weight: 700;
}
.pkg-card h3 { font-size: 18px; font-weight: 700; margin-bottom: 16px; color: var(--text-primary); }
.pkg-price { display: flex; align-items: baseline; margin-bottom: 24px; color: var(--accent-color); }
.pkg-card.hot .pkg-price { color: var(--primary-color); }
.sym { font-size: 18px; font-weight: 700; }
.num { font-size: 44px; font-weight: 900; line-height: 1; margin: 0 2px; }
.per { font-size: 13px; color: var(--text-secondary); }

.pkg-list { list-style: none; flex: 1; margin-bottom: 24px; }
.pkg-list li {
  display: flex; align-items: center; gap: 8px;
  color: var(--text-regular); font-size: 14px; line-height: 2;
}
.pkg-list li svg { color: var(--success-color); flex-shrink: 0; }
.pkg-btn { width: 100%; height: 44px; font-weight: 600; font-size: 15px; }

@media(max-width:820px) {
  .hero-inner { flex-direction: column; text-align: center; padding: 56px 24px 48px; }
  .hero-text p { margin-left: auto; margin-right: auto; }
  .hero-visual { order: -1; }
  .hero-visual svg { max-width: 220px; }
  .features, .pkg-grid { grid-template-columns: 1fr; }
}
</style>