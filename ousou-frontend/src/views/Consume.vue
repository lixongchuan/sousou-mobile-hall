<template>
  <div class="ss-consume-layout">
    <el-row :gutter="24" justify="center" align="middle">
      
      <!-- ================== 左栏：嗖嗖 15 Pro Max 手机 ================== -->
      <el-col :span="12" :xs="24" :sm="14" :md="11">
        <div class="ss-phone">
          <!-- 灵动岛 (Dynamic Island) -->
          <div class="ss-dynamic-island" :class="{ 'island-expanded': isPlaying }">
            <div class="island-content">
              <span class="island-icon" v-if="isPlaying">🎵</span>
              <span class="island-text" v-if="isPlaying">嗖嗖5G 正在流媒体传输...</span>
              <span class="island-idle" v-else> 嗖嗖移动</span>
            </div>
          </div>

          <!-- 手机听筒 -->
          <div class="ss-earpiece"></div>

          <!-- 手机主屏幕 -->
          <div class="ss-screen">
            <!-- 1. iOS 状态栏 -->
            <div class="ss-status-bar" :class="{ 'ss-status-calling': callState === 'active' }">
              <span class="ss-time">{{ currentTime }}</span>
              <div class="ss-status-icons">
                <span class="ss-carrier">嗖嗖 5G</span>
                <span>📶</span>
                <span>🔋 99%</span>
              </div>
            </div>

            <!-- 2. 应用视窗 -->
            <div class="ss-screen-content" :style="screenBackgroundStyle">
              
              <!-- =============== APP 0: 系统主桌面 =============== -->
              <div v-if="activeApp === 'home'" class="ss-home-desktop">
                <div class="ss-app-grid">
                  <div class="ss-app-icon" @click="openApp('phone')">
                    <div class="ss-icon-box ss-icon-phone">📞</div>
                    <span class="ss-app-label">电话</span>
                  </div>
                  <div class="ss-app-icon" @click="openApp('sms')">
                    <div class="ss-icon-box ss-icon-sms">💬</div>
                    <span class="ss-app-label">短信</span>
                  </div>
                  <div class="ss-app-icon" @click="openApp('music')">
                    <div class="ss-icon-box ss-icon-music">🎵</div>
                    <span class="ss-app-label">音乐</span>
                  </div>
                  <!-- 新增原生APP：嗖嗖掌上营业厅 -->
                  <div class="ss-app-icon" @click="openApp('carrier')">
                    <div class="ss-icon-box ss-icon-carrier">📱</div>
                    <span class="ss-app-label">掌上营业厅</span>
                  </div>
                </div>

                <!-- 底部毛玻璃 Dock 栏 -->
                <div class="ss-dock">
                  <div class="ss-dock-icon">🧭</div>
                  <div class="ss-dock-icon">✉️</div>
                  <div class="ss-dock-icon">🎵</div>
                  <div class="ss-dock-icon">⚙️</div>
                </div>
              </div>

              <!-- =============== APP 1: 仿 iOS 电话系统 =============== -->
              <div v-else-if="activeApp === 'phone'" class="ss-app-phone">
                <!-- 电话导航 Header -->
                <div class="phone-nav-header" v-if="callState === 'idle'">
                  <div class="tab-item" :class="{ active: phoneTab === 'dialer' }" @click="phoneTab = 'dialer'">
                    拨号键盘
                  </div>
                  <div class="tab-item" :class="{ active: phoneTab === 'recents' }" @click="phoneTab = 'recents'">
                    通话记录
                  </div>
                </div>

                <!-- 1.1 拨号键盘界面 (⭐已设为默认进入) -->
                <div v-if="callState === 'idle' && phoneTab === 'dialer'" class="dialer-screen">
                  <!-- ⭐品质感格式化排版 (如: 139 1234 5678) -->
                  <div class="dial-num-display">{{ formattedDialedNumber || ' ' }}</div>
                  <div class="dial-grid">
                    <button v-for="k in ['1','2','3','4','5','6','7','8','9','*','0','#']" 
                            :key="k" class="dial-key" @click="pressKey(k)">
                      {{ k }}
                    </button>
                  </div>
                  <div class="dial-bar">
                    <button class="dial-btn-green" @click="startCall">📞</button>
                    <button class="dial-btn-del" @click="deleteLastDigit" v-if="dialedNumber">⌫</button>
                  </div>
                </div>

                <!-- 1.2 通话记录界面 -->
                <div v-if="callState === 'idle' && phoneTab === 'recents'" class="recents-screen">
                  <div class="recents-list">
                    <div v-for="(log, idx) in callLogs" :key="idx" class="recent-item">
                      <div class="recent-left">
                        <span class="recent-icon">📞</span>
                        <div class="recent-info">
                          <!-- 通话历史也运用高级排版格式 -->
                          <span class="recent-num">{{ formatPhoneNumber(log.number) }}</span>
                          <span class="recent-time">{{ log.timeStr }}</span>
                        </div>
                      </div>
                      <span class="recent-duration">{{ log.duration }} 秒</span>
                    </div>
                    <div v-if="callLogs.length === 0" class="empty-recents">
                      无最近通话记录
                    </div>
                  </div>
                </div>

                <!-- 1.3 正在通话卡片界面 -->
                <div v-if="callState !== 'idle'" class="calling-card-screen">
                  <div class="calling-header">
                    <div class="calling-avatar">👤</div>
                    <div class="calling-name">{{ formattedDialedNumber }}</div>
                    <div class="calling-sub">{{ callState === 'calling' ? '正在呼叫...' : '嗖嗖 5G 语音通话中' }}</div>
                    <div class="calling-timer" v-if="callState === 'active'">{{ formatCallTime(callSeconds) }}</div>
                  </div>
                  <div class="calling-keypad-mock">
                    <div class="mock-grid">
                      <span class="mock-btn">静音</span>
                      <span class="mock-btn">键盘</span>
                      <span class="mock-btn">免提</span>
                      <span class="mock-btn">新增</span>
                      <span class="mock-btn">视频</span>
                      <span class="mock-btn">通讯录</span>
                    </div>
                  </div>
                  <button class="hangup-action-btn" @click="endCall">挂断</button>
                </div>
              </div>

              <!-- =============== APP 2: 短信应用 =============== -->
              <div v-else-if="activeApp === 'sms'" class="ss-app-sms">
                <div class="sms-header">
                  <span class="sms-back" @click="goHome">◀ 桌面</span>
                  <span class="sms-name">客服 10086</span>
                  <span style="width: 30px;"></span>
                </div>
                <div class="sms-body" ref="chatAreaRef">
                  <div v-for="(m, i) in chatMessages" :key="i" 
                       :class="['sms-bubble', m.sender === 'me' ? 'sms-me' : 'sms-other']">
                    {{ m.text }}
                  </div>
                </div>
                <div class="sms-footer">
                  <input v-model="smsInput" placeholder="输入短信..." @keyup.enter="sendSms" />
                  <button @click="sendSms">发送</button>
                </div>
              </div>

              <!-- =============== APP 3: 嗖嗖音乐 =============== -->
              <div v-else-if="activeApp === 'music'" class="ss-app-music" :style="musicBlurBackground">
                <!-- 音乐顶部导航 -->
                <div class="music-header">
                  <span class="music-back" @click="goHome">◀ 桌面</span>
                  <div class="music-title-box">
                    <span class="m-title">{{ songs[currentSongIndex]?.title }}</span>
                    <span class="m-artist">{{ songs[currentSongIndex]?.artist }}</span>
                  </div>
                  <span class="music-lyric-toggle" @click="showPlayList = !showPlayList">
                    {{ showPlayList ? '💿 播放' : '📑 歌单' }}
                  </span>
                </div>

                <!-- 3.1 黑胶唱片主面板 -->
                <div v-if="!showPlayList && !showLyricPanel" class="music-disc-view" @click="showLyricPanel = true">
                  <div class="music-tonearm" :class="{ 'tonearm-playing': isPlaying }"></div>
                  <div class="music-vinyl" :class="{ 'vinyl-rotate': isPlaying }">
                    <div class="vinyl-center-cover">
                      <img :src="songs[currentSongIndex]?.coverBase64" @error="handleCoverError" alt="cover"/>
                    </div>
                  </div>
                  <div class="music-billing-hint" v-if="isPlaying">
                    <span class="flow-dot"></span> 流量扣减中：7MB/3秒
                  </div>
                </div>

                <!-- 3.2 歌词同步滚动面板 -->
                <div v-else-if="!showPlayList && showLyricPanel" class="music-lyric-view" ref="lyricContainerRef" @click="showLyricPanel = false">
                  <div class="lyric-scroller">
                    <p v-for="(line, idx) in songs[currentSongIndex]?.lyrics" 
                       :key="idx"
                       :class="['lyric-line', { 'lyric-active': currentLyricIndex === idx }]">
                      {{ line.text }}
                    </p>
                  </div>
                </div>

                <!-- ⭐ 3.3 新增：App 内置播放列表 (歌单抽屉) -->
                <div v-else class="music-playlist-view">
                  <div class="playlist-header">🎵 嗖嗖推荐无损歌单</div>
                  <div class="playlist-container">
                    <div v-for="(song, idx) in songs" :key="song.id" 
                         :class="['playlist-item', { 'active-song': currentSongIndex === idx }]"
                         @click="selectAndPlaySong(idx)">
                      <div class="playlist-left">
                        <img :src="song.coverBase64" @error="handleCoverError" class="song-mini-cover" />
                        <div class="song-mini-info">
                          <span class="song-mini-title">{{ song.title }}</span>
                          <span class="song-mini-artist">{{ song.artist }}</span>
                        </div>
                      </div>
                      <span class="song-playing-wave" v-if="currentSongIndex === idx && isPlaying">🔊 播放中</span>
                    </div>
                  </div>
                </div>

                <!-- 3.4 音乐控制器底座 -->
                <div class="music-control-panel">
                  <div class="music-progress-info" v-if="isPlaying">
                    已持续扣除流量: <span class="highlight-flow">{{ sessionFlowConsumed }} MB</span>
                  </div>
                  <div class="music-progress-info" v-else>
                    准备中 - 嗖嗖无损高带宽传输
                  </div>
                  <div class="music-controls">
                    <button class="m-ctrl" @click="changeSong(-1)">⏮</button>
                    <button class="m-play" @click="toggleMusicPlay">{{ isPlaying ? '⏸' : '▶' }}</button>
                    <button class="m-ctrl" @click="changeSong(1)">⏭</button>
                  </div>
                </div>
              </div>

              <!-- =============== ⭐ APP 4: 新增【嗖嗖掌上营业厅】原生 App =============== -->
              <div v-else-if="activeApp === 'carrier'" class="ss-app-carrier">
                <div class="carrier-header">
                  <span class="carrier-back" @click="goHome">◀ 桌面</span>
                  <span class="carrier-title">嗖嗖 5G 掌上营业厅</span>
                  <span style="width: 30px;"></span>
                </div>
                
                <div class="carrier-content">
                  <!-- 顶部信息卡片 -->
                  <div class="carrier-info-card">
                    <div class="c-info-row">
                      <span>我的号码:</span>
                      <strong>{{ userStore.phone }}</strong>
                    </div>
                    <div class="c-info-row">
                      <span>账户话费余额:</span>
                      <strong class="c-balance">¥ {{ userStore.balance.toFixed(2) }}</strong>
                    </div>
                  </div>

                  <!-- 自助快捷服务 -->
                  <div class="carrier-service-box">
                    <div class="section-title">📱 话费快速充值</div>
                    <div class="recharge-grid">
                      <button v-for="amount in [10, 20, 50, 100]" :key="amount" 
                              @click="handleInternalRecharge(amount)" class="c-service-btn">
                        充值 {{ amount }}元
                      </button>
                    </div>

                    <el-divider style="margin: 16px 0;" />

                    <div class="section-title">⚡ 订购流量加油包</div>
                    <div class="pack-grid">
                      <button @click="handleInternalBuyPack(1)" class="c-pack-btn">1GB (¥10)</button>
                      <button @click="handleInternalBuyPack(3)" class="c-pack-btn">3GB (¥20)</button>
                      <button @click="handleInternalBuyPack(5)" class="c-pack-btn">5GB (¥30)</button>
                    </div>

                    <el-divider style="margin: 16px 0;" />

                    <div class="section-title">🔄 在线变更基础套餐</div>
                    <div class="change-pkg-box">
                      <button @click="handleInternalChangePkg('TALK')" class="c-pkg-btn p-talk">话唠套餐</button>
                      <button @click="handleInternalChangePkg('NET')" class="c-pkg-btn p-net">网虫套餐</button>
                      <button @click="handleInternalChangePkg('SUPER')" class="c-pkg-btn p-super">超人套餐</button>
                    </div>
                  </div>
                </div>
              </div>

            </div>

            <!-- 4. 底部 Home 条 -->
            <div class="ss-home-indicator" @click="goHome" v-if="activeApp !== 'home'"></div>
          </div>
        </div>
      </el-col>

      <!-- ================== 右栏：全局详单与看板 ================== -->
      <el-col :span="12" :xs="24" :sm="10" :md="10">
        <el-card shadow="never" class="ss-status-card">
          <template #header><span class="ss-title-text">📱 嗖嗖 状态</span></template>
          <div class="ss-status-body">
            <p><strong>当前主卡手机号:</strong> <el-tag type="success">{{ userStore.phone }}</el-tag></p>
            <p><strong>SIM 卡余额:</strong> <span class="ss-balance">¥ {{ userStore.balance.toFixed(2) }}</span></p>
            <el-divider style="margin: 12px 0;" />
