package com.service.batch.database.pub.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class InsMarketDTO {
    //b, s
    @NotNull
    private String orderSlct;
    //BTC
    @NotNull
    private String coinSlct;
    //l, s
    @NotNull
    private String marginSlct;
    @NotNull
    private Integer margin;
    @NotNull
    private Double cnt;
    @NotNull
    private Double price;
}
