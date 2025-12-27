package com.cumt.lyz.backend.consumer;

import com.cumt.lyz.backend.mapper.FakeVehicleAlertMapper;
import com.cumt.lyz.backend.pojo.EtcData;
import com.cumt.lyz.backend.pojo.FakeVehicleAlert;
import com.cumt.lyz.backend.service.EtcService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class EtcConsumer {

    @Autowired
    private EtcService etcService;

    // 注入报警表的 Mapper，用于直接存库
    @Autowired
    private FakeVehicleAlertMapper alertMapper;

    private final Gson gson = new Gson();

    private Date parseTime(String timeStr) {
        try {
            if (timeStr == null) return new Date();
            // 处理 Python 发来的 isoformat 时间格式 (例如 2023-12-16T10:00:00)
            String cleanTime = timeStr.replace("T", " ");
            if (cleanTime.length() > 19) {
                cleanTime = cleanTime.substring(0, 19);
            }
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(cleanTime);
        } catch (Exception e) {
            return new Date();
        }
    }

    /**
     * 监听器 1: 处理普通通行流水 (Topic: etc_traffic)
     */
    @KafkaListener(topics = "etc_traffic", groupId = "etc-backend-group")
    public void consumeTraffic(String message) {
        try {
            JsonObject json = JsonParser.parseString(message).getAsJsonObject();
            EtcData data = new EtcData();

            // ... (这里保持你之前的普通数据解析逻辑不变) ...
            // ... 之前的代码 ...
            if (json.has("XZQHMC")) {
                String rawName = json.get("XZQHMC").getAsString().trim();
                String cleanName = rawName.replace("徐州市", "");

                // 【新增】强制统一行政区划名称 (解决铜山县/区不一致问题)
                if (cleanName.equals("铜山县")) {
                    cleanName = "铜山区";
                }

                // 【智能补全逻辑】
                // 如果清洗后名字变为空（说明原名叫"徐州市"），或者原名就是"市辖区"
                // 此时尝试从卡口名称(KKMC)里提取真正的区县名
                if ((cleanName.isEmpty() || cleanName.equals("市辖区")) && json.has("KKMC")) {
                    String kkmc = json.get("KKMC").getAsString();

                    if (kkmc.contains("铜山")) cleanName = "铜山区";
                    else if (kkmc.contains("鼓楼")) cleanName = "鼓楼区";
                    else if (kkmc.contains("云龙")) cleanName = "云龙区";
                    else if (kkmc.contains("贾汪")) cleanName = "贾汪区";
                    else if (kkmc.contains("泉山")) cleanName = "泉山区";
                    else if (kkmc.contains("丰县")) cleanName = "丰县";
                    else if (kkmc.contains("沛县")) cleanName = "沛县";
                    else if (kkmc.contains("睢宁")) cleanName = "睢宁县";
                    else if (kkmc.contains("邳州")) cleanName = "邳州市";
                    else if (kkmc.contains("新沂")) cleanName = "新沂市";
                    else {
                        // 如果实在找不到，就兜底给一个默认值，比如铜山区（因为它最大，高速最多）
                        cleanName = "铜山区";
                    }
                }

                // 最终赋值（如果 cleanName 还是空，就保留 rawName 防止存入空串）
                data.setDistrictName(cleanName.isEmpty() ? "铜山区" : cleanName);
            }
            if (json.has("KKMC")) data.setBayonetName(json.get("KKMC").getAsString());
            if (json.has("FXLX")) data.setDirectionType(json.get("FXLX").getAsString());
            if (json.has("HPZL")) data.setPlateType(json.get("HPZL").getAsString());
            if (json.has("HPHM")) data.setPlateNumber(json.get("HPHM").getAsString());
            if (json.has("CLPPXH")) data.setVehicleModel(json.get("CLPPXH").getAsString());

            if (json.has("GCSJ")) data.setPassTime(parseTime(json.get("GCSJ").getAsString()));
            else data.setPassTime(new Date());

            if (json.has("JINGDU")) {
                try { data.setLongitude(json.get("JINGDU").getAsDouble()); } catch (Exception e) {}
            }
            if (json.has("WEIDU")) {
                try { data.setLatitude(json.get("WEIDU").getAsDouble()); } catch (Exception e) {}
            }

            etcService.save(data);

        } catch (Exception e) {
            System.err.println("流水处理异常: " + e.getMessage());
        }
    }

    /**
     * 监听器 2: 处理套牌车报警 (Topic: fake_plate_alert)
     * 这是 Python 代码算好直接发过来的
     */
    @KafkaListener(topics = "fake_plate_alert", groupId = "alert-backend-group")
    public void consumeAlert(String message) {
        try {
            // 1. 解析 Python 发来的复杂 JSON
            JsonObject root = JsonParser.parseString(message).getAsJsonObject();

            // 2. 映射到我们的扁平化实体类
            FakeVehicleAlert alert = new FakeVehicleAlert();

            // 车牌
            if (root.has("plate_number")) alert.setPlateNumber(root.get("plate_number").getAsString());

            // 报警时间
            if (root.has("alert_time")) alert.setEndTime(parseTime(root.get("alert_time").getAsString()));

            // 之前的记录 (起点)
            if (root.has("previous_record")) {
                JsonObject prev = root.getAsJsonObject("previous_record");
                if (prev.has("location")) alert.setStartBayonet(prev.get("location").getAsString());
                if (prev.has("time")) alert.setStartTime(parseTime(prev.get("time").getAsString()));
            }

            // 当前的记录 (终点)
            if (root.has("current_record")) {
                JsonObject curr = root.getAsJsonObject("current_record");
                if (curr.has("location")) alert.setEndBayonet(curr.get("location").getAsString());
            }

            // 分析数据 (速度、距离)
            if (root.has("analysis")) {
                JsonObject analysis = root.getAsJsonObject("analysis");
                if (analysis.has("distance_km")) alert.setDistance(analysis.get("distance_km").getAsDouble());
                if (analysis.has("time_diff_seconds")) alert.setTimeDiff(analysis.get("time_diff_seconds").getAsLong());
                if (analysis.has("average_speed_kmh")) alert.setActualSpeed(analysis.get("average_speed_kmh").getAsDouble());
            }

            // 设置一些默认值
            alert.setLimitSpeed(120.0); // 假设限速
            alert.setAlertLevel("HIGH");
            alert.setCreateTime(new Date());

            // 3. 存入数据库
            alertMapper.insert(alert);
            System.out.println("🚨 收到套牌报警并已入库: " + alert.getPlateNumber());

        } catch (Exception e) {
            System.err.println("报警处理异常: " + e.getMessage());
            e.printStackTrace(); // 调试时打印堆栈
        }
    }
}