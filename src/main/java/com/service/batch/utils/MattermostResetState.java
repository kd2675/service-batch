package com.service.batch.utils;

import com.service.batch.database.batch.repository.ResetPointREP;
import com.service.batch.utils.enums.ChannelEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class MattermostResetState {
    private static final int RESET_TRIGGER_COUNT = 3;
    private static final List<Integer> RESET_POINT_IDS = List.of(1, 2);
    private static final Set<String> RESET_MANAGED_CHANNEL_IDS = Set.of(
            ChannelEnum.MATTERMOST_CHANNEL_NEWS.getValue(),
            ChannelEnum.MATTERMOST_CHANNEL_NEWS_FLASH.getValue(),
            ChannelEnum.MATTERMOST_CHANNEL_NEWS_MARKETING.getValue(),
            ChannelEnum.MATTERMOST_CHANNEL_NEWS_STOCK.getValue(),
            ChannelEnum.MATTERMOST_CHANNEL_COIN.getValue(),
            ChannelEnum.MATTERMOST_CHANNEL_HOTDEAL.getValue()
    );

    private final Set<String> resettingChannelIds = ConcurrentHashMap.newKeySet();
    private final ResetPointREP resetPointREP;

    public void begin(String channelId) {
        resettingChannelIds.add(channelId);
    }

    public void end(String channelId) {
        resettingChannelIds.remove(channelId);
    }

    public boolean isResetting(String channelId) {
        return resettingChannelIds.contains(channelId)
                || RESET_MANAGED_CHANNEL_IDS.contains(channelId)
                && resetPointREP.countByResetYnAndPointIdIn("n", RESET_POINT_IDS) >= RESET_TRIGGER_COUNT;
    }
}
