package com.service.batch.database.batch.repository;

import com.service.batch.database.batch.entity.BatchStepExecutionContext;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BatchStepExecutionContextREP extends JpaRepository<BatchStepExecutionContext, Long> {
}