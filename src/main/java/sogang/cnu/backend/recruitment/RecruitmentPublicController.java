package sogang.cnu.backend.recruitment;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sogang.cnu.backend.recruitment.dto.RecruitmentRequestDto;
import sogang.cnu.backend.recruitment.dto.RecruitmentResponseDto;

import java.util.List;

@RestController
@RequestMapping("/api/public/recruitments")
@RequiredArgsConstructor
public class RecruitmentPublicController {
    private final RecruitmentService recruitmentService;

    @GetMapping("/active" )
    public ResponseEntity<RecruitmentResponseDto> getActiveRecruitment() {
        return ResponseEntity.ok(recruitmentService.getActiveRecruitment());
    }

    // 홈 배너용. active 플래그가 아니라 startAt/endAt 기준으로 가장 가까운 모집 1건을 반환한다.
    @GetMapping("/closest")
    public ResponseEntity<RecruitmentResponseDto> getClosestRecruitment() {
        return ResponseEntity.ok(recruitmentService.getClosestRecruitment());
    }

}

