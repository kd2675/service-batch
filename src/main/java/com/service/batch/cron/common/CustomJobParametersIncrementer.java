package com.service.batch.cron.common;

import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersIncrementer;

import java.time.LocalDateTime;

public class CustomJobParametersIncrementer implements JobParametersIncrementer {
    /**
     * Date 를 incrementer 로 지정해보자.
     * @param jobParameters
     * @return
     */
    @Override
    public JobParameters getNext(JobParameters jobParameters) {
        return new JobParametersBuilder().addLocalDateTime("run.time", LocalDateTime.now()).toJobParameters();
    }
}