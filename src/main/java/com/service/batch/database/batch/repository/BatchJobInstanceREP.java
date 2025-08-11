package com.service.batch.database.batch.repository;

import com.example.batch.database.batch.entity.BatchJobInstance;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BatchJobInstanceREP extends JpaRepository<BatchJobInstance, Long> {
}