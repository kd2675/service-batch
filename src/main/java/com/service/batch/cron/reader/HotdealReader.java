package com.service.batch.cron.reader;

import com.service.batch.cron.common.DelJpaPagingItemReader;
import com.service.batch.database.crawling.dto.HotdealDTO;
import com.service.batch.database.crawling.entity.HotdealAlimEntity;
import com.service.batch.database.crawling.entity.HotdealEntity;
import com.service.batch.database.crawling.repository.HotdealAlimEntityREP;
import com.service.batch.database.crawling.repository.HotdealEntityREP;
import com.service.batch.service.webhook.api.vo.MemberEnum;
import com.service.batch.utils.MattermostUtil;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class HotdealReader {
    private static final int PAGE_SIZE = 100;
    public static final String FIND_HOTDEAL = "findHotdeal";
    public static final String FIND_ALL_HOTDEAL_SEND_YN_N = "findAllHotdealSendYnN";
    public static final String FIND_ALL_HOTDEAL_FIX_PAGE_0 = "findAllHotdealFixPage0";

    private final RestTemplate restTemplate;

    private final HotdealEntityREP hotdealEntityREP;
    private final HotdealAlimEntityREP hotdealAlimEntityREP;
    private final MattermostUtil mattermostUtil;

    @Bean(name = FIND_HOTDEAL, destroyMethod = "")
    @StepScope
    public ListItemReader<HotdealDTO> findHotdeal() {
        return new ListItemReader<HotdealDTO>(this.getHotdeal());
    }

    @Bean(name = FIND_ALL_HOTDEAL_SEND_YN_N, destroyMethod = "")
    @StepScope
    public ListItemReader<HotdealEntity> findAllHotdealSendYnN(@Qualifier("crawlingEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new ListItemReader<>(
                hotdealEntityREP.findAllBySendYnOrderByIdDesc("n")
        );
    }

    @Bean(name = FIND_ALL_HOTDEAL_FIX_PAGE_0, destroyMethod = "")
    @StepScope
    public JpaPagingItemReader<HotdealEntity> hotdealFindAllFixPage0(@Qualifier("crawlingEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        JpaPagingItemReader<HotdealEntity> reader = new DelJpaPagingItemReader<>();

        reader.setName("jpaPagingItemReader");
        reader.setPageSize(PAGE_SIZE);
        reader.setEntityManagerFactory(entityManagerFactory);
        reader.setQueryString("SELECT e FROM HotdealEntity e WHERE e.createDate < :date");

        HashMap<String, Object> param = new HashMap<>();
        param.put("date", LocalDateTime.now().minusDays(15));
        reader.setParameterValues(param);
        return reader;
    }

    private List<HotdealDTO> getHotdeal() {
        List<HotdealDTO> result = new ArrayList<>();

        HotdealEntity hotdealEntity = hotdealEntityREP.findTop1ByOrderByProductIdDesc().stream().findFirst().orElse(null);

        if (hotdealEntity != null) {
            for (int i = 0; i < 5; i++) {
                List<HotdealDTO> hotdeal = this.getHotdeal(i);

                Long productId = hotdealEntity.getProductId();

                boolean limit1 = hotdeal.stream()
                        .anyMatch(v -> v.getProductId().compareTo(productId) == 0);

                boolean limit2 = hotdeal.stream()
                        .anyMatch(v -> v.getProductId().compareTo(productId) > 0);

                List<HotdealDTO> collect = hotdeal.stream()
                        .filter(v -> v.getProductId().compareTo(productId) > 0)
                        .toList();

                result.addAll(collect);

                if (limit1 || !limit2) {
                    break;
                }
            }
        } else {
            List<HotdealDTO> hotdeal = this.getHotdeal(0);
            result.addAll(hotdeal);
        }

        try {
            this.hotdealAlimSend(result);
        } catch (Exception e) {
            log.error("hotdealAlimSend error : {}", e);
        }

        return result;
    }

    private List<HotdealDTO> getHotdeal(int num) {
//        {
//            "types": "TYPE_ENDED",
//                "homeFeedType": "LATEST",
//                "site": "",
//                "topSequence": "795959",
//                "maxPid": "795959",
//                "prevTopDid": "794675"
//        }

        URI uri = UriComponentsBuilder
                .fromUriString("https://www.algumon.com")
                .path("/more/" + num)
                .queryParam("homeFeedType", "TYPE_ENDED")
                .queryParam("site", "")
                .queryParam("topSequence", "")
                .queryParam("maxPid", "")
                .queryParam("prevTopDid", "")
                .encode()
                .build()
                .toUri();

        // JSON 데이터 가져오기
        String response = restTemplate.getForObject(uri, String.class);

        // JSON 데이터 출력
        try { // ObjectMapper 객체 생성
            Document doc = Jsoup.parse(response);
            Elements postElements = doc.select(".post-li");

            List<HotdealDTO> hotdealDTOS = new ArrayList<>();

            for (Element postElement : postElements) {
                String id = postElement.attr("data-post-id");
                String title = postElement.select(".item-name").text().trim();

                String originPrice = postElement.select(".product-price").text();

                int price;
                String priceSlct = null;
                String priceStr = null;
                try {
                    if (originPrice.contains("$")) {
                        String replace = originPrice.split("\\.")[0]
                                .replace("원", "")
                                .replace(",", "")
                                .replace("$", "")
                                .replace(".", "")
                                .replace("다양", "");
                        price = StringUtils.isNotEmpty(replace) ? Integer.parseInt(replace) : 0;
                    } else {
                        String replace = originPrice
                                .replace("원", "")
                                .replace(",", "")
                                .replace("$", "")
                                .replace(".", "")
                                .replace("다양", "")
                                .trim();
                        price = StringUtils.isNotEmpty(replace) ? Integer.parseInt(replace) : 0;
                    }
                    priceSlct = originPrice.contains("$") ? "d" : "w";
                    priceStr = originPrice.trim();
                } catch (Exception e) {
                    price = 0;
                    priceSlct = "w";
                    priceStr = "0";
                    log.error("price error -> {}", e);
                }

                String link = "https://www.algumon.com" + postElement.select(".product-link").attr("href").trim();
                String img = postElement.select(".product-img").select("img").attr("src").trim();
                if (img.contains("?")) {
                    img = img.substring(0, img.indexOf("?"));
                }
                String shop = postElement.select(".label.shop").text().trim();
                String site = postElement.select(".label.site:nth-of-type(1)").text().trim();

                HotdealDTO product = new HotdealDTO(
                        null,
                        Long.valueOf(id),
                        title,
                        price,
                        priceSlct,
                        priceStr,
                        link,
                        img,
                        shop,
                        site,
                        "n"
                );
                hotdealDTOS.add(product);
            }

            return hotdealDTOS;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Collections.emptyList();
    }

    private void hotdealAlimSend(List<HotdealDTO> hotdealDTOS) {
        List<HotdealAlimEntity> hotdealAlimEntities = hotdealAlimEntityREP.findBySendYn("n");

        for (HotdealDTO hotdealDTO : hotdealDTOS) {
            String title = hotdealDTO.getTitle();
            String shop = hotdealDTO.getShop();
            String site = hotdealDTO.getSite();

            for (HotdealAlimEntity hotdealAlimEntity : hotdealAlimEntities) {
                String target = hotdealAlimEntity.getTarget();
                Arrays.stream(MemberEnum.values())
                        .filter(v -> StringUtils.equals(v.getTarget(), target))
                        .findFirst()
                        .ifPresentOrElse(
                                (v) -> {
                                    boolean contains = StringUtils.contains(title, hotdealAlimEntity.getKeyword());
                                    boolean contains2 = StringUtils.equals(shop, hotdealAlimEntity.getKeyword());
                                    boolean contains3 = StringUtils.equals(site, hotdealAlimEntity.getKeyword());

                                    if (contains || contains2 || contains3) {
                                        List<HotdealDTO> list = Arrays.asList(hotdealDTO);

//                                        hotdealAlimEntity.updSendYn("y");

                                        hotdealAlimEntityREP.save(hotdealAlimEntity);

//                                        mattermostUtil.sendBotChannel("@" + v.getUserId() + "핫딜 키워드 알림 : " + hotdealAlimEntity.getKeyword());
//                                        mattermostUtil.sendBotChannel(convertHotdealMattermostMessage(list));
//                                        mattermostUtil.send("핫딜 키워드 알림 : " + hotdealAlimEntity.getKeyword(), v.getDirectChannelId());
                                        mattermostUtil.send(convertHotdealMattermostMessage(list, hotdealAlimEntity.getKeyword()), v.getDirectChannelId());
                                    }
                                },
                                () -> {

                                }
                        );
            }
        }
    }

    private String convertHotdealMattermostMessage(List<HotdealDTO> entityList, String keyword) {
        StringBuilder result = new StringBuilder();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String regexEmojis = "[\uD83C-\uDBFF\uDC00-\uDFFF]+";

        String header = "| 키워드 | img | 제목 | 가격 |\n";
        String line = "| :--:|:--:|:-----:|:--: |\n";
//        String header = "| 시각 | 제목 | 시각 | 제목 |\n";
//        String line = "| :-:|:--:|:-:|:--: |\n";
        result.append(header)
                .append(line);


        Queue<HotdealDTO> q = new LinkedList<>(entityList);
        while (!q.isEmpty()) {
            StringBuilder content = new StringBuilder();
            for (int i = 0; i < 1; i++) {
                if (q.isEmpty()) {
                    break;
                }
                HotdealDTO remove = q.remove();

                content.append("| ")
                        .append(keyword)
                        .append(" | ")

                        .append(remove.getImgUrl100X100())
                        .append(" | ")

                        .append("[")
                        .append(remove.getTitle().replaceAll(regexEmojis, "")
                                .replace("[", "")
                                .replace("]", "")
                                .replace("♥", "")
                                .replace("|", ""))
                        .append("]")
                        .append("(")
                        .append(remove.getLink())
                        .append(")")
                        .append(" | ")

                        .append(remove.getPriceStr());
            }
            content.append(" |\n");
            result.append(content);
        }

        return result.toString();
    }
}
