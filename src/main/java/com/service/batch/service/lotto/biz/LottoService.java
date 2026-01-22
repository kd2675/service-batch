package com.service.batch.service.lotto.biz;

import com.service.batch.service.webhook.api.dto.WebhookDTO;

public interface LottoService {
    void account();
    void check();
    void buy();

    void checkBob(WebhookDTO webhookDTO);
}
