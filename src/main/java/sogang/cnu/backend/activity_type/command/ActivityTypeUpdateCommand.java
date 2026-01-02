package sogang.cnu.backend.activity_type.command;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ActivityTypeUpdateCommand {
    private String name;
}

