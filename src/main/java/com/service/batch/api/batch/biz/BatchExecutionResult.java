package com.service.batch.api.batch.biz;

public enum BatchExecutionResult {
    ACCEPTED,
    COMPLETED,
    SKIPPED_ALREADY_RUNNING,
    SKIPPED_RESETTING;

    public boolean isSkipped() {
        return this == SKIPPED_ALREADY_RUNNING || this == SKIPPED_RESETTING;
    }
}
