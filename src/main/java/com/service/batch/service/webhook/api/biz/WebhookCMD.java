package com.service.batch.service.webhook.api.biz;


import com.service.batch.service.webhook.api.dto.WebhookDTO;

public interface WebhookCMD {
    void cmdCall(final WebhookDTO webhookDTO);
}
