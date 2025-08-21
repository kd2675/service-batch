package com.service.batch.cron.writer;

import com.service.batch.cron.common.BasicWriter;
import com.service.batch.database.crawling.repository.CoinREP;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.Chunk;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@RequiredArgsConstructor
@Configuration
public class CoinWriter {
    private final CoinREP coinREP;
    public static final String DEL_COIN_BY_ID = "delCoinById";

    @Bean(name = DEL_COIN_BY_ID)
    @StepScope
    public BasicWriter<Long> itemWriter() {
        return new BasicWriter<Long>() {
            @Override
            public void write(Chunk<? extends Long> chunk) throws Exception {
                for (Long id : chunk) {
                    coinREP.deleteById(id);
                }
            }
        };
    }


}
