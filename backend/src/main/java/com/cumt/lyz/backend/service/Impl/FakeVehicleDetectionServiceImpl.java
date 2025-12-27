package com.cumt.lyz.backend.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cumt.lyz.backend.mapper.FakeVehicleAlertMapper;
import com.cumt.lyz.backend.pojo.FakeVehicleAlert;
import com.cumt.lyz.backend.service.FakeVehicleDetectionService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

/**
 * 业务逻辑实现类
 * 继承 ServiceImpl 自动获得 Mapper 的注入 (this.baseMapper)
 */
@Service
public class FakeVehicleDetectionServiceImpl
        extends ServiceImpl<FakeVehicleAlertMapper, FakeVehicleAlert>
        implements FakeVehicleDetectionService {

    @Override
    public List<FakeVehicleAlert> getRecentAlerts(int hours) {
        // 1. 计算时间阈值 (当前时间 - N小时)
        LocalDateTime startTime = LocalDateTime.now().minusHours(hours);
        Date startDate = Date.from(startTime.atZone(ZoneId.systemDefault()).toInstant());

        // 2. 构建查询条件
        QueryWrapper<FakeVehicleAlert> query = new QueryWrapper<>();
        query.ge("create_time", startDate) // 筛选：创建时间 >= 开始时间
                .orderByDesc("create_time")   // 排序：最新的在最前
                .last("LIMIT 50");            // 限制：只取最新的50条，防止大屏卡顿

        // 3. 执行查询
        // baseMapper 是 MyBatis-Plus 自动注入的 FakeVehicleAlertMapper
        return this.list(query);
    }
}