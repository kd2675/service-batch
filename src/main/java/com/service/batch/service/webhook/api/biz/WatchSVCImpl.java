package com.service.batch.service.webhook.api.biz;


import com.service.batch.database.crawling.entity.WatchEntity;
import com.service.batch.database.crawling.repository.WatchREP;
import com.service.batch.service.webhook.api.dto.WebhookDTO;
import com.service.batch.utils.MattermostUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class WatchSVCImpl implements WatchSVC {
    private final MattermostUtil mattermostUtil;

    private final WatchREP watchREP;

    @Override
    public void notRun(WebhookDTO webhookDTO) {
        mattermostUtil.sendWebhookChannel("잘못된 입력입니다. 설명을 보시려면 [$c]를 입력해주세요", webhookDTO);
    }

    @Override
    public void watch(WebhookDTO webhookDTO) {
        watchREP.findWatchRand().ifPresentOrElse(
                (musicEntity) -> {
                    String title = musicEntity.getTitle();

                    String str = title;
                    mattermostUtil.sendWebhookChannel(str, webhookDTO);
                },
                () -> {

                }
        );
    }

    @Override
    public void watchList(WebhookDTO webhookDTO) {
        Integer pageNo = 0;
        Integer pagePerCnt = 100;
        Pageable pageable = PageRequest.of(pageNo, pagePerCnt, Sort.Direction.DESC, "id");

        List<WatchEntity> content = watchREP.findByWatchYnOrderByIdDesc("n", pageable);

        if (!content.isEmpty()) {
            mattermostUtil.sendWebhookChannel(this.convertMattermostStr(content), webhookDTO);
        }
    }

    private String convertMattermostStr(List<WatchEntity> entities) {
        StringBuilder sb = new StringBuilder();
//        String header = "| id | title | singer | pubDate | youtube |\n";
//        String line = "| :-:|:-:|:--:|:-:|:-: |\n";
//        sb.append(header)
//                .append(line);

        entities.forEach(v -> {
            sb.append("|");
            sb.append(v.getId());
            sb.append("|");
            sb.append(v.getTitle());
            sb.append("|");
            sb.append(v.getStar());
            sb.append("|");
            sb.append("\n");
        });

        return sb.toString();
    }

    @Override
    public void watchAdd(WebhookDTO webhookDTO) {
        try {
            String[] split = parseSplitText(webhookDTO.getText());
            if (split.length < 2 || split.length > 3) {
                this.notRun(webhookDTO);
                return;
            }

            String title = split[1];
            int starInfo = Integer.parseInt(split[2].trim());

            if (0 > starInfo || starInfo > 100) {
                mattermostUtil.sendWebhookChannel("평점은 0부터 100까지 입니다.", webhookDTO);
                return;
            }

            WatchEntity watchEntity = WatchEntity.builder()
                    .title(title)
                    .star(starInfo)
                    .build();

            watchREP.save(watchEntity);

            mattermostUtil.sendWebhookChannel("완료", webhookDTO);
        } catch (NumberFormatException e) {
            mattermostUtil.sendWebhookChannel("에러", webhookDTO);
            log.error("watchAdd error", e);
        }
    }

    private String[] parseSplitText(String text) {
        if (text.contains("'") && text.length() == text.replace("'", "").length() + 2) {
            return text.split("'");
        } else if (text.contains("\"") && text.length() == text.replace("\"", "").length() + 2) {
            return text.split("\"");
        } else {
            if (text.split(" ")[1].startsWith("\'") && text.split(" ")[1].endsWith("\"")) {
                throw new RuntimeException("parseSplitText error");
            }
            return text.split(" ");
        }
    }

    @Override
    public void watchY(WebhookDTO webhookDTO) {
        try {
            Long id = Long.valueOf(webhookDTO.getText().split(" ")[1]);

            Optional<WatchEntity> watchEntity = watchREP.findById(id);
            watchEntity.ifPresent(
                    (v->{
                        v.updateWatchYn("y");
                        watchREP.save(v);
                    })
            );

            mattermostUtil.sendWebhookChannel("완료", webhookDTO);
        } catch (Exception e) {
            mattermostUtil.sendWebhookChannel("에러", webhookDTO);
            log.error("watchRemove error", e);
        }
    }

    @Override
    public void watchRemove(WebhookDTO webhookDTO) {
        try {
            Long id = Long.valueOf(webhookDTO.getText().split(" ")[1]);

            watchREP.deleteById(id);

            mattermostUtil.sendWebhookChannel("완료", webhookDTO);
        } catch (Exception e) {
            mattermostUtil.sendWebhookChannel("에러", webhookDTO);
            log.error("watchRemove error", e);
        }
    }
}
