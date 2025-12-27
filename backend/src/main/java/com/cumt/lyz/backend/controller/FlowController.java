package com.cumt.lyz.backend.controller;

import com.cumt.lyz.backend.common.Result;
import com.cumt.lyz.backend.mapper.EtcMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 流量统计API（极速优化版）
 */
@RestController
@RequestMapping("/api/flow")
public class FlowController {

    @Autowired
    private EtcMapper etcMapper;

    /**
     * 获取地区流量统计
     * 优化手段：使用数据库 GROUP BY 替代内存循环，大幅降低 IO 开销
     */
    @GetMapping("/district-traffic")
    public Result<Map<String, Object>> getDistrictTraffic(
            @RequestParam(defaultValue = "all") String timeRange) {
        try {
            // 1. 【核心优化】直接在数据库层面聚合，只返回几十条统计结果
            // 返回结构示例: [{"name": "徐州市鼓楼区", "value": 5000}, {"name": "跨省", "value": 20}]
            List<Map<String, Object>> rawStats = etcMapper.countByDistrict();

            // 2. 地区名称归一化映射配置
            Map<String, String> nameNormalizationMap = new HashMap<>();
            nameNormalizationMap.put("铜山县", "铜山区");
            nameNormalizationMap.put("市辖区", "铜山区");
            nameNormalizationMap.put("高速五大队", "铜山区"); // 之前的脏数据映射
            nameNormalizationMap.put("跨省", "铜山区");

            // 3. 二次聚合 (在内存中清洗这几十条数据，速度极快)
            Map<String, Long> districtVolume = new HashMap<>();
            long totalVolume = 0;

            for (Map<String, Object> map : rawStats) {
                String rawName = (String) map.get("name");
                // 注意：数据库返回的 count 类型可能是 Integer 或 Long，安全转换
                Number num = (Number) map.get("value");
                long count = num != null ? num.longValue() : 0;

                if (rawName != null && !rawName.isEmpty()) {
                    // 清洗名字：去掉 "徐州市" 前缀
                    String cleanName = rawName.replace("徐州市", "").trim();
                    // 映射修正
                    String finalName = nameNormalizationMap.getOrDefault(cleanName, cleanName);

                    // 累加 (防止多个原始名字映射到同一个区，比如 "铜山县" 和 "铜山区" 要合并)
                    districtVolume.put(finalName, districtVolume.getOrDefault(finalName, 0L) + count);

                    totalVolume += count;
                }
            }

            // 4. 构建返回结果
            List<DistrictStat> districts = new ArrayList<>();
            for (Map.Entry<String, Long> entry : districtVolume.entrySet()) {
                DistrictStat stat = new DistrictStat();
                stat.name = entry.getKey();
                stat.volume = entry.getValue();
                districts.add(stat);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("districts", districts);
            result.put("totalVolume", totalVolume);

            return Result.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.success(new HashMap<>());
        }
    }

    // 内部类 DTO
    public static class DistrictStat {
        public String name;
        public long volume; // 改成 long 以防溢出
    }
}