package com.service.batch.utils.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
public class BugsApiArtistsVO {
    @JsonProperty(value = "artist_id")
    private Long artistId;
    @JsonProperty(value = "artist_nm")
    private String artistNm;
    @JsonProperty(value = "upd_dt")
    private Date updDt;
    @JsonProperty(value = "adhoc_attr")
    private Map<String, Object> adhocAttr;
    @JsonProperty(value = "type")
    private Map<String, Object> type;
    @JsonProperty(value = "image")
    private Map<String, Object> image;
    @JsonProperty(value = "expose_yn")
    private Boolean exposeYn;
    @JsonProperty(value = "genres")
    private List<Map<String, Object>> genres;
    @JsonProperty(value = "valid_yn")
    private Boolean validYn;
}

//        "artists":
//        [{
//        "artist_id":80110450,
//        "artist_nm":"장범준",
//        "upd_dt":1566957256000,
//        "adhoc_attr":{"likes_yn":false,"vote_yn":false},
//        "type":{"group_yn":false,"group_cd_nm":"솔로","sex_cd_nm":"남성","category":"NORMAL"},
//        "image":{"path":"/801104/80110450.jpg","color":"343238","ratio":1.73235},
//        "expose_yn":true,
//        "genres":[{"svc_type":20013,"svc_nm":"락/메탈"}],
//        "valid_yn":true}]
//
