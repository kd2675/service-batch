package com.service.batch.utils;

public class MattermostResetBlockedException extends RuntimeException {
    public MattermostResetBlockedException(String channelId) {
        super("Mattermost channel is resetting: " + channelId);
    }
}
