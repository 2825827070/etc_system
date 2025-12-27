package com.cumt.lyz.backend.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cumt.lyz.backend.mapper.EtcMapper;
import com.cumt.lyz.backend.pojo.EtcData;
import com.cumt.lyz.backend.service.EtcService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class EtcServiceImpl extends ServiceImpl<EtcMapper, EtcData> implements EtcService {

    @Override
    public Page<EtcData> getHistoryList(int page, int size, String plateNumber) {
        Page<EtcData> pageParam = new Page<>(page, size);
        QueryWrapper<EtcData> query = new QueryWrapper<>();
        if (plateNumber != null && !plateNumber.isEmpty()) {
            query.like("plate_number", plateNumber);
        }
        query.orderByDesc("pass_time");
        return this.page(pageParam, query);
    }

    @Override
    public List<Map<String, Object>> getRealTimeTrend(String type) {
        return baseMapper.getRealTimeTrendAggregation(type);
    }

    // ========== 【新增】多曲线数据处理逻辑 ==========
    @Override
    public Map<String, Object> getTrendByCategory() {
        // 1. 查出原始数据 (时间, 类型, 数量)
        List<Map<String, Object>> rawList = baseMapper.getRealTimeTrendGroupByType();

        // 2. 提取所有不重复的时间点 (作为 X 轴)
        Set<String> timeSet = new TreeSet<>(); // TreeSet 自动排序
        for (Map<String, Object> map : rawList) {
            timeSet.add((String) map.get("time_str"));
        }
        List<String> xData = new ArrayList<>(timeSet);

        // 3. 初始化三个数据列表 (全部填0)
        List<Long> blueData = new ArrayList<>(); // 02
        List<Long> yellowData = new ArrayList<>(); // 01
        List<Long> greenData = new ArrayList<>(); // 52

        // 4. 构建快速查找表 (Time + Type -> Count)
        Map<String, Long> lookup = new HashMap<>();
        for (Map<String, Object> map : rawList) {
            String key = map.get("time_str") + "_" + map.get("plate_type");
            lookup.put(key, ((Number) map.get("vehicle_count")).longValue());
        }

        // 5. 遍历时间轴，填充数据 (数据补全)
        for (String time : xData) {
            blueData.add(lookup.getOrDefault(time + "_02", 0L));
            yellowData.add(lookup.getOrDefault(time + "_01", 0L));
            greenData.add(lookup.getOrDefault(time + "_52", 0L));
        }

        // 6. 封装返回
        Map<String, Object> result = new HashMap<>();
        result.put("xData", xData);
        result.put("blue", blueData);
        result.put("yellow", yellowData);
        result.put("green", greenData);
        return result;
    }

    @Override
    public List<Map<String, Object>> getDistrictStats() {
        return baseMapper.countByDistrict();
    }

    @Override
    public EtcData getLastRecord(String plateNumber) {
        QueryWrapper<EtcData> query = new QueryWrapper<>();
        query.eq("plate_number", plateNumber)
                .orderByDesc("pass_time")
                .last("LIMIT 1");
        return this.getOne(query);
    }
}