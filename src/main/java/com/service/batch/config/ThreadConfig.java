package com.service.batch.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@EnableAsync
@Configuration
public class ThreadConfig {
    @Bean
    public ThreadPoolTaskScheduler TaskScheduler() {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setThreadNamePrefix("sc-thread-");
        threadPoolTaskScheduler.setPoolSize(5);
        threadPoolTaskScheduler.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        threadPoolTaskScheduler.setWaitForTasksToCompleteOnShutdown(true);
        threadPoolTaskScheduler.setAwaitTerminationSeconds(30);
        threadPoolTaskScheduler.setThreadFactory(new ThreadFactory() {
            private AtomicInteger threadCount = new AtomicInteger(0);
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(threadPoolTaskScheduler.getThreadGroup(), r,
                        threadPoolTaskScheduler.getThreadNamePrefix() + threadCount.getAndIncrement());
                thread.setPriority(threadPoolTaskScheduler.getThreadPriority());
                thread.setDaemon(threadPoolTaskScheduler.isDaemon());
                thread.setUncaughtExceptionHandler((t, e) -> {
                    log.error("", e);
                });
                return thread;
            }
        });

        threadPoolTaskScheduler.initialize();
        return threadPoolTaskScheduler;
    }

    @Bean(name = "asyncTaskExecutor")
    public ThreadPoolTaskExecutor AsyncTaskExecutor() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setThreadNamePrefix("ex-thread-");
        threadPoolTaskExecutor.setCorePoolSize(2);
        threadPoolTaskExecutor.setMaxPoolSize(4);
        threadPoolTaskExecutor.setQueueCapacity(10);
        threadPoolTaskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        threadPoolTaskExecutor.setWaitForTasksToCompleteOnShutdown(true);
        threadPoolTaskExecutor.setAwaitTerminationSeconds(30);
        threadPoolTaskExecutor.setKeepAliveSeconds(60);
        threadPoolTaskExecutor.setThreadFactory(new ThreadFactory() {
            private AtomicInteger threadCount = new AtomicInteger(0);
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(threadPoolTaskExecutor.getThreadGroup(), r,
                        threadPoolTaskExecutor.getThreadNamePrefix() + threadCount.getAndIncrement());
                thread.setPriority(threadPoolTaskExecutor.getThreadPriority());
                thread.setDaemon(threadPoolTaskExecutor.isDaemon());
                thread.setUncaughtExceptionHandler((t, e) -> {
                    log.error("", e);
                });
                return thread;
            }
        });

        threadPoolTaskExecutor.initialize();
        return threadPoolTaskExecutor;
    }
}
