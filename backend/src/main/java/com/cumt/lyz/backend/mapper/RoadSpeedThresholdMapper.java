package com.cumt.lyz.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cumt.lyz.backend.pojo.RoadSpeedThreshold;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface RoadSpeedThresholdMapper extends BaseMapper<RoadSpeedThreshold> {
    // 专门查阈值的方法
    @Select("SELECT threshold_speed FROM road_speed_threshold WHERE start_bayonet=#{start} AND end_bayonet=#{end} AND hour_slot=#{hour} LIMIT 1")
    Double getThreshold(@Param("start") String start, @Param("end") String end, @Param("hour") int hour);
}