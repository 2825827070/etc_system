SET NAMES utf8mb4;

-- 1. ETC通行数据表 / ETC Data Table
CREATE TABLE IF NOT EXISTS `etc_data` (
    `id` bigint(20) NOT NULL COMMENT '主键ID (Snowflake)',
    `district_name` varchar(50) DEFAULT NULL COMMENT '行政区划',
    `bayonet_name` varchar(100) DEFAULT NULL COMMENT '卡口名称',
    `direction_type` varchar(50) DEFAULT NULL COMMENT '方向类型',
    `pass_time` datetime DEFAULT NULL COMMENT '过车时间',
    `plate_type` varchar(50) DEFAULT NULL COMMENT '号牌种类',
    `plate_number` varchar(20) DEFAULT NULL COMMENT '车牌号',
    `vehicle_model` varchar(100) DEFAULT NULL COMMENT '车辆品牌',
    `latitude` double DEFAULT NULL COMMENT '纬度',
    `longitude` double DEFAULT NULL COMMENT '经度',
    `flow_direction` int(11) DEFAULT NULL COMMENT '流向标识',
    PRIMARY KEY (`id`),
    KEY `idx_pass_time` (`pass_time`),
    KEY `idx_district` (`district_name`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- 2. 离线预测结果表 / Prediction Data Table
CREATE TABLE IF NOT EXISTS `traffic_prediction` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `district_name` varchar(50) DEFAULT NULL,
    `predict_time` datetime DEFAULT NULL COMMENT '预测时间点',
    `predicted_volume` int(11) DEFAULT NULL COMMENT '预测流量',
    `create_time` datetime DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- 3. 套牌车告警表 / Fake Vehicle Alert Table
CREATE TABLE IF NOT EXISTS `fake_vehicle_alert` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `plate_number` varchar(20) DEFAULT NULL COMMENT '异常车牌',
    `start_bayonet` varchar(100) DEFAULT NULL,
    `end_bayonet` varchar(100) DEFAULT NULL,
    `start_time` datetime DEFAULT NULL,
    `end_time` datetime DEFAULT NULL,
    `distance` double DEFAULT NULL,
    `time_diff` bigint(20) DEFAULT NULL,
    `actual_speed` double DEFAULT NULL,
    `limit_speed` double DEFAULT NULL,
    `alert_level` varchar(20) DEFAULT NULL,
    `create_time` datetime DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;