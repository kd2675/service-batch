package com.service.batch.database.crawling.entity;//package com.example.batch.common.database.rep.jpa.kospi;
//
//import jakarta.persistence.*;
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//import org.example.database.common.rep.jpa.CommonDateEntity;
//import org.hibernate.annotations.DynamicInsert;
//
//@Getter
//@Builder
//@NoArgsConstructor
//@AllArgsConstructor
//@DynamicInsert
//@Entity
//@Table(name = "KospiEntity")
//public class KospiEntity extends CommonDateEntity {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(name = "id")
//    private Long id;
//
////    @ColumnDefault("''")
//    @Column(name = "title", nullable = false, columnDefinition = "TEXT")
//    private String title;
//
//    @Column(name = "price", nullable = false)
//    private double price;
//
//    @Column(name = "nowPrice", nullable = false)
//    private double nowPrice;
//
//    @Column(name = "priceRange", nullable = false)
//    private double priceRange;
//
//    @Column(name = "priceRangePer", nullable = false)
//    private double priceRangePer;
//
//    @Column(name = "prevPrice", nullable = false)
//    private double prevPrice;
//
//    @Column(name = "lowestPrice", nullable = false)
//    private double lowestPrice;
//
//    @Column(name = "lowest52Price", nullable = false)
//    private double lowest52Price;
//
//    @Column(name = "highestPrice", nullable = false)
//    private double highestPrice;
//
//    @Column(name = "highest52Price", nullable = false)
//    private double highest52Price;
//
//    @Column(name = "tradingVolume", nullable = false)
//    private Long tradingVolume;
//
//    @Column(name = "personVolume", nullable = false)
//    private Long personVolume;
//
//    @Column(name = "foreignVolume", nullable = false)
//    private Long foreignVolume ;
//
//    @Column(name = "agencyVolume", nullable = false)
//    private Long agencyVolume;
//
//    @Column(name = "programVolume", nullable = false)
//    private Long programVolume;
//
//
//
//}
