package sogang.cnu.backend.recruitment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Recruitments used to point at a separate form (forms.schema). They now own their questions
 * (recruitments.questions). On startup this copies each recruitment's form questions over and
 * releases the old NOT NULL form_id column, so new recruitments can be saved without a form.
 * The old forms / form_submissions tables are left in place untouched. Idempotent.
 */
@Slf4j
@Component
@Order(0)
@RequiredArgsConstructor
public class RecruitmentQuestionsBackfill implements ApplicationRunner {
    private static final String EMPTY_QUESTIONS = "{\"version\":1,\"questions\":[]}";

    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (columnExists("recruitments", "form_id")) {
            jdbcTemplate.execute("ALTER TABLE recruitments ALTER COLUMN form_id DROP NOT NULL");
            if (columnExists("forms", "schema")) {
                int copied = jdbcTemplate.update("""
                        UPDATE recruitments r
                        SET questions = CASE WHEN json_typeof(f.schema) = 'string'
                                             THEN (f.schema #>> '{}')::json
                                             ELSE f.schema END
                        FROM forms f
                        WHERE r.form_id = f.id AND r.questions IS NULL AND f.schema IS NOT NULL
                        """);
                if (copied > 0) log.info("Copied form questions into {} recruitment(s)", copied);
            }
        }
        jdbcTemplate.update("UPDATE recruitments SET questions = ?::json WHERE questions IS NULL", EMPTY_QUESTIONS);
    }

    private boolean columnExists(String table, String column) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM information_schema.columns WHERE table_name = ? AND column_name = ?",
                Integer.class, table, column);
        return count != null && count > 0;
    }
}
