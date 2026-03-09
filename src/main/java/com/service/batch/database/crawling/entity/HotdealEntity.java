package com.service.batch.database.crawling.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.database.common.jpa.CommonDateEntity;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;

import java.time.LocalDateTime;

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

    @Column(name = "site_icon_url", columnDefinition = "TEXT")
    private String siteIconUrl;

    @Column(name = "rank_num")
    private Integer rankNum;

    @Column(name = "delivery_info", length = 255)
    private String deliveryInfo;

    @Column(name = "per_price_text", length = 255)
    private String perPriceText;

    @Column(name = "original_likes")
    private Integer originalLikes;

    @Column(name = "original_dis_likes")
    private Integer originalDisLikes;

    @Column(name = "original_comments")
    private Integer originalComments;

    @Column(name = "original_created_at")
    private LocalDateTime originalCreatedAt;

    @Column(name = "original_bought_at")
    private LocalDateTime originalBoughtAt;

    @Column(name = "user_want")
    private Boolean userWant;

    @Column(name = "user_bought")
    private Boolean userBought;

    @Column(name = "want_count")
    private Integer wantCount;

    @Column(name = "bought_count")
    private Integer boughtCount;

    @Column(name = "comment_count")
    private Integer commentCount;

    @Column(name = "author_nickname", length = 255)
    private String authorNickname;

    @Column(name = "legacy_edit_url", columnDefinition = "TEXT")
    private String legacyEditUrl;

    @Column(name = "ended")
    private Boolean ended;

    @Column(name = "block_new_comments")
    private Boolean blockNewComments;

    @Column(name = "exchange_rate", length = 255)
    private String exchangeRate;

    @Column(name = "is_read")
    private Boolean isRead;

    @Column(name = "is_new_window_open")
    private Boolean isNewWindowOpen;

    @Column(name = "now_click_count")
    private Integer nowClickCount;

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
