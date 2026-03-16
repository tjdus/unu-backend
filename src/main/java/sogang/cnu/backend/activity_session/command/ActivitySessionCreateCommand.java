package sogang.cnu.backend.activity_session.command;

import lombok.Builder;
import lombok.Getter;
import sogang.cnu.backend.activity.Activity;

import java.time.LocalDate;


@Getter
@Builder
public class ActivitySessionCreateCommand {
    private Activity activity;
    private Integer sessionNumber;
    private LocalDate date;
    private String description;
}

