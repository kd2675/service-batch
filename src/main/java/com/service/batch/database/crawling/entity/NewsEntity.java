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
@Table(name = "NEWS")
@Entity
public class NewsEntity extends CommonDateEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ColumnDefault("'n'")
    @Column(name = "sendYn", nullable = true, length = 1)
    private String sendYn;

    @Column(name = "category", nullable = true, columnDefinition = "TEXT")
    private String category;

    @Column(name = "company", nullable = true, columnDefinition = "TEXT")
    private String company;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "aiContent", nullable = true, columnDefinition = "TEXT")
    private String aiContent;

    @Column(name = "link", nullable = false, columnDefinition = "TEXT")
    private String link;

    @Column(name = "pubDate", nullable = false)
    private LocalDateTime pubDate;

    @ColumnDefault("0")
    @Column(name = "visitCnt", nullable = true)
    private int visitCnt;

    @ColumnDefault("0")
    @Column(name = "commentCnt", nullable = true)
    private int commentCnt;

//    @Builder.Default
//    @OneToMany(mappedBy = "newsEntity", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<MattermostSentEntity> mattermostSentEntityList = new ArrayList<>();

    public void updSendYn(String sendYn) {
        this.sendYn = sendYn;
    }
}