<el-alert style="background-color: #f4f4f5; color: #606266; border: 1px solid #e9e9eb;" :closable="false">
              通话 {{ pricing.talk }}元/分钟；
              流量 {{ pricing.net }}元/MB；
              短信 {{ pricing.sms }}元/条；
              (超出套餐额度或非套餐包含项将按此计费)

            </el-alert>
          </div>
        </el-card>

        <el-card shadow="never" class="ss-records-card" style="margin-top: 16px;">
          <template #header><span class="ss-title-text">📋 嗖嗖扣费详单</span></template>
          <el-table :data="recentRecords" style="width: 100%" size="small" empty-text="暂无扣费详单记录">
            <el-table-column prop="type" label="资费大类" width="90" />
            <el-table-column prop="amount" label="消耗明细">
              <template #default="scope">
                {{ scope.row.amount }} {{ getUnitString(scope.row.type) }}
              </template>
            </el-table-column>
            <el-table-column prop="cost" label="扣费金额">
              <template #default="scope">
                <span :style="{ color: scope.row.cost > 0 ? '#EF4444' : '#10B981', fontWeight: 600 }">
                  {{ scope.row.cost > 0 ? `-¥ ${scope.row.cost.toFixed(2)}` : '免费' }}
                </span>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useUserStore } from '../stores/user'
