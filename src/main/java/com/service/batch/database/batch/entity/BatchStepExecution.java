package com.service.batch.database.batch.entity;

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
@Table(name = "BATCH_STEP_EXECUTION")
public class BatchStepExecution {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "STEP_EXECUTION_ID")
    private Long id;
    @Column(name = "VERSION", nullable = false)
    private Long version;
    @Column(name = "STEP_NAME", nullable = false, length = 100)
    private String stepName;
    @Column(name = "START_TIME", nullable = false)
    private LocalDateTime startTime;
    @Column(name = "END_TIME")
    private LocalDateTime endTime;
    @Column(name = "STATUS", length = 10)
    private String status;
    @Column(name = "COMMIT_COUNT")
    private Long commitCount;
    @Column(name = "READ_COUNT")
    private Long readCount;
    @Column(name = "FILTER_COUNT")
    private Long filterCount;
    @Column(name = "WRITE_COUNT")
    private Long writeCount;
    @Column(name = "READ_SKIP_COUNT")
    private Long readSkipCount;
    @Column(name = "WRITE_SKIP_COUNT")
    private Long writeSkipCount;
    @Column(name = "PROCESS_SKIP_COUNT")
    private Long processSkipCount;
    @Column(name = "ROLLBACK_COUNT")
    private Long rollbackCount;
    @Column(name = "EXIT_CODE", columnDefinition = "TEXT")
    private String exitCode;
    @Column(name = "EXIT_MESSAGE", columnDefinition = "TEXT")
    private String exitMessage;
    @Column(name = "LAST_UPDATED")
    private LocalDateTime lastUpdated;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "JOB_EXECUTION_ID", nullable = false)
    private BatchJobExecution batchJobExecution;
}
