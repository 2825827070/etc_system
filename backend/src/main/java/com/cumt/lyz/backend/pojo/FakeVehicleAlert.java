package com.cumt.lyz.backend.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@NoArgsConstructor
@TableName("fake_vehicle_alert")
public class FakeVehicleAlert {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String plateNumber;
    private String startBayonet;
    private String endBayonet;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endTime; // 也就是 pass_time

    private Double distance;
    private Long timeDiff;
    private Double actualSpeed;
    private Double limitSpeed;
    private String alertLevel;
    private Date createTime;
}