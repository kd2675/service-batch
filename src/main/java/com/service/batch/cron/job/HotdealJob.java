package com.service.batch.cron.job;

import com.service.batch.cron.step.HotdealStep;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HotdealJob {
    public static final String INS_HOTDEAL_JOB = "insHotdealJob";
    public static final String SEND_HOTDEAL_JOB = "sendHotdealJob";
    public static final String DEL_SENT_HOTDEAL_JOB = "delSentHotdealJob";
    public static final String DEL_HOTDEAL_JOB = "delHotdealJob";

    @Bean(name = INS_HOTDEAL_JOB)
    public Job insNewsJob(
            JobRepository jobRepository,
            @Qualifier(HotdealStep.INS_HOTDEAL_STEP) Step step
    ) {
        return new JobBuilder(INS_HOTDEAL_JOB, jobRepository)
                .start(step)
                .build();
    }

    @Bean(name = SEND_HOTDEAL_JOB)
    public Job sendHotdealJob(
            JobRepository jobRepository,
            @Qualifier(HotdealStep.SEND_HOTDEAL_STEP) Step step
    ) {
        return new JobBuilder(SEND_HOTDEAL_JOB, jobRepository)
                .start(step)
                .build();
    }

    @Bean(name = DEL_SENT_HOTDEAL_JOB)
    public Job delSentHotdealJob(
            JobRepository jobRepository,
            @Qualifier(HotdealStep.DEL_SENT_HOTDEAL_STEP) Step step
    ) {
        return new JobBuilder(DEL_SENT_HOTDEAL_JOB, jobRepository)
                .start(step)
                .build();
    }

    @Bean(name = DEL_HOTDEAL_JOB)
    public Job delHotdealJob(
            JobRepository jobRepository,
            @Qualifier(HotdealStep.DEL_HOTDEAL_STEP) Step step
    ) {
        return new JobBuilder(DEL_HOTDEAL_JOB, jobRepository)
                .start(step)
                .build();
    }
}
