package sogang.cnu.backend.auth.DTO;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignUpResponseDTO {
    private Long id;
    private String email;
}
