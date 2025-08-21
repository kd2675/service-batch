package com.service.batch.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.support.JobRegistryBeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Slf4j
@RequiredArgsConstructor
@Configuration
@EnableScheduling
@EnableBatchProcessing(dataSourceRef = "batchMasterDatasource", transactionManagerRef = "batchTransactionManager")
public class ScheduleBatchConfig {
    private final JobRegistry jobRegistry;
    @Bean
    public JobRegistryBeanPostProcessor jobRegistryBeanPostProcessor(){
        JobRegistryBeanPostProcessor jobRegistryBeanPostProcessor = new JobRegistryBeanPostProcessor();
        jobRegistryBeanPostProcessor.setJobRegistry(jobRegistry);
        return jobRegistryBeanPostProcessor;
    }
}
