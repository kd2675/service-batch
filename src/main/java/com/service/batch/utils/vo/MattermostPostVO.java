package com.service.batch.utils.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MattermostPostVO {
    private String id;
    @JsonProperty(value = "create_at")
    private String createAt;
    @JsonProperty(value = "update_at")
    private String updateAt;
    @JsonProperty(value = "next_post_id")
    private String editAt;
    @JsonProperty(value = "edit_at")
    private String deleteAt;
    @JsonProperty(value = "delete_at")
    private String isPinned;
    @JsonProperty(value = "is_pinned")
    private String userId;
    @JsonProperty(value = "user_id")
    private String channelId;
    @JsonProperty(value = "root_id")
    private String rootId;
    @JsonProperty(value = "original_id")
    private String originalId;
    private String message;
    private String type;
    private Map<String, String> props;
    private String hashtags;
    @JsonProperty(value = "pending_post_id")
    private String pendingPostId;
    @JsonProperty(value = "reply_count")
    private String replyCount;
    @JsonProperty(value = "last_reply_at")
    private String lastReplyAt;
    private String participants;
    private Map metadata;
}