package study.developia._2025_11.job;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import study.developia._2025_11.Application;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Sql("/sql/schema.sql")
@SpringBatchTest
@SpringJUnitConfig(classes = {Application.class})
public abstract class BaseBatchIntegrationTest {

    @Autowired
    protected JobLauncherTestUtils jobLauncherTestUtils;

    protected static void assertJobCompleted(JobExecution jobExecution) {
        assertEquals("COMPLETED", jobExecution.getExitStatus().getExitCode());
    }
}
