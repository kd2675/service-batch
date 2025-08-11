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
@Entity
@Table(name = "WATCH_TB")
public class WatchEntity extends CommonDateEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "star", nullable = false)
    private Integer star;

    @ColumnDefault("'n'")
    @Column(name = "watch_yn", nullable = false)
    private String watchYn;

    public void updateWatchYn(String watchYn) {
        this.watchYn = watchYn;
    }
}
