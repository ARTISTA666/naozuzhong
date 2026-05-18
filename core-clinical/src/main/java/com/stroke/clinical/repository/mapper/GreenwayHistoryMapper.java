package com.stroke.clinical.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stroke.domain.entity.StrokeGreenwayHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 绿道历史记录 Mapper
 */
@Mapper
public interface GreenwayHistoryMapper extends BaseMapper<StrokeGreenwayHistory> {

    /**
     * 查询绿道的所有历史记录
     */
    @Select("SELECT * FROM stroke_greenway_history WHERE greenway_id = #{greenwayId} ORDER BY action_time ASC")
    List<StrokeGreenwayHistory> findByGreenwayId(@Param("greenwayId") Long greenwayId);
}
