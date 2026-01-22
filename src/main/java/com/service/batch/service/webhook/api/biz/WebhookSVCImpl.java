package com.service.batch.service.webhook.api.biz;


import com.service.batch.database.crawling.entity.NewsEntity;
import com.service.batch.database.crawling.entity.NewsSubscribeEntity;
import com.service.batch.database.crawling.entity.OldNewsEntity;
import com.service.batch.database.crawling.repository.NewsREP;
import com.service.batch.database.crawling.repository.NewsSubscribeEntityREP;
import com.service.batch.database.crawling.repository.OldNewsREP;
import com.service.batch.database.crawling.specification.NewsSpec;
import com.service.batch.database.crawling.specification.OldNewsSpec;
import com.service.batch.service.lotto.biz.LottoService;
import com.service.batch.service.webhook.api.dto.WebhookDTO;
import com.service.batch.service.webhook.api.vo.WebhookEnum;
import com.service.batch.utils.BugsApiUtil;
import com.service.batch.utils.MattermostUtil;
import com.service.batch.utils.YoutubeApiUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class WebhookSVCImpl implements WebhookCMD, WebhookSVC {
    private final MattermostUtil mattermostUtil;
    private final BugsApiUtil bugsApiUtil;
    private final YoutubeApiUtil youtubeApiUtil;

    private final MusicSVC musicSVC;
    private final WatchSVC watchSVC;
    private final HotdealSVC hotdealSVC;

    private final NewsREP newsREP;
    private final OldNewsREP oldNewsREP;
    private final NewsSubscribeEntityREP newsSubscribeEntityREP;
    private final LottoService lottoService;

    @Override
    public void notRun(WebhookDTO webhookDTO){
        mattermostUtil.sendWebhookChannel("잘못된 입력입니다. 설명을 보시려면 [$c]를 입력해주세요", webhookDTO);
    }

    @Transactional
    @Override
    public void cmdCall(WebhookDTO webhookDTO) {
        String cmd = webhookDTO.getText().split(" ")[0];

        Map<String, Runnable> commandMap = new HashMap<>();
        commandMap.put(WebhookEnum.COMMAND.getKey(), () -> this.help(webhookDTO));
        commandMap.put(WebhookEnum.COMMAND_100.getKey(), () -> this.time(webhookDTO));
        commandMap.put(WebhookEnum.COMMAND_101.getKey(), () -> this.uptime(webhookDTO));
        commandMap.put(WebhookEnum.COMMAND_200.getKey(), () -> this.news(webhookDTO));
        commandMap.put(WebhookEnum.COMMAND_201.getKey(), () -> this.oldNews(webhookDTO));
        commandMap.put(WebhookEnum.COMMAND_202.getKey(), () -> this.selNewsSubscribe(webhookDTO));
        commandMap.put(WebhookEnum.COMMAND_203.getKey(), () -> this.insNewsSubscribe(webhookDTO));
        commandMap.put(WebhookEnum.COMMAND_204.getKey(), () -> this.delNewsSubscribe(webhookDTO));
        commandMap.put(WebhookEnum.COMMAND_300.getKey(), () -> musicSVC.music(webhookDTO));
        commandMap.put(WebhookEnum.COMMAND_301.getKey(), () -> musicSVC.musicSearch(webhookDTO));
        commandMap.put(WebhookEnum.COMMAND_302.getKey(), () -> musicSVC.musicPlay(webhookDTO));
        commandMap.put(WebhookEnum.COMMAND_303.getKey(), () -> musicSVC.playlist(webhookDTO));
        commandMap.put(WebhookEnum.COMMAND_304.getKey(), () -> musicSVC.playlistAdd(webhookDTO));
        commandMap.put(WebhookEnum.COMMAND_305.getKey(), () -> musicSVC.playlistRemove(webhookDTO));
        commandMap.put(WebhookEnum.COMMAND_400.getKey(), () -> watchSVC.watch(webhookDTO));
        commandMap.put(WebhookEnum.COMMAND_401.getKey(), () -> watchSVC.watchList(webhookDTO));
        commandMap.put(WebhookEnum.COMMAND_402.getKey(), () -> watchSVC.watchAdd(webhookDTO));
        commandMap.put(WebhookEnum.COMMAND_403.getKey(), () -> watchSVC.watchY(webhookDTO));
        commandMap.put(WebhookEnum.COMMAND_404.getKey(), () -> watchSVC.watchRemove(webhookDTO));
        commandMap.put(WebhookEnum.COMMAND_600.getKey(), () -> hotdealSVC.hotdealSearch(webhookDTO));
        commandMap.put(WebhookEnum.COMMAND_601.getKey(), () -> hotdealSVC.hotdealSearchApi(webhookDTO));
        commandMap.put(WebhookEnum.COMMAND_602.getKey(), () -> hotdealSVC.hotdealAlimIns(webhookDTO));
        commandMap.put(WebhookEnum.COMMAND_603.getKey(), () -> hotdealSVC.hotdealAlimDel(webhookDTO));
        commandMap.put(WebhookEnum.COMMAND_604.getKey(), () -> hotdealSVC.hotdealAlimList(webhookDTO));
        commandMap.put(WebhookEnum.COMMAND_800.getKey(), () -> lottoService.checkBob(webhookDTO));

        for (WebhookEnum webhookEnum : WebhookEnum.values()) {
            commandMap.put(webhookEnum.getShortKey(), commandMap.get(webhookEnum.getKey()));
        }

        commandMap.getOrDefault(cmd, () -> notRun(webhookDTO)).run();
    }

    @Transactional(readOnly = true)
    @Override
    public void news(WebhookDTO webhookDTO) {
        String[] webhookText = WebhookUtils.parseSplitText(webhookDTO.getText());
        int[] pagingInfo = WebhookUtils.getPagingInfo(webhookText);

        if ((webhookText.length != 2 && webhookText.length != 4) || pagingInfo[1] > 10) {
            this.notRun(webhookDTO);
            return;
        }

        String searchText = webhookText[1];
        int pageNo = pagingInfo[0];
        int pagePerCnt = pagingInfo[1];

        List<NewsEntity> newsEntities = searchNews(searchText, pageNo, pagePerCnt);
        if (!newsEntities.isEmpty()) {
            mattermostUtil.sendWebhookChannel(convertNewsMattermostMessage(newsEntities), webhookDTO);
        } else {
            mattermostUtil.sendWebhookChannel("검색된 기사가 없습니다.", webhookDTO);
        }
    }

    private boolean isValidInput(String[] args) {
        return args.length == 4 && Integer.parseInt(args[3]) <= 10;
    }

    private List<NewsEntity> searchNews(String searchText, int pageNo, int pagePerCnt) {
        Pageable pageable = PageRequest.of(pageNo, pagePerCnt, Sort.Direction.DESC, "id");
        List<String> searchTerms = Arrays.asList(searchText.split(","));
        return newsREP.findAll(NewsSpec.searchWith(searchTerms), pageable).getContent();
    }

    @Transactional(readOnly = true)
    @Override
    public void oldNews(WebhookDTO webhookDTO) {
        String[] webhookText = WebhookUtils.parseSplitText(webhookDTO.getText());
        int[] pagingInfo = WebhookUtils.getPagingInfo(webhookText);

        if ((webhookText.length != 2 && webhookText.length != 4) || pagingInfo[1] > 10) {
            this.notRun(webhookDTO);
            return;
        }

        String searchText = webhookText[1];
        int pageNo = pagingInfo[0];
        int pagePerCnt = pagingInfo[1];

        List<OldNewsEntity> oldNewsEntities = searchOldNews(searchText, pageNo, pagePerCnt);
        if (!oldNewsEntities.isEmpty()) {
            mattermostUtil.sendWebhookChannel(convertOldNewsMattermostMessage(oldNewsEntities), webhookDTO);
        } else {
            mattermostUtil.sendWebhookChannel("검색된 기사가 없습니다.", webhookDTO);
        }
    }

    private List<OldNewsEntity> searchOldNews(String searchText, int pageNo, int pagePerCnt) {
        Pageable pageable = PageRequest.of(pageNo, pagePerCnt, Sort.Direction.DESC, "id");
        List<String> searchTerms = Arrays.asList(searchText.split(","));
        return oldNewsREP.findAll(OldNewsSpec.searchWith(searchTerms), pageable).getContent();
    }

    private String convertNewsMattermostMessage(List<NewsEntity> entityList) {
        StringBuilder result = new StringBuilder();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String regexEmojis = "[\uD83C-\uDBFF\uDC00-\uDFFF]+";

        String header = "| 시각 | 제목 |\n";
        String line = "| :-:|:--: |\n";
//        String header = "| 시각 | 제목 | 시각 | 제목 |\n";
//        String line = "| :-:|:--:|:-:|:--: |\n";
        result.append(header)
                .append(line);


        Queue<NewsEntity> q = new LinkedList<>(entityList);
        while (!q.isEmpty()) {
            String content = "";
            for (int i = 0; i < 1; i++) {
                if (q.isEmpty()) {
                    break;
                }
                NewsEntity remove = q.remove();

                content += "| " + dtf.format(remove.getPubDate())
                        + " | " + "[" + remove.getTitle().replaceAll(regexEmojis, "")
                        .replace("[", "")
                        .replace("]", "")
                        .replace("♥", "")
                        .replace("|", "") + "]" + "(" + remove.getLink() + ")";
            }
            content += " |\n";
            result.append(content);
        }

        return result.toString();
    }

    private String convertOldNewsMattermostMessage(List<OldNewsEntity> entityList) {
        StringBuilder result = new StringBuilder();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String regexEmojis = "[\uD83C-\uDBFF\uDC00-\uDFFF]+";

        String header = "| 시각 | 제목 |\n";
        String line = "| :-:|:--: |\n";
//        String header = "| 시각 | 제목 | 시각 | 제목 |\n";
//        String line = "| :-:|:--:|:-:|:--: |\n";
        result.append(header)
                .append(line);


        Queue<OldNewsEntity> q = new LinkedList<>(entityList);
        while (!q.isEmpty()) {
            String content = "";
            for (int i = 0; i < 1; i++) {
                if (q.isEmpty()) {
                    break;
                }
                OldNewsEntity remove = q.remove();

                content += "| " + dtf.format(remove.getPubDate())
                        + " | " + "[" + remove.getTitle().replaceAll(regexEmojis, "")
                        .replace("[", "")
                        .replace("]", "")
                        .replace("♥", "")
                        .replace("|", "") + "]" + "(" + remove.getLink() + ")";
            }
            content += " |\n";
            result.append(content);
        }

        return result.toString();
    }

    @Override
    public void help(WebhookDTO webhookDTO) {
        StringBuilder str = new StringBuilder();

        for (WebhookEnum webhook : WebhookEnum.values()) {
            str.append(webhook.getId())
                    .append(". ")
                    .append(webhook.getKey())
                    .append(", ")
                    .append(webhook.getShortKey())
                    .append(" : ")
                    .append(webhook.getValue())
                    .append("\n");
        }
        mattermostUtil.sendWebhookChannel(str.toString(), webhookDTO);
    }

    @Override
    public void time(WebhookDTO webhookDTO) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime target =
                LocalDateTime.of(
                        now.getYear(),
                        now.getMonth(),
                        now.getDayOfMonth(),
                        18,
                        0,
                        0
                );

        if (now.isBefore(target)) {
            Duration between = Duration.between(now, target);
            long seconds = between.getSeconds();
            long minute = seconds / 60;
            seconds = seconds % 60;
            long hour = minute / 60;
            minute = minute % 60;

            LocalDateTime localDateTime =
                    LocalDateTime.of(
                            now.getYear(),
                            now.getMonth(),
                            now.getDayOfMonth(),
                            Long.valueOf(hour).intValue(),
                            Long.valueOf(minute).intValue(),
                            Long.valueOf(seconds).intValue()
                    );

            String fullTime =
                    localDateTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"));

            String partTime =
                    localDateTime.minusHours(1)
                            .minusMinutes(30)
                            .format(DateTimeFormatter.ofPattern("HH:mm:ss"));

            StringBuilder str = new StringBuilder();
            str.append(fullTime)
                    .append(" 남았습니다.");

            mattermostUtil.sendWebhookChannel(str.toString(), webhookDTO);
        } else {
            mattermostUtil.sendWebhookChannel("퇴근하세요", webhookDTO);
        }
    }

    @Override
    public void uptime(WebhookDTO webhookDTO) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime target =
                LocalDateTime.of(
                        now.getYear(),
                        now.getMonth(),
                        now.getDayOfMonth(),
                        8,
                        30,
                        0
                );

        if (now.isAfter(target)) {
            Duration between = Duration.between(target, now);
            long seconds = between.getSeconds();
            long minute = seconds / 60;
            seconds = seconds % 60;
            long hour = minute / 60;
            minute = minute % 60;

            String format =
                    LocalDateTime.of(
                            now.getYear(),
                            now.getMonth(),
                            now.getDayOfMonth(),
                            Long.valueOf(hour).intValue(),
                            Long.valueOf(minute).intValue(),
                            Long.valueOf(seconds).intValue()
                    ).format(DateTimeFormatter.ofPattern("HH:mm:ss"));

            mattermostUtil.sendWebhookChannel(format + " 지났습니다.", webhookDTO);
        } else {
            mattermostUtil.sendWebhookChannel("출근하세요", webhookDTO);
        }
    }

    @Override
    @Transactional
    public void selNewsSubscribe(WebhookDTO webhookDTO) {
        for (NewsSubscribeEntity entity : newsSubscribeEntityREP.findAll()) {

        }
    }

    @Override
    @Transactional
    public void insNewsSubscribe(WebhookDTO webhookDTO) {
        String[] webhookText = WebhookUtils.parseSplitTextTwoWord(webhookDTO.getText());

        if (webhookText.length != 2) {
            this.notRun(webhookDTO);
            return;
        }

        String searchText = webhookText[1];

        NewsSubscribeEntity newsSubscribeEntity = NewsSubscribeEntity.builder()
                .keyword(searchText)
                .build();

        newsSubscribeEntityREP.save(newsSubscribeEntity);
    }

    @Override
    @Transactional
    public void delNewsSubscribe(WebhookDTO webhookDTO) {
        String[] webhookText = WebhookUtils.parseSplitTextTwoWord(webhookDTO.getText());

        if (webhookText.length != 2) {
            this.notRun(webhookDTO);
            return;
        }

        String searchText = webhookText[1];

        NewsSubscribeEntity newsSubscribeEntity = NewsSubscribeEntity.builder()
                .keyword(searchText)
                .build();

        newsSubscribeEntityREP.save(newsSubscribeEntity);
    }

    private String convertMattermostMessage(List<NewsSubscribeEntity> entityList) {
        StringBuilder result = new StringBuilder();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String regexEmojis = "[\uD83C-\uDBFF\uDC00-\uDFFF]+";

        String header = "| id | 키워드 |\n";
        String line = "| :-:|:--: |\n";
//        String header = "| 시각 | 제목 | 시각 | 제목 |\n";
//        String line = "| :-:|:--:|:-:|:--: |\n";
        result.append(header)
                .append(line);


        Queue<NewsSubscribeEntity> q = new LinkedList<>(entityList);
        while (!q.isEmpty()) {
            String content = "";
            for (int i = 0; i < 1; i++) {
                if (q.isEmpty()) {
                    break;
                }
                NewsSubscribeEntity remove = q.remove();

                content += "| " + remove.getId()
                        + " | " + remove.getKeyword();
            }
            content += " |\n";
            result.append(content);
        }

        return result.toString();
    }
}
