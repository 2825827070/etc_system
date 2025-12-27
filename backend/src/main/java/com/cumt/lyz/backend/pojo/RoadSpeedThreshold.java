package com.cumt.lyz.backend.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("road_speed_threshold")
public class RoadSpeedThreshold {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String startBayonet;
    private String endBayonet;
    private Integer hourSlot;
    private Double thresholdSpeed;
}