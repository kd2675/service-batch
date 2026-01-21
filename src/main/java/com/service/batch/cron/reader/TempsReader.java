package com.service.batch.cron.reader;

import com.service.batch.database.pub.entity.OrderEntity;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@RequiredArgsConstructor
@Configuration
public class TempsReader {
    public static final String GET_NOW_TEMPS = "getNowTemps";
    public static final int PAGE_SIZE = 100;

    @Bean(name = GET_NOW_TEMPS, destroyMethod = "")
    @StepScope
    public JpaPagingItemReader<OrderEntity> getNowTempsReader() {

        return null;
    }
}
