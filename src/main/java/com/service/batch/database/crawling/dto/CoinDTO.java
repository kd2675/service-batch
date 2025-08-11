package com.service.batch.database.crawling.dto;

import com.example.batch.database.crawling.entity.CoinEntity;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CoinDTO {
    private Long id;
    private String openingPrice;
    private String closingPrice;
    private String minPrice;
    private String maxPrice;
    private String unitsTraded;
    private String accTradeValue;
    private String prevClosingPrice;
    private String unitsTraded24H;
    private String accTradeValue24H;
    private String fluctate24H;
    private String fluctateRate24H;
    private LocalDateTime createDate;
    private LocalDateTime updateDate;

    public static CoinDTO ofEntity(CoinEntity coinEntity) {
        return new CoinDTO(
                coinEntity.getId(),
                coinEntity.getOpeningPrice(),
                coinEntity.getClosingPrice(),
                coinEntity.getMinPrice(),
                coinEntity.getMaxPrice(),
                coinEntity.getUnitsTraded(),
                coinEntity.getAccTradeValue(),
                coinEntity.getPrevClosingPrice(),
                coinEntity.getUnitsTraded24H(),
                coinEntity.getAccTradeValue24H(),
                coinEntity.getFluctate24H(),
                coinEntity.getFluctateRate24H(),
                coinEntity.getCreateDate(),
                coinEntity.getUpdateDate()
        );
    }

    private CoinDTO() {
    }

    private CoinDTO(Long id, String openingPrice, String closingPrice, String minPrice, String maxPrice, String unitsTraded, String accTradeValue, String prevClosingPrice, String unitsTraded24H, String accTradeValue24H, String fluctate24H, String fluctateRate24H, LocalDateTime createDate, LocalDateTime updateDate) {
        this.id = id;
        this.openingPrice = openingPrice;
        this.closingPrice = closingPrice;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.unitsTraded = unitsTraded;
        this.accTradeValue = accTradeValue;
        this.prevClosingPrice = prevClosingPrice;
        this.unitsTraded24H = unitsTraded24H;
        this.accTradeValue24H = accTradeValue24H;
        this.fluctate24H = fluctate24H;
        this.fluctateRate24H = fluctateRate24H;
        this.createDate = createDate;
        this.updateDate = updateDate;
    }
}
