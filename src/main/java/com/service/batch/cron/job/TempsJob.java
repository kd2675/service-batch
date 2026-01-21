package com.service.batch.cron.job;

import com.service.batch.cron.step.OrderStep;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TempsJob {
    public static final String GET_TEMPS_JOB = "getTempsJob";

    @Bean(name = GET_TEMPS_JOB)
    public Job delCoinJob(
            JobRepository jobRepository,
            @Qualifier(OrderStep.UPD_ORDER_STEP) Step step
    ) {
        return new JobBuilder(GET_TEMPS_JOB, jobRepository)
                .start(step)
                .build();
    }
}
