package com.service.batch.service.news.api.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(of = {"title"})
public class NaverNewsApiItemVO implements Comparable<NaverNewsApiItemVO> {
    private String title;
    @JsonProperty("originallink")
    private String originalLink;
    private String link;
    private String description;
    private String pubDate;

    private String category;

    public String getTitle() {
        return this.title.replace("<b>", "").replace("</b>", "");
    }

    public String getDescription() {
        return this.description.replace("<b>", "").replace("</b>", "");
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @Override
    public int compareTo(NaverNewsApiItemVO naverNewsApiItemVO) {
        if (LocalDateTime.parse(this.pubDate, DateTimeFormatter.RFC_1123_DATE_TIME)
                .isBefore(LocalDateTime.parse(naverNewsApiItemVO.getPubDate(), DateTimeFormatter.RFC_1123_DATE_TIME))
        ) {
            return -1;
        } else if (LocalDateTime.parse(this.pubDate, DateTimeFormatter.RFC_1123_DATE_TIME)
                .isAfter(LocalDateTime.parse(naverNewsApiItemVO.getPubDate(), DateTimeFormatter.RFC_1123_DATE_TIME))
        ) {
            return 1;
        }

        return 0;
    }
}
