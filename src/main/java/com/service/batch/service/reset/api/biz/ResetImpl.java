package com.service.batch.service.reset.api.biz;


import com.service.batch.database.batch.entity.ResetPointEntity;
import com.service.batch.database.batch.MySqlNamedLock;
import com.service.batch.database.batch.repository.ResetPointREP;
import com.service.batch.database.crawling.repository.MattermostSentREP;
import com.service.batch.utils.MattermostUtil;
import com.service.batch.utils.MattermostResetState;
import com.service.batch.utils.enums.ChannelEnum;
import com.service.batch.utils.vo.MattermostChannelVO;
import com.service.batch.utils.vo.MattermostPostVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class ResetImpl implements Reset {
    private static final String RESET_LOCK = "service-batch:mattermost-reset";
    private static final List<Integer> MATTERMOST_RESET_POINT_IDS = Arrays.asList(1, 2);
    private static final List<String> MATTERMOST_SENT_CATEGORIES = List.of("news", "coin", "hotdeal");

    private final ResetPointREP resetPointREP;
    private final MattermostSentREP mattermostSentREP;

    private final MattermostUtil mattermostUtil;
    private final MattermostResetState mattermostResetState;
    private final MySqlNamedLock mySqlNamedLock;

    @Override
    public void mattermostDelReset() {
        try {
            if (!mySqlNamedLock.runIfAcquired(RESET_LOCK, () -> {
                resetMattermost();
                return null;
            })) {
                log.info("Mattermost reset skipped because another instance is resetting");
            }
        } catch (Exception e) {
            throw new IllegalStateException("Mattermost reset failed", e);
        }
    }

    private void resetMattermost() {
        List<ResetPointEntity> resetPointEntities = resetPointREP.findByResetYnAndPointIdInOrderByCreateDateDesc("n", MATTERMOST_RESET_POINT_IDS);

        if (resetPointEntities.size() >= 3) {
            delChannelPost(ChannelEnum.MATTERMOST_CHANNEL_NEWS.getValue());
            delChannelPost(ChannelEnum.MATTERMOST_CHANNEL_NEWS_FLASH.getValue());
            delChannelPost(ChannelEnum.MATTERMOST_CHANNEL_NEWS_MARKETING.getValue());
            delChannelPost(ChannelEnum.MATTERMOST_CHANNEL_NEWS_STOCK.getValue());
            delChannelPost(ChannelEnum.MATTERMOST_CHANNEL_COIN.getValue());
            delChannelPost(ChannelEnum.MATTERMOST_CHANNEL_HOTDEAL.getValue());

            mattermostSentREP.deleteAllByCategoryIn(MATTERMOST_SENT_CATEGORIES);

            for (ResetPointEntity resetPointEntity : resetPointEntities) {
                resetPointEntity.setResetY();
            }

            resetPointREP.saveAll(resetPointEntities);
        }
    }

    private void delChannelPost(String id) {
        mattermostResetState.begin(id);
        try {
            for (;;) {
                Map<String, MattermostPostVO> posts = getChannelPosts(id);
                if (posts.isEmpty()) {
                    break;
                }


                for (MattermostPostVO vo : posts.values()) {
                    try {
                        mattermostUtil.deleteForReset(vo.getId());
                    } catch (Exception e) {
                    }
                }

                if (posts.values().size() < 100) {
                    break;
                }
            }
        } finally {
            mattermostResetState.end(id);
        }
    }

    private Map<String, MattermostPostVO> getChannelPosts(String channelId) {
        try {
            ResponseEntity<MattermostChannelVO> channel = mattermostUtil.selectAllChannel(channelId);
            if (channel.getBody() == null || channel.getBody().getPosts() == null) {
                return Map.of();
            }
            return channel.getBody().getPosts();
        } catch (Exception e) {
            log.warn("mattermost reset select channel posts skip channelId: {}", channelId, e);
            return Map.of();
        }
    }
}
