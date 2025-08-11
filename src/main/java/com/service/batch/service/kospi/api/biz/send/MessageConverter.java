package com.service.batch.service.kospi.api.biz.send;//package com.example.crawling.service.kospi.api.biz.send;
//
//import com.example.crawling.service.kospi.api.dto.KospiWeekDTO;
//import com.example.crawling.service.kospi.database.rep.jpa.kospi.KospiEntity;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//
//import java.time.format.DateTimeFormatter;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Slf4j
//@Service
//public class MessageConverter {
//    public String makeMattermostMessage() {
//        StringBuilder result = new StringBuilder();
//        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
////        KospiEntity kospiEntity = kospiRepository.findTop1ByOrderByIdDesc().orElseGet(KospiEntity::new);
//        KospiEntity kospiEntity = new KospiEntity();
//        String format = dtf.format(kospiEntity.getCreateDate());
//
//
//        String title = "종목 : " + kospiEntity.getTitle() + "\n";
//        String createDate = "시각 : " + format + "\n";
//        String price = "가격 : " + kospiEntity.getPrice() + "\n";
//        String priceRange = "등락 : " + kospiEntity.getPriceRange() + " (" + kospiEntity.getPriceRangePer() + "%" + ")" + "\n";
//        String nowPrice = "시가 : " + kospiEntity.getNowPrice() + "\n";
//        String prevPrice = "전일 : " + kospiEntity.getPrevPrice() + "\n";
//        String highestLowestPrice = "최고 : " + kospiEntity.getHighestPrice() + " 최저 : " + kospiEntity.getLowestPrice() + "\n";
//        String highestLowest52Price = "52주 최고 : " + kospiEntity.getHighest52Price() + " 52주 최저 : " + kospiEntity.getLowest52Price() + "\n";
//        String tradingVolume = "거래량 : " + kospiEntity.getTradingVolume() + "\n";
//        String Volume = "개인 : " + kospiEntity.getPersonVolume() + " 외국인 : " + kospiEntity.getForeignVolume() + " 기관 : " + kospiEntity.getAgencyVolume() + "\n";
//
//        result.append(title)
//                .append(createDate)
//                .append(price)
//                .append(priceRange)
//                .append(nowPrice)
//                .append(prevPrice)
//                .append(highestLowestPrice)
//                .append(highestLowest52Price)
//                .append(tradingVolume)
//                .append(Volume);
//
//        log.error("message : {}", result.toString());
//
//        return result.toString();
//    }
//
//    public String makeMattermostMessage2(KospiEntity kospiEntity) {
//        StringBuilder result = new StringBuilder();
//        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//        String format = dtf.format(kospiEntity.getCreateDate());
//
//        String header = "| 종목 | 시각 | 가격 | 등락(%) | 시가 | 전일 | 최고 / 52주 최고 | 최저 / 52주 최저 | 거래량 | 개인 / 외국인 / 기관 |\n";
//        String line = "| :-:|:-:|:-:|:-:|:-:|:-:|:-:|:-:|:-: |\n";
//        String content = "| " + kospiEntity.getTitle()
//                + " | " + format
//                + " | " + kospiEntity.getPrice()
//                + " | " + kospiEntity.getPriceRange() + " (" + kospiEntity.getPriceRangePer() + "%" + ")"
//                + " | " + kospiEntity.getNowPrice()
//                + " | " + kospiEntity.getPrevPrice()
//                + " | " + kospiEntity.getHighestPrice() + " / " + kospiEntity.getHighest52Price()
//                + " | " + kospiEntity.getLowestPrice() + " / " + kospiEntity.getLowest52Price()
//                + " | " + kospiEntity.getTradingVolume()
//                + " | " + kospiEntity.getPersonVolume() + " / " + kospiEntity.getForeignVolume() + " / " + kospiEntity.getAgencyVolume()
//                + " |";
//
//        result.append(header)
//                .append(line)
//                .append(content);
//
//        return result.toString();
//    }
//    public String makeMattermostMessageWeek(List<KospiEntity> kospiEntityFirstList, List<KospiEntity> kospiEntityLastList) {
//        StringBuilder result = new StringBuilder();
//        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//
//        List<String> lastDateList = kospiEntityLastList.stream()
//                .map(v -> dtf.format(v.getCreateDate()))
//                .collect(Collectors.toList());
//
//        List<KospiEntity> firstList = kospiEntityFirstList.stream()
//                .filter(v -> lastDateList.contains(dtf.format(v.getCreateDate())))
//                .collect(Collectors.toList());
//
//        List<String> dateList = firstList.stream()
//                .map(v -> dtf.format(v.getCreateDate()))
//                .collect(Collectors.toList());
//
//        List<KospiEntity> lastList = kospiEntityLastList.stream()
//                .filter(v -> dateList.contains(dtf.format(v.getCreateDate())))
//                .collect(Collectors.toList());
//
//        List<KospiWeekDTO> kospiWeekDTOS = new ArrayList<>();
//
//        for (String s : dateList) {
//            KospiEntity firstEntity = firstList.stream()
//                    .filter(v -> dtf.format(v.getCreateDate()).equals(s))
//                    .findFirst()
//                    .orElseGet(KospiEntity::new);
//
//            KospiEntity lastEntity = lastList.stream()
//                    .filter(v -> dtf.format(v.getCreateDate()).equals(s))
//                    .findFirst()
//                    .orElseGet(KospiEntity::new);
//
//            KospiWeekDTO kospiWeekDTO = KospiWeekDTO.of(s, firstEntity, lastEntity);
//
//            kospiWeekDTOS.add(kospiWeekDTO);
//        }
//
//        String emptyLine = "***\n";
//
//        String title = "### 코스피 7일 동향\n\n";
//
//        String header = "| 시각 | 종가(15시) | 등락(%) | 시작가(9시) | 등락(%) | 장 거래량 | 개인 / 외국인 / 기관 |\n";
//
//        String line = "| :-:|:-:|:-:|:-:|:-:|:-:|:-: |\n";
//
//        StringBuilder contentBuilder = new StringBuilder();
//
//        for (KospiWeekDTO vo : kospiWeekDTOS) {
//            String content = "| " + vo.getCreateDate()
//                    + " | " + vo.getPrice9()
//                    + " | " + vo.getPriceRange9() + " (" + vo.getPriceRangePer9() + "%" + ")"
//                    + " | " + vo.getPrice15()
//                    + " | " + vo.getPriceRange15() + " (" + vo.getPriceRangePer15() + "%" + ")"
//                    + " | " + vo.getTradingVolume()
//                    + " | " + vo.getPersonVolume() + " / " + vo.getForeignVolume() + " / " + vo.getAgencyVolume()
//                    + " |\n";
//            contentBuilder.append(content);
//        }
//
//        result.append(emptyLine)
//                .append(title)
//                .append(header)
//                .append(line)
//                .append(contentBuilder);
//
//        return result.toString();
//    }
//}
