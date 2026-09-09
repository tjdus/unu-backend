package sogang.cnu.backend.application.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

/**
 * 비밀번호 검증 없이 이름+이메일만으로 조회되는 응답이므로 최소 정보만 담는다.
 * 답변·학번·전화번호 등 민감 정보는 여기 넣지 않는다(비밀번호 검증 후 /verify에서만 반환).
 */
@Getter
@Builder
public class ApplicationLookupResponse {
    private UUID id;
    private String name;
    private String email;
    private String status;
    private String createdAt;
}
