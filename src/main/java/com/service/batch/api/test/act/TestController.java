package com.service.batch.api.test.act;

import lombok.RequiredArgsConstructor;
import org.example.core.request.BatchExecuteRequest;
import org.example.core.response.base.dto.ResponseDTO;
import org.example.core.response.base.vo.Code;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/service")
@RequiredArgsConstructor
public class TestController {
    @RequestMapping("/batch/api/gateway/execute")
    public ResponseDTO test(@RequestBody BatchExecuteRequest request) {
        System.out.println(request);
        return ResponseDTO.of(true, Code.OK);
    }
}
