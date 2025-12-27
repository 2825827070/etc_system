import request from '@/utils/request'

/**
 * ETC数据API接口
 */

// 分页查询历史数据
export const getEtcList = (params) => {
    return request({
        url: '/etc/list',
        method: 'get',
        params
    })
}

// 获取总车流量统计
export const getTotalFlow = () => {
    return request({
        url: '/etc/stats/total',
        method: 'get'
    })
}

// 获取各行政区车流占比
export const getDistrictStats = () => {
    return request({
        url: '/etc/stats/district',
        method: 'get'
    })
}

// 获取实时趋势数据
export const getTrend = () => {
    return request({
        url: '/etc/stats/trend',
        method: 'get'
    })
}

// 获取按车牌类型分类的趋势数据
export const getTrendByCategory = () => {
    return request({
        url: '/etc/stats/trend/category',
        method: 'get'
    })
}

// 推送数据（扩展功能，可选）
export const pushData = (data) => {
    return request({
        url: '/etc/push',
        method: 'post',
        data
    })
}

// 获取套牌车告警数据
export const getFakeVehicleAlerts = (hours = 24) => {
    return request({
        url: '/api/fake-vehicle/alerts',
        method: 'get',
        params: { hours }
    })
}

// ========== 离线预测分析API ==========

// 运行预测任务（触发Python脚本执行）
export const runPrediction = () => {
    return request({
        url: '/api/prediction/run',
        method: 'post',
        timeout: 120000 // 预测任务可能耗时较长，设置2分钟超时
    })
}

// 获取全市预测趋势数据
export const getCityPredictionTrend = () => {
    return request({
        url: '/api/prediction/city-trend',
        method: 'get'
    })
}

// 获取区域预测趋势数据
export const getDistrictPredictionTrend = (districtName) => {
    return request({
        url: '/api/prediction/district-trend',
        method: 'get',
        params: districtName ? { districtName } : {}
    })
}
