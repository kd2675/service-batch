package com.service.batch.api.batch.biz;

import com.service.batch.cron.common.CustomJobParametersIncrementer;
import com.service.batch.service.coin.api.biz.ins.InsCoinService;
import com.service.batch.service.lotto.biz.LottoService;
import com.service.batch.service.reset.api.biz.Reset;
import com.service.batch.service.sport.biz.ReserveSportSVC;
import com.service.batch.service.stock.biz.StockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.core.request.BatchExecuteRequest;
import org.example.core.request.BatchServiceRequest;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class BatchApiServiceImpl implements BatchApiService{
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
    @Async("asyncTaskExecutor")
    public void executeAsync(BatchExecuteRequest request) throws Exception {
        setExecute(request);
    }

    @Override
    public void execute(BatchExecuteRequest request) throws Exception{
        setExecute(request);
    }

    private void setExecute(BatchExecuteRequest request) throws JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException, JobParametersInvalidException, NoSuchJobException {
        validateExecuteRequest(request);
        log.info("Batch execute request: {}", request);

        jobLauncher.run(jobRegistry.getJob(request.getJobType()), getJobParameters());
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