import { consume, getRecords, getQuota, recharge, buyDataPack, changePackage } from '../api/user' // ⭐ 引入API
import { getMusicList } from '../api/user'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const recentRecords = ref([])

// 全局手机系统设置
const activeApp = ref('home')
const currentTime = ref('12:00')
let timeTimer = null

// =============== APP 1: 仿 iOS 电话核心逻辑 ===============
const phoneTab = ref('dialer') //  默认进入拨号键盘
const dialedNumber = ref('')
const callState = ref('idle')
const callSeconds = ref(0)
const callLogs = ref([])
let callInterval = null

//  手机号高级格式化排版 (e.g. 139 1234 5678)
const formattedDialedNumber = computed(() => {
  const raw = dialedNumber.value.replace(/\s+/g, '') // 去除空格
  if (raw.length <= 3) return raw
  if (raw.length <= 7) return `${raw.slice(0, 3)} ${raw.slice(3)}`
  return `${raw.slice(0, 3)} ${raw.slice(3, 7)} ${raw.slice(7, 11)}`
})

// 用途：为通话记录转换格式
const formatPhoneNumber = (num) => {
  if (!num) return ''
  const raw = num.replace(/\s+/g, '')
  if (raw.length === 11) {
    return `${raw.slice(0, 3)} ${raw.slice(3, 7)} ${raw.slice(7, 11)}`
  }
  return num
}

