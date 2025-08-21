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
@Table(name = "BATCH_JOB_EXECUTION_PARAMS")
public class BatchJobExecutionParams {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "JOB_EXECUTION_ID")
    private Long id;
    @Column(name = "PARAMETER_NAME", columnDefinition = "TEXT")
    private String keyName;
    @Column(name = "PARAMETER_TYPE", columnDefinition = "TEXT")
    private String stringVal;
    @Column(name = "PARAMETER_VALUE", columnDefinition = "TEXT")
    private String dateVal;
    @Column(name = "IDENTIFYING", nullable = false, length = 1)
    private char identifying;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "JOB_EXECUTION_ID", nullable = false, insertable = false, updatable = false)
    private BatchJobExecution batchJobExecution;
}
