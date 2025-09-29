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
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class HotdealSVCImpl implements HotdealSVC {
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
                            .price(v.getPrice())
                            .priceSlct(v.getPriceSlct())
                            .priceStr(v.getPriceStr())
                            .link(v.getLink())
                            .img(v.getImg())
                            .shop(v.getShop())
                            .site(v.getSite())
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

    private String convertHotdealMattermostMessage(List<HotdealEntity> entityList) {
        StringBuilder result = new StringBuilder();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String regexEmojis = "[\uD83C-\uDBFF\uDC00-\uDFFF]+";

        String header = "| img | 제목 | 가격 | 날짜 |\n";
        String line = "| :--:|:----:|:--: |\n";
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

                String date;
                if (remove == null || remove.getCreateDate() == null) {
                    date = "";
                } else {
                    date = remove.getCreateDate().format(dtf);
                }

                content.append("| ")
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

                        .append(date)
                        .append(" | ")

                        .append(remove.getPriceStr());
            }
            content.append(" |\n");
            result.append(content);
        }

        return result.toString();
    }

    private List<HotdealDTO> getHotdeal(int num, String keyword) {
        URI uri = UriComponentsBuilder
                .fromUriString("https://www.algumon.com")
                .path("/more/" + num)
                .queryParam("homeFeedType", "TYPE_ENDED")
                .queryParam("site", "")
                .queryParam("topSequence", "")
                .queryParam("maxPid", "")
                .queryParam("prevTopDid", "")
                .queryParam("keyword", keyword)
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
