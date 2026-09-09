package sogang.cnu.backend.recruitment.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import sogang.cnu.backend.common.domain.dto.AuditorDto;
import sogang.cnu.backend.form.dto.FormResponseDto;
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
    private FormResponseDto form;
    private String createdAt;
    private String modifiedAt;
    private AuditorDto createdBy;
    private AuditorDto modifiedBy;
    private RecruitmentType type;
}
