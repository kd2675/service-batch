package com.service.batch.cron.writer;

import com.service.batch.cron.common.BasicWriter;
import com.service.batch.database.crawling.entity.NewsEntity;
import com.service.batch.database.crawling.entity.OldNewsEntity;
import com.service.batch.database.crawling.repository.NewsREP;
import com.service.batch.database.crawling.repository.OldNewsREP;
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
public class NewsWriter {
    public static final String JPA_ITEM_WRITER = "jpaItemWriter";
    public static final String OLD_NEWS_SAVE = "saveOldNews";
    public static final String DEL_ALL_NEWS = "delAllNews";

    private final OldNewsREP oldNewsREP;
    private final NewsREP newsREP;

    @Bean(name = JPA_ITEM_WRITER)
    @StepScope
    public JpaItemWriter<NewsEntity> jpaItemWriter(@Qualifier("crawlingEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaItemWriterBuilder<NewsEntity>()
                .entityManagerFactory(entityManagerFactory)
                .build();
    }

    @Bean(name = OLD_NEWS_SAVE)
    @StepScope
    public BasicWriter<NewsEntity> oldNewsSave() {
        return new BasicWriter<NewsEntity>() {
            @Override
            public void write(Chunk<? extends NewsEntity> chunk) throws Exception {
                for (NewsEntity item : chunk) {
                    OldNewsEntity newsEntity = OldNewsEntity.builder()
                            .category(item.getCategory())
                            .company(item.getCompany())
                            .title(item.getTitle())
                            .content(item.getContent())
                            .link(item.getLink())
                            .pubDate(item.getPubDate())
                            .build();
                    oldNewsREP.save(newsEntity);
                }
            }
        };
    }

    @Bean(name = DEL_ALL_NEWS)
    @StepScope
    public BasicWriter<NewsEntity> newsDelAll() {
        return new BasicWriter<NewsEntity>() {
            @Override
            public void write(Chunk<? extends NewsEntity> chunk) throws Exception {
                newsREP.deleteAll(chunk);
            }
        };
    }
}
