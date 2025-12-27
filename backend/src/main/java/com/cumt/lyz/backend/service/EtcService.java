package com.cumt.lyz.backend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cumt.lyz.backend.pojo.EtcData;

import java.util.List;
import java.util.Map;

public interface EtcService extends IService<EtcData> {

    /**
     * 分页查询历史记录
     */
    Page<EtcData> getHistoryList(int page, int size, String plateNumber);

    /**
     * 【修改】获取大屏实时趋势
     * 改为返回聚合后的统计数据 (时间点 + 流量值)
     */
    List<Map<String, Object>> getRealTimeTrend(String type);

    /**
     * 获取各行政区车流量占比
     */
    List<Map<String, Object>> getDistrictStats();

    /**
     * 获取某车辆的最后一条记录（用于套牌车检测比对）
     */
    EtcData getLastRecord(String plateNumber);

    /**
     * 【新增】获取多曲线趋势数据
     * 
     * @return 包含 xData(时间轴), blue(蓝牌数据), yellow(黄牌数据), green(绿牌数据)
     */
    Map<String, Object> getTrendByCategory();
}