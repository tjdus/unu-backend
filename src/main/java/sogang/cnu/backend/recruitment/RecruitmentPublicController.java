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

}

