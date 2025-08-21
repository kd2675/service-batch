package com.service.batch.cron.common;


import org.springframework.batch.item.ItemProcessor;

public abstract class BasicProcessor<T, T2> implements ItemProcessor<T, T2> {
}
