import request from './index'

// 1. 浏览套餐资费说明
export function getTariffs() {
  return request.get('/api/user/tariffs')
}

// 2. 用户注册/开户
export function register(data) {
  // data: { password, type }
  return request.post('/api/user/register', data)
}

// 3. 用户登录
export function login(data) {
  // data: { phone, password }
  return request.post('/api/user/login', data)
}

// 4. 模拟消费操作
export function consume(data) {
  // data: { phone, type(int), amount(int) }
  return request.post('/api/user/consume', data)
}

// 5. 话费充值
export function recharge(data) {
  // data: { phone, amount(double) }
  return request.post('/api/user/recharge', data)
}

// 6. 套餐余量查询
export function getQuota(params) {
  // params: { phone }
  return request.get('/api/user/quota', { params })
}

// 7. 打印消费详单
export function getRecords(params) {
  // params: { phone }
  return request.get('/api/user/records', { params })
}

// 8. 套餐在线变更
export function changePackage(data) {
  // data: { phone, type }
  return request.post('/api/user/change-package', data)
}

// 9. 办理退网注销
export function cancelAccount(data) {
  // data: { phone } 
  // 注意：Axios 的 delete 请求传参需要放在 config 的 data 字段中
  return request.delete('/api/user/cancel', { data })
}

// 10. 模拟系统月结
export function nextMonth() {
  return request.post('/api/user/next-month')
}

// 11. 订购流量加油包
export function buyDataPack(params) {
  // params: { phone, size }
  return request.post('/api/user/buy-data-pack', null, { params })
}

// 12. 获取解析后的音乐媒体库列表
export function getMusicList() {
  return request.get('/api/music/list')
}