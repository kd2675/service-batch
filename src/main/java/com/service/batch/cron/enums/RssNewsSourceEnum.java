package com.service.batch.cron.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
@AllArgsConstructor
public enum RssNewsSourceEnum {
    ABC_TOP_STORIES("abc", "rss-us", "https://feeds.abcnews.com/abcnews/topstories"),
    FOX_NEWS_LATEST("foxNews", "rss-us", "https://moxie.foxnews.com/google-publisher/latest.xml"),
    NBC_TOP_STORIES("nbcNews", "rss-us", "https://feeds.nbcnews.com/nbcnews/public/news"),
    NPR_NEWS("npr", "rss-us", "https://feeds.npr.org/1001/rss.xml"),
    PBS_HEADLINES("pbsNewsHour", "rss-us", "https://www.pbs.org/newshour/feeds/rss/headlines"),
    NEWSNATION_LATEST("newsNation", "rss-us", "https://www.newsnationnow.com/feed/"),
    FOX_BUSINESS_LATEST("foxBusiness", "rss-business", "https://moxie.foxbusiness.com/google-publisher/latest.xml"),
    BLOOMBERG_MARKETS("bloomberg", "rss-business", "https://feeds.bloomberg.com/markets/news.rss");

    private String source;
    private String category;
    private String url;

    public static List<String> getNewsCategoryValue() {
        return Arrays.stream(RssNewsSourceEnum.values())
                .map(RssNewsSourceEnum::getCategory)
                .distinct()
                .toList();
    }
}
