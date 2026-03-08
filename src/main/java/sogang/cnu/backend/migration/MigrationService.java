package sogang.cnu.backend.migration;

import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import sogang.cnu.backend.migration.dto.UserMigrationResultDto;
import sogang.cnu.backend.quarter.Quarter;
import sogang.cnu.backend.quarter.QuarterRepository;
import sogang.cnu.backend.quarter.Season;
import sogang.cnu.backend.quarter.command.QuarterCreateCommand;
import sogang.cnu.backend.role.Role;
import sogang.cnu.backend.role.RoleRepository;
import sogang.cnu.backend.user.User;
import sogang.cnu.backend.user.UserRepository;
import sogang.cnu.backend.user_role.UserRole;
import sogang.cnu.backend.user_role.UserRoleRepository;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MigrationService {

    private final UserRepository userRepository;
    private final QuarterRepository quarterRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserMigrationResultDto migrateUsers(MultipartFile file) {
        int created = 0;
        int skipped = 0;
        int failed = 0;
        List<String> errors = new ArrayList<>();

        Role memberRole = roleRepository.findByName("MEMBER")
                .orElseThrow(() -> new IllegalStateException("Role MEMBER not found"));

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             CSVParser parser = CSVFormat.DEFAULT
                     .builder()
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .setTrim(true)
                     .build()
                     .parse(reader)) {

            for (CSVRecord record : parser) {
                long lineNumber = record.getRecordNumber() + 1;
                try {
                    String username = record.get("username");
                    String studentId = record.get("student_id");

                    if (userRepository.findByUsername(username).isPresent()) {
                        skipped++;
                        continue;
                    }
                    if (userRepository.findByStudentId(studentId).isPresent()) {
                        skipped++;
                        continue;
                    }

                    String quarterName = record.get("joined_quarter_name");
                    Quarter quarter = resolveQuarter(quarterName);

                    String rawPassword = record.get("password");
                    String encodedPassword = (rawPassword == null || rawPassword.isBlank())
                            ? null
                            : passwordEncoder.encode(rawPassword);

                    User user = User.builder()
                            .name(record.get("name"))
                            .username(username)
                            .password(encodedPassword)
                            .studentId(studentId)
                            .githubId(nullIfBlank(record.get("github_id")))
                            .phoneNumber(nullIfBlank(record.get("phone_number")))
                            .major(nullIfBlank(record.get("major")))
                            .subMajor(nullIfBlank(record.get("sub_major")))
                            .email(record.get("email"))
                            .isCurrentQuarterActive(false)
                            .joinedQuarter(quarter)
                            .isAlumni((record.get("is_alumni")) != null && record.get("is_alumni").equalsIgnoreCase("true"))
                            .build();

                    User savedUser = userRepository.save(user);
                    userRoleRepository.save(UserRole.builder()
                            .user(savedUser)
                            .role(memberRole)
                            .build());
                    created++;

                } catch (Exception e) {
                    failed++;
                    errors.add("Line " + lineNumber + ": " + e.getMessage());
                }
            }

        } catch (Exception e) {
            errors.add("Failed to parse CSV: " + e.getMessage());
            failed++;
        }

        return UserMigrationResultDto.builder()
                .created(created)
                .skipped(skipped)
                .failed(failed)
                .errors(errors)
                .build();
    }

    private Quarter resolveQuarter(String quarterName) {
        if (quarterName == null || quarterName.isBlank()) return null;

        String[] parts = quarterName.trim().split("\\s+", 2);
        if (parts.length != 2) return null;

        int year;
        try {
            year = Integer.parseInt(parts[0]);
        } catch (NumberFormatException e) {
            return null;
        }

        Season season;
        try {
            season = Season.valueOf(parts[1].toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }

        return quarterRepository.findFirstByYearAndSeason(year, season)
                .orElseGet(() -> quarterRepository.save(
                        Quarter.create(QuarterCreateCommand.builder()
                                .year(year)
                                .season(season)
                                .build())
                ));
    }

    private String nullIfBlank(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }
}
