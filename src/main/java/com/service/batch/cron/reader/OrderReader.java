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
public class OrderReader {
    public static final String FIND_ORDER_ENTITY = "findOrderEntity";
    public static final int PAGE_SIZE = 100;

    @Bean(name = FIND_ORDER_ENTITY, destroyMethod = "")
    @StepScope
    public JpaPagingItemReader<OrderEntity> jpaPagingItemReader(@Qualifier("pubEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        JpaPagingItemReader<OrderEntity> reader = new JpaPagingItemReader<>();

        reader.setName("jpaPagingItemReader");
        reader.setPageSize(PAGE_SIZE);
        reader.setEntityManagerFactory(entityManagerFactory);
        reader.setQueryString("SELECT p FROM OrderEntity p order by (p.price * p.cnt) desc");

//        HashMap<String, Object> param = new HashMap<>();
//        reader.setParameterValues(param);

        return reader;
    }
}
