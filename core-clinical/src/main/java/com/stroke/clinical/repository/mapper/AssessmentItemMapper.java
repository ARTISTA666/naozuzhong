package com.stroke.clinical.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stroke.domain.entity.AssessmentItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 评估明细表 Mapper
 */
@Mapper
public interface AssessmentItemMapper extends BaseMapper<AssessmentItem> {

    /**
     * 查询某次评估的所有明细项
     */
    @Select("SELECT * FROM assessment_item WHERE assessment_id = #{assessmentId} AND is_deleted = 0")
    List<AssessmentItem> findByAssessmentId(@Param("assessmentId") Long assessmentId);
}
