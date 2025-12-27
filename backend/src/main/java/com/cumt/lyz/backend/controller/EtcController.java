package com.cumt.lyz.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cumt.lyz.backend.common.Result;
import com.cumt.lyz.backend.pojo.EtcData;
import com.cumt.lyz.backend.service.EtcService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Api(tags = "ETC数据管理与大屏接口")
@RestController
@RequestMapping("/etc")
public class EtcController {

    @Autowired
    private EtcService etcService;

    // ... list, stats/total, stats/district 保持不变 ...
    @ApiOperation("分页查询历史数据")
    @GetMapping("/list")
    public Result<Page<EtcData>> list(@RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String plateNumber) {
        Page<EtcData> result = etcService.getHistoryList(page, size, plateNumber);
        return Result.success(result);
    }

    @ApiOperation("大屏：获取总车流量")
    @GetMapping("/stats/total")
    public Result<Long> getTotalFlow() {
        return Result.success(etcService.count());
    }

    @ApiOperation("大屏：获取各行政区车流占比")
    @GetMapping("/stats/district")
    public Result<List<Map<String, Object>>> getDistrictStats() {
        return Result.success(etcService.getDistrictStats());
    }

    // 1. 旧接口：单曲线 (查询全部)
    @ApiOperation("大屏：获取总趋势(单条线)")
    @GetMapping("/stats/trend")
    public Result<List<Map<String, Object>>> getTrend() {
        return Result.success(etcService.getRealTimeTrend(null));
    }

    /**
     * 2. 【新增】多曲线接口 (查询分类)
     * 返回格式: { "xData": [...], "blue": [...], "yellow": [...], "green": [...] }
     */
    @ApiOperation("大屏：获取分类趋势(三条线)")
    @GetMapping("/stats/trend/category")
    public Result<Map<String, Object>> getTrendByCategory() {
        return Result.success(etcService.getTrendByCategory());
    }

    // ... push 接口保持不变 ...
    @PostMapping("/push")
    public Result<Boolean> pushData(@RequestBody EtcData data) {
        return Result.success(etcService.save(data));
    }
}