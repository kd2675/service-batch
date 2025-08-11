package com.service.batch.database.batch.entity;

import com.example.batch.database.batch.entity.BatchJobInstance;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@Entity
@Table(name = "BATCH_JOB_EXECUTION")
public class BatchJobExecution {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "JOB_EXECUTION_ID")
    private Long id;
    @Column(name = "VERSION")
    private Long version;
    @Column(name = "CREATE_TIME", nullable = false)
    private LocalDateTime createTime;
    @Column(name = "START_TIME")
    private LocalDateTime startTime;
    @Column(name = "END_TIME")
    private LocalDateTime endTime;
    @Column(name = "STATUS", length = 10)
    private String status;
    @Column(name = "EXIT_CODE", columnDefinition = "TEXT")
    private String exitCode;
    @Column(name = "EXIT_MESSAGE", columnDefinition = "TEXT")
    private String exitMessage;
    @Column(name = "LAST_UPDATED")
    private LocalDateTime lastUpdated;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "JOB_INSTANCE_ID")
    private BatchJobInstance batchJobInstance;
}
