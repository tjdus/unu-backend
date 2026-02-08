package sogang.cnu.backend.recruitment.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import sogang.cnu.backend.form.dto.FormResponseDto;
import sogang.cnu.backend.quarter.dto.QuarterResponseDto;

@Getter
@Setter
@Builder
public class RecruitmentResponseDto {
    private Long id;
    private String title;
    private String description;
    private String startAt;
    private String endAt;
    private QuarterResponseDto quarter;
    private Boolean active;
    private FormResponseDto form;
    private String createdAt;
    private String modifiedAt;
    private String createdBy;
    private String modifiedBy;
}

