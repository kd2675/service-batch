package com.service.batch.service.kospi.api.biz.send;//package com.example.crawling.service.kospi.api.biz.send;
//
//import com.example.crawling.service.kospi.database.rep.jpa.kospi.KospiEntity;
//import com.example.crawling.service.kospi.database.rep.jpa.kospi.KospiREP;
//import com.example.crawling.utils.MattermostUtil;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.RestTemplate;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//@Slf4j
//@RequiredArgsConstructor
//@Service
//public class SendKospiSVCImpl implements SendKospiSVC {
//
//    private final KospiREP kospiREP;
//    private final MessageConverter messageConverter;
//    private final RestTemplate restTemplate;
//    private final MattermostUtil mattermostUtil;
//
//    @Override
//    public void sendMattermostKospi() {
//        KospiEntity kospiEntity = kospiREP.findTop1ByOrderByIdDesc().orElseThrow(RuntimeException::new);
//
//        mattermostUtil.send(messageConverter.makeMattermostMessage2(kospiEntity), "6w3xkrc3c7go7jp9q44uio9i4c");
//    }
//
//
//    @Override
//    public void sendMattermostKospiWeek() {
//        LocalDateTime prev8Days = LocalDateTime.now().minusDays(8L);
//        List<KospiEntity> kospiEntityFirstList = kospiREP.findKospiEntityByCreateDateBetweenAndCreateDateBetweenFirst(prev8Days);
//        List<KospiEntity> kospiEntityLastList = kospiREP.findKospiEntityByCreateDateBetweenAndCreateDateBetweenLast(prev8Days);
//
//        mattermostUtil.send(messageConverter.makeMattermostMessageWeek(kospiEntityFirstList, kospiEntityLastList), "6w3xkrc3c7go7jp9q44uio9i4c");
//    }
//
//
//}
