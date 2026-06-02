package com.service.batch.service.news.api.vo;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@EqualsAndHashCode(of = {"link"})
public class RssNewsItemVO implements Comparable<RssNewsItemVO> {
    private String source;
    private String category;
    private String title;
    private String description;
    private String link;
    private LocalDateTime pubDate;

    @Override
    public int compareTo(RssNewsItemVO item) {
        return this.pubDate.compareTo(item.getPubDate());
    }
}
