package com.stroke.clinical.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stroke.domain.entity.StrokeGreenway;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 绿道记录 Mapper
 */
@Mapper
public interface GreenwayMapper extends BaseMapper<StrokeGreenway> {

    /**
     * 乐观锁方式更新状态
     *
     * @param greenway 绿道记录（需包含 id, version 和要更新的字段）
     * @return 影响行数（0 表示乐观锁冲突）
     */
    @Update("UPDATE stroke_greenway SET " +
            "status = #{g.status}, " +
            "ct_order_time = #{g.ctOrderTime}, " +
            "ct_complete_time = #{g.ctCompleteTime}, " +
            "decision_time = #{g.decisionTime}, " +
            "needle_time = #{g.needleTime}, " +
            "abort_reason = #{g.abortReason}, " +
            "updated_by = #{g.updatedBy}, " +
            "updated_time = #{g.updatedTime}, " +
            "version = version + 1 " +
            "WHERE id = #{g.id} AND version = #{g.version}")
    int optimisticUpdate(@Param("g") StrokeGreenway greenway);

    /**
     * 根据就诊ID查询绿道
     */
    @Select("SELECT * FROM stroke_greenway WHERE encounter_id = #{encounterId} AND is_deleted = 0")
    StrokeGreenway findByEncounterId(@Param("encounterId") Long encounterId);
}
