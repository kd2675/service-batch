package com.service.batch.cron.job;

import com.service.batch.cron.step.CoinStep;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.SimpleJob;
import org.springframework.batch.core.job.flow.FlowJob;
import org.springframework.batch.core.repository.JobRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class CoinJobTest {

    @Mock
    private JobRepository jobRepository;
    
    @Mock
    private Step sendCoinStep;
    
    @Mock
    private Step sentCoinStep;
    
    @Mock
    private Step delCoinStep;
    
    private CoinJob coinJob;

    @BeforeEach
    void setUp() {
        coinJob = new CoinJob();
    }

    @Test
    @DisplayName("sendCoinJob이 정상적으로 생성되는지 테스트")
    void sendCoinJob_ShouldCreateJobSuccessfully() {
        // When
        Job result = coinJob.sendCoinJob(jobRepository, sendCoinStep);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(SimpleJob.class);
        assertThat(result.getName()).isEqualTo(CoinJob.SEND_COIN_JOB);
        assertThat(result.isRestartable()).isTrue();
    }

    @Test
    @DisplayName("delCoinJob이 정상적으로 생성되는지 테스트")
    void delCoinJob_ShouldCreateJobSuccessfully() {
        // When
        Job result = coinJob.delCoinJob(jobRepository, sentCoinStep, delCoinStep);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(SimpleJob.class);
        assertThat(result.getName()).isEqualTo(CoinJob.DEL_COIN_JOB);
        assertThat(result.isRestartable()).isTrue();
    }

    @Test
    @DisplayName("생성된 Job들이 서로 다른 인스턴스인지 테스트")
    void createdJobs_ShouldBeDifferentInstances() {
        // When
        Job sendJob1 = coinJob.sendCoinJob(jobRepository, sendCoinStep);
        Job sendJob2 = coinJob.sendCoinJob(jobRepository, sendCoinStep);
        Job delJob = coinJob.delCoinJob(jobRepository, sentCoinStep, delCoinStep);

        // Then
        assertThat(sendJob1).isNotSameAs(sendJob2);
        assertThat(sendJob1).isNotSameAs(delJob);
        assertThat(sendJob2).isNotSameAs(delJob);
    }

    @Test
    @DisplayName("Job 이름이 상수와 일치하는지 테스트")
    void jobNames_ShouldMatchConstants() {
        // When
        Job sendJob = coinJob.sendCoinJob(jobRepository, sendCoinStep);
        Job delJob = coinJob.delCoinJob(jobRepository, sentCoinStep, delCoinStep);

        // Then
        assertThat(sendJob.getName()).isEqualTo(CoinJob.SEND_COIN_JOB);
        assertThat(delJob.getName()).isEqualTo(CoinJob.DEL_COIN_JOB);
    }

    @Test
    @DisplayName("생성된 Job들이 모두 재시작 가능한지 테스트")
    void createdJobs_ShouldBeRestartable() {
        // When
        Job sendJob = coinJob.sendCoinJob(jobRepository, sendCoinStep);
        Job delJob = coinJob.delCoinJob(jobRepository, sentCoinStep, delCoinStep);

        // Then
        assertThat(sendJob.isRestartable()).isTrue();
        assertThat(delJob.isRestartable()).isTrue();
    }
}