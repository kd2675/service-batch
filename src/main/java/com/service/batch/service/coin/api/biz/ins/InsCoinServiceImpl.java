package com.service.batch.service.coin.api.biz.ins;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.service.batch.database.crawling.entity.CoinEntity;
import com.service.batch.database.crawling.repository.CoinREP;
import com.service.batch.service.coin.api.vo.BitHumbDataVO;
import com.service.batch.service.coin.api.vo.BitHumbResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class InsCoinServiceImpl implements InsCoinService {
    private static final List<String> COIN_SYMBOLS = List.of("BTC", "ETH", "XRP");

    private final RestTemplate restTemplate;
    private final CoinREP coinREP;

    @Transactional
    @Override
    public void saveCoinDataBTC() {
        URI uri = UriComponentsBuilder
                .fromUriString("https://api.bithumb.com")
                .path("/public/ticker/ALL")
                .encode()
                .build()
                .toUri();

        BitHumbResultVO forObject = restTemplate.getForObject(uri, BitHumbResultVO.class);
        if (forObject == null || forObject.getData() == null) {
            log.warn("bithumb ticker response is empty");
            return;
        }

        ObjectMapper objectMapper = new ObjectMapper();
        COIN_SYMBOLS.forEach(symbol -> {
            Object data = forObject.getData().get(symbol);
            if (data == null) {
                log.warn("bithumb ticker data is empty: {}", symbol);
                return;
            }

            BitHumbDataVO bitHumbDataVO = objectMapper.convertValue(data, BitHumbDataVO.class);

            CoinEntity coinEntity = CoinEntity.builder()
                    .coinSymbol(symbol)
                    .openingPrice(bitHumbDataVO.getOpeningPrice())
                    .closingPrice(bitHumbDataVO.getClosingPrice())
                    .minPrice(bitHumbDataVO.getMinPrice())
                    .maxPrice(bitHumbDataVO.getMaxPrice())
                    .unitsTraded(bitHumbDataVO.getUnitsTraded())
                    .accTradeValue(bitHumbDataVO.getAccTradeValue())
                    .prevClosingPrice(bitHumbDataVO.getPrevClosingPrice())
                    .unitsTraded24H(bitHumbDataVO.getUnitsTraded24H())
                    .accTradeValue24H(bitHumbDataVO.getAccTradeValue24H())
                    .fluctate24H(bitHumbDataVO.getFluctate24H())
                    .fluctateRate24H(bitHumbDataVO.getFluctateRate24H())
                    .build();

            coinREP.save(coinEntity);
        });
    }
}
