package com.service.batch.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class YoutubeApiUtilImpl implements YoutubeApiUtil {
    private static final String Youtube = "https://www.youtube.com/youtubei/v1/search?prettyPrint=false";
    private static final String BUGS_API_PATH = "/youtubei/v1/search?prettyPrint=false";

    private final RestTemplate restTemplate;

    @Override
    public ResponseEntity conn(String query) {
        Map<String, Object> context = new HashMap<>();
        Map<String, Object> client = new HashMap<>();
        String clientName = "WEB";
        String clientVersion = "2.20240715.01.00";

        client.put("clientName", clientName);
        client.put("clientVersion", clientVersion);
        context.put("client", client);

//        URI uri = UriComponentsBuilder
//                .fromUriString(BUGS_API_URL)
//                .path(BUGS_API_PATH)
//                .encode()
//                .build()
//                .toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Request Body 설정
        JSONObject requestBody = new JSONObject();
        requestBody.put("context", context);
        requestBody.put("query", query);

        // Request Entity 생성
        HttpEntity entity = new HttpEntity(requestBody.toString(), headers);
//        HttpEntity entity = new HttpEntity(headers);

        // API 호출
        ResponseEntity response = restTemplate.exchange(Youtube, HttpMethod.POST, entity, String.class);

        return response;
    }
}
