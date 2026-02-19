package sogang.cnu.backend.auth.dto;

import lombok.Getter;
import lombok.Setter;
import sogang.cnu.backend.quarter.dto.QuarterResponseDto;

@Getter
@Setter
public class UserInfoResponseDto {
    private String name;
    private String username;
    private String studentId;
    private String email;
    private String phoneNumber;
    private String githubId;
    private Boolean isActive;
    private QuarterResponseDto joinedQuarter;
}
