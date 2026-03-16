package sogang.cnu.backend.attendance.command;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Getter;
import sogang.cnu.backend.attendance.AttendanceStatus;

@Getter
@Builder
public class AttendanceUpdateCommand {
    private AttendanceStatus status;
}

