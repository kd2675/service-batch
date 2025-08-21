package com.service.batch.database.batch.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@Entity
@Table(name = "BATCH_JOB_INSTANCE")
public class BatchJobInstance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "JOB_INSTANCE_ID")
    private Long id;
    @Column(name = "VERSION")
    private Long version;
    @Column(name = "JOB_NAME", unique = true, nullable = false, length = 100)
    private String jobName;
    @Column(name = "JOB_KEY", unique = true, nullable = false, length = 32)
    private String jobKey;
    @OneToMany(mappedBy = "batchJobInstance")
    private List<BatchJobExecution> batchJobExecution;
}
