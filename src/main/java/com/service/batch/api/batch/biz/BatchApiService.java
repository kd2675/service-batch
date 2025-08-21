package com.service.batch.api.batch.biz;

import org.example.core.request.BatchExecuteRequest;
import org.example.core.request.BatchServiceRequest;

public interface BatchApiService {
    void execute(BatchExecuteRequest request) throws Exception;
    void service(BatchServiceRequest request);
}
