package com.service.batch.service.webhook.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class WebhookDTO {
    @JsonProperty("channel_id")
    private String channelId;
    @JsonProperty("channel_name")
    private String channelName;
    @JsonProperty("team_domain")
    private String teamDomain;
    @JsonProperty("team_id")
    private String teamId;
    @JsonProperty("post_id")
    private String postId;
    private String text;
    private String timestamp;
    private String token;
    @JsonProperty("trigger_word")
    private String triggerWord;
    @JsonProperty("user_id")
    private String userId;
    @JsonProperty("user_name")
    private String userName;

    private String webhookType;
}
