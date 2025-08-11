package com.service.batch.service.coin.api.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BitHumbResultVO {
    private String status;
    private Map<String, Object> data = new HashMap<>();
}
