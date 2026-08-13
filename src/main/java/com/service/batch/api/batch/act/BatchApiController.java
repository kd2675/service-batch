package com.service.batch.api.batch.act;

import com.service.batch.api.batch.biz.BatchApiService;
import com.service.batch.api.batch.biz.BatchExecutionResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.core.request.BatchExecuteRequest;
import org.example.core.request.BatchServiceRequest;
import org.example.core.response.base.dto.ResponseDTO;
import org.example.core.response.base.vo.Code;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/service/batch/api")
@RequiredArgsConstructor
@Slf4j
public class BatchApiController {
    private final BatchApiService batchApiService;

    @PostMapping("/gateway/executeAsync")
    public ResponseEntity<ResponseDTO> executeAsync(@RequestBody BatchExecuteRequest request) {
        try {
            batchApiService.validateExecuteRequest(request);
            return executionResponse(batchApiService.executeAsync(request));
        } catch (Exception e) {
            log.error("BatchApiController execute error", e);
        }

        return response(false, Code.BAD_REQUEST);
    }

    @PostMapping("/gateway/execute")
    public ResponseEntity<ResponseDTO> execute(@RequestBody BatchExecuteRequest request) {
        try {
            batchApiService.validateExecuteRequest(request);
            return executionResponse(batchApiService.execute(request));
        } catch (Exception e) {
            log.error("BatchApiController execute error", e);
        }

        return response(false, Code.BAD_REQUEST);
    }

    @PostMapping("/gateway/serviceAsync")
    public ResponseEntity<ResponseDTO> serviceAsync(@RequestBody BatchServiceRequest request) {
        try {
            batchApiService.validateServiceRequest(request);
            batchApiService.serviceAsync(request);

            return response(true, Code.OK_ASYNC);
        } catch (Exception e) {
            log.error("BatchApiController execute error", e);
        }

        return response(false, Code.BAD_REQUEST);
    }

    @PostMapping("/gateway/service")
    public ResponseEntity<ResponseDTO> service(@RequestBody BatchServiceRequest request) {
        try {
            batchApiService.validateServiceRequest(request);
            batchApiService.service(request);

            return response(true, Code.OK);
        } catch (IllegalArgumentException e) {
            log.error("BatchApiController service validation error", e);
            return response(false, Code.BAD_REQUEST);
        } catch (Exception e) {
            log.error("BatchApiController service execution error", e);
            return response(false, Code.INTERNAL_SERVER_ERROR);
        }
    }

    private ResponseEntity<ResponseDTO> response(boolean success, Code code) {
        return ResponseEntity.status(code.getHttpStatus())
                .body(ResponseDTO.of(success, code));
    }

    private ResponseEntity<ResponseDTO> executionResponse(BatchExecutionResult result) {
        if (result.isSkipped()) {
            return response(true, Code.OK_SKIPPED);
        }
        return response(true, result == BatchExecutionResult.ACCEPTED ? Code.OK_ASYNC : Code.OK);
    }
}
