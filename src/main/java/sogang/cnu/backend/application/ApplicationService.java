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
import sogang.cnu.backend.common.exception.BadRequestException;
import sogang.cnu.backend.common.exception.NotFoundException;
import sogang.cnu.backend.recruitment.Recruitment;
import sogang.cnu.backend.recruitment.RecruitmentRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationService {
    private final ApplicationRepository applicationRepository;
    private final ApplicationMapper applicationMapper;
    private final RecruitmentRepository recruitmentRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public ApplicationResponse getById(Long id) {
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Application not found"));
        return applicationMapper.toResponseDto(application);
    }

    @Transactional(readOnly = true)
    public ApplicationResponse getByIdWithPassword(Long id, String password) {
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Application not found"));

        validatePassword(password, application.getPassword());

        return applicationMapper.toResponseDto(application);
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> getByRecruitmentId(Long recruitmentId) {
        return applicationRepository.findByRecruitmentId(recruitmentId).stream()
                .map(applicationMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ApplicationResponse create(ApplicationRequestDto dto) {
        Recruitment recruitment = findRecruitment(dto.getRecruitmentId());

        validateRecruitmentActive(recruitment);
        validateRecruitmentPeriod(recruitment);

        ApplicationCreateCommand command = ApplicationCreateCommand.builder()
                .recruitment(recruitment)
                .name(dto.getName())
                .studentId(dto.getStudentId())
                .major(dto.getMajor())
                .subMajor(dto.getSubMajor())
                .email(dto.getEmail())
                .githubId(dto.getGithubId())
                .phoneNumber(dto.getPhoneNumber())
                .answers(dto.getAnswers())
                .formSnapshot(recruitment.getForm().getSchema())
                .password(passwordEncoder.encode(dto.getPassword()))
                .build();

        Application application = Application.create(command);
        Application savedApplication = applicationRepository.save(application);
        return applicationMapper.toResponseDto(savedApplication);
    }

    @Transactional
    public ApplicationResponse update(Long id, ApplicationRequestDto dto) {
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Application not found"));

        // Only allow updates if the application is still in APPLIED status
        if (application.getStatus() != ApplicationStatus.APPLIED) {
            throw new IllegalStateException("Can only update applications in APPLIED status");
        }

        validatePassword(dto.getPassword(), application.getPassword());

        ApplicationUpdateCommand command = ApplicationUpdateCommand.builder()
                .name(dto.getName())
                .studentId(dto.getStudentId())
                .major(dto.getMajor())
                .subMajor(dto.getSubMajor())
                .email(dto.getEmail())
                .githubId(dto.getGithubId())
                .phoneNumber(dto.getPhoneNumber())
                .answers(dto.getAnswers())
                .build();

        application.update(command);
        return applicationMapper.toResponseDto(application);
    }

    @Transactional
    public void delete(Long id) {
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Application not found"));

        applicationRepository.delete(application);
    }

    @Transactional(readOnly = true)
    public ApplicationResponse lookup(ApplicationLookupRequestDto query) {
        Application application = applicationRepository.findFirstByNameAndEmailOrderByCreatedAtDesc(query.getName(), query.getEmail())
                .orElseThrow(() -> new NotFoundException("Application not found"));
        return applicationMapper.toResponseDto(application);
    }

    @Transactional
    public void cancelWithPassword(Long id, String password) {
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Application not found"));

        validatePassword(password, application.getPassword());

        if (application.getStatus() != ApplicationStatus.APPLIED) {
            throw new IllegalStateException("Can only cancel applications in APPLIED status");
        }

        application.updateStatus(ApplicationStatus.CANCELED);
    }

    @Transactional
    public ApplicationResponse updateStatus(Long id, String status) {
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Application not found"));

        ApplicationStatus newStatus = ApplicationStatus.valueOf(status);
        validateStatusTransition(application.getStatus(), newStatus);

        application.updateStatus(newStatus);
        return applicationMapper.toResponseDto(application);
    }

    @Transactional
    public void cancel(Long id) {
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Application not found"));

        if (application.getStatus() != ApplicationStatus.APPLIED) {
            throw new IllegalStateException("Can only cancel applications in APPLIED status");
        }

        application.updateStatus(ApplicationStatus.CANCELED);
    }

    private Recruitment findRecruitment(Long recruitmentId) {
        return recruitmentRepository.findById(recruitmentId)
                .orElseThrow(() -> new NotFoundException("Recruitment not found"));
    }

    private void validateRecruitmentActive(Recruitment recruitment) {
        if (!recruitment.getActive()) {
            throw new IllegalStateException("Recruitment is not active");
        }
    }

    private void validateRecruitmentPeriod(Recruitment recruitment) {
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(recruitment.getStartAt()) || now.isAfter(recruitment.getEndAt())) {
            throw new IllegalStateException("Recruitment is not in the active period");
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
        if (!passwordEncoder.matches(rawPassword, encodedPassword)) {
            throw new IllegalArgumentException("Invalid password");
        }
    }
}

