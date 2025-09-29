package com.service.batch.api.batch.act;

import com.service.batch.api.batch.biz.BatchApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.core.request.BatchExecuteRequest;
import org.example.core.request.BatchServiceRequest;
import org.example.core.response.base.dto.ResponseDTO;
import org.example.core.response.base.vo.Code;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/service/batch/api")
@RequiredArgsConstructor
@Slf4j
public class BatchApiController {
    private final BatchApiService batchApiService;

    @RequestMapping("/gateway/executeAsync")
    public ResponseDTO executeAsync(@RequestBody BatchExecuteRequest request) {
        try {
            batchApiService.executeAsync(request);

            return ResponseDTO.of(true, Code.OK);
        } catch (Exception e) {
            log.error("BatchApiController execute error", e);
        }

        return ResponseDTO.of(false, Code.BAD_REQUEST);
    }

    @RequestMapping("/gateway/execute")
    public ResponseDTO execute(@RequestBody BatchExecuteRequest request) {
        try {
            batchApiService.execute(request);

            return ResponseDTO.of(true, Code.OK);
        } catch (Exception e) {
            log.error("BatchApiController execute error", e);
        }

        return ResponseDTO.of(false, Code.BAD_REQUEST);
    }

    @RequestMapping("/gateway/serviceAsync")
    public ResponseDTO serviceAsync(@RequestBody BatchServiceRequest request) {
        try {
            batchApiService.serviceAsync(request);

            return ResponseDTO.of(true, Code.OK);
        } catch (Exception e) {
            log.error("BatchApiController execute error", e);
        }

        return ResponseDTO.of(false, Code.BAD_REQUEST);
    }

    @RequestMapping("/gateway/service")
    public ResponseDTO service(@RequestBody BatchServiceRequest request) {
        try {
            batchApiService.service(request);

            return ResponseDTO.of(true, Code.OK);
        } catch (Exception e) {
            log.error("BatchApiController execute error", e);
        }

        return ResponseDTO.of(false, Code.BAD_REQUEST);
    }
}
