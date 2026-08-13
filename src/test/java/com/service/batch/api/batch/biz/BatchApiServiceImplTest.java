package com.service.batch.api.batch.biz;

import com.service.batch.database.batch.MySqlNamedLock;
import com.service.batch.service.coin.api.biz.ins.InsCoinService;
import com.service.batch.service.lotto.biz.LottoService;
import com.service.batch.service.reset.api.biz.Reset;
import com.service.batch.service.sport.biz.ReserveSportSVC;
import com.service.batch.service.stock.biz.StockService;
import com.service.batch.utils.MattermostResetState;
import org.example.core.request.BatchExecuteRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.JobLauncher;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BatchApiServiceImplTest {
    @Mock
    private JobLauncher jobLauncher;
    @Mock
    private JobRegistry jobRegistry;
    @Mock
    private LottoService lottoService;
    @Mock
    private Reset reset;
    @Mock
    private InsCoinService insCoinService;
    @Mock
    private StockService stockService;
    @Mock
    private ReserveSportSVC reserveSportSVC;
    @Mock
    private MySqlNamedLock mySqlNamedLock;
    @Mock
    private MattermostResetState mattermostResetState;
    @Mock
    private Executor asyncTaskExecutor;
    @InjectMocks
    private BatchApiServiceImpl service;

    @Test
    void skipsNewsFlashBeforeCreatingMetadataDuringReset() throws Exception {
        BatchExecuteRequest request = BatchExecuteRequest.sendNewsFlashJob();
        when(jobRegistry.getJob("sendNewsFlashJob")).thenReturn(org.mockito.Mockito.mock(Job.class));
        when(mattermostResetState.isResetting(any())).thenReturn(true);

        BatchExecutionResult result = service.execute(request);

        assertThat(result).isEqualTo(BatchExecutionResult.SKIPPED_RESETTING);
        verify(jobLauncher, never()).run(any(), any());
        verify(mySqlNamedLock, never()).runIfAcquired(any(), any());
    }

    @Test
    void skipsWhenAnotherInstanceIsRunningTheJob() throws Exception {
        BatchExecuteRequest request = BatchExecuteRequest.insNewsJob();
        when(jobRegistry.getJob("insNewsJob")).thenReturn(org.mockito.Mockito.mock(Job.class));
        when(mySqlNamedLock.runIfAcquired(eq("service-batch:insNewsJob"), any())).thenReturn(false);

        BatchExecutionResult result = service.execute(request);

        assertThat(result).isEqualTo(BatchExecutionResult.SKIPPED_ALREADY_RUNNING);
        verify(jobLauncher, never()).run(any(), any());
    }

    @Test
    void launchesAndReportsTheJobWhileHoldingTheLock() throws Exception {
        BatchExecuteRequest request = BatchExecuteRequest.insNewsJob();
        Job job = org.mockito.Mockito.mock(Job.class);
        JobExecution execution = org.mockito.Mockito.mock(JobExecution.class);
        when(jobRegistry.getJob("insNewsJob")).thenReturn(job);
        when(jobLauncher.run(eq(job), any(JobParameters.class))).thenReturn(execution);
        when(execution.getStatus()).thenReturn(BatchStatus.COMPLETED);
        when(mySqlNamedLock.runIfAcquired(eq("service-batch:insNewsJob"), any())).thenAnswer(invocation -> {
            Callable<?> action = invocation.getArgument(1);
            action.call();
            return true;
        });

        BatchExecutionResult result = service.execute(request);

        assertThat(result).isEqualTo(BatchExecutionResult.COMPLETED);
        verify(jobLauncher).run(eq(job), any(JobParameters.class));
    }

    @Test
    void asyncRequestSkipsBeforeItCanQueueWhenLockIsHeld() throws Exception {
        BatchExecuteRequest request = BatchExecuteRequest.insNewsJob();
        when(jobRegistry.getJob("insNewsJob")).thenReturn(org.mockito.Mockito.mock(Job.class));
        when(mySqlNamedLock.submitIfAcquired(eq("service-batch:insNewsJob"), eq(asyncTaskExecutor), any()))
                .thenReturn(false);

        BatchExecutionResult result = service.executeAsync(request);

        assertThat(result).isEqualTo(BatchExecutionResult.SKIPPED_ALREADY_RUNNING);
        verify(jobLauncher, never()).run(any(), any());
    }
}
