package com.service.batch.cron.processor;

import com.service.batch.cron.common.BasicProcessor;
import com.service.batch.database.crawling.entity.CoinEntity;
import com.service.batch.database.crawling.repository.CoinREP;
import com.service.batch.database.pub.entity.OrderEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class OrderProcessor {
    public static final String ADJ_ORDER_PROCESSOR = "adjOrderProcessor";

    private final CoinREP coinREP;

    @Bean(name = ADJ_ORDER_PROCESSOR)
    @StepScope
    public BasicProcessor<OrderEntity, OrderEntity> itemProcessor() {
        CoinEntity coinEntity = coinREP.findTop1ByOrderByIdDesc().stream().findFirst().orElseGet(CoinEntity::new);
        int closingPrice = Integer.parseInt(coinEntity.getClosingPrice());

        return new BasicProcessor<OrderEntity, OrderEntity>() {
            @Override
            public OrderEntity process(OrderEntity orderEntity) throws Exception {
                //b, s
                String orderSlct = orderEntity.getOrderSlct();
                //l, s
                String marginSlct = orderEntity.getMarginSlct();
                Double orderEntityPrice = orderEntity.getPrice();

                if ("b".equals(orderSlct) && "l".equals(marginSlct) && orderEntityPrice >= closingPrice) {
                    return orderEntity;
                } else if ("s".equals(orderSlct) && "l".equals(marginSlct) && orderEntityPrice <= closingPrice) {
                    return orderEntity;
                } else if ("b".equals(orderSlct) && "s".equals(marginSlct) && orderEntityPrice <= closingPrice) {
                    return orderEntity;
                } else if ("s".equals(orderSlct) && "s".equals(marginSlct) && orderEntityPrice >= closingPrice) {
                    return orderEntity;
                }

                return null;
            }
        };
//        return CoinEntity::getId;
//        return item -> CoinDTO.ofEntity(item).getId();
//        return item -> item;
    }
}
