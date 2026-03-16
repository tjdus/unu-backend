package sogang.cnu.backend.migration.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class UserMigrationResultDto {
    private int created;
    private int skipped;
    private int failed;
    private List<String> errors;
}
