package com.service.batch.cron.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
@AllArgsConstructor
public enum GoogleNewsRssSourceEnum {
    CNN("googleNewsCnn", "rss-us", "https://news.google.com/rss/search?q=site%3Acnn.com&hl=en-US&gl=US&ceid=US%3Aen"),
    REUTERS("googleNewsReuters", "rss-us", "https://news.google.com/rss/search?q=site%3Areuters.com&hl=en-US&gl=US&ceid=US%3Aen"),
    AP("googleNewsAp", "rss-us", "https://news.google.com/rss/search?q=site%3Aapnews.com&hl=en-US&gl=US&ceid=US%3Aen");

    private String source;
    private String category;
    private String url;

    public static List<String> getNewsCategoryValue() {
        return Arrays.stream(GoogleNewsRssSourceEnum.values())
                .map(GoogleNewsRssSourceEnum::getCategory)
                .distinct()
                .toList();
    }
}
