package com.service.batch.service.webhook.api.biz;

import com.service.batch.service.webhook.api.dto.WebhookDTO;

public interface NotRunSVC {
    void notRun(WebhookDTO webhookDTO);
}
