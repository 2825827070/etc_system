package com.cumt.lyz.backend.service;

import com.cumt.lyz.backend.pojo.EtcData;

public interface CrossProvinceFlowService {

    /**
     * 判断卡口是否为省际卡口
     */
    boolean isProvincialBorder(String bayonetName);

    /**
     * 判断流向
     * @return 0=省内, 1=入徐, 2=出徐
     */
    int getFlowDirection(EtcData etcData);

    /**
     * 获取流向的中文描述
     */
    String getFlowDirectionName(int direction);
}