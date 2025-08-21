package com.service.batch.cron.processor;

import com.service.batch.cron.common.BasicProcessor;
import com.service.batch.database.crawling.entity.NewsEntity;
import com.service.batch.service.news.api.vo.NaverNewsApiItemVO;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Configuration
public class NewsProcessor {
    public static final String NEWS_ENTITY_UPD_SEND_YN_Y = "newsEntityUpdSendYnToY";
    public static final String NAVER_NEWS_API_ITEM_VO_TO_NEWS_ENTITY = "naverNewsApiItemVoToNewsEntity";
    @Bean(name = NAVER_NEWS_API_ITEM_VO_TO_NEWS_ENTITY)
    @StepScope
    public BasicProcessor<NaverNewsApiItemVO, NewsEntity> naverNewsApiItemVoToNewsEntity() {
        return new BasicProcessor<NaverNewsApiItemVO, NewsEntity>() {
            @Override
            public NewsEntity process(NaverNewsApiItemVO item) throws Exception {
                NewsEntity newsEntity = NewsEntity.builder()
                        .category(item.getCategory())
                        .company("naverApi")
                        .title(item.getTitle())
                        .content(item.getDescription())
                        .link(item.getLink())
                        .pubDate(LocalDateTime.parse(item.getPubDate(), DateTimeFormatter.RFC_1123_DATE_TIME))
                        .build();

                return newsEntity;
            }
        };
    }

    @Bean(name = NEWS_ENTITY_UPD_SEND_YN_Y)
    @StepScope
    public BasicProcessor<NewsEntity, NewsEntity> itemProcessor() {
        return new BasicProcessor<NewsEntity, NewsEntity>() {
            @Override
            public NewsEntity process(NewsEntity item) throws Exception {
                item.updSendYn("y");
                return item;
            }
        };
    }
}
