package com.service.batch.utils;

import org.springframework.http.ResponseEntity;

public interface NaverApiUtil {
    ResponseEntity conn(String path, String query, int display, int start, String sort);
}
