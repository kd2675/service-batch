package com.service.batch.cron.reader;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.service.batch.cron.common.DelJpaPagingItemReader;
import com.service.batch.cron.enums.GoogleNewsRssSourceEnum;
import com.service.batch.cron.enums.NewsKeywordEnum;
import com.service.batch.cron.enums.RssNewsSourceEnum;
import com.service.batch.database.crawling.entity.NewsEntity;
import com.service.batch.database.crawling.entity.NewsSubscribeEntity;
import com.service.batch.database.crawling.repository.NewsREP;
import com.service.batch.database.crawling.repository.NewsSubscribeEntityREP;
import com.service.batch.service.news.api.vo.NaverNewsApiItemVO;
import com.service.batch.service.news.api.vo.NaverNewsApiVO;
import com.service.batch.service.news.api.vo.RssNewsItemVO;
import com.service.batch.utils.MattermostUtil;
import com.service.batch.utils.NaverApiUtil;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.time.ZoneId;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class NewsReader {
    private static final int PAGE_SIZE = 100;
    private static final int RSS_NEWS_LOOKBACK_HOURS = 6;
    public static final String FIND_NAVER_NEWS_API = "findNaverNewsApi";
    public static final String FIND_RSS_NEWS = "findRssNews";
    public static final String FIND_TOP_15_NEWS = "findTop15News";
    public static final String FIND_TOP_15_NEWS_FLASH = "findTop15NewsFlash";
    public static final String FIND_TOP_15_NEWS_MARKETING = "findTop15NewsMarketing";
    public static final String FIND_TOP_15_NEWS_STOCK = "findTop15NewsStock";
    public static final String FIND_ALL_NEWS_FIX_PAGE_0 = "findAllNewsFixPage0";

    private final NewsREP newsREP;
    private final NaverApiUtil naverApiUtil;
    private final NewsSubscribeEntityREP newsSubscribeEntityREP;
    private final MattermostUtil mattermostUtil;
    private final RestTemplate restTemplate;

    @Bean(name = FIND_NAVER_NEWS_API, destroyMethod = "")
    @StepScope
    public ListItemReader<NaverNewsApiItemVO> findNaverNewsApi() {
        return new ListItemReader<NaverNewsApiItemVO>(this.getNaverNewsApiItemVOS());
    }

    @Bean(name = FIND_RSS_NEWS, destroyMethod = "")
    @StepScope
    public ListItemReader<RssNewsItemVO> findRssNews() {
        return new ListItemReader<RssNewsItemVO>(this.getRssNewsItemVOS());
    }

    @Bean(name = FIND_TOP_15_NEWS, destroyMethod = "")
    @StepScope
    public ListItemReader<NewsEntity> findTop15News(@Qualifier("crawlingEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        List<String> categories = new ArrayList<>(NewsKeywordEnum.getNewsKeywordValue());

        return new ListItemReader<>(
                newsREP.findTop15BySendYnAndCategoryInOrderByIdDesc("n", categories)
        );
    }

    @Bean(name = FIND_TOP_15_NEWS_FLASH, destroyMethod = "")
    @StepScope
    public ListItemReader<NewsEntity> findTop15NewsFlash(@Qualifier("crawlingEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        LocalDateTime localDateTime = LocalDateTime.now().minusHours(1);
        ;
        return new ListItemReader<>(
                newsREP.findBySendYnAndCategoryInAndCreateDateAfterOrderByIdDesc("n", NewsKeywordEnum.getNewsFlashKeywordValue(), localDateTime)
        );
    }

    @Bean(name = FIND_TOP_15_NEWS_MARKETING, destroyMethod = "")
    @StepScope
    public ListItemReader<NewsEntity> findTop15NewsMarketing(@Qualifier("crawlingEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new ListItemReader<>(
                newsREP.findTop15BySendYnAndCategoryInOrderByIdDesc("n", NewsKeywordEnum.getNewsMarketingKeywordValue())
        );
    }

    @Bean(name = FIND_TOP_15_NEWS_STOCK, destroyMethod = "")
    @StepScope
    public ListItemReader<NewsEntity> findTop15NewsStock(@Qualifier("crawlingEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new ListItemReader<>(
                newsREP.findTop15BySendYnAndCategoryInOrderByIdDesc("n", NewsKeywordEnum.getNewsStockKeywordValue())
        );
    }

    @Bean(name = FIND_ALL_NEWS_FIX_PAGE_0, destroyMethod = "")
    @StepScope
    public JpaPagingItemReader<NewsEntity> newsFindAllFixPage0(@Qualifier("crawlingEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        JpaPagingItemReader<NewsEntity> reader = new DelJpaPagingItemReader<>();

        reader.setName("jpaPagingItemReader");
        reader.setPageSize(PAGE_SIZE);
        reader.setEntityManagerFactory(entityManagerFactory);
        reader.setQueryString("SELECT e FROM NewsEntity e WHERE e.pubDate < :date");

        HashMap<String, Object> param = new HashMap<>();
        param.put("date", LocalDateTime.now().minusHours(24));
        reader.setParameterValues(param);
        return reader;
    }

    private List<NaverNewsApiItemVO> getNaverNewsApiItemVOS() {
        LocalDateTime LOCAL_DATE_TIME_1 = LocalDateTime.now().minusMinutes(5);
        LocalDateTime LOCAL_DATE_TIME_2 = LocalDateTime.now().minusMinutes(6);

        Set<NaverNewsApiItemVO> set = new HashSet<>();

        for (NewsKeywordEnum keyword : NewsKeywordEnum.values()) {
            String s = keyword.getValue();

            int start = 1;

            do {
                List<NaverNewsApiItemVO> items = getItems(s, start);
                set.addAll(items.stream()
                        .peek(v -> v.setCategory(s))
                        .filter(v -> StringUtils.isNotBlank(v.getLink()))
                        .filter(v -> LocalDateTime.parse(v.getPubDate(), DateTimeFormatter.RFC_1123_DATE_TIME).isAfter(LOCAL_DATE_TIME_2)
                                && LocalDateTime.parse(v.getPubDate(), DateTimeFormatter.RFC_1123_DATE_TIME).isBefore(LOCAL_DATE_TIME_1))
                        .toList()
                );
                start += 100;

                if (items.stream()
                        .anyMatch(v -> LocalDateTime.parse(v.getPubDate(), DateTimeFormatter.RFC_1123_DATE_TIME).isBefore(LOCAL_DATE_TIME_2))
                ) {
                    start = 1100;
                }
            } while (start < 1000);
        }

        Set<String> newLinks = findNewLinks(set.stream()
                .map(NaverNewsApiItemVO::getLink)
                .collect(Collectors.toSet()));
        List<NaverNewsApiItemVO> news = new ArrayList<>(set.stream()
                .filter(item -> newLinks.remove(item.getLink()))
                .toList());
        Collections.sort(news);

//        try {
//            this.newsSubscribeAlim(news);
//        } catch (Exception e) {
//            log.error("hotdealAlimSend error : {}", e);
//        }

        return news;
    }

    private List<RssNewsItemVO> getRssNewsItemVOS() {
        LocalDateTime from = LocalDateTime.now().minusHours(RSS_NEWS_LOOKBACK_HOURS);
        LocalDateTime to = LocalDateTime.now().plusMinutes(5);
        Set<String> links = new HashSet<>();
        List<RssNewsItemVO> news = new ArrayList<>();

        for (RssNewsSourceEnum source : RssNewsSourceEnum.values()) {
            try {
                addRecentRssItems(news, links, getRssItems(source.getSource(), source.getCategory(), source.getUrl()), from, to);
            } catch (Exception e) {
                log.error("rss news collect error source : {}, url : {}", source.getSource(), source.getUrl(), e);
            }
        }

        for (GoogleNewsRssSourceEnum source : GoogleNewsRssSourceEnum.values()) {
            try {
                addRecentRssItems(news, links, getRssItems(source.getSource(), source.getCategory(), source.getUrl()), from, to);
            } catch (Exception e) {
                log.error("google news rss collect error source : {}, url : {}", source.getSource(), source.getUrl(), e);
            }
        }

        Set<String> newLinks = findNewLinks(links);
        news.removeIf(item -> !newLinks.contains(item.getLink()));
        Collections.sort(news);
        return news;
    }

    private void addRecentRssItems(List<RssNewsItemVO> news, Set<String> links, List<RssNewsItemVO> items, LocalDateTime from, LocalDateTime to) {
        for (RssNewsItemVO item : items) {
            if (item.getPubDate().isAfter(from)
                    && item.getPubDate().isBefore(to)
                    && links.add(item.getLink())
            ) {
                news.add(item);
            }
        }
    }

    Set<String> findNewLinks(Collection<String> links) {
        if (links.isEmpty()) {
            return new HashSet<>();
        }

        Set<String> newLinks = new HashSet<>(links);
        newLinks.removeAll(newsREP.findExistingLinks(List.copyOf(newLinks)));
        return newLinks;
    }

    private List<RssNewsItemVO> getRssItems(String source, String category, String url) {
        String xml = getRssXml(url);
        if (StringUtils.isBlank(xml)) {
            return new ArrayList<>();
        }

        Document document = Jsoup.parse(xml, url, Parser.xmlParser());
        List<Element> elements = document.select("channel > item");
        if (elements.isEmpty()) {
            elements = document.select("feed > entry");
        }

        List<RssNewsItemVO> result = new ArrayList<>();
        for (Element element : elements) {
            toRssNewsItem(source, category, element).ifPresent(result::add);
        }
        return result;
    }

    private String getRssXml(String url) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.USER_AGENT, "Mozilla/5.0 news-rss-batch");

        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );
        return response.getBody();
    }

    private Optional<RssNewsItemVO> toRssNewsItem(String source, String category, Element element) {
        String title = limitText(cleanRssText(firstTagText(element, "title")), 255);
        String description = cleanRssText(firstTagText(element, "description", "summary"));
        String link = normalizeLink(firstRssLink(element));
        LocalDateTime pubDate = parseRssDate(firstTagText(element, "pubDate", "published", "updated", "dc:date", "dc|date"));

        if (StringUtils.isBlank(title) || StringUtils.isBlank(link) || pubDate == null) {
            return Optional.empty();
        }

        if (StringUtils.isBlank(description)) {
            description = title;
        }

        return Optional.of(new RssNewsItemVO(
                source,
                category,
                title,
                description,
                link,
                pubDate
        ));
    }

    private String firstRssLink(Element element) {
        Element linkElement = firstTag(element, "link");
        if (linkElement == null) {
            return "";
        }
        if (StringUtils.isNotBlank(linkElement.attr("href"))) {
            return linkElement.attr("abs:href");
        }
        return linkElement.text();
    }

    private String firstTagText(Element element, String... tagNames) {
        Element tag = firstTag(element, tagNames);
        if (tag == null) {
            return "";
        }
        return tag.text();
    }

    private Element firstTag(Element element, String... tagNames) {
        for (String tagName : tagNames) {
            Element tag = element.getElementsByTag(tagName).first();
            if (tag != null) {
                return tag;
            }
        }
        return null;
    }

    private String cleanRssText(String value) {
        if (StringUtils.isBlank(value)) {
            return "";
        }
        return Jsoup.parse(value).text()
                .replace("[", "")
                .replace("]", "")
                .replace("♥", "")
                .replace("|", "")
                .trim();
    }

    private String limitText(String value, int maxLength) {
        if (StringUtils.length(value) <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    private String normalizeLink(String link) {
        return StringUtils.defaultString(link).trim();
    }

    private LocalDateTime parseRssDate(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }

        String date = value.trim();
        for (DateTimeFormatter formatter : List.of(
                DateTimeFormatter.RFC_1123_DATE_TIME,
                DateTimeFormatter.ISO_ZONED_DATE_TIME,
                DateTimeFormatter.ISO_OFFSET_DATE_TIME
        )) {
            try {
                return ZonedDateTime.parse(date, formatter)
                        .withZoneSameInstant(ZoneId.systemDefault())
                        .toLocalDateTime();
            } catch (DateTimeParseException ignored) {
            }
        }

        try {
            return LocalDateTime.parse(date, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }

    private List<NaverNewsApiItemVO> getItems(String query, int start) {
        try {
            ResponseEntity conn = naverApiUtil.conn("news", query, 100, start, "date");
            String body = (String) conn.getBody();

            ObjectMapper objectMapper = new ObjectMapper();
            NaverNewsApiVO naverNewsApiVO = objectMapper.readValue(body, NaverNewsApiVO.class);

            return naverNewsApiVO.getItems();
        } catch (JsonProcessingException e) {
            log.error("{}", e);
        }
        return new ArrayList<>();
    }

    private void newsSubscribeAlim(List<NaverNewsApiItemVO> newsEntityVOS) {
        List<NewsSubscribeEntity> all = newsSubscribeEntityREP.findAll();

        for (NaverNewsApiItemVO itemVO : newsEntityVOS) {
            all.stream()
                    .filter(v -> {
                        if (StringUtils.contains(itemVO.getTitle(), v.getKeyword())
                                || StringUtils.contains(itemVO.getDescription(), v.getKeyword())
                        ) {
                            return true;
                        }

                        return false;
                    })
                    .findFirst()
                    .ifPresentOrElse(
                            v -> {
                                mattermostUtil.sendSubNewsChannel(convertNewsMattermostMessage(itemVO));
                            },
                            () -> {

                            }
                    );
        }
    }

    public String convertNewsMattermostMessage(NaverNewsApiItemVO itemVO) {
        StringBuilder result = new StringBuilder();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String regexEmojis = "[\uD83C-\uDBFF\uDC00-\uDFFF]+";

        String header = "| 시각 | 제목 |\n";
        String line = "| :-:|:--: |\n";
//        String header = "| 시각 | 제목 | 시각 | 제목 |\n";
//        String line = "| :-:|:--:|:-:|:--: |\n";
        result.append(header)
                .append(line);

        String content = "";
        for (int i = 0; i < 1; i++) {
            content += "| " + dtf.format(LocalDateTime.parse(itemVO.getPubDate(), DateTimeFormatter.RFC_1123_DATE_TIME))
                    + " | " + "[" + itemVO.getTitle().replaceAll(regexEmojis, "")
                    .replace("[", "")
                    .replace("]", "")
                    .replace("♥", "")
                    .replace("|", "") + "]" + "(" + itemVO.getLink() + ")";
        }
        content += " |\n";
        result.append(content);


        return result.toString();
    }
}
