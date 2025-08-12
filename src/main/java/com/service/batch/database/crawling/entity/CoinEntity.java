package com.service.batch.database.crawling.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.database.common.jpa.CommonDateEntity;
import org.hibernate.annotations.DynamicInsert;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@Entity
@Table(name = "CoinEntity")
public class CoinEntity extends CommonDateEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "openingPrice", nullable = false, length = 255)
    private String openingPrice;
    @Column(name = "closingPrice", nullable = false, length = 255)
    private String closingPrice;
    @Column(name = "minPrice", nullable = false, length = 255)
    private String minPrice;
    @Column(name = "maxPrice", nullable = false, length = 255)
    private String maxPrice;
    @Column(name = "unitsTraded", nullable = false, length = 255)
    private String unitsTraded;
    @Column(name = "accTradeValue", nullable = false, length = 255)
    private String accTradeValue;
    @Column(name = "prevClosingPrice", nullable = false, length = 255)
    private String prevClosingPrice;
    @Column(name = "unitsTraded24H", nullable = false, length = 255)
    private String unitsTraded24H;
    @Column(name = "accTradeValue24H", nullable = false, length = 255)
    private String accTradeValue24H;
    @Column(name = "fluctate24H", nullable = false, length = 255)
    private String fluctate24H;
    @Column(name = "fluctateRate24H", nullable = false, length = 255)
    private String fluctateRate24H;
}
