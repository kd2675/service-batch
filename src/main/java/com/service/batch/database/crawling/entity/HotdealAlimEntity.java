package com.service.batch.database.crawling.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.database.common.jpa.CommonDateEntity;
import org.hibernate.annotations.DynamicInsert;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@Table(name = "HOT_DEAL_ALIM")
@Entity
public class HotdealAlimEntity extends CommonDateEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "keyword", nullable = false, length = 255)
    private String keyword;

    @Column(name = "target", nullable = false, length = 255)
    private String target;

    @Column(name = "send_yn", nullable = false, length = 1)
    private String sendYn;

    public void updSendYn(String sendYn) {
        this.sendYn = sendYn;
    }

}
