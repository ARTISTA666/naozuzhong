package com.stroke.clinical.service;

import com.stroke.clinical.dto.assessment.NihssAssessmentVO;
import com.stroke.clinical.dto.assessment.NihssItemVO;
import com.stroke.clinical.dto.assessment.SubmitNihssRequest;
import com.stroke.clinical.repository.mapper.AssessmentItemMapper;
import com.stroke.clinical.repository.mapper.AssessmentMapper;
import com.stroke.clinical.service.assessment.NihssScaleConfig;
import com.stroke.common.BusinessException;
import com.stroke.domain.entity.Assessment;
import com.stroke.domain.entity.AssessmentItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 评估服务 — 对应设计文档 3.3 节
 * <p>
 * 负责 NIHSS、mRS、ASPECTS、GCS 等量表的评分管理。
 * 核心原则：
 * 1. 所有评分操作记录完整审计
 * 2. 修改即生成新版本，不覆盖原始记录
 * 3. PAD端支持离线评分，上传后标记来源
 */
@Service
public class AssessmentService {

    private static final Logger log = LoggerFactory.getLogger(AssessmentService.class);

    private final AssessmentMapper assessmentMapper;
    private final AssessmentItemMapper assessmentItemMapper;
    private final NihssScaleConfig nihssConfig;

    public AssessmentService(AssessmentMapper assessmentMapper,
                             AssessmentItemMapper assessmentItemMapper,
                             NihssScaleConfig nihssConfig) {
        this.assessmentMapper = assessmentMapper;
        this.assessmentItemMapper = assessmentItemMapper;
        this.nihssConfig = nihssConfig;
    }

    /**
     * 提交 NIHSS 评分 — 对应设计文档 3.2.2 第5步
     * <p>
     * 校验 → 计算总分 → 创建新版本 → 保存各项分数
     */
    @Transactional
    public NihssAssessmentVO submitNihss(SubmitNihssRequest request) {
        // 1. 验证评分有效性
        int totalScore = nihssConfig.validateAndCalculate(request.getScores());

        // 2. 确定版本号（新版本，不覆盖）
        Integer currentMaxVersion = assessmentMapper.getMaxVersion(request.getEncounterId());
        int newVersion = (currentMaxVersion == null ? 0 : currentMaxVersion) + 1;

        // 3. 将前一个 active 版本标记为 superseded
        Assessment latestActive = assessmentMapper.findLatestNihss(request.getEncounterId());
        if (latestActive != null) {
            latestActive.setRecordStatus("superseded");
            // 用 UpdateWrapper 绕过乐观锁（自定义SQL查询不携带 version）
            assessmentMapper.update(latestActive,
                    new com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<Assessment>()
                            .eq("id", latestActive.getId()));
        }

        // 4. 创建新评估主记录
        Assessment assessment = new Assessment();
        assessment.setPatientId(request.getPatientId());
        assessment.setEncounterId(request.getEncounterId());
        assessment.setGreenwayId(request.getGreenwayId());
        assessment.setAssessmentType("NIHSS");
        assessment.setVersionNo(newVersion);
        assessment.setTotalScore(totalScore);
        assessment.setAssessmentTime(LocalDateTime.now());
        assessment.setOperatorId(request.getOperatorId());
        assessment.setOperatorName(request.getOperatorName());
        assessment.setSource(request.getSource() != null ? request.getSource() : "ONLINE");
        assessment.setRecordStatus("active");
        assessment.setCreatedBy(request.getOperatorId());
        assessment.setCreatedTime(LocalDateTime.now());

        assessmentMapper.insert(assessment);

        // 5. 创建各条目明细
        List<AssessmentItem> items = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : request.getScores().entrySet()) {
            NihssScaleConfig.NihssItemDef def = nihssConfig.getItem(entry.getKey());
            if (def == null) continue;

            AssessmentItem item = new AssessmentItem();
            item.setAssessmentId(assessment.getId());
            item.setItemCode(entry.getKey());
            item.setItemName(def.getName());
            item.setScore(entry.getValue());
            item.setCreatedBy(request.getOperatorId());
            item.setCreatedTime(LocalDateTime.now());
            assessmentItemMapper.insert(item);
            items.add(item);
        }

        log.info("NIHSS评分提交: encounterId={}, version={}, total={}, source={}",
                request.getEncounterId(), newVersion, totalScore, assessment.getSource());

        return toNihssVO(assessment, items);
    }

    /**
     * 查询某次就诊的所有 NIHSS 评分记录
     */
    public List<NihssAssessmentVO> getNihssHistory(Long encounterId) {
        List<Assessment> assessments = assessmentMapper.findNihssByEncounterId(encounterId);
        if (assessments.isEmpty()) {
            return Collections.emptyList();
        }

        List<NihssAssessmentVO> result = new ArrayList<>();
        for (Assessment assessment : assessments) {
            List<AssessmentItem> items = assessmentItemMapper.findByAssessmentId(assessment.getId());
            result.add(toNihssVO(assessment, items));
        }
        return result;
    }

    /**
     * 查询最新 NIHSS 评分
     */
    public NihssAssessmentVO getLatestNihss(Long encounterId) {
        Assessment assessment = assessmentMapper.findLatestNihss(encounterId);
        if (assessment == null) {
            throw new BusinessException(404, "未找到NIHSS评估记录，encounterId=" + encounterId);
        }
        List<AssessmentItem> items = assessmentItemMapper.findByAssessmentId(assessment.getId());
        return toNihssVO(assessment, items);
    }

    /**
     * 查询 NIHSS 量表配置（所有条目定义）
     */
    public List<NihssItemVO> getScaleItems() {
        return nihssConfig.getAllItems().stream()
                .map(def -> new NihssItemVO(
                        def.getCode(), def.getName(), null,
                        def.getMinScore(), def.getMaxScore(), def.getDescription()))
                .collect(Collectors.toList());
    }

    // ==================== 内部转换 ====================

    private NihssAssessmentVO toNihssVO(Assessment assessment, List<AssessmentItem> items) {
        NihssAssessmentVO vo = new NihssAssessmentVO();
        vo.setAssessmentId(assessment.getId());
        vo.setEncounterId(assessment.getEncounterId());
        vo.setPatientId(assessment.getPatientId());
        vo.setVersionNo(assessment.getVersionNo());
        vo.setTotalScore(assessment.getTotalScore());
        vo.setSeverityLevel(nihssConfig.getSeverityLevel(
                assessment.getTotalScore() != null ? assessment.getTotalScore() : 0));
        vo.setAssessmentTime(assessment.getAssessmentTime());
        vo.setOperatorName(assessment.getOperatorName());
        vo.setSource(assessment.getSource());
        vo.setRecordStatus(assessment.getRecordStatus());
        vo.setItems(items.stream()
                .map(item -> {
                    NihssScaleConfig.NihssItemDef def = nihssConfig.getItem(item.getItemCode());
                    return new NihssItemVO(
                            item.getItemCode(),
                            item.getItemName(),
                            item.getScore(),
                            def != null ? def.getMinScore() : 0,
                            def != null ? def.getMaxScore() : 0,
                            def != null ? def.getDescription() : "");
                })
                .collect(Collectors.toList()));
        return vo;
    }
}
