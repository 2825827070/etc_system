package com.cumt.lyz.backend.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;

/**
 * ETC通行数据实体类
 * 对应数据库表: etc_data
 * MyCat 会根据 ID 将数据分片存储到 db1 和 db2
 */
@Data
@TableName("etc_data")
public class EtcData implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID (对应 CSV: GCXH)
     * 注意：使用 MyCat 时，ID通常由雪花算法生成，
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * 行政区划 (对应 CSV: XZQHMC)
     * 自动映射数据库字段: district_name
     */
    private String districtName;
    /**
     * 卡口名称 (对应 CSV: KKMC)
     * 自动映射数据库字段: bayonet_name
     */
    private String bayonetName;
    /**
     * 方向类型 (对应 CSV: FXLX)
     * 自动映射数据库字段: direction_type
     */
    private String directionType;
    /**
     * 过车时间 (对应 CSV: GCSJ)
     * 自动映射数据库字段: pass_time
     * @JsonFormat 用于将后端 Date 对象转换为前端易读的字符串
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date passTime;
    /**
     * 号牌种类 (对应 CSV: HPZL)
     * 自动映射数据库字段: plate_type
     */
    private String plateType;
    /**
     * 车牌号 (对应 CSV: HPHM)
     * 自动映射数据库字段: plate_number
     */
    private String plateNumber;
    /**
     * 车辆品牌型号 (对应 CSV: CLPPXH)
     * 自动映射数据库字段: vehicle_model
     */
    private String vehicleModel;

    private Double latitude;

    private Double longitude;

    private Integer flowDirection;


}