package sogang.cnu.backend.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sogang.cnu.backend.application.command.ApplicationCreateCommand;
import sogang.cnu.backend.application.dto.ApplicationCreateRequest;
import sogang.cnu.backend.application.dto.ApplicationResponse;
import sogang.cnu.backend.common.exception.NotFoundException;
import sogang.cnu.backend.recruitment.Recruitment;
import sogang.cnu.backend.recruitment.RecruitmentRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApplicationService {
    private final ApplicationRepository applicationRepository;
    private final ApplicationMapper applicationMapper;
    private final RecruitmentRepository recruitmentRepository;

    @Transactional(readOnly = true)
    public ApplicationResponse getById(Long id) {
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Application not found"));
        return applicationMapper.toResponseDto(application);
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> getByRecruitmentId(Long recruitmentId) {
        return applicationRepository.findByRecruitmentId(recruitmentId).stream()
                .map(applicationMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ApplicationResponse create(ApplicationCreateRequest dto) {
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
                .build();

        Application application = Application.create(command);
        Application savedApplication = applicationRepository.save(application);
        return applicationMapper.toResponseDto(savedApplication);
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
        if (currentStatus == ApplicationStatus.APPLIED) {
            if (newStatus != ApplicationStatus.PASSED && newStatus != ApplicationStatus.REJECTED) {
                throw new IllegalStateException("Can only transition from APPLIED to PASSED or REJECTED");
            }
        } else {
            throw new IllegalStateException("Cannot change status after review");
        }
    }
}

