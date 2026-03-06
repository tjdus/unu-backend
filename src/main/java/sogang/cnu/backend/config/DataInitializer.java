package sogang.cnu.backend.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import sogang.cnu.backend.activity_type.ActivityType;
import sogang.cnu.backend.activity_type.ActivityTypeRepository;
import sogang.cnu.backend.quarter.CurrentQuarter;
import sogang.cnu.backend.quarter.CurrentQuarterRepository;
import sogang.cnu.backend.quarter.Quarter;
import sogang.cnu.backend.quarter.QuarterRepository;
import sogang.cnu.backend.quarter.Season;
import sogang.cnu.backend.role.Role;
import sogang.cnu.backend.role.RoleRepository;
import sogang.cnu.backend.user.User;
import sogang.cnu.backend.user.UserRepository;
import sogang.cnu.backend.user_role.UserRole;
import sogang.cnu.backend.user_role.UserRoleRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final QuarterRepository quarterRepository;
    private final CurrentQuarterRepository currentQuarterRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final ActivityTypeRepository activityTypeRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        initQuarters();
        initActivityTypes();
        initRoles();
        initUsers();
    }

    // ── Quarters ──────────────────────────────────────────────────────────────

    private void initQuarters() {
        if (quarterRepository.count() > 0) return;

        record QuarterDef(int year, Season season, LocalDate start, LocalDate end) {}

        List<QuarterDef> defs = List.of(
            new QuarterDef(2025, Season.SPRING, LocalDate.of(2025, 3,  1), LocalDate.of(2025, 6, 30)),
            new QuarterDef(2025, Season.SUMMER, LocalDate.of(2025, 7,  1), LocalDate.of(2025, 8, 31)),
            new QuarterDef(2025, Season.FALL,   LocalDate.of(2025, 9,  1), LocalDate.of(2025, 12, 31)),
            new QuarterDef(2025, Season.WINTER, LocalDate.of(2026, 1,  1), LocalDate.of(2026, 2, 28)),
            new QuarterDef(2026, Season.SPRING, LocalDate.of(2026, 3,  1), LocalDate.of(2026, 6, 30))
        );

        Quarter currentQuarter = null;
        for (QuarterDef d : defs) {
            Quarter q = quarterRepository.save(
                Quarter.builder()
                    .year(d.year())
                    .season(d.season())
                    .startDate(d.start())
                    .endDate(d.end())
                    .build()
            );
            if (d.year() == 2026 && d.season() == Season.SPRING) {
                currentQuarter = q;
            }
        }

        // CurrentQuarter is a singleton row with a fixed UUID
        UUID singletonId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        CurrentQuarter cq = currentQuarterRepository.findById(singletonId)
                .orElse(new CurrentQuarter(singletonId, null));
        cq.update(currentQuarter);
        currentQuarterRepository.save(cq);

        log.info("Seeded {} quarters; current quarter set to 2026 SPRING", defs.size());
    }

    // ── Types ─────────────────────────────────────────────────────────────────

    private void initActivityTypes() {
        if (activityTypeRepository.count() > 0) return;

        record ActivityTypeDef(String name, String code) {}

        List<ActivityTypeDef> defs = List.of(
            new ActivityTypeDef("프로젝트", "PROJECT"),
            new ActivityTypeDef("스터디", "STUDY"),
            new ActivityTypeDef("온라인 강의", "ONLINE_COURSE")
        );

        for (ActivityTypeDef d : defs) {
            activityTypeRepository.save(ActivityType.builder()
                    .name(d.name)
                    .code(d.code())
                    .build());
        }

        log.info("Seeded activity types: 프로젝트, 스터디, 온라인 강의");
    }

    // ── Roles ─────────────────────────────────────────────────────────────────

    private void initRoles() {
        if (roleRepository.count() > 0) return;

        for (String name : List.of("MEMBER", "MANAGER", "ADMIN")) {
            roleRepository.save(Role.builder().name(name).build());
        }

        log.info("Seeded roles: MEMBER, MANAGER, ADMIN");
    }

    // ── Users ─────────────────────────────────────────────────────────────────

    private void initUsers() {
        if (userRepository.count() > 0) return;

        Role managerRole = roleRepository.findAll().stream()
                .filter(r -> r.getName().equals("MANAGER"))
                .findFirst().orElseThrow();
        Role adminRole = roleRepository.findAll().stream()
                .filter(r -> r.getName().equals("ADMIN"))
                .findFirst().orElseThrow();

        record UserDef(String name, String username, String studentId, String email, Role role) {}

        List<UserDef> defs = List.of(
            new UserDef("CNU Admin",   "cnu-admin",   "ADMIN001", "cnu-admin@cnu.ac.kr",   adminRole)
        );

        for (UserDef d : defs) {
            // Default password = username (change after first login)
            User user = userRepository.save(
                User.builder()
                    .name(d.name())
                    .username(d.username())
                    .password(passwordEncoder.encode(d.username()))
                    .studentId(d.studentId())
                    .email(d.email())
                    .isActive(true)
                    .build()
            );

            userRoleRepository.save(
                UserRole.builder()
                    .user(user)
                    .role(d.role())
                    .build()
            );
        }

        log.info("Seeded users: cnu-admin");
    }
}