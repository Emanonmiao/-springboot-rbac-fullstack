import axios from 'axios'
import { Message } from 'element-ui'
import router from '@/router'
import store from '@/store'

// 创建axios实例
const request = axios.create({
  baseURL: process.env.VUE_APP_BASE_API || '/api',
  timeout: 15000
})

// 请求拦截器：携带Authorization token
request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = 'Bearer ' + token
    }
    return config
  },
  error => Promise.reject(error)
)

// 响应拦截器：处理各种业务状态码
request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code === 200) {
      return res
    }

    // 401：未登录或Token过期
    if (res.code === 401) {
      Message.error('登录已过期，请重新登录')
      store.dispatch('logout')
      router.push('/login')
      return Promise.reject(new Error(res.msg))
    }

    // 423：账号锁定
    if (res.code === 423) {
      Message.error('账号已被锁定，请联系管理员')
      return Promise.reject(new Error(res.msg))
    }

    // 428：密码过期，跳转修改密码
    if (res.code === 428) {
      Message.warning('密码已过期，请先修改密码')
      router.push('/change-password')
      return Promise.reject(new Error(res.msg))
    }

    // 403：无权限
    if (res.code === 403) {
      Message.error('没有操作权限')
      return Promise.reject(new Error(res.msg))
    }

    // 其他业务错误
    Message.error(res.msg || '请求失败')
    return Promise.reject(new Error(res.msg))
  },
  error => {
    // 网络异常/HTTP错误
    if (error.response && error.response.status === 401) {
      store.dispatch('logout')
      router.push('/login')
    }
    Message.error(error.message || '网络错误')
    return Promise.reject(error)
  }
)

export default request
