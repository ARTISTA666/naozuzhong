package com.stroke.clinical.dto.assessment;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * NIHSS 评分提交请求
 */
@Schema(description = "NIHSS评分提交请求")
public class SubmitNihssRequest {

    @NotNull(message = "就诊ID不能为空")
    @Schema(description = "就诊ID")
    private Long encounterId;

    @NotNull(message = "患者ID不能为空")
    @Schema(description = "患者ID")
    private Long patientId;

    @Schema(description = "绿道ID（可选）")
    private Long greenwayId;

    @NotEmpty(message = "评分项不能为空")
    @Schema(description = "评分项: 条目编码→分数", example = "{\"1a\": 0, \"1b\": 1, \"4\": 2, \"5a\": 3}")
    private Map<String, Integer> scores;

    @Schema(description = "来源: ONLINE / OFFLINE", example = "ONLINE")
    private String source;

    @Schema(description = "操作人ID")
    private String operatorId;

    @Schema(description = "操作人姓名")
    private String operatorName;

    // ====== Getters & Setters ======

    public Long getEncounterId() { return encounterId; }
    public void setEncounterId(Long encounterId) { this.encounterId = encounterId; }

    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }

    public Long getGreenwayId() { return greenwayId; }
    public void setGreenwayId(Long greenwayId) { this.greenwayId = greenwayId; }

    public Map<String, Integer> getScores() { return scores; }
    public void setScores(Map<String, Integer> scores) { this.scores = scores; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getOperatorId() { return operatorId; }
    public void setOperatorId(String operatorId) { this.operatorId = operatorId; }

    public String getOperatorName() { return operatorName; }
    public void setOperatorName(String operatorName) { this.operatorName = operatorName; }
}
