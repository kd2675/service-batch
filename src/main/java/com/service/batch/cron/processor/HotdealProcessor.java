package com.service.batch.cron.processor;

import com.service.batch.cron.common.BasicProcessor;
import com.service.batch.database.crawling.dto.HotdealDTO;
import com.service.batch.database.crawling.entity.HotdealEntity;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

@Configuration
public class HotdealProcessor {
    public static final String INS_HOTDEAL_PROCESSOR = "insHotdealProcessor";
    public static final String UPD_HOTDEAL_SEND_YN_Y = "updHotdealSendYnY";

    @Bean(name = INS_HOTDEAL_PROCESSOR)
    @StepScope
    public BasicProcessor<HotdealDTO, HotdealEntity> insHotdealProcessor() {


        return new BasicProcessor<HotdealDTO, HotdealEntity>() {
            @Override
            public HotdealEntity process(HotdealDTO item) throws Exception {
                HotdealEntity hotdealEntity = HotdealEntity.builder()
                        .productId(item.getProductId())
                        .title(item.getTitle())
                        .price(item.getPrice())
                        .priceSlct(item.getPriceSlct())
                        .priceStr(item.getPriceStr())
                        .link(item.getLink())
                        .img(item.getImg())
                        .shop(item.getShop())
                        .site(item.getSite())
                        .siteIconUrl(item.getSiteIconUrl())
                        .rankNum(item.getRankNum())
                        .deliveryInfo(item.getDeliveryInfo())
                        .perPriceText(item.getPerPriceText())
                        .originalLikes(item.getOriginalLikes())
                        .originalDisLikes(item.getOriginalDisLikes())
                        .originalComments(item.getOriginalComments())
                        .originalCreatedAt(parseDateTime(item.getCreatedAt()))
                        .originalBoughtAt(parseDateTime(item.getBoughtAt()))
                        .userWant(item.getUserWant())
                        .userBought(item.getUserBought())
                        .wantCount(item.getWantCount())
                        .boughtCount(item.getBoughtCount())
                        .commentCount(item.getCommentCount())
                        .authorNickname(item.getAuthorNickname())
                        .legacyEditUrl(item.getLegacyEditUrl())
                        .ended(item.getEnded())
                        .blockNewComments(item.getBlockNewComments())
                        .exchangeRate(item.getExchangeRate())
                        .isRead(item.getIsRead())
                        .isNewWindowOpen(item.getIsNewWindowOpen())
                        .nowClickCount(item.getNowClickCount())
                        .build();


                return hotdealEntity;
            }
        };
    }

    private LocalDateTime parseDateTime(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return LocalDateTime.parse(value);
    }

    @Bean(name = UPD_HOTDEAL_SEND_YN_Y)
    @StepScope
    public BasicProcessor<HotdealEntity, HotdealEntity> itemProcessor() {
        return new BasicProcessor<HotdealEntity, HotdealEntity>() {
            @Override
            public HotdealEntity process(HotdealEntity item) throws Exception {
                item.updSendYn("y");
                return item;
            }
        };
    }
}
