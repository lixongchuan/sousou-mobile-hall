import axios from 'axios'
import qs from 'qs'
import { ElMessage } from 'element-plus'

// 创建 Axios 实例
const service = axios.create({
  // 因为在 vite.config.js 中配置了代理，这里直接写相对路径即可
  baseURL: '', 
  timeout: 10000 
})

// 请求拦截器：处理表单数据格式
service.interceptors.request.use(
  config => {
    // 如果是 POST/PUT/DELETE 请求，且数据不是 FormData，使用 qs 序列化
    if (['post', 'put', 'delete'].includes(config.method) && config.data && !(config.data instanceof FormData)) {
      config.data = qs.stringify(config.data)
      config.headers['Content-Type'] = 'application/x-www-form-urlencoded'
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器：统一处理错误码
service.interceptors.response.use(
  response => {
    const res = response.data
    
    // 严格判断：如果 code 不是 200，统一视为错误
    if (res.code !== 200) {
      const errorMessage = res.error || res.msg || '系统异常，请稍后重试'
      ElMessage.error(errorMessage)
      
      // 特殊处理：如果密码错误或未登录，可以根据业务决定是否强制跳转登录页
      // 此处返回 Promise.reject，让具体的调用方 catch 到错误
      return Promise.reject(new Error(errorMessage))
    } else {
      // code === 200，正常返回整个数据对象 { code, data, msg }
      return res
    }
  },
  error => {
    // HTTP 状态码异常 (如 404, 500)
    ElMessage.error('网络异常，请检查网络连接')
    return Promise.reject(error)
  }
)

export default service