package com.service.batch.service.webhook.api.biz;


import com.service.batch.database.crawling.dto.HotdealDTO;
import com.service.batch.database.crawling.entity.HotdealAlimEntity;
import com.service.batch.database.crawling.entity.HotdealEntity;
import com.service.batch.database.crawling.repository.HotdealAlimEntityREP;
import com.service.batch.database.crawling.repository.HotdealEntityREP;
import com.service.batch.database.crawling.specification.HotdealSpec;
import com.service.batch.service.webhook.api.dto.WebhookDTO;
import com.service.batch.service.webhook.api.vo.MemberEnum;
import com.service.batch.utils.MattermostUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.example.log.annotation.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@RequiredArgsConstructor
@Service
public class HotdealSVCImpl implements HotdealSVC {
    private static final int MAX_HOTDEAL_CALL_COUNT = 5;
    private static final String ALGUMON_URL = "https://www.algumon.com";
    private static final Pattern END_CURSOR_PATTERN = Pattern.compile("endCursor\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern HAS_NEXT_PATTERN = Pattern.compile("hasNext\\s*:\\s*(true|false)");
    private static final Pattern SIGNATURE_PATTERN = Pattern.compile("s\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern UNQUOTED_KEY_PATTERN = Pattern.compile("([\\{,])\\s*([A-Za-z_][A-Za-z0-9_]*)\\s*:");

    private final MattermostUtil mattermostUtil;
    private final HotdealEntityREP hotdealEntityREP;

    private final RestTemplate restTemplate;
    private final HotdealAlimEntityREP hotdealAlimEntityREP;

    @Override
    public void notRun(WebhookDTO webhookDTO) {
        mattermostUtil.sendWebhookChannel("잘못된 입력입니다. 설명을 보시려면 [$c]를 입력해주세요", webhookDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public void hotdealSearch(WebhookDTO webhookDTO) {
        String[] webhookText = WebhookUtils.parseSplitText(webhookDTO.getText());
        int[] pagingInfo = WebhookUtils.getPagingInfo(webhookText);

        if ((webhookText.length != 2 && webhookText.length != 4) || pagingInfo[1] > 10) {
            this.notRun(webhookDTO);
            return;
        }

        try {
            String searchText = webhookText[1];
            int pageNo = pagingInfo[0];
            int pagePerCnt = pagingInfo[1];

            List<HotdealEntity> hotdealEntities = searchHotdeal(searchText, pageNo, pagePerCnt);
            if (!hotdealEntities.isEmpty()) {
                mattermostUtil.sendWebhookChannel(this.convertHotdealMattermostMessage(hotdealEntities), webhookDTO);
            } else {
                mattermostUtil.sendWebhookChannel("검색된 핫딜이 없습니다.", webhookDTO);
            }
        } catch (NumberFormatException e) {
            this.notRun(webhookDTO);
        }
    }

    @Override
    public void hotdealSearchApi(WebhookDTO webhookDTO) {
        String[] args = webhookDTO.getText().split(" ");
        if (!(args.length == 3 || args.length == 2)) {
            this.notRun(webhookDTO);
            return;
        }

        try {
            String searchText = args[1];
            int pageNo = args.length == 3 ? Integer.parseInt(args[2]) : 0;

            List<HotdealDTO> hotdealDTOS = getHotdeal(pageNo, searchText);

            List<HotdealEntity> hotdealEntities = hotdealDTOS.stream()
                    .map(v -> HotdealEntity.builder()
                            .productId(v.getProductId())
                            .title(v.getTitle())
                            .priceStr(v.getPriceStr())
                            .link(v.getLink())
                            .img(v.getImg())
                            .shop(v.getShop())
                            .site(v.getSite())
                            .siteIconUrl(v.getSiteIconUrl())
                            .rankNum(v.getRankNum())
                            .deliveryInfo(v.getDeliveryInfo())
                            .perPriceText(v.getPerPriceText())
                            .originalLikes(v.getOriginalLikes())
                            .originalDisLikes(v.getOriginalDisLikes())
                            .originalComments(v.getOriginalComments())
                            .originalCreatedAt(parseDateTime(v.getCreatedAt()))
                            .originalBoughtAt(parseDateTime(v.getBoughtAt()))
                            .userWant(v.getUserWant())
                            .userBought(v.getUserBought())
                            .wantCount(v.getWantCount())
                            .boughtCount(v.getBoughtCount())
                            .commentCount(v.getCommentCount())
                            .authorNickname(v.getAuthorNickname())
                            .legacyEditUrl(v.getLegacyEditUrl())
                            .ended(v.getEnded())
                            .blockNewComments(v.getBlockNewComments())
                            .exchangeRate(v.getExchangeRate())
                            .isRead(v.getIsRead())
                            .isNewWindowOpen(v.getIsNewWindowOpen())
                            .nowClickCount(v.getNowClickCount())
                            .sendYn(v.getSendYn()).build()
                    ).toList();

            if (!hotdealEntities.isEmpty()) {
                mattermostUtil.sendWebhookChannel(this.convertHotdealMattermostMessage(hotdealEntities), webhookDTO);
            } else {
                mattermostUtil.sendWebhookChannel("검색된 핫딜이 없습니다.", webhookDTO);
            }
        } catch (NumberFormatException e) {
            this.notRun(webhookDTO);
        }
    }

    @Override
    @Transactional
    public void hotdealAlimIns(WebhookDTO webhookDTO) {
        String[] args = webhookDTO.getText().split(" ");
        if (!(args.length == 3)) {
            this.notRun(webhookDTO);
            return;
        }

        try {
            String searchText = args[1];
            String target = args[2];

            if(Arrays.stream(MemberEnum.values()).
                    noneMatch(v->StringUtils.equals(v.getTarget(), target))
            ){
                this.notRun(webhookDTO);
                return;
            }

            if (searchText.isEmpty()){
                this.notRun(webhookDTO);
                return;
            }

            List<HotdealAlimEntity> byTarget = hotdealAlimEntityREP.findByTargetAndSendYn(target, "n");
            if (byTarget.size() >= 10){
                mattermostUtil.sendWebhookChannel("알림 등록은 최대 10건 입니다.", webhookDTO);
                return;
            }

            HotdealAlimEntity keywordAndTargetAndSendYn = hotdealAlimEntityREP.findByKeywordAndTargetAndSendYn(searchText, target, "n");
            if (keywordAndTargetAndSendYn != null){
                mattermostUtil.sendWebhookChannel("같은 알림이 이미 존재합니다.", webhookDTO);
                return;
            }

            HotdealAlimEntity hotdealAlimEntity = HotdealAlimEntity.builder()
                    .keyword(searchText)
                    .target(target)
                    .build();

            HotdealAlimEntity save = hotdealAlimEntityREP.save(hotdealAlimEntity);

            mattermostUtil.sendWebhookChannel("키워드알림 등록 : id : " + " " + save.getId() + " 키워드 : " + save.getKeyword() + " 대상 : " + save.getTarget(), webhookDTO);
        } catch (Exception e) {
            this.notRun(webhookDTO);
        }
    }

    @Override
    @Transactional
    @Log
    public void hotdealAlimDel(WebhookDTO webhookDTO) {
        String[] args = webhookDTO.getText().split(" ");
        if (!(args.length == 2)) {
            this.notRun(webhookDTO);
            return;
        }

        try {
            String searchText = args[1];

            hotdealAlimEntityREP.findById(Long.valueOf(searchText)).stream()
                    .findFirst()
                            .ifPresentOrElse(
                                    (entity) -> {
                                        hotdealAlimEntityREP.delete(entity);
                                        mattermostUtil.sendWebhookChannel("삭제되었습니다.", webhookDTO);
                                    },
                                    ()-> mattermostUtil.sendWebhookChannel("존재하지 않는 알림입니다.", webhookDTO)
                            );
        } catch (Exception e) {
            this.notRun(webhookDTO);
        }
    }

    @Override
    public void hotdealAlimList(WebhookDTO webhookDTO) {
        String[] args = webhookDTO.getText().split(" ");
        if (!(args.length == 2)) {
            this.notRun(webhookDTO);
            return;
        }

        try {
            String searchText = args[1];

            if(Arrays.stream(MemberEnum.values()).
                    noneMatch(v->StringUtils.equals(v.getTarget(), searchText))
            ){
                this.notRun(webhookDTO);
                return;
            }

            List<HotdealAlimEntity> byTarget = hotdealAlimEntityREP.findByTargetAndSendYn(searchText, "n");

            if (!byTarget.isEmpty()) {
                mattermostUtil.sendWebhookChannel(convertHotdealAlimMattermostMessage(byTarget), webhookDTO);
            } else {
                mattermostUtil.sendWebhookChannel("검색된 알림이 없습니다.", webhookDTO);
            }
        } catch (Exception e) {
            this.notRun(webhookDTO);
        }
    }

    public List<HotdealEntity> searchHotdeal(String searchText, int pageNo, int pagePerCnt) {
        Pageable pageable = PageRequest.of(pageNo, pagePerCnt, Sort.Direction.DESC, "id");
        List<String> searchTerms = Arrays.asList(searchText.split(","));
        return hotdealEntityREP.findAll(HotdealSpec.searchWith(searchTerms), pageable).getContent();
    }

    private boolean isValidInput(String[] args) {
        return args.length == 4 && Integer.parseInt(args[3]) <= 10 || args.length == 2;
    }

    private LocalDateTime parseDateTime(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        return LocalDateTime.parse(value);
    }

    private String convertHotdealMattermostMessage(List<HotdealEntity> entityList) {
        StringBuilder result = new StringBuilder();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String regexEmojis = "[\uD83C-\uDBFF\uDC00-\uDFFF]+";

        String header = "| img | 제목 | 가격 | 날짜 |\n";
        String line = "| :--:|:----:|:----:|:----: |\n";
//        String header = "| 시각 | 제목 | 시각 | 제목 |\n";
//        String line = "| :-:|:--:|:-:|:--: |\n";
        result.append(header)
                .append(line);

        Queue<HotdealEntity> q = new LinkedList<>(entityList);
        while (!q.isEmpty()) {
            StringBuilder content = new StringBuilder();
            for (int i = 0; i < 1; i++) {
                if (q.isEmpty()) {
                    break;
                }
                HotdealEntity remove = q.remove();

                String date = "";
                if (remove != null) {
                    LocalDateTime dateTime = remove.getCreateDate() != null ? remove.getCreateDate() : remove.getOriginalCreatedAt();
                    date = dateTime == null ? "" : dateTime.format(dtf);
                }

                String priceStr = remove == null || StringUtils.isBlank(remove.getPriceStr()) ? "0" : remove.getPriceStr();
                String title = remove == null ? "" : remove.getTitle();
                String link = remove == null ? "" : remove.getLink();
                String img = remove == null ? "" : remove.getImgUrl100X100();

                content.append("| ")
                        .append(img)
                        .append(" | ")

                        .append("[")
                        .append(title.replaceAll(regexEmojis, "")
                                .replace("[", "")
                                .replace("]", "")
                                .replace("♥", "")
                                .replace("|", ""))
                        .append("]")
                        .append("(")
                        .append(link)
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

    private List<HotdealDTO> getHotdeal(int num, String keyword) {
        HotdealPage hotdealPage = this.getInitialHotdealPage(keyword);
        if (num <= 0) {
            return hotdealPage.hotdeals();
        }

        int currentPage = 0;
        int callCount = 1;

        while (currentPage < num) {
            if (!hotdealPage.hasNext() || callCount >= MAX_HOTDEAL_CALL_COUNT) {
                return Collections.emptyList();
            }

            hotdealPage = this.getHotdealByCursor(hotdealPage.endCursor(), hotdealPage.h(), hotdealPage.t(), keyword);
            currentPage++;
            callCount++;
        }

        return hotdealPage.hotdeals();
    }

    private HotdealPage getInitialHotdealPage(String keyword) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder
                .fromUriString(ALGUMON_URL)
                .path("/n/deal");

        if (StringUtils.isNotBlank(keyword)) {
            uriBuilder.queryParam("keyword", keyword);
        }

        URI uri = uriBuilder
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
        String priceStr;

        try {
            priceStr = this.buildPriceStr(originPrice, deliveryInfo, perPriceText);
        } catch (Exception e) {
            priceStr = "0";
            log.error("price error -> {}", e.getMessage());
        }

        return new PriceInfo(priceStr);
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

    private record PriceInfo(String priceStr) {
    }

    private String convertHotdealAlimMattermostMessage(List<HotdealAlimEntity> entityList) {
        StringBuilder result = new StringBuilder();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String regexEmojis = "[\uD83C-\uDBFF\uDC00-\uDFFF]+";

        String header = "| id | 키워드 | 대상 |\n";
        String line = "| :--:|:----:|:--: |\n";
//        String header = "| 시각 | 제목 | 시각 | 제목 |\n";
//        String line = "| :-:|:--:|:-:|:--: |\n";
        result.append(header)
                .append(line);


        Queue<HotdealAlimEntity> q = new LinkedList<>(entityList);
        while (!q.isEmpty()) {
            StringBuilder content = new StringBuilder();
            for (int i = 0; i < 1; i++) {
                if (q.isEmpty()) {
                    break;
                }
                HotdealAlimEntity remove = q.remove();

                content.append("| ")
                        .append(remove.getId())
                        .append(" | ")

                        .append(remove.getKeyword())
                        .append(" | ")

                        .append(remove.getTarget());
            }
            content.append(" |\n");
            result.append(content);
        }

        return result.toString();
    }

}
