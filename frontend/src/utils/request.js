import axios from 'axios'
import { ElMessage } from 'element-plus'

// 创建axios实例
const request = axios.create({
    baseURL: '/api',
    timeout: 10000
})

// 请求拦截器
request.interceptors.request.use(
    config => {
        return config
    },
    error => {
        console.error('请求错误：', error)
        return Promise.reject(error)
    }
)

// 响应拦截器
request.interceptors.response.use(
    response => {
        const res = response.data

        // 统一处理后端返回的Result结构
        if (res.code && res.code !== 200) {
            ElMessage.error(res.msg || '请求失败')
            return Promise.reject(new Error(res.msg || '请求失败'))
        }

        return res
    },
    error => {
        console.error('响应错误：', error)
        ElMessage.error(error.message || '网络异常，请稍后重试')
        return Promise.reject(error)
    }
)

export default request
