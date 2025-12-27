package com.cumt.lyz.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cumt.lyz.backend.pojo.PredictionData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface PredictionMapper extends BaseMapper<PredictionData> {

    /**
     * 聚合查询：按时间点统计全市的总预测流量
     * (用于前端展示"全市未来趋势"折线图)
     */
    @Select("SELECT predict_time, SUM(predicted_volume) as total_volume " +
            "FROM traffic_prediction " +
            "GROUP BY predict_time " +
            "ORDER BY predict_time ASC")
    List<Map<String, Object>> getCityTotalPrediction();
}