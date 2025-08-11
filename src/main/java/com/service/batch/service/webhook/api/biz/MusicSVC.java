package com.service.batch.service.webhook.api.biz;


import com.service.batch.service.webhook.api.dto.WebhookDTO;

public interface MusicSVC extends NotRunSVC {
    void insMusic();
    void music(WebhookDTO webhookDTO);
    void musicSearch(WebhookDTO webhookDTO);
    void musicPlay(WebhookDTO webhookDTO);
    void playlist(WebhookDTO webhookDTO);
    void playlistAdd(WebhookDTO webhookDTO);
    void playlistRemove(WebhookDTO webhookDTO);
}
