package com.cumt.lyz.backend.service.Impl;

import com.cumt.lyz.backend.pojo.EtcData;
import com.cumt.lyz.backend.service.CrossProvinceFlowService;
import org.springframework.stereotype.Service;

@Service
public class CrossProvinceFlowServiceImpl implements CrossProvinceFlowService {

    @Override
    public boolean isProvincialBorder(String bayonetName) {
        if (bayonetName == null || bayonetName.isEmpty()) return false;
        // 只要卡口名字里带这些词，就认为是省界
        return bayonetName.contains("界") || bayonetName.contains("省际")
                || bayonetName.contains("跨省") || bayonetName.contains("检查站");
    }

    @Override
    public int getFlowDirection(EtcData etcData) {
        String bayonet = etcData.getBayonetName();
        String direction = etcData.getDirectionType();

        if (!isProvincialBorder(bayonet)) {
            return 0; // 省内
        }

        // 1. 根据方向字段判断
        if (direction != null) {
            if (direction.contains("上行") || direction.equals("1")) return 1; // 入徐
            if (direction.contains("下行") || direction.equals("2")) return 2; // 出徐
        }

        // 2. 根据卡口名称关键字推断
        if (bayonet.contains("入") || bayonet.contains("进")) return 1;
        if (bayonet.contains("出") || bayonet.contains("往")) return 2;

        // 3. 简单兜底：根据非徐州地区车牌或者其他逻辑（此处简化）
        return 0;
    }

    @Override
    public String getFlowDirectionName(int direction) {
        switch (direction) {
            case 1: return "入徐";
            case 2: return "出徐";
            default: return "省内";
        }
    }
}