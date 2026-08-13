package com.service.batch.api.batch.biz;

import com.service.batch.cron.common.CustomJobParametersIncrementer;
import com.service.batch.cron.job.NewsJob;
import com.service.batch.database.batch.MySqlNamedLock;
import com.service.batch.service.coin.api.biz.ins.InsCoinService;
import com.service.batch.service.lotto.biz.LottoService;
import com.service.batch.service.reset.api.biz.Reset;
import com.service.batch.service.sport.biz.ReserveSportSVC;
import com.service.batch.service.stock.biz.StockService;
import com.service.batch.utils.MattermostResetState;
import com.service.batch.utils.enums.ChannelEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.core.request.BatchExecuteRequest;
import org.example.core.request.BatchServiceRequest;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;

@Service
@RequiredArgsConstructor
@Slf4j
public class BatchApiServiceImpl implements BatchApiService{
    private static final String LOCK_PREFIX = "service-batch:";
    private static final Set<String> SINGLE_INSTANCE_JOB_TYPES = Set.of(
            NewsJob.INS_NEWS_JOB,
            NewsJob.SEND_NEWS_FLASH_JOB
    );
    private static final Set<String> SERVICE_JOB_TYPES = Set.of(
            "account",
            "check",
            "buy",
            "reset",
            "saveCoinDataBTC",
            "logCacheStats",
            "beforeCheckJangsung"
    );

    private final JobLauncher jobLauncher;
    private final JobRegistry jobRegistry;

    private final LottoService lottoService;
    private final Reset reset;
    private final InsCoinService insCoinService;
    private final StockService stockService;
    private final ReserveSportSVC reserveSportSVC;
    private final MySqlNamedLock mySqlNamedLock;
    private final MattermostResetState mattermostResetState;
    @Qualifier("asyncTaskExecutor")
    private final Executor asyncTaskExecutor;

    @Override
    public void validateExecuteRequest(BatchExecuteRequest request) throws NoSuchJobException {
        if (request == null || !StringUtils.hasText(request.getJobType())) {
            throw new IllegalArgumentException("jobType is required");
        }

        jobRegistry.getJob(request.getJobType());
    }

    @Override
    public void validateServiceRequest(BatchServiceRequest request) {
        if (request == null || !StringUtils.hasText(request.getJobType())) {
            throw new IllegalArgumentException("jobType is required");
        }

        if (!SERVICE_JOB_TYPES.contains(request.getJobType())) {
            throw new IllegalArgumentException("Unsupported service jobType: " + request.getJobType());
        }
    }

    @Override
    public BatchExecutionResult executeAsync(BatchExecuteRequest request) throws Exception {
        validateExecuteRequest(request);
        String jobType = request.getJobType();
        log.info("Async batch execute request: {}", request);

        BatchExecutionResult skipResult = skipResult(jobType);
        if (skipResult != null) {
            return skipResult;
        }

        if (!SINGLE_INSTANCE_JOB_TYPES.contains(jobType)) {
            asyncTaskExecutor.execute(() -> launchAsync(jobType));
            return BatchExecutionResult.ACCEPTED;
        }

        if (!mySqlNamedLock.submitIfAcquired(LOCK_PREFIX + jobType, asyncTaskExecutor, () -> {
            launch(jobType);
            return null;
        })) {
            log.info("Batch execution skipped because it is already running: {}", jobType);
            return BatchExecutionResult.SKIPPED_ALREADY_RUNNING;
        }

        return BatchExecutionResult.ACCEPTED;
    }

    @Override
    public BatchExecutionResult execute(BatchExecuteRequest request) throws Exception{
        return setExecute(request);
    }

    private BatchExecutionResult setExecute(BatchExecuteRequest request) throws JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException, JobParametersInvalidException, NoSuchJobException {
        validateExecuteRequest(request);
        log.info("Batch execute request: {}", request);

        String jobType = request.getJobType();
        BatchExecutionResult skipResult = skipResult(jobType);
        if (skipResult != null) {
            return skipResult;
        }

        if (!SINGLE_INSTANCE_JOB_TYPES.contains(jobType)) {
            launch(jobType);
            return BatchExecutionResult.COMPLETED;
        }

        try {
            if (!mySqlNamedLock.runIfAcquired(LOCK_PREFIX + jobType, () -> {
                launch(jobType);
                return null;
            })) {
                log.info("Batch execution skipped because it is already running: {}", jobType);
                return BatchExecutionResult.SKIPPED_ALREADY_RUNNING;
            }
        } catch (JobExecutionAlreadyRunningException | JobRestartException |
                 JobInstanceAlreadyCompleteException | JobParametersInvalidException | NoSuchJobException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Batch execution lock failed: " + jobType, e);
        }

        return BatchExecutionResult.COMPLETED;
    }

    private BatchExecutionResult skipResult(String jobType) {
        if (NewsJob.SEND_NEWS_FLASH_JOB.equals(jobType)
                && mattermostResetState.isResetting(ChannelEnum.MATTERMOST_CHANNEL_NEWS_FLASH.getValue())) {
            log.info("Batch execution skipped during Mattermost reset: {}", jobType);
            return BatchExecutionResult.SKIPPED_RESETTING;
        }
        return null;
    }

    private void launchAsync(String jobType) {
        try {
            launch(jobType);
        } catch (Exception e) {
            log.error("Async batch execution failed: {}", jobType, e);
        }
    }

    private void launch(String jobType) throws JobExecutionAlreadyRunningException, JobRestartException,
            JobInstanceAlreadyCompleteException, JobParametersInvalidException, NoSuchJobException {
        JobExecution execution = jobLauncher.run(jobRegistry.getJob(jobType), getJobParameters());
        log.info("Batch execution finished: jobType={}, executionId={}, status={}",
                jobType, execution.getId(), execution.getStatus());
    }

    @Override
    @Async("asyncTaskExecutor")
    public void serviceAsync(BatchServiceRequest request) {
        setService(request);
    }

    @Override
    public void service(BatchServiceRequest request) {
        setService(request);
    }

    private void setService(BatchServiceRequest request) {
        validateServiceRequest(request);
        log.info("Batch service request: {}", request);

        switch (request.getJobType()) {
            case "account" -> lottoService.account();
            case "check" -> lottoService.check();
            case "buy" -> lottoService.buy();
            case "reset" -> reset.mattermostDelReset();
            case "saveCoinDataBTC" -> insCoinService.saveCoinDataBTC();
            case "logCacheStats" -> stockService.logCacheStats();
            case "beforeCheckJangsung" -> reserveSportSVC.beforeCheckJangsung(
                    requiredParameter(request.getParameters(), "year"),
                    requiredParameter(request.getParameters(), "month"),
                    requiredParameter(request.getParameters(), "day"),
                    requiredParameter(request.getParameters(), "st")
            );
            default -> throw new IllegalArgumentException("Unsupported service jobType: " + request.getJobType());
        }
    }

    private String requiredParameter(Map<String, Object> parameters, String name) {
        if (parameters == null || parameters.get(name) == null) {
            throw new IllegalArgumentException("Missing service parameter: " + name);
        }
        return parameters.get(name).toString();
    }

    private static JobParameters getJobParameters() {
        return new JobParametersBuilder()
                .addJobParameters(new CustomJobParametersIncrementer().getNext(new JobParameters()))
                .toJobParameters();
    }
}
