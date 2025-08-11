package com.service.batch.database.batch.repository;

import com.example.batch.database.batch.entity.BatchJobExecution;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BatchJobExecutionREP extends JpaRepository<BatchJobExecution, Long> {
}