package com.service.batch.cron.common;

import org.springframework.batch.item.database.JpaPagingItemReader;

public class DelJpaPagingItemReader<T> extends JpaPagingItemReader<T> {
    @Override
    public int getPage() {
        return 0;
    }
}
