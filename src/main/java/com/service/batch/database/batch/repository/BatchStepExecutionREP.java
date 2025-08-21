package com.service.batch.database.batch.repository;

import com.service.batch.database.batch.entity.BatchStepExecution;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface BatchStepExecutionREP extends JpaRepository<BatchStepExecution, Long> {
    List<BatchStepExecution> findAllByStartTimeBefore(LocalDateTime startTime);
}