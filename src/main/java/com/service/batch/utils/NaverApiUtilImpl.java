package com.service.batch.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Slf4j
@RequiredArgsConstructor
@Service
public class NaverApiUtilImpl implements NaverApiUtil{
    private static final String NAVER_API_URL = "https://openapi.naver.com";
    private static final String NAVER_API_PATH = "/v1/search/";
    private static final String NAVER_API_CLIENT_ID = "97avHwhY7N2bJ4RysxAx";
    private static final String NAVER_API_CLIENT_SECRET = "74r7XpIXPi";

    private final RestTemplate restTemplate;

    @Override
    public ResponseEntity conn(String path, String query, int display, int start, String sort) {
        //이 가 다 는 을 고 하 에
        //속보 코인 주식(주가)
        URI uri = UriComponentsBuilder
                .fromUriString(NAVER_API_URL)
                .path(NAVER_API_PATH + path + ".json")
                .queryParam("query", query)
                .queryParam("display", display)
                .queryParam("start", start)
                .queryParam("sort", sort)
                .encode()
                .build()
                .toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Naver-Client-Id", NAVER_API_CLIENT_ID);
        headers.set("X-Naver-Client-Secret", NAVER_API_CLIENT_SECRET);

        // Request Body 설정
//        JSONObject requestBody = new JSONObject();
//        requestBody.put("message", message);
//        requestBody.put("username", "systemBot");
//        requestBody.put("channel_id", channelId);

        // Request Entity 생성
//        HttpEntity entity = new HttpEntity(requestBody.toString(), headers);
        HttpEntity entity = new HttpEntity(headers);

        // API 호출
        ResponseEntity response = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);

        return response;
    }
}
