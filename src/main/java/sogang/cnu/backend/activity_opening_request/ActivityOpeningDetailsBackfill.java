package sogang.cnu.backend.activity_opening_request;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ActivityOpeningDetailsBackfill implements ApplicationRunner {
    private final ActivityOpeningRequestRepository requestRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        requestRepository.findAllByApprovedActivityIsNotNull().forEach(request ->
                request.getApprovedActivity().restoreOpeningDetails(
                        request.getOperationPlan(),
                        request.getInstructorCareer()
                )
        );
    }
}
