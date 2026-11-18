package sogang.cnu.backend.recruitment.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import sogang.cnu.backend.common.domain.dto.AuditorDto;
import sogang.cnu.backend.quarter.dto.QuarterResponseDto;
import sogang.cnu.backend.recruitment.RecruitmentType;

import java.util.UUID;

@Getter
@Setter
@Builder
public class RecruitmentResponseDto {
    private UUID id;
    private String title;
    private String description;
    private String completionMessage;
    private String startAt;
    private String endAt;
    private QuarterResponseDto quarter;
    private Boolean active;
    private JsonNode questions;
    private String createdAt;
    private String modifiedAt;
    private AuditorDto createdBy;
    private AuditorDto modifiedBy;
    private RecruitmentType type;
}
