package com.service.batch.utils.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
public class BugsApiListVO {
    @JsonProperty(value = "track_no")
    private Long trackNo;
    @JsonProperty(value = "track_id")
    private Long trackId;
    @JsonProperty(value = "track_title")
    private String trackTitle;
    @JsonProperty(value = "upd_dt")
    private Date updDt;
    @JsonProperty(value = "album")
    private BugsApiAlbumVO album;
    @JsonProperty(value = "artists")
    private List<BugsApiArtistsVO> artists;
    @JsonProperty(value = "adhoc_attr")
    private Map<String, Object> adhocAttr;
    @JsonProperty(value = "disc_id")
    private Long discId;
    @JsonProperty(value = "track_gain")
    private Double trackGain;
    @JsonProperty(value = "bitrates")
    private List<Object> bitrates;
    @JsonProperty(value = "rights")
    private Map<String, Object> rights;
    @JsonProperty(value = "title_yn")
    private Boolean titleYn;
    @JsonProperty(value = "adult_yn")
    private Boolean adultYn;
    @JsonProperty(value = "purchase")
    private Map<String, Object> purchase;
    @JsonProperty(value = "flac_sample_rate")
    private Map<String, Object> flacSampleRate;
    @JsonProperty(value = "free_yn")
    private Boolean freeYn;
    @JsonProperty(value = "len")
    private String len;
    @JsonProperty(value = "valid_yn")
    private Boolean validYn;

    @JsonProperty(value = "mvs")
    private Object mvs;

    @JsonProperty(value = "connect")
    private Object connect;
}

//{"track_no":1,
//        "track_id":31681439,
//        "track_title":"흔들리는 꽃들 속에서 네 샴푸향이 느껴진거야",
//        "upd_dt":1698267671000,
//        ,"adhoc_attr":{"play_count":0,"comment_group_id":232618911,"likes_count":17823,"likes_yn":false,"vote_yn":false},
//        "disc_id":1,
//        "track_gain":-10.31,
//        "bitrates":["aac","320k","aac256","flac"],
//        "rights":{"streaming":{"service_yn":true,"premium_yn":false,"flac_premium_yn":false,"pps_yn":false},"download":{"service_yn":true,"premium_yn":false,"ppd_yn":false},"download_flac":{"service_flac_yn":true,"service_flac24_yn":false},"save":{"service_yn":true,"premium_yn":false}}
//        ,"title_yn":true,
//        "adult_yn":false,
//        "purchase":{"price":700,"track_buy_yn":true,"album_buy_only_yn":false,"pc_limit_cnt":0}
//        ,"flac_sample_rate":{"flac16":"44.1"},
//        "free_yn":false,
//        "len":"02:48",
//        "valid_yn":true}