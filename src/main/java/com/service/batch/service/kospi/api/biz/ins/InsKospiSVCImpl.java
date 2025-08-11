package com.service.batch.service.kospi.api.biz.ins;//package com.example.crawling.service.kospi.api.biz.ins;
//
//import com.example.crawling.service.kospi.database.rep.jpa.kospi.KospiEntity;
//import com.example.crawling.service.kospi.database.rep.jpa.kospi.KospiREP;
//import com.example.crawling.utils.ChromeDriverConnUtil;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.jsoup.nodes.Document;
//import org.jsoup.nodes.Element;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//@Slf4j
//@RequiredArgsConstructor
//@Service
//public class InsKospiSVCImpl implements InsKospiSVC {
//    String URL = "https://m.stock.naver.com/domestic/index/KOSPI/total";
//    String URL200 = "https://m.stock.naver.com/domestic/index/KPI200/enrollstocks";
//
//    private final KospiREP kospiREP;
//    private final ChromeDriverConnUtil chromeDriverConnUtil;
//
//    @Transactional
//    @Override
//    public void saveKospi() {
//        Document doc = chromeDriverConnUtil.conn(URL);
//
//        Element root = doc.getElementById("root").children().first().children().get(1).children().first().children().first();
//
//        String title = root.children().get(1).children().first().children().first().children().get(1).text();
//        double price = Double.parseDouble(isDash(root.children().get(1).children().first().children().first().children().get(2).text().replace(",", "")));
//        double priceRange = Double.parseDouble(isDash(root.children().get(1).children().first().children().first().children().get(3).children().get(0).children().first().text().replace(",", "")));
//        double priceRangePer = Double.parseDouble(isDash(root.children().get(1).children().first().children().first().children().get(3).children().get(1).text().replace(",", "").replace("%", "")));
//
//        double nowPrice = 0;
//        double prevPrice = 0;
//        double lowestPrice = 0;
//        double lowest52Price = 0;
//        double highestPrice = 0;
//        double highest52Price = 0;
//        long tradingVolume = 0;
//        long personVolume = 0;
//        long foreignVolume = 0;
//        long agencyVolume = 0;
//        long programVolume = 0;
//
//        try {
//            nowPrice = Double.parseDouble(isDash(root.children().get(4).children().first().children().get(1).children().first().children().get(1).text().replace(",", "").replace("%", "")));
//            prevPrice = Double.parseDouble(isDash(root.children().get(4).children().first().children().get(0).children().first().children().get(1).text().replace(",", "").replace("%", "")));
//            lowestPrice = Double.parseDouble(isDash(root.children().get(4).children().first().children().get(3).children().first().children().get(1).text().replace(",", "").replace("%", "")));
//            lowest52Price = Double.parseDouble(isDash(root.children().get(4).children().first().children().get(7).children().first().children().get(1).text().replace(",", "").replace("%", "")));
//            highestPrice = Double.parseDouble(isDash(root.children().get(4).children().first().children().get(2).children().first().children().get(1).text().replace(",", "").replace("%", "")));
//            highest52Price = Double.parseDouble(isDash(root.children().get(4).children().first().children().get(6).children().first().children().get(1).text().replace(",", "").replace("%", "")));
//            tradingVolume = Long.parseLong(isDash(root.children().get(4).children().first().children().get(4).children().first().children().get(1).text().replace(",", "").replaceAll("[\uAC00-\uD7A3]", "")));
//            personVolume = Long.parseLong(isDash(root.children().get(6).children().get(1).children().get(0).children().get(1).children().get(0).children().get(1).text().replace(",", "").replaceAll("[\uAC00-\uD7A3]", "")));
//            foreignVolume = Long.parseLong(isDash(root.children().get(6).children().get(1).children().get(0).children().get(1).children().get(1).children().get(1).text().replace(",", "").replaceAll("[\uAC00-\uD7A3]", "")));
//            agencyVolume = Long.parseLong(isDash(root.children().get(6).children().get(1).children().get(0).children().get(1).children().get(2).children().get(1).text().replace(",", "").replaceAll("[\uAC00-\uD7A3]", "")));
//            programVolume = Long.parseLong(isDash(root.children().get(6).children().get(1).children().get(1).children().get(1).children().get(2).children().get(1).text().replace(",", "").replaceAll("[\uAC00-\uD7A3]", "")));
//        } catch (Exception e) {
//            log.warn("saveKospi ins now price error ---> {}", e);
//        }
//
//        KospiEntity kospiEntity = KospiEntity.builder()
//                .title(title)
//                .price(price)
//                .priceRange(priceRange)
//                .priceRangePer(priceRangePer)
//                .nowPrice(nowPrice)
//                .prevPrice(prevPrice)
//                .lowestPrice(lowestPrice)
//                .lowest52Price(lowest52Price)
//                .highestPrice(highestPrice)
//                .highest52Price(highest52Price)
//                .tradingVolume(tradingVolume)
//                .personVolume(personVolume)
//                .foreignVolume(foreignVolume)
//                .agencyVolume(agencyVolume)
//                .programVolume(programVolume).build();
//
//        kospiREP.save(kospiEntity);
//        log.error("save1");
//    }
//
//    @Transactional
//    @Override
//    public void saveKospi200() {
//
//    }
//}
