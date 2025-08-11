package com.service.batch.utils;

import org.springframework.http.ResponseEntity;

public interface YoutubeApiUtil {
    ResponseEntity conn(String query);
}
