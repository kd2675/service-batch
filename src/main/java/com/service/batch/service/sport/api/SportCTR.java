package com.service.batch.service.sport.api;

import com.service.batch.service.sport.biz.InsSportSVC;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/sport")
public class SportCTR {
    private final InsSportSVC insSportSVC;

    @RequestMapping("/ins")
    public String sport(){
//        insSportSVC.saveSport();
        return "ok";
    }

}
