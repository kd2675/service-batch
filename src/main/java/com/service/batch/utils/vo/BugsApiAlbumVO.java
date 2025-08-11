package com.service.batch.utils.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;
import java.util.Map;

@Data
public class BugsApiAlbumVO {
    @JsonProperty(value = "album_id")
    private Long albumId;
    @JsonProperty(value = "title")
    private String title;
    @JsonProperty(value = "upd_dt")
    private Date updDt;
    @JsonProperty(value = "track_count")
    private Integer trackCount;
    @JsonProperty(value = "image")
    private Map<String, Object> image;
    @JsonProperty(value = "live_image")
    private Object liveImage;
    @JsonProperty(value = "release_ymd")
    private String releaseYmd;
    @JsonProperty(value = "release_local_ymd")
    private String releaseLocalYmd;
    @JsonProperty(value = "valid_yn")
    private Boolean validYn;
}

//"album_id":20272602,
//        "title":"멜로가 체질 OST Part 3",
//        "upd_dt":1698267671000,"track_count":0,
//        "image":{"path":"/202726/20272602.jpg","color":"252121","ratio":1.0},
//        "release_ymd":"20190823","release_local_ymd":"20190823","valid_yn":true}
