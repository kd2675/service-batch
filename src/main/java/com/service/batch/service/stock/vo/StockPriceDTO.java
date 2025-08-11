package com.service.batch.service.stock.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class StockPriceDTO {
    private String symbol;
    private BigDecimal price;
    private BigDecimal change;
    private BigDecimal changePercent;
    private String currency;
    private Long marketCap;
    private Long volume;
    private LocalDateTime timestamp;
}
