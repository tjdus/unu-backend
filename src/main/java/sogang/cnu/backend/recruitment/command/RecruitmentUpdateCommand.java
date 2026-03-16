package sogang.cnu.backend.recruitment.command;

import lombok.Builder;
import lombok.Getter;
import sogang.cnu.backend.form.Form;
import sogang.cnu.backend.quarter.Quarter;

import java.time.LocalDateTime;

@Getter
@Builder
public class RecruitmentUpdateCommand {
    private String title;
    private String description;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private Quarter quarter;
    private Boolean active;
    private Form form;
}

