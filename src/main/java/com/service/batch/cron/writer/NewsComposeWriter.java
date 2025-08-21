package com.service.batch.cron.writer;

import com.service.batch.cron.common.BasicWriter;
import com.service.batch.database.crawling.entity.NewsEntity;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.support.CompositeItemWriter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class NewsComposeWriter {
    public static final String SAVE_OLD_NEWS_AND_DEL_ALL_NEWS = "saveOldNewsAndDelAllNews";
    public static final String SEND_NEWS_TO_MATTERMOST_AND_SAVE_MATTERMOST_SENT_AND_UPD_SEND_YN = "sendNewsToMattermostAndSaveMattermostSentAndUpdSendYn";
    public static final String SEND_NEWS_FLASH_TO_MATTERMOST_AND_SAVE_MATTERMOST_SENT_AND_UPD_SEND_YN = "sendNewsFlashToMattermostAndSaveMattermostSentAndUpdSendYn";
    public static final String SEND_NEWS_MARKETING_TO_MATTERMOST_AND_SAVE_MATTERMOST_SENT_AND_UPD_SEND_YN = "sendNewsMarketingToMattermostAndSaveMattermostSentAndUpdSendYn";
    public static final String SEND_NEWS_STOCK_TO_MATTERMOST_AND_SAVE_MATTERMOST_SENT_AND_UPD_SEND_YN = "sendNewsStockToMattermostAndSaveMattermostSentAndUpdSendYn";

    @Bean(name = SAVE_OLD_NEWS_AND_DEL_ALL_NEWS)
    @StepScope
    public CompositeItemWriter<NewsEntity> saveOldNewsAndDelAllNews(
            @Qualifier(NewsWriter.OLD_NEWS_SAVE) BasicWriter<NewsEntity> oldNewsSave,
            @Qualifier(NewsWriter.DEL_ALL_NEWS) BasicWriter<NewsEntity> newsDelAll
    ) {
        CompositeItemWriter<NewsEntity> compositeItemWriter = new CompositeItemWriter<>();
        compositeItemWriter.setDelegates(Arrays.asList(oldNewsSave, newsDelAll));
        return compositeItemWriter;
    }

    @Bean(name = SEND_NEWS_TO_MATTERMOST_AND_SAVE_MATTERMOST_SENT_AND_UPD_SEND_YN)
    @StepScope
    public CompositeItemWriter<NewsEntity> sendNewsToMattermostAndSaveMattermostSentAndUpdSendYn(
            @Qualifier(MattermostWriter.SEND_NEWS_AND_SAVE_MATTERMOST_SENT) BasicWriter<NewsEntity> itemSender,
            @Qualifier(NewsWriter.JPA_ITEM_WRITER) JpaItemWriter<NewsEntity> itemWriter
    ) {
        CompositeItemWriter<NewsEntity> compositeItemWriter = new CompositeItemWriter<>();
        compositeItemWriter.setDelegates(Arrays.asList(itemSender, itemWriter));
        return compositeItemWriter;
    }

    @Bean(name = SEND_NEWS_FLASH_TO_MATTERMOST_AND_SAVE_MATTERMOST_SENT_AND_UPD_SEND_YN)
    @StepScope
    public CompositeItemWriter<NewsEntity> sendNewsFlashToMattermostAndSaveMattermostSentAndUpdSendYn(
            @Qualifier(MattermostWriter.SEND_NEWS_FLASH_AND_SAVE_MATTERMOST_SENT) BasicWriter<NewsEntity> itemSender,
            @Qualifier(NewsWriter.JPA_ITEM_WRITER) JpaItemWriter<NewsEntity> itemWriter
    ) {
        CompositeItemWriter<NewsEntity> compositeItemWriter = new CompositeItemWriter<>();
        compositeItemWriter.setDelegates(Arrays.asList(itemSender, itemWriter));
        return compositeItemWriter;
    }

    @Bean(name = SEND_NEWS_MARKETING_TO_MATTERMOST_AND_SAVE_MATTERMOST_SENT_AND_UPD_SEND_YN)
    @StepScope
    public CompositeItemWriter<NewsEntity> sendNewsMarketingToMattermostAndSaveMattermostSentAndUpdSendYn(
            @Qualifier(MattermostWriter.SEND_NEWS_MARKETING_AND_SAVE_MATTERMOST_SENT) BasicWriter<NewsEntity> itemSender,
            @Qualifier(NewsWriter.JPA_ITEM_WRITER) JpaItemWriter<NewsEntity> itemWriter
    ) {
        CompositeItemWriter<NewsEntity> compositeItemWriter = new CompositeItemWriter<>();
        compositeItemWriter.setDelegates(Arrays.asList(itemSender, itemWriter));
        return compositeItemWriter;
    }

    @Bean(name = SEND_NEWS_STOCK_TO_MATTERMOST_AND_SAVE_MATTERMOST_SENT_AND_UPD_SEND_YN)
    @StepScope
    public CompositeItemWriter<NewsEntity> sendNewsStockToMattermostAndSaveMattermostSentAndUpdSendYn(
            @Qualifier(MattermostWriter.SEND_NEWS_STOCK_AND_SAVE_MATTERMOST_SENT) BasicWriter<NewsEntity> itemSender,
            @Qualifier(NewsWriter.JPA_ITEM_WRITER) JpaItemWriter<NewsEntity> itemWriter
    ) {
        CompositeItemWriter<NewsEntity> compositeItemWriter = new CompositeItemWriter<>();
        compositeItemWriter.setDelegates(Arrays.asList(itemSender, itemWriter));
        return compositeItemWriter;
    }
}
