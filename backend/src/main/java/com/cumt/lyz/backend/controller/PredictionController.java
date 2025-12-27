package com.cumt.lyz.backend.controller;

import com.cumt.lyz.backend.common.Result;
import com.cumt.lyz.backend.pojo.PredictionData;
import com.cumt.lyz.backend.service.PredictionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Api(tags = "离线流量预测接口")
@RestController
@RequestMapping("/api/prediction")
public class PredictionController {

    @Autowired
    private PredictionService predictionService;

    /**
     * 【核心功能】立即运行预测并返回结果
     * 流程：前端调用 -> Java调Python -> 等待结束 -> Java查库 -> 返回新数据
     */
    @ApiOperation("运行预测并获取最新结果")
    @PostMapping("/run")
    public Result<List<Map<String, Object>>> runAndGetPrediction() {
        // 1. 同步执行 Python 脚本 (这步会耗时几秒钟)
        boolean success = predictionService.runAnalysisTask();

        if (success) {
            // 2. 如果成功，立刻查询数据库里刚生成好的最新数据
            List<Map<String, Object>> newData = predictionService.getCityTotalTrend();

            // 3. 直接返回给前端
            return Result.success("预测成功，数据已更新", newData);
        } else {
            // 4. 【修复】手动构建失败结果，确保泛型类型匹配
            // 不能直接用 Result.fail()，因为它返回的是 Result<Void>
            Result<List<Map<String, Object>>> errorResult = new Result<>();
            errorResult.setCode(500);
            errorResult.setMessage("预测脚本执行失败，请检查后端日志");
            return errorResult;
        }
    }

    // --- 原有的普通查询接口 ---

    @ApiOperation("获取全市预测趋势")
    @GetMapping("/city-trend")
    public Result<List<Map<String, Object>>> getCityTrend() {
        return Result.success(predictionService.getCityTotalTrend());
    }

    @ApiOperation("获取区域预测数据")
    @GetMapping("/district-trend")
    public Result<List<PredictionData>> getDistrictTrend(
            @RequestParam(required = false) String districtName) {
        return Result.success(predictionService.getDistrictTrend(districtName));
    }
}