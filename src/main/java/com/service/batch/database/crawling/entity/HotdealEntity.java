package com.service.batch.database.crawling.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.database.common.jpa.CommonDateEntity;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@Table(name = "HOT_DEAL")
@Entity
public class HotdealEntity extends CommonDateEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "price", nullable = false)
    private int price;

    @Column(name = "price_slct", nullable = false, length = 1)
    private String priceSlct;

    @Column(name = "price_str", nullable = false, length = 255)
    private String priceStr;

    @Column(name = "link", nullable = false, columnDefinition = "TEXT")
    private String link;

    @Column(name = "img", nullable = false, columnDefinition = "TEXT")
    private String img;

    @Column(name = "shop", nullable = false, length = 255)
    private String shop;

    @Column(name = "site", nullable = false, length = 255)
    private String site;

    @ColumnDefault("'n'")
    @Column(name = "send_yn", nullable = false, length = 1)
    private String sendYn;

    public void updSendYn(String sendYn) {
        this.sendYn = sendYn;
    }

    public String getImgUrl(){
        return "![](" + this.img + ")";
    }

    public String getImgUrl100X100(){
        return "![](" + this.img + "?d=100x100" + ")";
    }
}
