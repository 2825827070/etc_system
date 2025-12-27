package com.cumt.lyz.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cumt.lyz.backend.pojo.EtcData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface EtcMapper extends BaseMapper<EtcData> {

    @Select("SELECT district_name as name, COUNT(*) as value FROM etc_data GROUP BY district_name")
    List<Map<String, Object>> countByDistrict();

    // 旧接口：查总数 (或者单类型筛选)
    @Select("<script>" +
            "SELECT DATE_FORMAT(pass_time, '%H:%i') as time_str, COUNT(*) as vehicle_count " +
            "FROM etc_data " +
            "WHERE pass_time &gt;= DATE_SUB(NOW(), INTERVAL 1 HOUR) " +
            "<if test='type != null and type != \"\"'> " +
            "  AND plate_type = #{type} " +
            "</if> " +
            "GROUP BY time_str " +
            "ORDER BY time_str ASC" +
            "</script>")
    List<Map<String, Object>> getRealTimeTrendAggregation(@Param("type") String type);

    /**
     * 【新增】查分类明细 (用于三条曲线)
     * 结果示例: [{"time_str": "10:01", "plate_type": "02", "vehicle_count": 5}, ...]
     */
    @Select("SELECT DATE_FORMAT(pass_time, '%H:%i') as time_str, plate_type, COUNT(*) as vehicle_count " +
            "FROM etc_data " +
            "WHERE pass_time >= DATE_SUB(NOW(), INTERVAL 1 HOUR) " +
            "GROUP BY time_str, plate_type " +
            "ORDER BY time_str ASC")
    List<Map<String, Object>> getRealTimeTrendGroupByType();
}