package com.service.batch.utils;


import com.service.batch.database.batch.entity.ResetPointEntity;
import com.service.batch.database.batch.repository.ResetPointREP;
import com.service.batch.service.webhook.api.dto.WebhookDTO;
import com.service.batch.utils.enums.ChannelEnum;
import com.service.batch.utils.vo.MattermostChannelVO;
import com.service.batch.utils.vo.MattermostPostVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;


@Slf4j
@RequiredArgsConstructor
@Service
public class MattermostUtilImpl implements MattermostUtil {
    private static final String MATTERMOST_SYSTEM_BOT_TOKEN = "dxzfhkdinpgn8pqxydwaro13fo";

    private final RestTemplate restTemplate;
    private final ResetPointREP resetPointREP;

    @Override
    public ResponseEntity<MattermostPostVO> send(String message, String channelId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(MATTERMOST_SYSTEM_BOT_TOKEN);

        // Request Body 설정
        JSONObject requestBody = new JSONObject();
        requestBody.put("message", message);
        requestBody.put("username", "system-bot");
        requestBody.put("channel_id", channelId);

        // Request Entity 생성
        HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);

        // API 호출
        String url = "http://kimd0.iptime.org:8066/api/v4/posts";

        try {
            ResponseEntity<MattermostPostVO> response = restTemplate.exchange(url, HttpMethod.POST, entity, MattermostPostVO.class);
            return response;
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("mattermost send error -> {}", e.toString());

            ResetPointEntity resetPointEntity = ResetPointEntity.builder()
                    .pointId(2)
                    .pointExplain("mattermost send error")
                    .build();
            resetPointREP.save(resetPointEntity);

            throw e;
        }
    }

    @Override
    public ResponseEntity<MattermostPostVO> sendWebhookChannel(String message, WebhookDTO webhookVO) {
        String channelIdA = ChannelEnum.MATTERMOST_CHANNEL_BOB.getValue();
        String channelIdB = ChannelEnum.MATTERMOST_CHANNEL_BOT.getValue();
        if ("a".equals(webhookVO.getWebhookType())) {
            return send(message, channelIdA);
        } else if ("b".equals(webhookVO.getWebhookType())) {
            return send(message, channelIdB);
        }
        return null;
    }

    @Override
    public ResponseEntity<MattermostPostVO> sendBotChannel(String message) {
        String channelId = ChannelEnum.MATTERMOST_CHANNEL_BOT.getValue();
        return send(message, channelId);
    }

    @Override
    public ResponseEntity<MattermostPostVO> sendCoinChannel(String message) {
        String channelId = ChannelEnum.MATTERMOST_CHANNEL_COIN.getValue();
        return send(message, channelId);
    }

    @Override
    public ResponseEntity<MattermostPostVO> sendSubNewsChannel(String message) {
        String channelId = ChannelEnum.MATTERMOST_CHANNEL_SUB_NEWS.getValue();
        return send(message, channelId);
    }

    @Override
    public ResponseEntity<MattermostPostVO> sendNewsChannel(String message) {
        String channelId = ChannelEnum.MATTERMOST_CHANNEL_NEWS.getValue();
        return send(message, channelId);
    }

    @Override
    public ResponseEntity<MattermostPostVO> sendNewsFlashChannel(String message) {
        String channelId = ChannelEnum.MATTERMOST_CHANNEL_NEWS_FLASH.getValue();
        return send(message, channelId);
    }

    @Override
    public ResponseEntity<MattermostPostVO> sendNewsMarketingChannel(String message) {
        String channelId = ChannelEnum.MATTERMOST_CHANNEL_NEWS_MARKETING.getValue();
        return send(message, channelId);
    }

    @Override
    public ResponseEntity<MattermostPostVO> sendNewsStockChannel(String message) {
        String channelId = ChannelEnum.MATTERMOST_CHANNEL_NEWS_STOCK.getValue();
        return send(message, channelId);
    }

    @Override
    public ResponseEntity<MattermostPostVO> sendHotdealChannel(String message) {
        String channelId = ChannelEnum.MATTERMOST_CHANNEL_HOTDEAL.getValue();
        return send(message, channelId);
    }

    @Override
    public ResponseEntity<MattermostChannelVO> selectAllChannel(String channelId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(MATTERMOST_SYSTEM_BOT_TOKEN);

        // Request Body 설정
        JSONObject requestBody = new JSONObject();

        // Request Entity 생성
        HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);

        // API 호출
        String url = "http://kimd0.iptime.org:8066/api/v4/channels/" + channelId + "/posts?page=0&per_page=100";

        try {
            ResponseEntity<MattermostChannelVO> response = restTemplate.exchange(url, HttpMethod.GET, entity, MattermostChannelVO.class);
            return response;
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("mattermost selectAllChannel error -> {}", e.toString());
            throw e;
        }
    }

    @Override
    public ResponseEntity delete(String sentId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(MATTERMOST_SYSTEM_BOT_TOKEN);

        // Request Body 설정
        JSONObject requestBody = new JSONObject();

        // Request Entity 생성
        HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);

        // API 호출
        String url = "http://kimd0.iptime.org:8066/api/v4/posts/" + sentId;

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, entity, String.class);
            return response;
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("mattermost delete error -> {}", e.toString());

            ResetPointEntity resetPointEntity = ResetPointEntity.builder()
                    .pointId(1)
                    .pointExplain("mattermost delete error")
                    .build();
            resetPointREP.save(resetPointEntity);

            throw e;
        }
    }
}
