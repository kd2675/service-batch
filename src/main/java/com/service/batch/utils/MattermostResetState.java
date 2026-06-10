package com.service.batch.utils;

import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MattermostResetState {
    private final Set<String> resettingChannelIds = ConcurrentHashMap.newKeySet();

    public void begin(String channelId) {
        resettingChannelIds.add(channelId);
    }

    public void end(String channelId) {
        resettingChannelIds.remove(channelId);
    }

    public boolean isResetting(String channelId) {
        return resettingChannelIds.contains(channelId);
    }
}
