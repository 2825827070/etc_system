package com.cumt.lyz.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cumt.lyz.backend.pojo.FakeVehicleAlert;
import java.util.List;

/**
 * 套牌车检测服务接口
 */
public interface FakeVehicleDetectionService extends IService<FakeVehicleAlert> {

    /**
     * 查询最近 N 小时的报警记录
     * (数据来源：Spark计算后存入的MySQL表)
     * * @param hours 时间范围
     * @return 报警实体列表
     */
    List<FakeVehicleAlert> getRecentAlerts(int hours);
}