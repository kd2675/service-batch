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
    AP("googleNewsAp", "rss-us", "https://news.google.com/rss/search?q=site%3Aapnews.com&hl=en-US&gl=US&ceid=US%3Aen"),
    POLITICO("googleNewsPolitico", "rss-us", "https://news.google.com/rss/search?q=site%3Apolitico.com&hl=en-US&gl=US&ceid=US%3Aen"),
    AXIOS("googleNewsAxios", "rss-us", "https://news.google.com/rss/search?q=site%3Aaxios.com&hl=en-US&gl=US&ceid=US%3Aen"),
    WASHINGTON_POST("googleNewsWashingtonPost", "rss-us", "https://news.google.com/rss/search?q=site%3Awashingtonpost.com&hl=en-US&gl=US&ceid=US%3Aen"),
    USA_TODAY("googleNewsUsaToday", "rss-us", "https://news.google.com/rss/search?q=site%3Ausatoday.com&hl=en-US&gl=US&ceid=US%3Aen"),
    NEWSMAX("googleNewsNewsmax", "rss-us", "https://news.google.com/rss/search?q=site%3Anewsmax.com&hl=en-US&gl=US&ceid=US%3Aen"),
    TIME("googleNewsTime", "rss-us", "https://news.google.com/rss/search?q=site%3Atime.com&hl=en-US&gl=US&ceid=US%3Aen"),
    WALL_STREET_JOURNAL("googleNewsWsj", "rss-business", "https://news.google.com/rss/search?q=site%3Awsj.com&hl=en-US&gl=US&ceid=US%3Aen");

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
