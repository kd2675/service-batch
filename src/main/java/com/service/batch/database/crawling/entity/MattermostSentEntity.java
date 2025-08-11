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
@Table(name = "MATTERMOST_SENT")
@Entity
public class MattermostSentEntity extends CommonDateEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "sent_id", nullable = false, unique = true, updatable = false)
    private String sentId;

    @Column(name = "category", nullable = false, updatable = false)
    private String category;

//    @ManyToOne
//    @JoinColumn(name = "news_id", nullable = false)
//    NewsEntity newsEntity;
}
