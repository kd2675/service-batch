package com.service.batch.database.batch.repository;

import com.service.batch.database.batch.entity.BatchJobExecutionContext;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BatchJobExecutionContextREP extends JpaRepository<BatchJobExecutionContext, Long> {
}