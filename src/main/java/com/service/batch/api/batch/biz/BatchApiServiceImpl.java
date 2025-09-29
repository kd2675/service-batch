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

@Service
@RequiredArgsConstructor
@Slf4j
public class BatchApiServiceImpl implements BatchApiService{
    private final JobLauncher jobLauncher;
    private final JobRegistry jobRegistry;

    private final LottoService lottoService;
    private final Reset reset;
    private final InsCoinService insCoinService;
    private final StockService stockService;
    private final ReserveSportSVC reserveSportSVC;

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
        System.out.println(request);

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
        System.out.println(request);

        switch (request.getJobType()) {
            case "account" -> lottoService.account();
            case "check" -> lottoService.check();
            case "buy" -> lottoService.buy();
            case "reset" -> reset.mattermostDelReset();
            case "saveCoinDataBTC" -> insCoinService.saveCoinDataBTC();
            case "logCacheStats" -> stockService.logCacheStats();
            case "beforeCheckJangsung" -> reserveSportSVC.beforeCheckJangsung(
                    request.getParameters().get("year").toString(),
                    request.getParameters().get("month").toString(),
                    request.getParameters().get("day").toString(),
                    request.getParameters().get("st").toString()
            );
        }
    }

    private static JobParameters getJobParameters() {
        return new JobParametersBuilder()
                .addJobParameters(new CustomJobParametersIncrementer().getNext(new JobParameters()))
                .toJobParameters();
    }
}
