package com.service.batch.service.webhook.api.biz;

import com.service.batch.service.webhook.api.dto.WebhookDTO;

public interface HotdealSVC extends NotRunSVC{
    void hotdealSearch(WebhookDTO webhookDTO);
    void hotdealSearchApi(WebhookDTO webhookDTO);
    void hotdealAlimIns(WebhookDTO webhookDTO);
    void hotdealAlimDel(WebhookDTO webhookDTO);
    void hotdealAlimList(WebhookDTO webhookDTO);
}