// =============== APP 2: 短信聊天气泡逻辑 ===============
const smsInput = ref('')
const chatAreaRef = ref(null)
const chatMessages = ref([
  { sender: 'other', text: '您好！欢迎进入嗖嗖移动 10086 在线文字营业厅。发送单条扣费 0.1 元。' }
])

// =============== APP 3: 后端驱动的 FLAC 播放与歌单模块 ===============
const isPlaying = ref(false)
const currentSongIndex = ref(0)
const sessionFlowConsumed = ref(0)
const showLyricPanel = ref(false)
const showPlayList = ref(false) // 开启歌单状态
const currentLyricIndex = ref(0)
const lyricContainerRef = ref(null)
let flowTimer = null

const songs = ref([
  {
    id: 1,
    title: '加载中...',
    artist: '正在提取 FLAC 元数据',
    coverBase64: '',
    src: '',
    lyrics: [{ time: 0, text: '正在解析内嵌歌词...' }]
  }
])

let audio = new Audio()

const loadBackendMusicLibrary = async () => {
  try {
    const res = await getMusicList()
    if (res.code === 200 && res.data && res.data.length > 0) {
      songs.value = res.data
    }
  } catch (err) {
    ElMessage.error('无法读取后端 FLAC 媒体库！')
  }
}

// 通过歌单选择歌曲播放
const selectAndPlaySong = (idx) => {
  pauseMusic()
  currentSongIndex.value = idx
  sessionFlowConsumed.value = 0
  currentLyricIndex.value = 0
  showPlayList.value = false // 切回播放页
  playMusic()
}

// 📱 手机背景设计：显示壁纸
const wallpaperExists = ref(true)
const screenBackgroundStyle = computed(() => {
  if (wallpaperExists.value) {
    return {
      backgroundImage: `url('/wallpaper.jpg')`,
      backgroundSize: 'cover',
      backgroundPosition: 'center'
    }
  }
  return { background: 'radial-gradient(circle at center, #1e293b, #0f172a)' }
})

const musicBlurBackground = computed(() => {
  const cover = songs.value[currentSongIndex.value]?.coverBase64
  const bgImage = cover ? `url('${cover}')` : "none"
  return {
    backgroundImage: `linear-gradient(rgba(15,23,42,0.85), rgba(15,23,42,0.95)), ${bgImage}`,
    backgroundSize: 'cover',
    backgroundPosition: 'center'
  }
})

const pricing = computed(() => { return { talk: 0.2, sms: 0.1, net: 0.1 } })

