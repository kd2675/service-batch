package com.service.batch.cron.writer;

import com.service.batch.cron.common.BasicWriter;
import com.service.batch.database.pub.entity.OrderEntity;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.support.CompositeItemWriter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class OrderComposeWriter {
    public static final String COMPLETE_ORDER_TO_MARKET_AND_DEL_ORDER = "completeOrderToMarketAndDelOrder";

    @Bean(name = COMPLETE_ORDER_TO_MARKET_AND_DEL_ORDER)
    @StepScope
    public CompositeItemWriter<OrderEntity> saveOldNewsAndDelAllNews(
            @Qualifier(OrderWriter.COMPLETE_ORDER_TO_MARKET) BasicWriter<OrderEntity> completeOrderToMarket,
            @Qualifier(OrderWriter.DEL_ORDER) BasicWriter<OrderEntity> delOrder
    ) {
        CompositeItemWriter<OrderEntity> compositeItemWriter = new CompositeItemWriter<>();
        compositeItemWriter.setDelegates(Arrays.asList(completeOrderToMarket, delOrder));
        return compositeItemWriter;
    }
}
