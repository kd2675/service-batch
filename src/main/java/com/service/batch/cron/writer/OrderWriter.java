package com.service.batch.cron.writer;

import com.service.batch.cron.common.BasicWriter;
import com.service.batch.database.crawling.entity.CoinEntity;
import com.service.batch.database.crawling.repository.CoinREP;
import com.service.batch.database.pub.dto.InsMarketDTO;
import com.service.batch.database.pub.dto.MarketDTO;
import com.service.batch.database.pub.entity.MarketEntity;
import com.service.batch.database.pub.entity.OrderEntity;
import com.service.batch.database.pub.repository.MarketRepository;
import com.service.batch.database.pub.repository.OrderRepository;
import com.service.batch.database.pub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.example.database.database.auth.dto.UserDTO;
import org.example.database.database.auth.entity.UserEntity;
import org.example.database.database.auth.entity.WalletEntity;
import org.example.log.biz.LogService;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.Chunk;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.Collections;

@RequiredArgsConstructor
@Configuration
public class OrderWriter {
    public static final String COMPLETE_ORDER_TO_MARKET = "completeOrderToMarket";
    public static final String DEL_ORDER = "delOrder";

    private final LogService logService;

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final MarketRepository marketRepository;
    private final CoinREP coinREP;

    @Bean(name = COMPLETE_ORDER_TO_MARKET)
    @StepScope
    public BasicWriter<OrderEntity> orderEntityBasicWriter() {

        CoinEntity coinEntity = coinREP.findTopByOrderByIdDesc();

        return new BasicWriter<OrderEntity>() {
            @Override
            public void write(Chunk<? extends OrderEntity> chunk) throws Exception {
                for (OrderEntity orderEntity : chunk) {

                    InsMarketDTO insMarketDTO = new InsMarketDTO(
                            orderEntity.getOrderSlct(),
                            orderEntity.getCoinSlct(),
                            orderEntity.getMarginSlct(),
                            orderEntity.getMargin(),
                            orderEntity.getCnt(),
                            orderEntity.getPrice()
                    );

                    double coinPrice = Double.parseDouble(coinEntity.getClosingPrice());

                    double fullPrice = coinPrice * insMarketDTO.getCnt();

                    UserEntity userEntity = orderEntity.getUserEntity();

//                    if ("b".equals(insMarketDTO.getOrderSlct()) && userEntity.getWalletEntity().getPoint() < fullPrice) {
//                        throw new RuntimeException();
//                    }

                    marketRepository.findByUserEntityInAndCoinSlctInAndMarginSlctIn(
                            Collections.singletonList(userEntity),
                            Collections.singletonList(insMarketDTO.getCoinSlct()),
                            Collections.singletonList(insMarketDTO.getMarginSlct())
                    ).ifPresentOrElse(
                            marketEntity -> {
                                if ("b".equals(insMarketDTO.getOrderSlct())) {
                                    marketAdjust(insMarketDTO, userEntity, marketEntity, coinPrice);
                                } else if ("s".equals(insMarketDTO.getOrderSlct())) {
                                    marketSell(insMarketDTO, userEntity, marketEntity, coinPrice);
                                }
                            },
                            () -> {
                                if ("b".equals(insMarketDTO.getOrderSlct())) {
                                    marketBuy(insMarketDTO, userEntity, coinPrice);
                                } else if ("s".equals(insMarketDTO.getOrderSlct())) {
                                    throw new RuntimeException();
                                }
                            }
                    );
                }
            }
        };
    }

    @Bean(name = DEL_ORDER)
    @StepScope
    public BasicWriter<OrderEntity> delOrderWriter() {
        return new BasicWriter<OrderEntity>() {
            @Override
            public void write(Chunk<? extends OrderEntity> chunk) throws Exception {
                orderRepository.deleteAll(chunk);
            }
        };
    }

