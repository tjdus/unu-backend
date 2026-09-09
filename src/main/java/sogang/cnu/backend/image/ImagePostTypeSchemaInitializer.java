package sogang.cnu.backend.image;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ImagePostTypeSchemaInitializer implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        List<ConstraintInfo> constraints = jdbcTemplate.query(
                """
                select c.conname, pg_get_constraintdef(c.oid)
                from pg_constraint c
                join pg_class t on t.oid = c.conrelid
                where t.relname = 'images'
                  and c.contype = 'c'
                  and pg_get_constraintdef(c.oid) like '%post_type%'
                """,
                (resultSet, rowNum) -> new ConstraintInfo(
                        resultSet.getString(1),
                        resultSet.getString(2)
                )
        );

        if (constraints.stream().anyMatch(info -> info.definition().contains("ABOUT_EXAMPLE"))) {
            return;
        }

        for (ConstraintInfo constraint : constraints) {
            String escapedName = constraint.name().replace("\"", "\"\"");
            jdbcTemplate.execute("alter table images drop constraint \"" + escapedName + "\"");
        }
        jdbcTemplate.execute("""
                alter table images
                add constraint images_post_type_check
                check (post_type in ('BLOG', 'PORTFOLIO', 'ABOUT_EXAMPLE'))
                """);
    }

    private record ConstraintInfo(String name, String definition) {
    }
}
