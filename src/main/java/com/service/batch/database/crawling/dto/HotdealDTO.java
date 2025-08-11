package com.service.batch.database.crawling.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.example.database.common.jpa.CommonDateEntity;

@Data
@AllArgsConstructor
public class HotdealDTO extends CommonDateEntity {
    private Long id;
    private Long productId;
    private String title;
    private int price;
    private String priceSlct;
    private String priceStr;
    private String link;
    private String img;
    private String shop;
    private String site;
    private String sendYn;

    public String getImgUrl100X100(){
        return "![](" + this.img + "?d=100x100" + ")";
    }
}
