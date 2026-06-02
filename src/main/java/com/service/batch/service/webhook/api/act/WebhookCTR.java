package com.service.batch.service.webhook.api.act;


import com.service.batch.service.webhook.api.biz.WebhookCMD;
import com.service.batch.service.webhook.api.biz.WebhookSVC;
import com.service.batch.service.webhook.api.dto.WebhookDTO;
import com.service.batch.utils.MattermostUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/service/batch/webhook")
public class WebhookCTR {
    private final WebhookSVC webhookSVC;
    private final WebhookCMD webhookCMD;
    private final MattermostUtil mattermostUtil;

    @Value("${webhook.mattermost.bot-token:}")
    private String mattermostBotWebhookToken;

    @Value("${webhook.mattermost.bob-token:}")
    private String mattermostBobWebhookToken;

    @PostMapping("test")
    public ResponseEntity<String> test(@RequestBody final WebhookDTO webhookDTO){
        if (!isValidMattermostToken(webhookDTO)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String text = webhookDTO.getText().replace(webhookDTO.getTriggerWord() + " ", "");

        mattermostUtil.sendBotChannel(text);

        return ResponseEntity.ok("OK");
    }

    @PostMapping("/")
    public ResponseEntity<HashMap<String, String>> webhook(@RequestBody final WebhookDTO webhookDTO){
        if (!isValidMattermostToken(webhookDTO)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        webhookDTO.setWebhookType("a");

        webhookCMD.cmdCall(webhookDTO);

        return ResponseEntity.ok(new HashMap<>());
    }

    @PostMapping("/cmd")
    public ResponseEntity<HashMap<String, String>> command(@RequestBody final WebhookDTO webhookDTO){
        if (!isValidMattermostToken(webhookDTO)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        webhookDTO.setWebhookType("b");

        webhookCMD.cmdCall(webhookDTO);

        return ResponseEntity.ok(new HashMap<>());
    }

    private boolean isValidMattermostToken(WebhookDTO webhookDTO) {
        if (webhookDTO.getToken() == null) {
            return false;
        }

        return isSameToken(mattermostBotWebhookToken, webhookDTO.getToken())
                || isSameToken(mattermostBobWebhookToken, webhookDTO.getToken());
    }

    private boolean isSameToken(String configuredToken, String requestToken) {
        return configuredToken != null
                && !configuredToken.isBlank()
                && Objects.equals(configuredToken, requestToken);
    }
}
