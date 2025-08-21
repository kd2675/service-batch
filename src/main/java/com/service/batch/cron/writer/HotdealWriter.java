package com.service.batch.cron.writer;

import com.service.batch.cron.common.BasicWriter;
import com.service.batch.database.crawling.entity.HotdealEntity;
import com.service.batch.database.crawling.repository.HotdealEntityREP;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class HotdealWriter {
    public static final String JPA_ITEM_WRITER = "insHotdealWhiter";
    public static final String DEL_ALL_HOTDEAL = "delAllHotdeal";

    private final HotdealEntityREP hotdealEntityREP;

    @Bean(name = JPA_ITEM_WRITER)
    @StepScope
    public JpaItemWriter<HotdealEntity> jpaItemWriter(@Qualifier("crawlingEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaItemWriterBuilder<HotdealEntity>()
                .entityManagerFactory(entityManagerFactory)
                .build();
    }

    @Bean(name = DEL_ALL_HOTDEAL)
    @StepScope
    public BasicWriter<HotdealEntity> delAllHotdeal() {
        return new BasicWriter<HotdealEntity>() {
            @Override
            public void write(Chunk<? extends HotdealEntity> chunk) throws Exception {
                hotdealEntityREP.deleteAll(chunk);
            }
        };
    }
}