// 时钟
const refreshClock = () => {
  const d = new Date()
  currentTime.value = `${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

const openApp = (appName) => { activeApp.value = appName }

const goHome = () => {
  if (callState.value === 'active' || callState.value === 'calling') { endCall() }
  activeApp.value = 'home'
}

const handleCoverError = (e) => {
  e.target.src = "data:image/svg+xml;utf8,<svg xmlns='http://www.w3.org/2000/svg' width='100' height='100' style='background:linear-gradient(135deg,%23f43f5e,%233b82f6)'><text x='50%' y='55%' font-size='12' fill='white' text-anchor='middle'>嗖嗖无损</text></svg>"
}

// =============== APP 1 电话拨号键盘功能 ===============
const pressKey = (key) => {
  if (dialedNumber.value.length < 11) dialedNumber.value += key
}
const deleteLastDigit = () => { dialedNumber.value = dialedNumber.value.slice(0, -1) }

const startCall = () => {
  if (!dialedNumber.value) { ElMessage.warning('请输入要拨打的号码！'); return }
  callState.value = 'calling'
  setTimeout(() => {
    if (callState.value === 'calling') {
      callState.value = 'active'
      callSeconds.value = 0
      callInterval = setInterval(() => { callSeconds.value++ }, 1000)
    }
  }, 2000)
}

const endCall = async () => {
  clearInterval(callInterval)
  const durationSec = callSeconds.value
  const minutes = Math.ceil(durationSec / 60)
  if (durationSec > 0) {
    try {
      const res = await consume({ phone: userStore.phone, type: 1, amount: minutes })
      ElMessage.success(`通话结束，时长 ${durationSec}秒 (折合 ${minutes}分钟)`)
      callLogs.value.unshift({
        number: dialedNumber.value,
        timeStr: `今天 ${currentTime.value}`,
        duration: durationSec
      })
      syncData()
    } catch (err) { ElMessage.error('扣费结算失败') }
  }
  callState.value = 'idle'
  callSeconds.value = 0
  dialedNumber.value = ''
}

const formatCallTime = (totalSec) => {
  const m = String(Math.floor(totalSec / 60)).padStart(2, '0')
  const s = String(totalSec % 60).padStart(2, '0')
  return `${m}:${s}`
}

// =============== APP 2 短信发送 ===============
const sendSms = async () => {
  if (!smsInput.value.trim()) return
  const text = smsInput.value
  chatMessages.value.push({ sender: 'me', text })
  smsInput.value = ''
  scrollToBottom()
  try {
    await consume({ phone: userStore.phone, type: 3, amount: 1 })
    ElMessage.success('短信发送成功')
    setTimeout(() => {
      chatMessages.value.push({ sender: 'other', text: '嗖嗖短信回执：您的短信已投递成功，扣费0.1元。' })
      scrollToBottom()
    }, 1000)
    syncData()
  } catch (err) { ElMessage.error('发送失败') }
}

const scrollToBottom = () => {
  nextTick(() => { if (chatAreaRef.value) chatAreaRef.value.scrollTop = chatAreaRef.value.scrollHeight })
}

// =============== APP 3 音乐播放与歌词同步 ===============
const toggleMusicPlay = () => {
  if (isPlaying.value) { pauseMusic() } else { playMusic() }
}

const playMusic = () => {
  const currentSong = songs.value[currentSongIndex.value]
  if (!currentSong || !currentSong.src) return

  // 定义后端的真实服务地址与端口
  const backendBaseUrl = 'http://localhost:8080'
  
  // 是相对路径则自动补全后端主域名，是绝对路径则直接使用
  const absoluteSrc = currentSong.src.startsWith('http') 
    ? currentSong.src 
    : `${backendBaseUrl}${currentSong.src}`

  // 音频源发生了改变，或者还未载入，则重新赋值
  if (!audio.src || audio.src !== absoluteSrc) {
    audio.src = absoluteSrc
  }

  audio.play().then(() => {
    isPlaying.value = true
    flowTimer = setInterval(async () => {
      try {
        await consume({ phone: userStore.phone, type: 2, amount: 7 })
        sessionFlowConsumed.value += 7
        syncData()
      } catch (err) {
        pauseMusic()
        ElMessage.warning('话费已耗尽，嗖嗖 5G 网络流媒体传输已中断。')
      }
    }, 3000)
  }).catch((err) => {
    // 打印真实错误信息到控制台，方便调试
    console.error("浏览理解音频播放失败，详情原因: ", err)
    ElMessage.error('本地无损 FLAC 音频文件未在服务器 static/music 目录下就位！')
  })
}

const pauseMusic = () => {
  audio.pause()
  isPlaying.value = false
  clearInterval(flowTimer)
}

const changeSong = (direction) => {
  pauseMusic()
  currentSongIndex.value = (currentSongIndex.value + direction + songs.value.length) % songs.value.length
  sessionFlowConsumed.value = 0
  currentLyricIndex.value = 0
  playMusic()
}

const bindAudioLyricEngine = () => {
  audio.addEventListener('timeupdate', () => {
    const time = audio.currentTime
    const lyrics = songs.value[currentSongIndex.value]?.lyrics
    if (!lyrics) return

    const matchedIdx = lyrics.findIndex((lyric, index) => {
      const nextLine = lyrics[index + 1]
      return time >= lyric.time && (!nextLine || time < nextLine.time)
    })

    if (matchedIdx !== -1 && matchedIdx !== currentLyricIndex.value) {
      currentLyricIndex.value = matchedIdx
      nextTick(() => {
        const container = lyricContainerRef.value
        if (container) {
          const activeEl = container.querySelector('.lyric-active')
          if (activeEl) {
            container.scrollTop = activeEl.offsetTop - container.clientHeight / 2 + activeEl.clientHeight / 2
          }
        }
      })
    }
  })
}

// ===============  APP 4: 【嗖嗖营业厅 App】核心服务功能 ===============
const handleInternalRecharge = async (amount) => {
  try {
    const res = await recharge({ phone: userStore.phone, amount })
    ElMessage.success(res.msg)
    syncData()
  } catch (err) {
    ElMessage.error('充值失败')
  }
}

const handleInternalBuyPack = async (size) => {
  try {
    const res = await buyDataPack({ phone: userStore.phone, size })
    ElMessage.success(res.msg)
    syncData()
  } catch (err) {
    ElMessage.error(err.message || '购买流量包失败')
  }
}

const handleInternalChangePkg = async (type) => {
  try {
    const res = await changePackage({ phone: userStore.phone, type })
    ElMessage.success(res.msg)
    syncData()
  } catch (err) {
    ElMessage.error(err.message || '套餐变更失败')
  }
}

// =============== 业务同步 ===============
const syncData = async () => {
  try {
    const q = await getQuota({ phone: userStore.phone })
    userStore.updateBalance(q.data.balance)
    fetchRecords()
  } catch (err) { console.error(err) }
}

const fetchRecords = async () => {
  try {
    const res = await getRecords({ phone: userStore.phone })
    recentRecords.value = (res.data || []).reverse().slice(0, 5)
  } catch (e) { console.error(e) }
}

const getUnitString = (type) => {
  if (type.includes('通话')) return '分钟'
  if (type.includes('上网') || type.includes('流量')) return 'MB'
  if (type.includes('短信')) return '条'
  return ''
}

// =============== 生命周期 ===============
onMounted(() => {
  refreshClock()
  timeTimer = setInterval(refreshClock, 1000)
  fetchRecords()
  loadBackendMusicLibrary()
  bindAudioLyricEngine()
})

onUnmounted(() => {
  clearInterval(timeTimer)
  clearInterval(callInterval)
  clearInterval(flowTimer)
  audio.pause()
  audio = null
})
</script>

<style scoped>
/* 整个布局样式 */
.ss-consume-layout {
  padding: 10px;
}

/* 📱 嗖嗖 手机机身 */
.ss-phone {
  width: 360px;
  height: 720px;
  background: #1e293b;
  border-radius: 46px;
  box-shadow: 0 30px 60px -15px rgba(0, 0, 0, 0.5);
  border: 12px solid #334155;
  position: relative;
  margin: 0 auto;
}

/* 听筒 */
.ss-earpiece {
  position: absolute;
  top: 13px;
  left: 50%;
  transform: translateX(-50%);
  width: 50px;
  height: 4px;
  background: #1e293b;
  border-radius: 2px;
  z-index: 10;
}

/* 灵动岛 (Dynamic Island) */
.ss-dynamic-island {
  position: absolute;
  top: 10px;
  left: 50%;
  transform: translateX(-50%);
  width: 90px;
  height: 25px;
  background: #000;
  border-radius: 20px;
  z-index: 12;
  transition: all 0.4s cubic-bezier(0.18, 0.89, 0.32, 1.28);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.island-expanded {
  width: 190px !important;
}

.island-content {
  color: #fff;
  font-size: 10px;
  display: flex;
  align-items: center;
  gap: 6px;
  white-space: nowrap;
}

.island-icon {
  animation: blink 1.2s infinite;
}

.island-idle {
  color: #334155;
  font-weight: bold;
}

/* 物理屏幕 */
.ss-screen {
  width: 100%;
  height: 100%;
  background: #000;
  border-radius: 34px;
  overflow: hidden;
  position: relative;
  display: flex;
  flex-direction: column;
}

/* 状态栏 */
.ss-status-bar {
  height: 40px;
  background: rgba(0, 0, 0, 0.2);
  backdrop-filter: blur(10px);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  color: #fff;
  font-size: 11px;
  z-index: 9;
}

.ss-status-calling {
  background: #10b981 !important;
}

.ss-status-icons {
  display: flex;
  gap: 6px;
}

/* 屏幕显示承载区 */
.ss-screen-content {
  flex: 1;
  position: relative;
  overflow: hidden;
}

/* =============== APP 0: 桌面 =============== */
.ss-home-desktop {
  width: 100%;
  height: 100%;
  position: relative;
}

.ss-app-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  padding: 24px 16px;
}

.ss-app-icon {
  display: flex;
  flex-direction: column;
  align-items: center;
  cursor: pointer;
}

.ss-icon-box {
  width: 54px;
  height: 54px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26px;
  box-shadow: 0 8px 16px rgba(0,0,0,0.3);
  transition: transform 0.2s;
}

.ss-icon-box:hover {
  transform: scale(1.08);
}

.ss-icon-phone { background: linear-gradient(135deg, #10b981, #059669); }
.ss-icon-sms { background: linear-gradient(135deg, #3b82f6, #1d4ed8); }
.ss-icon-music { background: linear-gradient(135deg, #ec4899, #be185d); }
.ss-icon-carrier { background: linear-gradient(135deg, #3b82f6, #00d2ff); } /* 原生APP */

.ss-app-label {
  color: #fff;
  text-shadow: 0 2px 4px rgba(0,0,0,0.8);
  font-size: 11px;
  margin-top: 6px;
  font-weight: 500;
}

/* 仿 iOS Dock 栏 */
.ss-dock {
  position: absolute;
  bottom: 24px;
  left: 50%;
  transform: translateX(-50%);
  width: 88%;
  height: 60px;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(25px);
  border-radius: 18px;
  display: flex;
  justify-content: space-around;
  align-items: center;
  box-shadow: 0 10px 25px rgba(0,0,0,0.2);
}

.ss-dock-icon {
  font-size: 24px;
  cursor: pointer;
  transition: transform 0.2s;
}
.ss-dock-icon:hover { transform: scale(1.15); }

/* =============== APP 1: 仿 iOS 电话系统 =============== */
.ss-app-phone {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #000;
  color: #fff;
}

.phone-nav-header {
  height: 48px;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 40px;
  border-bottom: 1px solid #111;
  background: #111;
}

.phone-nav-header .tab-item {
  font-size: 14px;
  color: #666;
  cursor: pointer;
  transition: color 0.2s;
}

.phone-nav-header .tab-item.active {
  color: #fff;
  font-weight: bold;
}

/* 1.1 通话记录 */
.recents-screen {
  flex: 1;
  overflow-y: auto;
  padding: 10px;
}

.recents-list {
  display: flex;
  flex-direction: column;
}

.recent-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 6px;
  border-bottom: 1px solid #111;
}

.recent-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.recent-icon { font-size: 16px; color: #10b981; }
.recent-info { display: flex; flex-direction: column; }
.recent-num { font-size: 14px; font-weight: 500; }
.recent-time { font-size: 11px; color: #666; }
.recent-duration { font-size: 12px; color: #888; }
.empty-recents { text-align: center; color: #444; margin-top: 100px; font-size: 13px; }

/* 1.2 拨号键盘 */
.dialer-screen {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 16px;
}

.dial-num-display {
  height: 70px;
  font-size: 26px; /* 稍微缩小确保排版整齐 */
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 300;
  margin-bottom: 10px;
  color: #fff;
}

.dial-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 14px;
  justify-items: center;
}

.dial-key {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  border: none;
  background: #1c1c1e;
  color: #fff;
  font-size: 24px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}
.dial-key:active { background: #2c2c2e; }

.dial-bar {
  margin-top: auto;
  margin-bottom: 24px;
  display: flex;
  justify-content: center;
  position: relative;
  width: 100%;
}

.dial-btn-green {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: #30d158;
  border: none;
  font-size: 24px;
  color: #fff;
  cursor: pointer;
}

.dial-btn-del {
  position: absolute;
  right: 40px;
  top: 15px;
  background: none;
  border: none;
  color: #999;
  font-size: 20px;
  cursor: pointer;
}

/* 1.3 通话 */
.calling-card-screen {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: space-around;
  height: 100%;
  padding: 40px 20px;
}

.calling-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: #2c2c2e;
  font-size: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 10px;
}

.calling-name { font-size: 24px; font-weight: bold; }
.calling-sub { font-size: 13px; color: #10b981; margin-top: 4px; }
.calling-timer { font-size: 38px; font-weight: 300; margin-top: 10px; }

.calling-keypad-mock {
  width: 100%;
}

.mock-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  text-align: center;
}

.mock-btn {
  font-size: 12px;
  color: #666;
  cursor: not-allowed;
  background: #1c1c1e;
  padding: 10px 0;
  border-radius: 12px;
}

.hangup-action-btn {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: #ff3b30;
  border: none;
  color: #fff;
  font-size: 15px;
  font-weight: bold;
  cursor: pointer;
}

/* =============== APP 2: 短信应用 =============== */
.ss-app-sms {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: #1e1e1e;
  color: #fff;
}

.sms-header {
  height: 48px;
  background: #2a2a2a;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 10px;
}

.sms-back { font-size: 12px; color: #007aff; cursor: pointer; }
.sms-name { font-size: 14px; font-weight: bold; }

.sms-body {
  flex: 1;
  padding: 12px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.sms-bubble {
  max-width: 75%;
  padding: 10px 14px;
  border-radius: 18px;
  font-size: 13px;
  line-height: 1.4;
}

.sms-me {
  background: #007aff;
  color: #fff;
  align-self: flex-end;
  border-bottom-right-radius: 2px;
}

.sms-other {
  background: #262629;
  color: #fff;
  align-self: flex-start;
  border-bottom-left-radius: 2px;
}

.sms-footer {
  padding: 8px;
  background: #111;
  display: flex;
  gap: 8px;
}

.sms-footer input {
  flex: 1;
  background: #222;
  border: none;
  border-radius: 20px;
  padding: 0 14px;
  color: #fff;
  outline: none;
}

.sms-footer button {
  background: #007aff;
  border: none;
  color: #fff;
  padding: 6px 14px;
  border-radius: 16px;
  cursor: pointer;
}

/* =============== APP 3: 嗖嗖音乐 (含歌单抽屉) =============== */
.ss-app-music {
  height: 100%;
  display: flex;
  flex-direction: column;
  color: #fff;
  position: relative;
  transition: all 0.5s ease;
  backdrop-filter: blur(40px);
}

.music-header {
  height: 50px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 14px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
}

.music-back { font-size: 12px; color: #ff2d55; cursor: pointer; }
.music-title-box { display: flex; flex-direction: column; align-items: center; max-width: 180px; }
.m-title { font-size: 13px; font-weight: bold; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; width: 100%; text-align: center; }
.m-artist { font-size: 10px; color: #aaa; margin-top: 2px; }
.music-lyric-toggle { font-size: 12px; color: #ff2d55; cursor: pointer; }

/* 3.1 唱片盘视图 */
.music-disc-view {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  position: relative;
  cursor: pointer;
}

.music-tonearm {
  width: 70px;
  height: 100px;
  background-image: url("data:image/svg+xml;utf8,<svg xmlns='http://www.w3.org/2000/svg' width='70' height='100'><path d='M10,10 L30,40 L40,90' stroke='white' stroke-width='3' fill='none'/><circle cx='10' cy='10' r='5' fill='gray'/></svg>");
  position: absolute;
  top: 0;
  left: 55%;
  transform-origin: 10px 10px;
  transform: rotate(-30deg);
  transition: transform 0.5s ease;
  z-index: 5;
}

.tonearm-playing {
  transform: rotate(0deg) !important;
}

.music-vinyl {
  width: 170px;
  height: 170px;
  border-radius: 50%;
  background: #000;
  border: 12px solid #111;
  box-shadow: 0 10px 30px rgba(0,0,0,0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  animation: rotateDisc 22s linear infinite paused;
}

.vinyl-center-cover {
  width: 105px;
  height: 105px;
  border-radius: 50%;
  overflow: hidden;
  border: 2px solid #000;
}

.vinyl-center-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.vinyl-rotate {
  animation-play-state: running !important;
}

.music-billing-hint {
  margin-top: 24px;
  background: rgba(239, 68, 68, 0.15);
  border: 1px solid #ff2d55;
  color: #ff2d55;
  padding: 4px 12px;
  border-radius: 14px;
  font-size: 11px;
}

.flow-dot {
  display: inline-block;
  width: 6px;
  height: 6px;
  background: #ff2d55;
  border-radius: 50%;
  animation: blink 1.2s infinite;
}

/* 3.2 仿主流：同步歌词滚动面板 */
.music-lyric-view {
  flex: 1;
  overflow-y: scroll;
  padding: 24px 20px;
  scroll-behavior: smooth;
  cursor: pointer;
  scrollbar-width: none;
}
.music-lyric-view::-webkit-scrollbar { display: none; }

.lyric-scroller {
  padding: 100px 0;
  text-align: center;
}

.lyric-line {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.4);
  margin-bottom: 20px;
  transition: all 0.3s ease;
  line-height: 1.5;
}

.lyric-active {
  color: #ff2d55 !important;
  font-size: 17px !important;
  font-weight: bold;
  transform: scale(1.08);
}

/* ⭐ 3.3 新增：App 内置原生歌单列表样式 */
.music-playlist-view {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  background: rgba(15, 23, 42, 0.3);
}

.playlist-header {
  font-size: 14px;
  font-weight: bold;
  color: #ff2d55;
  margin-bottom: 12px;
  border-left: 3px solid #ff2d55;
  padding-left: 8px;
}

.playlist-container {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.playlist-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: rgba(255, 255, 255, 0.05);
  padding: 8px 12px;
  border-radius: 10px;
  cursor: pointer;
  transition: background 0.2s;
}

.playlist-item:hover, .active-song {
  background: rgba(255, 45, 85, 0.15) !important;
  border: 1px solid rgba(255, 45, 85, 0.3);
}

.playlist-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.song-mini-cover {
  width: 36px;
  height: 36px;
  border-radius: 6px;
  object-fit: cover;
}

.song-mini-info {
  display: flex;
  flex-direction: column;
  max-width: 150px;
}

.song-mini-title {
  font-size: 12px;
  font-weight: bold;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.song-mini-artist {
  font-size: 10px;
  color: #aaa;
  margin-top: 2px;
}

.song-playing-wave {
  font-size: 10px;
  color: #ff2d55;
  font-weight: bold;
}

/* 3.4 音乐底座控制器 */
.music-control-panel {
  padding: 20px 14px;
  border-top: 1px solid rgba(255, 255, 255, 0.05);
  background: rgba(15, 23, 42, 0.5);
}

.music-progress-info {
  text-align: center;
  font-size: 11px;
  color: #888;
  margin-bottom: 12px;
}

.highlight-flow {
  color: #ff2d55;
  font-weight: bold;
}

.music-controls {
  display: flex;
  justify-content: center;
  gap: 40px;
  align-items: center;
}

.m-ctrl {
  background: none;
  border: none;
  font-size: 28px;
  color: #fff;
  cursor: pointer;
}

.m-play {
  width: 54px;
  height: 54px;
  border-radius: 50%;
  background: #ff2d55;
  border: none;
  color: #fff;
  font-size: 22px;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(255, 45, 85, 0.4);
}

/* =============== ⭐ APP 4: 新增【掌上营业厅】原生 APP 样式 =============== */
.ss-app-carrier {
  height: 100%;
  background: linear-gradient(180deg, #1d4ed8, #0f172a);
  color: #fff;
  display: flex;
  flex-direction: column;
}

.carrier-header {
  height: 48px;
  background: rgba(15, 23, 42, 0.5);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 10px;
  border-bottom: 1px solid rgba(255,255,255,0.05);
}

.carrier-back { font-size: 12px; color: #00d2ff; cursor: pointer; }
.carrier-title { font-size: 13px; font-weight: bold; }

.carrier-content {
  flex: 1;
  padding: 16px;
  overflow-y: auto;
  scrollbar-width: none;
}
.carrier-content::-webkit-scrollbar { display: none; }

.carrier-info-card {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  padding: 14px;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0,0,0,0.2);
  border: 1px solid rgba(255,255,255,0.1);
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.c-info-row {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #e2e8f0;
}

.c-balance {
  color: #00d2ff;
  font-size: 16px;
}

.carrier-service-box {
  margin-top: 16px;
}

.section-title {
  font-size: 12px;
  font-weight: bold;
  color: #00d2ff;
  margin-bottom: 10px;
}

.recharge-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 8px;
}

.c-service-btn {
  background: rgba(255,255,255,0.08);
  border: 1px solid rgba(255,255,255,0.1);
  color: #fff;
  padding: 8px 0;
  border-radius: 10px;
  font-size: 11px;
  cursor: pointer;
  transition: all 0.2s;
}
.c-service-btn:active { background: #00d2ff; color: #000; }

.pack-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
}

.c-pack-btn {
  background: rgba(0, 210, 255, 0.15);
  border: 1px solid #00d2ff;
  color: #00d2ff;
  padding: 8px 0;
  border-radius: 10px;
  font-size: 11px;
  cursor: pointer;
}
.c-pack-btn:active { background: #00d2ff; color: #000; }

.change-pkg-box {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.c-pkg-btn {
  border: none;
  color: #fff;
  padding: 8px 0;
  border-radius: 10px;
  font-size: 12px;
  font-weight: bold;
  cursor: pointer;
}
.p-talk { background: linear-gradient(135deg, #10b981, #059669); }
.p-net { background: linear-gradient(135deg, #3b82f6, #1d4ed8); }
.p-super { background: linear-gradient(135deg, #ec4899, #be185d); }

/* 虚拟底部小横条 */
.ss-home-indicator {
  position: absolute;
  bottom: 8px;
  left: 50%;
  transform: translateX(-50%);
  width: 120px;
  height: 4px;
  background: rgba(255,255,255,0.4);
  border-radius: 2px;
  cursor: pointer;
  z-index: 10;
}

/* 动画定义 */
@keyframes rotateDisc {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

@keyframes blink {
  50% { opacity: 0; }
}

/* 右侧面板样式 */
.ss-title-text { font-weight: 600; color: #1e293b; }
.ss-balance { font-size: 24px; color: #ef4444; font-weight: bold; margin-left: 6px; }
.ss-status-body { font-size: 14px; line-height: 1.8; }
.ss-records-card { height: calc(100% - 170px); }
</style>