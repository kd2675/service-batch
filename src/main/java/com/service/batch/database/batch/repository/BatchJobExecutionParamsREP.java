package com.service.batch.database.batch.repository;

import com.service.batch.database.batch.entity.BatchJobExecutionParams;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BatchJobExecutionParamsREP extends JpaRepository<BatchJobExecutionParams, Long> {
}