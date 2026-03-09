package com.service.batch.database.crawling.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.database.common.jpa.CommonDateEntity;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HotdealDTO extends CommonDateEntity {
    private Long id;
    private Long productId;
    private String title;
    private String priceStr;
    private String link;
    private String img;
    private String shop;
    private String site;
    private String sendYn;
    private String siteIconUrl;
    private Integer rankNum;
    private String deliveryInfo;
    private String perPriceText;
    private Integer originalLikes;
    private Integer originalDisLikes;
    private Integer originalComments;
    private String createdAt;
    private String boughtAt;
    private Boolean userWant;
    private Boolean userBought;
    private Integer wantCount;
    private Integer boughtCount;
    private Integer commentCount;
    private String authorNickname;
    private String legacyEditUrl;
    private Boolean ended;
    private Boolean blockNewComments;
    private String exchangeRate;
    private Boolean isRead;
    private Boolean isNewWindowOpen;
    private Integer nowClickCount;

    public HotdealDTO(
            Long id,
            Long productId,
            String title,
            String priceStr,
            String link,
            String img,
            String shop,
            String site,
            String sendYn
    ) {
        this.id = id;
        this.productId = productId;
        this.title = title;
        this.priceStr = priceStr;
        this.link = link;
        this.img = img;
        this.shop = shop;
        this.site = site;
        this.sendYn = sendYn;
    }

    public String getImgUrl100X100(){
        return "![](" + this.img + "?d=100x100" + ")";
    }
}