    private void marketAdjust(InsMarketDTO insMarketDTO, UserEntity userEntity, MarketEntity marketEntity, Double coinPrice) {
        if (Integer.compare(insMarketDTO.getMargin(), marketEntity.getMargin()) != 0) {
            throw new RuntimeException();
        }

        Integer margin = marketEntity.getMargin();
        Double price = marketEntity.getPrice();
        Double cnt = marketEntity.getCnt();
        Double cleanPrice = marketEntity.getCleanPrice();

        double oldFullPrice = price * cnt;
        double orderFullPrice = coinPrice * insMarketDTO.getCnt();

        double newFullPrice = oldFullPrice + orderFullPrice;
        double newCnt = BigDecimal.valueOf(cnt).add(BigDecimal.valueOf(insMarketDTO.getCnt())).doubleValue();
        double newPrice = newFullPrice / newCnt;

        double newCleanPrice;
        String marginSlct = marketEntity.getMarginSlct();
        if ("l".equals(marginSlct)) {
            newCleanPrice = newPrice - newPrice * (((double) 100 / margin) / 100);
        } else if ("s".equals(marginSlct)) {
            newCleanPrice = newPrice + newPrice * (((double) 100 / margin) / 100);
        } else {
            throw new RuntimeException();
        }

        marketEntity.marketAdjust(newPrice, newCnt, newCleanPrice);

        marketRepository.save(marketEntity);

        logService.market(UserDTO.of(userEntity), MarketDTO.of(marketEntity), "buy");

        WalletEntity walletEntity = userEntity.getWalletEntity();
        walletEntity.buy(orderFullPrice);
        userEntity.setWalletEntity(walletEntity);
        userRepository.save(userEntity);
        //차액 지급
//        WalletEntity walletEntity = userEntity.getWalletEntity();
//        walletEntity.sell(insMarketDTO.getPrice() * insMarketDTO.getCnt() - coinPrice * insMarketDTO.getCnt());
//        userEntity.setWalletEntity(walletEntity);
//        userRepository.save(userEntity);
    }

    private void marketSell(InsMarketDTO insMarketDTO, UserEntity userEntity, MarketEntity marketEntity, Double coinPrice) {
        if (Integer.compare(insMarketDTO.getMargin(), marketEntity.getMargin()) != 0) {
            throw new RuntimeException();
        }

        Integer margin = marketEntity.getMargin();
        Double price = marketEntity.getPrice();
        Double cnt = marketEntity.getCnt();
        String marginSlct = marketEntity.getMarginSlct();

        double orderFullPrice = coinPrice * insMarketDTO.getCnt();

        if (Double.compare(insMarketDTO.getCnt(), marketEntity.getCnt()) > 0) {
            throw new RuntimeException();
        } else if (Double.compare(insMarketDTO.getCnt(), marketEntity.getCnt()) == 0) {
            marketRepository.delete(marketEntity);
            logService.market(UserDTO.of(userEntity), MarketDTO.of(marketEntity), "sell");
        } else {
            double newCnt = BigDecimal.valueOf(cnt).subtract(BigDecimal.valueOf(insMarketDTO.getCnt())).doubleValue();
            marketEntity.sell(newCnt);
            marketRepository.save(marketEntity);
            logService.market(UserDTO.of(userEntity), MarketDTO.of(marketEntity), "sell");
        }

        Double resultPrice;
        if ("l".equals(marginSlct)) {
            resultPrice = (marketEntity.getPrice() * insMarketDTO.getCnt()) + ((coinPrice - marketEntity.getPrice()) * marketEntity.getMargin() * insMarketDTO.getCnt());
        } else if ("s".equals(marginSlct)) {
            resultPrice = (marketEntity.getPrice() * insMarketDTO.getCnt()) + ((marketEntity.getPrice() - coinPrice) * marketEntity.getMargin() * insMarketDTO.getCnt());
        } else {
            throw new RuntimeException();
        }

        WalletEntity walletEntity = userEntity.getWalletEntity();
        walletEntity.sell(resultPrice);
        userEntity.setWalletEntity(walletEntity);
        userRepository.save(userEntity);
    }

    private void marketBuy(InsMarketDTO insMarketDTO, UserEntity userEntity, Double coinPrice) {
        double newCleanPrice;
        String marginSlct = insMarketDTO.getMarginSlct();
        if ("l".equals(marginSlct)) {
            newCleanPrice = coinPrice - coinPrice * (((double) 100 / insMarketDTO.getMargin()) / 100);
        } else if ("s".equals(marginSlct)) {
            newCleanPrice = coinPrice + coinPrice * (((double) 100 / insMarketDTO.getMargin()) / 100);
        } else {
            throw new RuntimeException();
        }

        MarketEntity marketEntity = MarketEntity.builder()
                .coinSlct(insMarketDTO.getCoinSlct())
                .marginSlct(insMarketDTO.getMarginSlct())
                .margin(insMarketDTO.getMargin())
                .price(coinPrice)
                .cnt(insMarketDTO.getCnt())
                .cleanPrice(newCleanPrice)
                .userEntity(userEntity)
                .build();

        marketRepository.save(marketEntity);

        logService.market(UserDTO.of(userEntity), MarketDTO.of(marketEntity), "buy");

        WalletEntity walletEntity = userEntity.getWalletEntity();
        walletEntity.buy(coinPrice * insMarketDTO.getCnt());
        userEntity.setWalletEntity(walletEntity);
        userRepository.save(userEntity);
        //차액 지급
//        WalletEntity walletEntity = userEntity.getWalletEntity();
//        walletEntity.sell(insMarketDTO.getPrice() * insMarketDTO.getCnt() - coinPrice * insMarketDTO.getCnt());
//        userEntity.setWalletEntity(walletEntity);
//        userRepository.save(userEntity);
    }
}
