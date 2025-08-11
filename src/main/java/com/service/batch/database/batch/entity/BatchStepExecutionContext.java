package com.service.batch.database.batch.entity;

import com.example.batch.database.batch.entity.BatchStepExecution;
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
@Table(name = "BATCH_STEP_EXECUTION_CONTEXT")
public class BatchStepExecutionContext {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "STEP_EXECUTION_ID")
    private Long id;
    @Column(name = "SHORT_CONTEXT", columnDefinition = "TEXT")
    private String shortContext;
    @Column(name = "SERIALIZED_CONTEXT", columnDefinition = "TEXT")
    private String serializedContext;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "STEP_EXECUTION_ID", nullable = false, insertable = false, updatable = false)
    private BatchStepExecution batchStepExecution;
}
