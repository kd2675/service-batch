package com.service.batch.utils;

import com.service.batch.service.webhook.api.dto.WebhookDTO;
import com.service.batch.utils.vo.MattermostChannelVO;
import com.service.batch.utils.vo.MattermostPostVO;
import org.springframework.http.ResponseEntity;

public interface MattermostUtil {
    ResponseEntity<MattermostPostVO> send(String message, String channelId);

    ResponseEntity<MattermostPostVO> sendWebhookChannel(String message, WebhookDTO webhookDTO);

    ResponseEntity<MattermostPostVO> sendBotChannel(String message);

    ResponseEntity<MattermostPostVO> sendSubNewsChannel(String message);
    ResponseEntity<MattermostPostVO> sendCoinChannel(String message);
    ResponseEntity<MattermostPostVO> sendNewsChannel(String message);
    ResponseEntity<MattermostPostVO> sendNewsFlashChannel(String message);
    ResponseEntity<MattermostPostVO> sendNewsMarketingChannel(String message);
    ResponseEntity<MattermostPostVO> sendNewsStockChannel(String message);

    ResponseEntity<MattermostPostVO> sendHotdealChannel(String message);

    ResponseEntity<MattermostChannelVO> selectAllChannel(String channelId);
    ResponseEntity delete(String sentId);
}
