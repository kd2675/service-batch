package com.service.batch.service.news.api.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NaverNewsApiVO {
    private String lastBuildDate;
    private int total;
    private int start;
    private int display;

    private List<NaverNewsApiItemVO> items;
}
