package com.service.batch.service.webhook.api.biz;

import com.service.batch.service.webhook.api.dto.WebhookDTO;

public interface WatchSVC extends NotRunSVC {
    void watch(WebhookDTO webhookDTO);

    void watchList(WebhookDTO webhookDTO);

    void watchAdd(WebhookDTO webhookDTO);

    void watchY(WebhookDTO webhookDTO);

    void watchRemove(WebhookDTO webhookDTO);
}
