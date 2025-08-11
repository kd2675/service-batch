package com.service.batch.utils.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class BugsApiVO {
    List<BugsApiListVO> list = new ArrayList<>();
    Map<String, Object> info = new HashMap<>();
    Map<String, Object> pager = new HashMap<>();
    @JsonProperty(value = "ret_code")
    Integer retCode;
    @JsonProperty(value = "ret_msg")
    String retMsg;
}

//{"list":[
//        {"track_no":1,
//        "track_id":31681439,
//        "track_title":"흔들리는 꽃들 속에서 네 샴푸향이 느껴진거야",
//        "upd_dt":1698267671000,
//        "album":
//        {"album_id":20272602,
//        "title":"멜로가 체질 OST Part 3",
//        "upd_dt":1698267671000,"track_count":0,
//        "image":{"path":"/202726/20272602.jpg","color":"252121","ratio":1.0},
//        "release_ymd":"20190823","release_local_ymd":"20190823","valid_yn":true},
//        "artists":[{"artist_id":80110450,"artist_nm":"장범준","upd_dt":1566957256000,"adhoc_attr":{"likes_yn":false,"vote_yn":false},"type":{"group_yn":false,"group_cd_nm":"솔로","sex_cd_nm":"남성","category":"NORMAL"},"image":{"path":"/801104/80110450.jpg","color":"343238","ratio":1.73235},"expose_yn":true,"genres":[{"svc_type":20013,"svc_nm":"락/메탈"}],"valid_yn":true}]
//        ,"adhoc_attr":{"play_count":0,"comment_group_id":232618911,"likes_count":17823,"likes_yn":false,"vote_yn":false},
//        "disc_id":1,"track_gain":-10.31,"bitrates":["aac","320k","aac256","flac"],
//        "rights":{"streaming":{"service_yn":true,"premium_yn":false,"flac_premium_yn":false,"pps_yn":false},"download":{"service_yn":true,"premium_yn":false,"ppd_yn":false},"download_flac":{"service_flac_yn":true,"service_flac24_yn":false},"save":{"service_yn":true,"premium_yn":false}}
//        ,"title_yn":true,"adult_yn":false,"purchase":{"price":700,"track_buy_yn":true,"album_buy_only_yn":false,"pc_limit_cnt":0}
//        ,"flac_sample_rate":{"flac16":"44.1"},"free_yn":false,"len":"02:48","valid_yn":true}]
//
//        ,"info":{"order":0,"total":0,"target":"track","list_identity":{"style":"search_track","id":"0"}}
//        ,"pager":{"page":1,"list":[1,2,3,4,5,6,7,8,9,10],"total_count":472,"page_size":1,"set_size":10,"first_page":1,"last_page":472,"prev_page":1,"next_page":2,"first_yn":true,"last_yn":false,"remain_count":471}
//        ,"ret_code":0,
//        "ret_msg":"성공하였습니다."}