package com.service.batch.database.batch.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@Entity
@Table(name = "BATCH_JOB_SEQ")
public class BatchJobSeq {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;
    @Column(name = "UNIQUE_KEY",nullable = false, length = 1)
    private char uniqueKey;
}
