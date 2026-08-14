package sogang.cnu.backend.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sogang.cnu.backend.application.dto.ApplicationLectureRoomScheduleImportResponse;
import sogang.cnu.backend.common.exception.BadRequestException;
import sogang.cnu.backend.common.exception.NotFoundException;
import sogang.cnu.backend.lecture_room_schedule.LectureRoomSchedule;
import sogang.cnu.backend.lecture_room_schedule.LectureRoomScheduleRepository;
import sogang.cnu.backend.lecture_room_schedule.command.LectureRoomScheduleCreateCommand;
import sogang.cnu.backend.quarter.Quarter;
import sogang.cnu.backend.user.User;
import sogang.cnu.backend.user.UserRepository;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class ApplicationLectureRoomScheduleService {
    private static final Map<String, DayOfWeek> QUESTION_DAYS = Map.of(
            "월요일관리가능한시간", DayOfWeek.MONDAY,
            "화요일관리가능한시간", DayOfWeek.TUESDAY,
            "수요일관리가능한시간", DayOfWeek.WEDNESDAY,
            "목요일관리가능한시간", DayOfWeek.THURSDAY,
            "금요일관리가능한시간", DayOfWeek.FRIDAY
    );
    private static final Map<Integer, LocalTime> PERIOD_TIME_SLOTS = Map.of(
            1, LocalTime.of(9, 0),
            2, LocalTime.of(10, 15),
            3, LocalTime.of(11, 45),
            4, LocalTime.of(13, 15),
            5, LocalTime.of(14, 45),
            6, LocalTime.of(16, 15),
            7, LocalTime.of(17, 45),
            8, LocalTime.of(19, 15)
    );
    private static final Pattern PERIOD_PATTERN = Pattern.compile("^\\s*([1-8])\\s*교시(?:\\s*\\(.*\\))?\\s*$");

    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final LectureRoomScheduleRepository scheduleRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public ApplicationLectureRoomScheduleImportResponse importSchedule(UUID applicationId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new NotFoundException("Application not found"));
        Quarter quarter = application.getRecruitment().getQuarter();
        if (quarter == null) {
            throw new BadRequestException("모집 공고에 연결된 분기가 없습니다.");
        }

        User user = userRepository.findByStudentId(application.getStudentId().trim())
                .orElseThrow(() -> new BadRequestException("지원서 학번과 일치하는 학회원 계정이 없습니다."));
        Set<ScheduleEntry> entries = extractEntries(application.getFormSnapshot(), application.getAnswers());
        if (entries.isEmpty()) {
            throw new BadRequestException("시간표에 반영할 관리 가능 시간이 없습니다.");
        }

        int createdCount = 0;
        int existingCount = 0;
        for (ScheduleEntry entry : entries) {
            boolean exists = scheduleRepository.existsByQuarterIdAndDayOfWeekAndTimeSlotAndUserId(
                    quarter.getId(), entry.dayOfWeek(), entry.timeSlot(), user.getId());
            if (exists) {
                existingCount++;
                continue;
            }

            scheduleRepository.save(LectureRoomSchedule.create(
                    LectureRoomScheduleCreateCommand.builder()
                            .quarter(quarter)
                            .dayOfWeek(entry.dayOfWeek())
                            .timeSlot(entry.timeSlot())
                            .user(user)
                            .build()
            ));
            createdCount++;
        }

        return ApplicationLectureRoomScheduleImportResponse.builder()
                .quarterId(quarter.getId())
                .userId(user.getId())
                .userName(user.getName())
                .createdCount(createdCount)
                .existingCount(existingCount)
                .build();
    }

    private Set<ScheduleEntry> extractEntries(JsonNode rawSchema, JsonNode rawAnswers) {
        JsonNode schema = normalizeJson(rawSchema, "지원서 양식이 올바르지 않습니다.");
        JsonNode answers = normalizeJson(rawAnswers, "지원서 답변이 올바르지 않습니다.");
        JsonNode questions = schema.path("questions");
        if (!questions.isArray() || !answers.isObject()) {
            throw new BadRequestException("지원서 양식 또는 답변 형식이 올바르지 않습니다.");
        }

        Map<String, DayOfWeek> questionDays = new LinkedHashMap<>();
        for (JsonNode question : questions) {
            String id = question.path("id").asText("");
            String normalizedTitle = question.path("title").asText("").replaceAll("\\s+", "");
            DayOfWeek dayOfWeek = QUESTION_DAYS.get(normalizedTitle);
            if (dayOfWeek == null) {
                continue;
            }
            if (id.isBlank() || questionDays.containsValue(dayOfWeek)) {
                throw new BadRequestException("학회실 관리 시간 문항 구성이 올바르지 않습니다.");
            }
            questionDays.put(id, dayOfWeek);
        }

        if (questionDays.size() != QUESTION_DAYS.size()) {
            throw new BadRequestException("학회실 관리 시간 템플릿의 월요일부터 금요일 문항이 필요합니다.");
        }

        Set<ScheduleEntry> entries = new LinkedHashSet<>();
        questionDays.forEach((questionId, dayOfWeek) -> {
            List<String> selectedOptions = selectedOptions(answers.get(questionId));
            boolean selectedNone = selectedOptions.stream().anyMatch(option -> "없음".equals(option.trim()));
            if (selectedNone && selectedOptions.size() > 1) {
                throw new BadRequestException("'없음'은 다른 관리 가능 시간과 함께 선택할 수 없습니다.");
            }
            if (selectedNone) {
                return;
            }

            for (String option : selectedOptions) {
                Matcher matcher = PERIOD_PATTERN.matcher(option);
                if (!matcher.matches()) {
                    throw new BadRequestException("관리 가능 시간 선택지가 올바르지 않습니다: " + option);
                }
                int period = Integer.parseInt(matcher.group(1));
                entries.add(new ScheduleEntry(dayOfWeek, PERIOD_TIME_SLOTS.get(period)));
            }
        });
        return entries;
    }

    private List<String> selectedOptions(JsonNode answer) {
        if (answer == null || answer.isNull()) {
            return List.of();
        }
        if (answer.isTextual()) {
            return List.of(answer.asText());
        }
        if (!answer.isArray()) {
            throw new BadRequestException("관리 가능 시간 답변 형식이 올바르지 않습니다.");
        }

        List<String> options = new ArrayList<>();
        for (JsonNode option : answer) {
            if (!option.isTextual()) {
                throw new BadRequestException("관리 가능 시간 답변 형식이 올바르지 않습니다.");
            }
            options.add(option.asText());
        }
        return options;
    }

    private JsonNode normalizeJson(JsonNode value, String errorMessage) {
        if (value == null) {
            throw new BadRequestException(errorMessage);
        }
        if (!value.isTextual()) {
            return value;
        }
        try {
            return objectMapper.readTree(value.asText());
        } catch (JsonProcessingException e) {
            throw new BadRequestException(errorMessage);
        }
    }

    private record ScheduleEntry(DayOfWeek dayOfWeek, LocalTime timeSlot) {
    }
}
