package com.service.batch.cron.reader;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.service.batch.cron.common.DelJpaPagingItemReader;
import com.service.batch.cron.enums.NewsKeywordEnum;
import com.service.batch.database.crawling.entity.NewsEntity;
import com.service.batch.database.crawling.entity.NewsSubscribeEntity;
import com.service.batch.database.crawling.repository.NewsREP;
import com.service.batch.database.crawling.repository.NewsSubscribeEntityREP;
import com.service.batch.service.news.api.vo.NaverNewsApiItemVO;
import com.service.batch.service.news.api.vo.NaverNewsApiVO;
import com.service.batch.utils.MattermostUtil;
import com.service.batch.utils.NaverApiUtil;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class NewsReader {
    private static final int PAGE_SIZE = 100;
    public static final String FIND_NAVER_NEWS_API = "findNaverNewsApi";
    public static final String FIND_TOP_15_NEWS = "findTop15News";
    public static final String FIND_TOP_15_NEWS_FLASH = "findTop15NewsFlash";
    public static final String FIND_TOP_15_NEWS_MARKETING = "findTop15NewsMarketing";
    public static final String FIND_TOP_15_NEWS_STOCK = "findTop15NewsStock";
    public static final String FIND_ALL_NEWS_FIX_PAGE_0 = "findAllNewsFixPage0";

    private final NewsREP newsREP;
    private final NaverApiUtil naverApiUtil;
    private final NewsSubscribeEntityREP newsSubscribeEntityREP;
    private final MattermostUtil mattermostUtil;

    @Bean(name = FIND_NAVER_NEWS_API, destroyMethod = "")
    @StepScope
    public ListItemReader<NaverNewsApiItemVO> findNaverNewsApi() {
        return new ListItemReader<NaverNewsApiItemVO>(this.getNaverNewsApiItemVOS());
    }

    @Bean(name = FIND_TOP_15_NEWS, destroyMethod = "")
    @StepScope
    public ListItemReader<NewsEntity> findTop15News(@Qualifier("crawlingEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new ListItemReader<>(
                newsREP.findTop15BySendYnAndCategoryInOrderByIdDesc("n", NewsKeywordEnum.getNewsKeywordValue())
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
        param.put("date", LocalDateTime.now().minusHours(3));
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

        List<NaverNewsApiItemVO> news = new ArrayList<>(set);
        Collections.sort(news);

        try {
            this.newsSubscribeAlim(news);
        } catch (Exception e) {
            log.error("hotdealAlimSend error : {}", e);
        }

        return news;
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
