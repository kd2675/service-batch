package com.service.batch.cron.writer;

import com.service.batch.database.crawling.entity.MattermostSentEntity;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.CompositeItemWriter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class MattermostComposeWriter {
    public static final String DEL_MATTERMOST_UTIL_BY_ID_AND_DEL_ALL_MATTERMOST_SENT = "delMattermostUtilByIdAndDelAllMattermostSent";

    @Bean(name = DEL_MATTERMOST_UTIL_BY_ID_AND_DEL_ALL_MATTERMOST_SENT)
    @StepScope
    public CompositeItemWriter<MattermostSentEntity> delMattermostUtilByIdAndDelAllMattermostSent(
            @Qualifier(MattermostWriter.DEL_MATTERMOST_UTIL_BY_ID) ItemWriter<MattermostSentEntity> itemCopier,
            @Qualifier(MattermostWriter.DEL_ALL_MATTERMOST_SENT) ItemWriter<MattermostSentEntity> itemWriter
    ) {
        CompositeItemWriter<MattermostSentEntity> compositeItemWriter = new CompositeItemWriter<>();
        compositeItemWriter.setDelegates(Arrays.asList(itemCopier, itemWriter));
        return compositeItemWriter;
    }
}
