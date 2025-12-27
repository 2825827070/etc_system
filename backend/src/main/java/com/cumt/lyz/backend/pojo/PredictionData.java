package com.cumt.lyz.backend.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.util.Date;

/**
 * 离线预测数据实体类
 * 对应表: traffic_prediction
 */
@Data
@TableName("traffic_prediction")
public class PredictionData {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 行政区划名称 (如: 鼓楼区)
     */
    private String districtName;

    /**
     * 预测的时间点 (未来时间)
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date predictTime;

    /**
     * 预测的车流量
     */
    private Integer predictedVolume;

    /**
     * 计算产生的时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}