package com.service.batch.service.reset.api.biz;


import com.service.batch.database.batch.entity.ResetPointEntity;
import com.service.batch.database.batch.repository.ResetPointREP;
import com.service.batch.database.crawling.entity.MattermostSentEntity;
import com.service.batch.database.crawling.repository.MattermostSentREP;
import com.service.batch.utils.MattermostUtil;
import com.service.batch.utils.enums.ChannelEnum;
import com.service.batch.utils.vo.MattermostChannelVO;
import com.service.batch.utils.vo.MattermostPostVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@RequiredArgsConstructor
@Service
public class ResetImpl implements Reset {
    private final ResetPointREP resetPointREP;
    private final MattermostSentREP mattermostSentREP;

    private final MattermostUtil mattermostUtil;

    @Transactional
    @Override
    public void mattermostDelReset() {
        List<ResetPointEntity> resetPointEntities = resetPointREP.findByResetYnAndPointIdInOrderByCreateDateDesc("n", Collections.singletonList(1));

        if (resetPointEntities.size() >= 3) {
            delChannelPost(ChannelEnum.MATTERMOST_CHANNEL_NEWS.getValue());
            delChannelPost(ChannelEnum.MATTERMOST_CHANNEL_NEWS_FLASH.getValue());
            delChannelPost(ChannelEnum.MATTERMOST_CHANNEL_NEWS_MARKETING.getValue());
            delChannelPost(ChannelEnum.MATTERMOST_CHANNEL_NEWS_STOCK.getValue());
            delChannelPost(ChannelEnum.MATTERMOST_CHANNEL_COIN.getValue());
            delChannelPost(ChannelEnum.MATTERMOST_CHANNEL_HOTDEAL.getValue());

            List<MattermostSentEntity> allByCategoryNews = mattermostSentREP.findAllByCategory("news");
            List<MattermostSentEntity> allByCategoryCoin = mattermostSentREP.findAllByCategory("coin");
            List<MattermostSentEntity> allByCategoryHotdeal = mattermostSentREP.findAllByCategory("hotdeal");
            List<MattermostSentEntity> allByCategory = Stream.concat(Stream.concat(allByCategoryNews.stream(), allByCategoryCoin.stream()), allByCategoryHotdeal.stream()).collect(Collectors.toList());

            mattermostSentREP.deleteAll(allByCategory);

            for (ResetPointEntity resetPointEntity : resetPointEntities) {
                resetPointEntity.setResetY();
            }

            resetPointREP.saveAll(resetPointEntities);
        }
    }

    private void delChannelPost(String id) {
        for (;;) {
            ResponseEntity<MattermostChannelVO> channel = mattermostUtil.selectAllChannel(id);
            Map<String, MattermostPostVO> posts = channel.getBody().getPosts();

            System.out.println(channel.getBody().getNextPostId());
            System.out.println(posts.values().size());
            System.out.println(channel.getBody().getHasNext());

            int idx = 0;
            for (MattermostPostVO vo : posts.values()) {
                try {
                    mattermostUtil.delete(vo.getId());
                }catch (Exception e){}
            }

            if (posts.values().size() < 100) {
                break;
            }
        }
    }
}
