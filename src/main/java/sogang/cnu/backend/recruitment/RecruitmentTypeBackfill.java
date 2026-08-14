package sogang.cnu.backend.recruitment;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class RecruitmentTypeBackfill implements ApplicationRunner {
    private final RecruitmentRepository recruitmentRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        recruitmentRepository.findAll().forEach(Recruitment::assignDefaultType);
    }
}
