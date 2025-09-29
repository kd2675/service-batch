package com.service.batch.service.webhook.api.act;


import com.service.batch.service.webhook.api.biz.WebhookCMD;
import com.service.batch.service.webhook.api.biz.WebhookSVC;
import com.service.batch.service.webhook.api.dto.WebhookDTO;
import com.service.batch.utils.MattermostUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/service/batch/webhook")
public class WebhookCTR {
    private final WebhookSVC webhookSVC;
    private final WebhookCMD webhookCMD;
    private final MattermostUtil mattermostUtil;

    @PostMapping("test")
    public String test(@RequestBody final WebhookDTO webhookDTO){
        String text = webhookDTO.getText().replace(webhookDTO.getTriggerWord() + " ", "");

        mattermostUtil.sendBotChannel(text);

        return "OK";
    }

    @PostMapping("/")
    public HashMap<String, String> webhook(@RequestBody final WebhookDTO webhookDTO){
        webhookDTO.setWebhookType("a");

        webhookCMD.cmdCall(webhookDTO);

        return new HashMap<>();
    }

    @PostMapping("/cmd")
    public HashMap<String, String> command(@RequestBody final WebhookDTO webhookDTO){
        webhookDTO.setWebhookType("b");

        webhookCMD.cmdCall(webhookDTO);

        return new HashMap<>();
    }
}
