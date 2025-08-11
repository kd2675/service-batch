package com.service.batch.utils.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ChannelEnum {

    MATTERMOST_CHANNEL_BOB(6L, "bob", "ao4inqs76jf4bqsw3ukyj84tyw"),
    MATTERMOST_CHANNEL_BOT(7L, "bob", "py5k9sbz1f8udfa5fgj9abcjsw"),


    MATTERMOST_CHANNEL_SUB_NEWS(100L, "subNews", "ds83w917zj83ipcyamx3juemfc"),
    MATTERMOST_CHANNEL_COIN(0L, "coin", "49te3so5pirzubzrwis4h39uya"),
    MATTERMOST_CHANNEL_NEWS(1L, "news", "7ah4awp48fd9dfqxs87tia4h6c"),
    MATTERMOST_CHANNEL_NEWS_FLASH(2L, "news", "y5g4ki1ypbbezk97rwuwqnpiga"),
    MATTERMOST_CHANNEL_NEWS_MARKETING(3L, "news", "mnkeeidfgbbexf5zkmpthqhzza"),
    MATTERMOST_CHANNEL_NEWS_STOCK(4L, "news", "q3outnqxs3gidxrwzk1y6z7ika"),
    MATTERMOST_CHANNEL_HOTDEAL(5L, "hotdeal", "cmn3hqjawpdn8cwoscock4bmsw");

    private Long id;
    private String key;
    private String value;
}
