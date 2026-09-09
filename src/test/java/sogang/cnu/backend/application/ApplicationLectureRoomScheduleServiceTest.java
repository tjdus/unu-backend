package sogang.cnu.backend.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sogang.cnu.backend.application.dto.ApplicationLectureRoomScheduleImportResponse;
import sogang.cnu.backend.common.exception.BadRequestException;
import sogang.cnu.backend.lecture_room_schedule.LectureRoomSchedule;
import sogang.cnu.backend.lecture_room_schedule.LectureRoomScheduleRepository;
import sogang.cnu.backend.quarter.Quarter;
import sogang.cnu.backend.recruitment.Recruitment;
import sogang.cnu.backend.user.User;
import sogang.cnu.backend.user.UserRepository;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationLectureRoomScheduleServiceTest {
    @Mock
    private ApplicationRepository applicationRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private LectureRoomScheduleRepository scheduleRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private ApplicationLectureRoomScheduleService service;

    @BeforeEach
    void setUp() {
        service = new ApplicationLectureRoomScheduleService(
                applicationRepository,
                userRepository,
                scheduleRepository,
                objectMapper
        );
    }

    @Test
    void importsRecognizedAnswersRegardlessOfApplicationStatus() throws Exception {
        UUID applicationId = UUID.randomUUID();
        UUID quarterId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Application application = application(applicationId, quarterId, "20201234", """
                {
                  "monday": ["1교시(9:00 ~ 10:15)", "3교시(11:45 ~ 13:15)"],
                  "tuesday": ["없음"],
                  "wednesday": ["5교시(14:45 ~ 16:15)"],
                  "thursday": ["없음"],
                  "friday": ["8교시(19:15 ~ 20:45)"]
                }
                """, ApplicationStatus.REJECTED);
        User user = User.builder().id(userId).name("테스트 사용자").studentId("20201234").build();

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(application));
        when(userRepository.findByStudentId("20201234")).thenReturn(Optional.of(user));
        when(scheduleRepository.existsByQuarterIdAndDayOfWeekAndTimeSlotAndUserId(
                any(), any(), any(), any())).thenReturn(false);
        when(scheduleRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ApplicationLectureRoomScheduleImportResponse response = service.importSchedule(applicationId);

        assertThat(response.getCreatedCount()).isEqualTo(4);
        assertThat(response.getExistingCount()).isZero();
        ArgumentCaptor<LectureRoomSchedule> captor = ArgumentCaptor.forClass(LectureRoomSchedule.class);
        verify(scheduleRepository, org.mockito.Mockito.times(4)).save(captor.capture());
        List<LectureRoomSchedule> saved = captor.getAllValues();
        assertThat(saved)
                .extracting(LectureRoomSchedule::getDayOfWeek, LectureRoomSchedule::getTimeSlot)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple(DayOfWeek.MONDAY, LocalTime.of(9, 0)),
                        org.assertj.core.groups.Tuple.tuple(DayOfWeek.MONDAY, LocalTime.of(11, 45)),
                        org.assertj.core.groups.Tuple.tuple(DayOfWeek.WEDNESDAY, LocalTime.of(14, 45)),
                        org.assertj.core.groups.Tuple.tuple(DayOfWeek.FRIDAY, LocalTime.of(19, 15))
                );
    }

    @Test
    void rejectsNoneCombinedWithAnotherTime() throws Exception {
        UUID applicationId = UUID.randomUUID();
        Application application = application(applicationId, UUID.randomUUID(), "20201234", """
                {
                  "monday": ["없음", "1교시(9:00 ~ 10:15)"],
                  "tuesday": ["없음"],
                  "wednesday": ["없음"],
                  "thursday": ["없음"],
                  "friday": ["없음"]
                }
                """, ApplicationStatus.APPLIED);
        User user = User.builder().id(UUID.randomUUID()).name("테스트 사용자").studentId("20201234").build();

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(application));
        when(userRepository.findByStudentId("20201234")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service.importSchedule(applicationId))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("'없음'");
    }

    private Application application(
            UUID applicationId,
            UUID quarterId,
            String studentId,
            String answers,
            ApplicationStatus status
    ) throws Exception {
        Quarter quarter = Quarter.builder().id(quarterId).build();
        Recruitment recruitment = Recruitment.builder().quarter(quarter).build();
        String schema = """
                {
                  "version": 1,
                  "questions": [
                    {"id":"monday","type":"MULTIPLE_CHOICE","title":"월요일 관리 가능한 시간"},
                    {"id":"tuesday","type":"MULTIPLE_CHOICE","title":"화요일 관리 가능한 시간"},
                    {"id":"wednesday","type":"MULTIPLE_CHOICE","title":"수요일 관리 가능한 시간"},
                    {"id":"thursday","type":"MULTIPLE_CHOICE","title":"목요일 관리 가능한 시간"},
                    {"id":"friday","type":"MULTIPLE_CHOICE","title":"금요일 관리 가능한 시간"}
                  ]
                }
                """;
        return Application.builder()
                .id(applicationId)
                .recruitment(recruitment)
                .studentId(studentId)
                .status(status)
                .formSnapshot(objectMapper.readTree(schema))
                .answers(objectMapper.readTree(answers))
                .build();
    }
}
