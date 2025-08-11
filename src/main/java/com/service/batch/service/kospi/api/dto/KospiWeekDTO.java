package com.service.batch.service.kospi.api.dto;//package com.example.batch.service.kospi.api.dto;
//
//import com.example.batch.common.database.rep.jpa.kospi.KospiEntity;
//import lombok.Data;
//
//@Data
//public class KospiWeekDTO {
//    private String createDate;
//    private double price9;
//    private double price15;
//    private double priceRange9;
//    private double priceRangePer9;
//    private double priceRange15;
//    private double priceRangePer15;
//    private Long tradingVolume;
//    private Long personVolume;
//    private Long foreignVolume ;
//    private Long agencyVolume;
//
//    public static KospiWeekDTO of(String createDate, KospiEntity firstEntity, KospiEntity lastEntity) {
//        return new KospiWeekDTO(
//                createDate,
//                firstEntity.getPrice(),
//                lastEntity.getPrice(),
//                firstEntity.getPriceRange(),
//                firstEntity.getPriceRangePer(),
//                lastEntity.getPriceRange(),
//                lastEntity.getPriceRangePer(),
//                lastEntity.getTradingVolume(),
//                lastEntity.getAgencyVolume(),
//                lastEntity.getPersonVolume(),
//                lastEntity.getForeignVolume()
//        );
//    }
//
//    private KospiWeekDTO() {
//    }
//
//    private KospiWeekDTO(String createDate, double price9, double price15, double priceRange9, double priceRangePer9, double priceRange15, double priceRangePer15, Long tradingVolume, Long personVolume, Long foreignVolume, Long agencyVolume) {
//        this.createDate = createDate;
//        this.price9 = price9;
//        this.price15 = price15;
//        this.priceRange9 = priceRange9;
//        this.priceRangePer9 = priceRangePer9;
//        this.priceRange15 = priceRange15;
//        this.priceRangePer15 = priceRangePer15;
//        this.tradingVolume = tradingVolume;
//        this.personVolume = personVolume;
//        this.foreignVolume = foreignVolume;
//        this.agencyVolume = agencyVolume;
//    }
//}
