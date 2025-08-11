package com.service.batch.utils.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MattermostChannelVO {
    private List<String> order;
    private Map<String, MattermostPostVO> posts;
    @JsonProperty(value = "next_post_id")
    private String nextPostId;
    @JsonProperty(value = "prev_post_id")
    private String prevPostId;
    @JsonProperty(value = "has_next")
    private String hasNext;
    @JsonProperty(value = "first_inaccessible_post_time")
    private String firstInaccessiblePostTime;
}
