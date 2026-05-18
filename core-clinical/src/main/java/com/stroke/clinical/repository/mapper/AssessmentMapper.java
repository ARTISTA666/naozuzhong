package com.stroke.clinical.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stroke.domain.entity.Assessment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 评估主表 Mapper
 */
@Mapper
public interface AssessmentMapper extends BaseMapper<Assessment> {

    /**
     * 查询某次就诊的所有 NIHSS 评估记录（按版本倒序）
     */
    @Select("SELECT * FROM assessment WHERE encounter_id = #{encounterId} " +
            "AND assessment_type = 'NIHSS' AND is_deleted = 0 " +
            "ORDER BY version_no DESC")
    List<Assessment> findNihssByEncounterId(@Param("encounterId") Long encounterId);

    /**
     * 查询某次就诊的最新 NIHSS 评估
     */
    @Select("SELECT * FROM assessment WHERE encounter_id = #{encounterId} " +
            "AND assessment_type = 'NIHSS' AND is_deleted = 0 " +
            "AND record_status = 'active' ORDER BY version_no DESC LIMIT 1")
    Assessment findLatestNihss(@Param("encounterId") Long encounterId);

    /**
     * 获取某个就诊评估的最大版本号
     */
    @Select("SELECT COALESCE(MAX(version_no), 0) FROM assessment " +
            "WHERE encounter_id = #{encounterId} AND assessment_type = 'NIHSS'")
    Integer getMaxVersion(@Param("encounterId") Long encounterId);
}
