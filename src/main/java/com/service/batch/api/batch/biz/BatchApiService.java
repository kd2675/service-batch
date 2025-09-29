package com.service.batch.api.batch.biz;

import org.example.core.request.BatchExecuteRequest;
import org.example.core.request.BatchServiceRequest;

public interface BatchApiService {
    void executeAsync(BatchExecuteRequest request) throws Exception;
    void execute(BatchExecuteRequest request) throws Exception;
    void serviceAsync(BatchServiceRequest request);
    void service(BatchServiceRequest request);
}
