package study.developia._2025_11.job.product.upload;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.test.context.TestPropertySource;
import study.developia._2025_11.job.BaseBatchIntegrationTest;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertAll;

@TestPropertySource(properties = {"spring.batch.job.name=productUploadJob"})
class ProductUploadJobConfigurationTest extends BaseBatchIntegrationTest {

    @Value("classpath:/data/products_for_upload.csv")
    private Resource resource;

    @Test
    void testJob(@Autowired Job productUploadJob) throws Exception {
        JobParameters jobParameters = jobParameters();
        jobLauncherTestUtils.setJob(productUploadJob);

        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        assertAll(() -> assertJobCompleted(jobExecution));
    }

    private JobParameters jobParameters() throws IOException {
        return new JobParametersBuilder()
                .addJobParameter("inputFilePath",
                        new JobParameter<>(resource.getFile().getPath(), String.class, false))
                .addJobParameter("gridSize",
                        new JobParameter<>(3, Integer.class, false))
                .toJobParameters();
    }
}
