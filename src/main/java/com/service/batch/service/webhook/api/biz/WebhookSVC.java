package com.service.batch.service.webhook.api.biz;


import com.service.batch.service.webhook.api.dto.WebhookDTO;

public interface WebhookSVC extends NotRunSVC {
    void help(WebhookDTO webhookDTO);
    void time(WebhookDTO webhookDTO);
    void uptime(WebhookDTO webhookDTO);
    void news(WebhookDTO webhookDTO);
    void oldNews(WebhookDTO webhookDTO);
    void insNewsSubscribe(WebhookDTO webhookDTO);
    void delNewsSubscribe(WebhookDTO webhookDTO);
    void selNewsSubscribe(WebhookDTO webhookDTO);
}