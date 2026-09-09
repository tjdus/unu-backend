package sogang.cnu.backend.application.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class ApplicationLectureRoomScheduleImportResponse {
    private UUID quarterId;
    private UUID userId;
    private String userName;
    private int createdCount;
    private int existingCount;
}
