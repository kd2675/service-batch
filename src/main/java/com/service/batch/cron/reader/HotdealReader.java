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
import org.json.JSONArray;
import org.json.JSONObject;
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
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class HotdealReader {
    private static final int PAGE_SIZE = 100;
    private static final int MAX_HOTDEAL_CALL_COUNT = 5;
    private static final String ALGUMON_URL = "https://www.algumon.com";
    private static final Pattern END_CURSOR_PATTERN = Pattern.compile("endCursor\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern HAS_NEXT_PATTERN = Pattern.compile("hasNext\\s*:\\s*(true|false)");
    private static final Pattern SIGNATURE_PATTERN = Pattern.compile("s\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern UNQUOTED_KEY_PATTERN = Pattern.compile("([\\{,])\\s*([A-Za-z_][A-Za-z0-9_]*)\\s*:");
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
        Map<Long, HotdealDTO> resultMap = new LinkedHashMap<>();

        HotdealEntity hotdealEntity = hotdealEntityREP.findTop1ByOrderByProductIdDesc().stream().findFirst().orElse(null);
        HotdealPage hotdealPage = this.getInitialHotdealPage();

        if (hotdealEntity != null && hotdealPage != null) {
            Long productId = hotdealEntity.getProductId();
            int callCount = 1;

            while (hotdealPage != null) {
                List<HotdealDTO> hotdeal = hotdealPage.hotdeals();

                boolean limit1 = hotdeal.stream()
                        .anyMatch(v -> v.getProductId().compareTo(productId) == 0);

                boolean limit2 = hotdeal.stream()
                        .anyMatch(v -> v.getProductId().compareTo(productId) > 0);

                List<HotdealDTO> collect = hotdeal.stream()
                        .filter(v -> v.getProductId().compareTo(productId) > 0)
                        .toList();

                collect.forEach(v -> resultMap.putIfAbsent(v.getProductId(), v));

                if (limit1 || !limit2 || !hotdealPage.hasNext() || callCount >= MAX_HOTDEAL_CALL_COUNT) {
                    break;
                }

                hotdealPage = this.getHotdealByCursor(hotdealPage.endCursor(), hotdealPage.h(), hotdealPage.t(), null);
                callCount++;
            }
        } else if (hotdealPage != null) {
            hotdealPage.hotdeals().forEach(v -> resultMap.putIfAbsent(v.getProductId(), v));
        }

        List<HotdealDTO> result = new ArrayList<>(resultMap.values());

        try {
            this.hotdealAlimSend(result);
        } catch (Exception e) {
            log.error("hotdealAlimSend error : {}", e);
        }

        return result;
    }

    private HotdealPage getInitialHotdealPage() {
        URI uri = UriComponentsBuilder
                .fromUriString(ALGUMON_URL)
                .path("/n/deal")
                .encode()
                .build()
                .toUri();

        try {
            byte[] responseBytes = restTemplate.getForObject(uri, byte[].class);
            String response = this.decodeUtf8(responseBytes);
            if (StringUtils.isBlank(response)) {
                return HotdealPage.empty();
            }

            List<HotdealDTO> hotdealDTOS = this.parseHotdealFromScript(response);
            if (hotdealDTOS.isEmpty()) {
                Document doc = Jsoup.parse(response);
                hotdealDTOS = this.parseHotdealFromHtml(doc);
            }
            String endCursor = this.findFirstGroup(response, END_CURSOR_PATTERN).orElse("");
            boolean hasNext = this.findFirstGroup(response, HAS_NEXT_PATTERN)
                    .map(Boolean::parseBoolean)
                    .orElse(false);
            SignatureToken signatureToken = this.findFirstGroup(response, SIGNATURE_PATTERN)
                    .map(this::decodeSignature)
                    .orElse(SignatureToken.empty());

            return new HotdealPage(hotdealDTOS, endCursor, hasNext, signatureToken.h(), signatureToken.t());
        } catch (Exception e) {
            log.error("getInitialHotdealPage error", e);
            return HotdealPage.empty();
        }
    }

    private HotdealPage getHotdealByCursor(String cursor, String h, String t, String keyword) {
        if (StringUtils.isAnyBlank(cursor, h, t)) {
            return HotdealPage.empty();
        }

        try {
            UriComponentsBuilder uriBuilder = UriComponentsBuilder
                    .fromUriString(ALGUMON_URL)
                    .path("/n/deal/listv2")
                    .queryParam("cursor", cursor)
                    .queryParam("h", h)
                    .queryParam("t", t);

            if (StringUtils.isNotBlank(keyword)) {
                uriBuilder.queryParam("keyword", keyword);
            }

            URI uri = uriBuilder.encode().build().toUri();
            byte[] responseBytes = restTemplate.getForObject(uri, byte[].class);
            String response = this.decodeUtf8(responseBytes);
            if (StringUtils.isBlank(response)) {
                return HotdealPage.empty();
            }

            JSONObject jsonObject = new JSONObject(response);
            JSONArray contents = jsonObject.optJSONArray("contents");
            List<HotdealDTO> hotdealDTOS = new ArrayList<>();

            if (contents != null) {
                for (int i = 0; i < contents.length(); i++) {
                    JSONObject item = contents.optJSONObject(i);
                    if (item == null) {
                        continue;
                    }
                    hotdealDTOS.add(this.toHotdealDTO(item));
                }
            }

            return new HotdealPage(
                    hotdealDTOS,
                    jsonObject.optString("endCursor", ""),
                    jsonObject.optBoolean("hasNext", false),
                    jsonObject.optString("h", ""),
                    jsonObject.optString("t", "")
            );
        } catch (Exception e) {
            log.error("getHotdealByCursor error : cursor={}, h={}, t={}", cursor, h, t, e);
            return HotdealPage.empty();
        }
    }

    private List<HotdealDTO> parseHotdealFromScript(String response) {
        String contentsLiteral = this.extractDealContentsLiteral(response);
        if (StringUtils.isBlank(contentsLiteral)) {
            return Collections.emptyList();
        }

        try {
            String contentsJson = this.normalizeContentsToJson(contentsLiteral);
            JSONArray contents = new JSONArray(contentsJson);
            List<HotdealDTO> hotdealDTOS = new ArrayList<>();

            for (int i = 0; i < contents.length(); i++) {
                JSONObject item = contents.optJSONObject(i);
                if (item == null) {
                    continue;
                }
                hotdealDTOS.add(this.toHotdealDTO(item));
            }

            return hotdealDTOS;
        } catch (Exception e) {
            log.error("parseHotdealFromScript error", e);
            return Collections.emptyList();
        }
    }

    private String extractDealContentsLiteral(String response) {
        Matcher startMatcher = Pattern.compile("deals\\s*:\\s*\\{\\s*contents\\s*:\\s*\\[").matcher(response);
        if (!startMatcher.find()) {
            return "";
        }

        int arrayStart = response.indexOf('[', startMatcher.start());
        if (arrayStart < 0) {
            return "";
        }

        int depth = 0;
        boolean inString = false;
        boolean escaped = false;

        for (int i = arrayStart; i < response.length(); i++) {
            char current = response.charAt(i);

            if (inString) {
                if (escaped) {
                    escaped = false;
                    continue;
                }
                if (current == '\\') {
                    escaped = true;
                    continue;
                }
                if (current == '"') {
                    inString = false;
                }
                continue;
            }

            if (current == '"') {
                inString = true;
                continue;
            }

            if (current == '[') {
                depth++;
                continue;
            }

            if (current == ']') {
                depth--;
                if (depth == 0) {
                    return response.substring(arrayStart + 1, i);
                }
            }
        }

        return "";
    }

    private String normalizeContentsToJson(String contentsLiteral) {
        String jsonArray = "[" + contentsLiteral + "]";
        jsonArray = jsonArray.replace("void 0", "null");
        return UNQUOTED_KEY_PATTERN.matcher(jsonArray).replaceAll("$1\"$2\":");
    }

    private List<HotdealDTO> parseHotdealFromHtml(Document doc) {
        Elements dealElements = doc.select("div[id^=deal-]");
        List<HotdealDTO> hotdealDTOS = new ArrayList<>();

        for (Element dealElement : dealElements) {
            String id = StringUtils.removeStart(dealElement.id(), "deal-");
            if (!StringUtils.isNumeric(id)) {
                continue;
            }

            Element titleLink = dealElement.selectFirst("h3 a[href]");
            if (titleLink == null) {
                continue;
            }

            String title = titleLink.text().trim();
            String link = titleLink.attr("href").trim();
            if (StringUtils.startsWith(link, "/")) {
                link = ALGUMON_URL + link;
            }

            Element imageElement = dealElement.selectFirst(".avatar a img");
            String img = imageElement == null ? "" : imageElement.attr("src").trim();
            if (img.contains("?")) {
                img = img.substring(0, img.indexOf("?"));
            }

            Element priceElement = dealElement.selectFirst(".deal-price-text");
            String originPrice = priceElement == null ? "" : priceElement.ownText().trim();
            if (StringUtils.isBlank(originPrice) && priceElement != null) {
                originPrice = priceElement.text().trim();
            }
            PriceInfo priceInfo = this.parsePriceInfo(originPrice);

            String shop = Optional.ofNullable(dealElement.selectFirst("a.badge.badge-soft.badge-xs"))
                    .map(v -> v.text().trim())
                    .orElse("");

            String site = Optional.ofNullable(dealElement.selectFirst("span.badge.badge-soft.badge-xs"))
                    .map(v -> v.text().trim())
                    .orElse("");

            hotdealDTOS.add(new HotdealDTO(
                    null,
                    Long.valueOf(id),
                    title,
                    priceInfo.price(),
                    priceInfo.priceSlct(),
                    priceInfo.priceStr(),
                    link,
                    img,
                    shop,
                    site,
                    "n"
            ));
        }

        return hotdealDTOS;
    }

    private HotdealDTO toHotdealDTO(JSONObject item) {
        String originPrice = item.optString("price", "");
        String deliveryInfo = item.optString("deliveryInfo", "");
        String perPriceText = item.optString("perPriceText", "");
        PriceInfo priceInfo = this.parsePriceInfo(originPrice, deliveryInfo, perPriceText);

        String title = item.optString("title", "");
        if (item.optBoolean("ended", false)) {
            title = "[종료] " + title;
        }

        String img = item.optString("thumbnailUrl", "");
        if (img.contains("?")) {
            img = img.substring(0, img.indexOf("?"));
        }

        HotdealDTO hotdealDTO = new HotdealDTO(
                null,
                item.optLong("id"),
                title,
                priceInfo.price(),
                priceInfo.priceSlct(),
                priceInfo.priceStr(),
                item.optString("originalUrl", ""),
                img,
                item.optString("storeName", ""),
                item.optString("siteName", ""),
                "n"
        );

        hotdealDTO.setSiteIconUrl(this.optStringOrNull(item, "siteIconUrl"));
        hotdealDTO.setRankNum(this.optInteger(item, "rankNum"));
        hotdealDTO.setDeliveryInfo(this.optStringOrNull(item, "deliveryInfo"));
        hotdealDTO.setPerPriceText(this.optStringOrNull(item, "perPriceText"));
        hotdealDTO.setOriginalLikes(this.optInteger(item, "originalLikes"));
        hotdealDTO.setOriginalDisLikes(this.optInteger(item, "originalDisLikes"));
        hotdealDTO.setOriginalComments(this.optInteger(item, "originalComments"));
        hotdealDTO.setCreatedAt(this.optStringOrNull(item, "createdAt"));
        hotdealDTO.setBoughtAt(this.optStringOrNull(item, "boughtAt"));
        hotdealDTO.setUserWant(this.optBoolean(item, "userWant"));
        hotdealDTO.setUserBought(this.optBoolean(item, "userBought"));
        hotdealDTO.setWantCount(this.optInteger(item, "wantCount"));
        hotdealDTO.setBoughtCount(this.optInteger(item, "boughtCount"));
        hotdealDTO.setCommentCount(this.optInteger(item, "commentCount"));
        hotdealDTO.setAuthorNickname(this.optStringOrNull(item, "authorNickname"));
        hotdealDTO.setLegacyEditUrl(this.optStringOrNull(item, "legacyEditUrl"));
        hotdealDTO.setEnded(this.optBoolean(item, "ended"));
        hotdealDTO.setBlockNewComments(this.optBoolean(item, "blockNewComments"));
        hotdealDTO.setExchangeRate(this.optStringOrNull(item, "exchangeRate"));
        hotdealDTO.setIsRead(this.optBoolean(item, "isRead"));
        hotdealDTO.setIsNewWindowOpen(this.optBoolean(item, "isNewWindowOpen"));
        hotdealDTO.setNowClickCount(this.optInteger(item, "nowClickCount"));

        return hotdealDTO;
    }

    private Integer optInteger(JSONObject item, String key) {
        return item.isNull(key) ? null : item.optInt(key);
    }

    private Boolean optBoolean(JSONObject item, String key) {
        return item.isNull(key) ? null : item.optBoolean(key);
    }

    private String optStringOrNull(JSONObject item, String key) {
        return item.isNull(key) ? null : item.optString(key, "");
    }

    private PriceInfo parsePriceInfo(String originPrice) {
        return this.parsePriceInfo(originPrice, "", "");
    }

    private PriceInfo parsePriceInfo(String originPrice, String deliveryInfo, String perPriceText) {
        int price;
        String priceSlct;
        String priceStr;

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
            priceStr = this.buildPriceStr(originPrice, deliveryInfo, perPriceText);
        } catch (Exception e) {
            price = 0;
            priceSlct = "w";
            priceStr = "0";
            log.error("price error -> {}", e.getMessage());
        }

        return new PriceInfo(price, priceSlct, priceStr);
    }

    private String buildPriceStr(String originPrice, String deliveryInfo, String perPriceText) {
        StringBuilder priceStr = new StringBuilder();

        if (StringUtils.isNotBlank(originPrice)) {
            priceStr.append(originPrice.trim());
        }

        if (StringUtils.isNotBlank(perPriceText)) {
            if (!priceStr.isEmpty()) {
                priceStr.append(" ");
            }
            priceStr.append(perPriceText.trim());
        }

        if (StringUtils.isNotBlank(deliveryInfo)) {
            if (!priceStr.isEmpty()) {
                priceStr.append(" ");
            }
            priceStr.append("(배송 ").append(deliveryInfo.trim()).append(")");
        }

        return priceStr.isEmpty() ? "0" : priceStr.toString();
    }

    private Optional<String> findFirstGroup(String target, Pattern pattern) {
        Matcher matcher = pattern.matcher(target);
        if (!matcher.find() || matcher.groupCount() < 1) {
            return Optional.empty();
        }
        return Optional.ofNullable(matcher.group(1));
    }

    private SignatureToken decodeSignature(String encoded) {
        try {
            String decoded = new String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8);
            int delimiterIndex = decoded.indexOf('_');
            if (delimiterIndex < 0) {
                return SignatureToken.empty();
            }
            return new SignatureToken(decoded.substring(0, delimiterIndex), decoded.substring(delimiterIndex + 1));
        } catch (Exception e) {
            log.error("decodeSignature error", e);
            return SignatureToken.empty();
        }
    }

    private String decodeUtf8(byte[] responseBytes) {
        if (responseBytes == null || responseBytes.length == 0) {
            return "";
        }
        return new String(responseBytes, StandardCharsets.UTF_8);
    }

    private record SignatureToken(String h, String t) {
        private static SignatureToken empty() {
            return new SignatureToken("", "");
        }
    }

    private record HotdealPage(List<HotdealDTO> hotdeals, String endCursor, boolean hasNext, String h, String t) {
        private static HotdealPage empty() {
            return new HotdealPage(Collections.emptyList(), "", false, "", "");
        }
    }

    private record PriceInfo(int price, String priceSlct, String priceStr) {
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

        String header = "| 키워드 | img | 제목 | 가격 | 날짜 |\n";
        String line = "| :--:|:--:|:-----:|:--:|:----: |\n";
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
                String date = this.formatOriginalCreatedAt(remove.getCreatedAt(), dtf);
                String priceStr = StringUtils.defaultIfBlank(remove.getPriceStr(), "0");

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

                        .append(priceStr)
                        .append(" | ")

                        .append(date);
            }
            content.append(" |\n");
            result.append(content);
        }

        return result.toString();
    }

    private String formatOriginalCreatedAt(String createdAt, DateTimeFormatter dtf) {
        if (StringUtils.isBlank(createdAt)) {
            return "";
        }

        try {
            return LocalDateTime.parse(createdAt).format(dtf);
        } catch (Exception e) {
            return createdAt;
        }
    }
}
