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
public class BugsApiUtilImpl implements BugsApiUtil{
    private static final String BUGS_API_URL = "https://m.bugs.co.kr";
    private static final String BUGS_API_PATH = "/api/getSearchList";

    private final RestTemplate restTemplate;

    @Override
    public ResponseEntity conn(String type, String query, int pageNo, int pagePerCnt) {
        //이 가 다 는 을 고 하 에
        //속보 코인 주식(주가)
        URI uri = UriComponentsBuilder
                .fromUriString(BUGS_API_URL)
                .path(BUGS_API_PATH)
                .queryParam("type", type)
                .queryParam("query", query)
                .queryParam("page", pageNo)
                .queryParam("size", pagePerCnt)
                .encode()
                .build()
                .toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

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
