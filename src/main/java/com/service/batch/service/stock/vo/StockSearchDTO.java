package com.service.batch.service.stock.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class StockSearchDTO {
    private String symbol;
    private String shortName;
    private String longName;
    private String market;
    private String exchange;
    private String quoteType;
    private String typeDisp;
}