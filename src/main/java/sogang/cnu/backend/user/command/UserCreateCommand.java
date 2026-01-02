package sogang.cnu.backend.user.command;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import sogang.cnu.backend.quarter.Season;

import java.time.LocalDate;

@Getter
@Setter
@Builder
public class UserCreateCommand {
    private String name;
    private String username;
    private String password;
    private String studentId;
    private String githubId;
    private String phoneNumber;
    private String email;
    private Boolean isActive;
}

