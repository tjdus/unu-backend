package sogang.cnu.backend.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sogang.cnu.backend.application.command.ApplicationCreateCommand;
import sogang.cnu.backend.application.command.ApplicationUpdateCommand;
import sogang.cnu.backend.application.dto.ApplicationRequestDto;
import sogang.cnu.backend.application.dto.ApplicationResponse;
import sogang.cnu.backend.application.dto.ApplicationLookupRequestDto;
import sogang.cnu.backend.application.dto.ApplicationLookupResponse;
import sogang.cnu.backend.application.dto.ApplicationVerificationResponse;
import sogang.cnu.backend.common.exception.BadRequestException;
import sogang.cnu.backend.common.exception.ForbiddenException;
import sogang.cnu.backend.common.exception.NotFoundException;
import sogang.cnu.backend.recruitment.Recruitment;
import sogang.cnu.backend.recruitment.RecruitmentRepository;
import sogang.cnu.backend.security.JwtTokenProvider;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationService {
    private final ApplicationRepository applicationRepository;
    private final ApplicationMapper applicationMapper;
    private final RecruitmentRepository recruitmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationAnswerValidator answerValidator;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional(readOnly = true)
    public ApplicationResponse getById(UUID id) {
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Application not found"));
        return applicationMapper.toResponseDto(application);
    }

    @Transactional(readOnly = true)
    public ApplicationVerificationResponse verify(UUID id, String password) {
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Application not found"));

        validatePassword(password, application.getPassword());

        return ApplicationVerificationResponse.builder()
                .application(applicationMapper.toResponseDto(application))
                .accessToken(jwtTokenProvider.generateApplicationToken(application.getId()))
                .build();
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> getByRecruitmentId(UUID recruitmentId) {
        return applicationRepository.findByRecruitmentId(recruitmentId).stream()
                .map(applicationMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ApplicationResponse create(ApplicationRequestDto dto) {
        validateCreatePassword(dto.getPassword());
        Recruitment recruitment = findRecruitmentForUpdate(dto.getRecruitmentId());
        String studentId = dto.getStudentId().trim();
        String email = dto.getEmail().trim();

        validateRecruitmentActive(recruitment);
        validateRecruitmentPeriod(recruitment);
        validateNoDuplicate(recruitment.getId(), studentId, email, null);
        answerValidator.validate(recruitment.getForm().getSchema(), dto.getAnswers());

        ApplicationCreateCommand command = ApplicationCreateCommand.builder()
                .recruitment(recruitment)
                .name(dto.getName().trim())
                .studentId(studentId)
                .major(dto.getMajor().trim())
                .subMajor(trimToNull(dto.getSubMajor()))
                .email(email)
                .githubId(trimToNull(dto.getGithubId()))
                .phoneNumber(dto.getPhoneNumber().trim())
                .answers(dto.getAnswers())
                .formSnapshot(recruitment.getForm().getSchema())
                .password(passwordEncoder.encode(dto.getPassword()))
                .build();

        Application application = Application.create(command);
        Application savedApplication = applicationRepository.save(application);
        return applicationMapper.toResponseDto(savedApplication);
    }

    @Transactional
    public ApplicationResponse update(UUID id, ApplicationRequestDto dto, String accessToken) {
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Application not found"));

        // Only allow updates if the application is still in APPLIED status
        if (application.getStatus() != ApplicationStatus.APPLIED) {
            throw new BadRequestException("검토가 시작된 지원서는 수정할 수 없습니다.");
        }

        authorizeApplicant(application, accessToken, dto.getPassword());
        if (!application.getRecruitment().getId().equals(dto.getRecruitmentId())) {
            throw new BadRequestException("지원서의 모집 정보를 변경할 수 없습니다.");
        }

        Recruitment recruitment = findRecruitmentForUpdate(application.getRecruitment().getId());
        String studentId = dto.getStudentId().trim();
        String email = dto.getEmail().trim();
        validateRecruitmentActive(recruitment);
        validateRecruitmentPeriod(recruitment);
        validateNoDuplicate(recruitment.getId(), studentId, email, application.getId());
        answerValidator.validate(application.getFormSnapshot(), dto.getAnswers());

        ApplicationUpdateCommand command = ApplicationUpdateCommand.builder()
                .name(dto.getName().trim())
                .studentId(studentId)
                .major(dto.getMajor().trim())
                .subMajor(trimToNull(dto.getSubMajor()))
                .email(email)
                .githubId(trimToNull(dto.getGithubId()))
                .phoneNumber(dto.getPhoneNumber().trim())
                .answers(dto.getAnswers())
                .build();

        application.update(command);
        return applicationMapper.toResponseDto(application);
    }

    @Transactional
    public void delete(UUID id) {
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Application not found"));

        applicationRepository.delete(application);
    }

    @Transactional(readOnly = true)
    public ApplicationLookupResponse lookup(ApplicationLookupRequestDto query) {
        Application application = applicationRepository.findFirstByNameAndEmailOrderByCreatedAtDesc(query.getName(), query.getEmail())
                .orElseThrow(() -> new NotFoundException("Application not found"));
        // 비밀번호 검증 전 단계이므로 존재 확인에 필요한 최소 정보만 반환한다.
        // 답변/학번/전화번호 등은 /verify(비밀번호 필요)에서만 노출된다.
        return ApplicationLookupResponse.builder()
                .id(application.getId())
                .name(application.getName())
                .email(application.getEmail())
                .status(application.getStatus() != null ? application.getStatus().name() : null)
                .createdAt(application.getCreatedAt() != null ? application.getCreatedAt().toString() : null)
                .build();
    }

    @Transactional
    public void cancelByApplicant(UUID id, String password, String accessToken) {
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Application not found"));

        authorizeApplicant(application, accessToken, password);

        if (application.getStatus() != ApplicationStatus.APPLIED) {
            throw new BadRequestException("제출 상태의 지원서만 취소할 수 있습니다.");
        }

        application.updateStatus(ApplicationStatus.CANCELED);
    }

    @Transactional
    public ApplicationResponse updateStatus(UUID id, String status) {
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Application not found"));

        ApplicationStatus newStatus = ApplicationStatus.valueOf(status);
        validateStatusTransition(application.getStatus(), newStatus);

        application.updateStatus(newStatus);
        return applicationMapper.toResponseDto(application);
    }

    @Transactional
    public void cancel(UUID id) {
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Application not found"));

        if (application.getStatus() != ApplicationStatus.APPLIED) {
            throw new IllegalStateException("Can only cancel applications in APPLIED status");
        }

        application.updateStatus(ApplicationStatus.CANCELED);
    }

    private Recruitment findRecruitmentForUpdate(UUID recruitmentId) {
        return recruitmentRepository.findByIdForUpdate(recruitmentId)
                .orElseThrow(() -> new NotFoundException("Recruitment not found"));
    }

    private void validateRecruitmentActive(Recruitment recruitment) {
        if (!recruitment.getActive()) {
            throw new BadRequestException("현재 지원할 수 없는 모집입니다.");
        }
    }

    private void validateRecruitmentPeriod(Recruitment recruitment) {
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(recruitment.getStartAt()) || now.isAfter(recruitment.getEndAt())) {
            throw new BadRequestException("모집 기간에만 지원서를 제출하거나 수정할 수 있습니다.");
        }
    }

    private void validateStatusTransition(ApplicationStatus currentStatus, ApplicationStatus newStatus) {
        // 같은 상태로는 변경 불가
        if (currentStatus == newStatus) {
            throw new BadRequestException("Cannot transition to the same status");
        }

        // CANCELED 상태에서는 어떤 상태로도 변경 불가
        if (currentStatus == ApplicationStatus.CANCELED) {
            throw new BadRequestException("Cannot change status from CANCELED");
        }
    }

    private void validatePassword(String rawPassword, String encodedPassword) {
        if (rawPassword == null || !passwordEncoder.matches(rawPassword, encodedPassword)) {
            throw new ForbiddenException("비밀번호가 올바르지 않습니다.");
        }
    }

    private void authorizeApplicant(Application application, String accessToken, String password) {
        if (accessToken != null && !accessToken.isBlank()) {
            if (!jwtTokenProvider.isApplicationTokenFor(accessToken, application.getId())) {
                throw new ForbiddenException("지원서 인증이 만료되었거나 올바르지 않습니다.");
            }
            return;
        }
        validatePassword(password, application.getPassword());
    }

    private void validateCreatePassword(String password) {
        if (password == null || password.isBlank() || password.length() < 6 || password.length() > 100) {
            throw new BadRequestException("비밀번호는 6자 이상 100자 이하여야 합니다.");
        }
    }

    private void validateNoDuplicate(UUID recruitmentId, String studentId, String email, UUID excludedId) {
        boolean duplicate;
        if (excludedId == null) {
            duplicate = applicationRepository.existsByRecruitmentIdAndStudentIdAndStatusNot(
                    recruitmentId, studentId, ApplicationStatus.CANCELED)
                    || applicationRepository.existsByRecruitmentIdAndEmailIgnoreCaseAndStatusNot(
                    recruitmentId, email, ApplicationStatus.CANCELED);
        } else {
            duplicate = applicationRepository.existsByRecruitmentIdAndStudentIdAndStatusNotAndIdNot(
                    recruitmentId, studentId, ApplicationStatus.CANCELED, excludedId)
                    || applicationRepository.existsByRecruitmentIdAndEmailIgnoreCaseAndStatusNotAndIdNot(
                    recruitmentId, email, ApplicationStatus.CANCELED, excludedId);
        }
        if (duplicate) {
            throw new BadRequestException("이미 제출한 지원서가 있습니다.");
        }
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
