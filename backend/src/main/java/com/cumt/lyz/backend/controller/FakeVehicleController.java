package com.cumt.lyz.backend.controller;

import com.cumt.lyz.backend.common.Result;
import com.cumt.lyz.backend.pojo.FakeVehicleAlert;
import com.cumt.lyz.backend.service.FakeVehicleDetectionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 套牌车告警接口
 * 负责接收前端的大屏查询请求
 */
@Api(tags = "套牌车检测与告警")
@RestController
@RequestMapping("/api/fake-vehicle")
public class FakeVehicleController {

    // ✅ 规范：Controller 调用 Service，不直接调 Mapper
    @Autowired
    private FakeVehicleDetectionService fakeService;

    /**
     * 获取最近 N 小时的套牌报警记录
     * 前端大屏会轮询调用此接口
     *
     * @param hours 时间范围（默认24小时）
     * @return 报警列表
     */
    @ApiOperation("获取最近报警记录")
    @GetMapping("/alerts")
    public Result<List<FakeVehicleAlert>> getAlerts(
            @RequestParam(defaultValue = "24") int hours) {

        // 逻辑下沉到 Service
        List<FakeVehicleAlert> alerts = fakeService.getRecentAlerts(hours);

        return Result.success(alerts);
    }
}