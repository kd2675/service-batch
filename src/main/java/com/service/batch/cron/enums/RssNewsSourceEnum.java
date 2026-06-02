package com.service.batch.cron.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
@AllArgsConstructor
public enum RssNewsSourceEnum {
    ABC_TOP_STORIES("abc", "rss-us", "https://feeds.abcnews.com/abcnews/topstories"),
    ABC_US("abc", "rss-us", "https://feeds.abcnews.com/abcnews/usheadlines"),
    ABC_POLITICS("abc", "rss-us", "https://feeds.abcnews.com/abcnews/politicsheadlines"),
    ABC_INTERNATIONAL("abc", "rss-us", "https://feeds.abcnews.com/abcnews/internationalheadlines"),
    ABC_BUSINESS("abc", "rss-business", "https://feeds.abcnews.com/abcnews/businessheadlines"),
    ABC_TECHNOLOGY("abc", "rss-us", "https://feeds.abcnews.com/abcnews/technologyheadlines"),
    CBS_TOP("cbs", "rss-us", "https://www.cbsnews.com/latest/rss/main"),
    CBS_US("cbs", "rss-us", "https://www.cbsnews.com/latest/rss/us"),
    CBS_POLITICS("cbs", "rss-us", "https://www.cbsnews.com/latest/rss/politics"),
    CBS_WORLD("cbs", "rss-us", "https://www.cbsnews.com/latest/rss/world"),
    CBS_MONEYWATCH("cbs", "rss-business", "https://www.cbsnews.com/latest/rss/moneywatch"),
    CBS_TECHNOLOGY("cbs", "rss-us", "https://www.cbsnews.com/latest/rss/technology"),
    FOX_NEWS_LATEST("foxNews", "rss-us", "https://moxie.foxnews.com/google-publisher/latest.xml"),
    NBC_TOP_STORIES("nbcNews", "rss-us", "https://feeds.nbcnews.com/nbcnews/public/news"),
    NPR_NEWS("npr", "rss-us", "https://feeds.npr.org/1001/rss.xml"),
    NPR_POLITICS("npr", "rss-us", "https://feeds.npr.org/1014/rss.xml"),
    NPR_WORLD("npr", "rss-us", "https://feeds.npr.org/1004/rss.xml"),
    NPR_BUSINESS("npr", "rss-business", "https://feeds.npr.org/1006/rss.xml"),
    NPR_TECHNOLOGY("npr", "rss-us", "https://feeds.npr.org/1019/rss.xml"),
    PBS_HEADLINES("pbsNewsHour", "rss-us", "https://www.pbs.org/newshour/feeds/rss/headlines"),
    NEWSNATION_LATEST("newsNation", "rss-us", "https://www.newsnationnow.com/feed/"),
    NEWSMAX_NEWSFRONT("newsmax", "rss-us", "https://www.newsmax.com/rss/newsfront/16/"),
    FOX_BUSINESS_LATEST("foxBusiness", "rss-business", "https://moxie.foxbusiness.com/google-publisher/latest.xml"),
    NY_TIMES_HOME("nyTimes", "rss-us", "https://rss.nytimes.com/services/xml/rss/nyt/HomePage.xml"),
    NY_TIMES_US("nyTimes", "rss-us", "https://rss.nytimes.com/services/xml/rss/nyt/US.xml"),
    NY_TIMES_WORLD("nyTimes", "rss-us", "https://rss.nytimes.com/services/xml/rss/nyt/World.xml"),
    NY_TIMES_POLITICS("nyTimes", "rss-us", "https://rss.nytimes.com/services/xml/rss/nyt/Politics.xml"),
    NY_TIMES_BUSINESS("nyTimes", "rss-business", "https://rss.nytimes.com/services/xml/rss/nyt/Business.xml"),
    NY_TIMES_TECHNOLOGY("nyTimes", "rss-us", "https://rss.nytimes.com/services/xml/rss/nyt/Technology.xml"),
    CNBC_TOP("cnbc", "rss-business", "https://www.cnbc.com/id/100003114/device/rss/rss.html"),
    CNBC_WORLD("cnbc", "rss-business", "https://www.cnbc.com/id/100727362/device/rss/rss.html"),
    CNBC_MARKETS("cnbc", "rss-business", "https://www.cnbc.com/id/15839135/device/rss/rss.html"),
    CNBC_TECHNOLOGY("cnbc", "rss-business", "https://www.cnbc.com/id/19854910/device/rss/rss.html"),
    BLOOMBERG_MARKETS("bloomberg", "rss-business", "https://feeds.bloomberg.com/markets/news.rss"),
    MARKETWATCH_TOP("marketWatch", "rss-business", "https://feeds.content.dowjones.io/public/rss/mw_topstories"),
    THE_HILL_ALL("theHill", "rss-us", "https://thehill.com/feed/"),
    THE_HILL_HOMENEWS("theHill", "rss-us", "https://thehill.com/homenews/feed/"),
    THE_HILL_POLICY("theHill", "rss-us", "https://thehill.com/policy/feed/"),
    THE_HILL_BUSINESS("theHill", "rss-business", "https://thehill.com/business/feed/"),
    BBC_US_CANADA("bbc", "rss-us", "https://feeds.bbci.co.uk/news/world/us_and_canada/rss.xml"),
    GUARDIAN_US("guardian", "rss-us", "https://www.theguardian.com/us-news/rss"),
    LA_TIMES_WORLD_NATION("laTimes", "rss-us", "https://www.latimes.com/world-nation/rss2.0.xml"),
    LA_TIMES_POLITICS("laTimes", "rss-us", "https://www.latimes.com/politics/rss2.0.xml"),
    LA_TIMES_BUSINESS("laTimes", "rss-business", "https://www.latimes.com/business/rss2.0.xml"),
    LA_TIMES_TECHNOLOGY("laTimes", "rss-us", "https://www.latimes.com/business/technology/rss2.0.xml"),
    TIME_TOP("time", "rss-us", "https://time.com/feed/"),
    PROPUBLICA("proPublica", "rss-us", "https://www.propublica.org/feeds/propublica/main"),
    SEMAFOR("semafor", "rss-us", "https://www.semafor.com/rss.xml"),
    TECHCRUNCH("techCrunch", "rss-us", "https://techcrunch.com/feed/"),
    ARS_TECHNICA("arsTechnica", "rss-us", "https://feeds.arstechnica.com/arstechnica/index"),
    CSMONITOR_USA("csMonitor", "rss-us", "https://rss.csmonitor.com/feeds/usa");

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
