package com.cumt.lyz.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cumt.lyz.backend.pojo.PredictionData;

import java.util.List;
import java.util.Map;

public interface PredictionService extends IService<PredictionData> {

    /**
     * 获取全市的预测趋势数据 (聚合后)
     */
    List<Map<String, Object>> getCityTotalTrend();

    /**
     * 获取指定行政区的预测数据
     * @param districtName 行政区名称 (如: 鼓楼区)
     */
    List<PredictionData> getDistrictTrend(String districtName);

    /**
     * 【新增】执行 Python 预测脚本
     * @return true=执行成功, false=失败
     */
    boolean runAnalysisTask();
}