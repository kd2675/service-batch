package com.service.batch.cron.writer;

import com.service.batch.cron.common.BasicWriter;
import com.service.batch.database.crawling.entity.CoinEntity;
import com.service.batch.database.crawling.entity.HotdealEntity;
import com.service.batch.database.crawling.entity.MattermostSentEntity;
import com.service.batch.database.crawling.entity.NewsEntity;
import com.service.batch.database.crawling.repository.MattermostSentREP;
import com.service.batch.utils.MattermostUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Queue;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class MattermostWriter {
    public static final String SEND_NEWS_AND_SAVE_MATTERMOST_SENT = "sendNewsAndSaveMattermostSent";
    public static final String SEND_NEWS_FLASH_AND_SAVE_MATTERMOST_SENT = "sendNewsFlashAndSaveMattermostSent";
    public static final String SEND_NEWS_MARKETING_AND_SAVE_MATTERMOST_SENT = "sendNewsMarketingAndSaveMattermostSent";
    public static final String SEND_NEWS_STOCK_AND_SAVE_MATTERMOST_SENT = "sendNewsStockAndSaveMattermostSent";
    public static final String SEND_HOTDEAL_AND_SAVE_MATTERMOST_SENT = "sendHotdealAndSaveMattermostSent";
    public static final String SEND_COIN_AND_SAVE_MATTERMOST_SENT = "sendCoinAndSaveMattermostSent";
    public static final String DEL_MATTERMOST_UTIL_BY_ID = "delMattermostUtilById";
    public static final String DEL_ALL_MATTERMOST_SENT = "delAllMattermostSent";

    private final MattermostSentREP mattermostSentREP;
    private final MattermostUtil mattermostUtil;

    @Bean(name = SEND_NEWS_AND_SAVE_MATTERMOST_SENT)
    @StepScope
    public BasicWriter<NewsEntity> sendNewsAndSaveMattermostSent() {
        return new BasicWriter<NewsEntity>() {
            @Override
            public void write(Chunk<? extends NewsEntity> chunk) throws Exception {
                mattermostSentREP.save(MattermostSentEntity.builder()
                        .sentId(
                                mattermostUtil.sendNewsChannel(convertNewsMattermostMessage(chunk))
                                        .getBody()
                                        .getId()
                        )
                        .category("news")
                        .build()
                );
            }
        };
    }

    @Bean(name = SEND_NEWS_FLASH_AND_SAVE_MATTERMOST_SENT)
    @StepScope
    public BasicWriter<NewsEntity> sendNewsFlashAndSaveMattermostSent() {
        return new BasicWriter<NewsEntity>() {
            @Override
            public void write(Chunk<? extends NewsEntity> chunk) throws Exception {
                mattermostSentREP.save(MattermostSentEntity.builder()
                        .sentId(
                                mattermostUtil.sendNewsFlashChannel(convertNewsMattermostMessage(chunk))
                                        .getBody()
                                        .getId()
                        )
                        .category("news")
                        .build()
                );
            }
        };
    }

    @Bean(name = SEND_NEWS_MARKETING_AND_SAVE_MATTERMOST_SENT)
    @StepScope
    public BasicWriter<NewsEntity> sendNewsMarketingAndSaveMattermostSent() {
        return new BasicWriter<NewsEntity>() {
            @Override
            public void write(Chunk<? extends NewsEntity> chunk) throws Exception {
                mattermostSentREP.save(MattermostSentEntity.builder()
                        .sentId(
                                mattermostUtil.sendNewsMarketingChannel(convertNewsMattermostMessage(chunk))
                                        .getBody()
                                        .getId()
                        )
                        .category("news")
                        .build()
                );
            }
        };
    }

    @Bean(name = SEND_NEWS_STOCK_AND_SAVE_MATTERMOST_SENT)
    @StepScope
    public BasicWriter<NewsEntity> sendNewsStockAndSaveMattermostSent() {
        return new BasicWriter<NewsEntity>() {
            @Override
            public void write(Chunk<? extends NewsEntity> chunk) throws Exception {
                mattermostSentREP.save(MattermostSentEntity.builder()
                        .sentId(
                                mattermostUtil.sendNewsStockChannel(convertNewsMattermostMessage(chunk))
                                        .getBody()
                                        .getId()
                        )
                        .category("news")
                        .build()
                );
            }
        };
    }

    @Bean(name = SEND_HOTDEAL_AND_SAVE_MATTERMOST_SENT)
    @StepScope
    public BasicWriter<HotdealEntity> sendHotdealAndSaveMattermostSent() {
        return new BasicWriter<HotdealEntity>() {
            @Override
            public void write(Chunk<? extends HotdealEntity> chunk) throws Exception {
                mattermostSentREP.save(MattermostSentEntity.builder()
                        .sentId(
                                mattermostUtil.sendHotdealChannel(convertHotdealMattermostMessage(chunk))
                                        .getBody()
                                        .getId()
                        )
                        .category("hotdeal")
                        .build()
                );
            }
        };
    }

    @Bean(name = SEND_COIN_AND_SAVE_MATTERMOST_SENT)
    @StepScope
    public BasicWriter<CoinEntity> sendCoinAndSaveMattermostSent() {
        return new BasicWriter<CoinEntity>() {
            @Override
            public void write(Chunk<? extends CoinEntity> chunk) throws Exception {
                mattermostSentREP.save(
                        MattermostSentEntity.builder()
                                .sentId(
                                        mattermostUtil.sendCoinChannel(convertCoinMattermostMessageCol(chunk))
                                                .getBody()
                                                .getId()
                                )
                                .category("coin")
                                .build()
                );
            }
        };
    }

    @Bean(name = DEL_MATTERMOST_UTIL_BY_ID)
    @StepScope
    public BasicWriter<MattermostSentEntity> delMattermostUtilById() {
        return new BasicWriter<MattermostSentEntity>() {
            @Override
            public void write(Chunk<? extends MattermostSentEntity> chunk) throws Exception {
                chunk.forEach(v -> mattermostUtil.delete(v.getSentId()));
            }
        };
    }

    @Bean(name = DEL_ALL_MATTERMOST_SENT)
    @StepScope
    public ItemWriter<MattermostSentEntity> delAllMattermostSent() {
        return new BasicWriter<MattermostSentEntity>() {
            @Override
            public void write(Chunk<? extends MattermostSentEntity> chunk) throws Exception {
                mattermostSentREP.deleteAll(chunk);
            }
        };
    }

    public String convertNewsMattermostMessage(Chunk<? extends NewsEntity> entityList) {
        StringBuilder result = new StringBuilder();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String regexEmojis = "[\uD83C-\uDBFF\uDC00-\uDFFF]+";

        String header = "| 시각 | 제목 |\n";
        String line = "| :-:|:--: |\n";
//        String header = "| 시각 | 제목 | 시각 | 제목 |\n";
//        String line = "| :-:|:--:|:-:|:--: |\n";
        result.append(header)
                .append(line);


        Queue<NewsEntity> q = new LinkedList<>(entityList.getItems());
        while (!q.isEmpty()) {
            String content = "";
            for (int i = 0; i < 1; i++) {
                if (q.isEmpty()) {
                    break;
                }
                NewsEntity remove = q.remove();

                content += "| " + dtf.format(remove.getPubDate())
                        + " | " + "[" + remove.getTitle().replaceAll(regexEmojis, "")
                        .replace("[", "")
                        .replace("]", "")
                        .replace("♥", "")
                        .replace("|", "") + "]" + "(" + remove.getLink() + ")";
            }
            content += " |\n";
            result.append(content);
        }

        return result.toString();
    }

    private String convertHotdealMattermostMessage(Chunk<? extends HotdealEntity> entityList) {
        StringBuilder result = new StringBuilder();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String regexEmojis = "[\uD83C-\uDBFF\uDC00-\uDFFF]+";

        String header = "| 시각 | img | 제목 | 가격 | 브랜드 |\n";
        String line = "| :--:|:--:|:----:|:--:|:--: |\n";
//        String header = "| 시각 | 제목 | 시각 | 제목 |\n";
//        String line = "| :-:|:--:|:-:|:--: |\n";
        result.append(header)
                .append(line);


        Queue<HotdealEntity> q = new LinkedList<>(entityList.getItems());
        while (!q.isEmpty()) {
            StringBuilder content = new StringBuilder();
            for (int i = 0; i < 1; i++) {
                if (q.isEmpty()) {
                    break;
                }
                HotdealEntity remove = q.remove();

                content.append("| ")
                        .append(dtf.format(remove.getCreateDate()))
                        .append(" | ")

                        .append(remove.getImgUrl100X100())
                        .append(" | ")

                        .append("[")
                        .append(remove.getTitle().replaceAll(regexEmojis, "")
                                .replace("[", "")
                                .replace("]", "")
                                .replace("♥", "")
                                .replace("|", ""))
                        .append("]")
                        .append("(")
                        .append(remove.getLink())
                        .append(")")
                        .append(" | ")

                        .append(remove.getPriceStr())
                        .append(" | ")

                        .append(remove.getShop());
            }
            content.append(" |\n");
            result.append(content);
        }

        return result.toString();
    }

    public String convertCoinMattermostMessage(Chunk<? extends CoinEntity> entityList) {
        StringBuilder result = new StringBuilder();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        String header = "| 시각 | 제목 | 현재가격 | 최대가격 | 최소가격 | 24H 변동률(%) | 24H 변동률 | 오늘 거래량 | 24H 거래량 | 오늘 거래금액 | 24H 거래금액 |\n";
        String line = "| :-:|:--:|:-:|:--:|:--:|:--:|:--:|:--:|:--:|:--:|:--: |\n";
        result.append(header)
                .append(line);


        Queue<CoinEntity> q = new LinkedList<>(entityList.getItems());
        while (!q.isEmpty()) {
            String content = "";
            if (q.isEmpty()) {
                break;
            }
            CoinEntity remove = q.remove();

            content += "| " + dtf.format(remove.getCreateDate())
                    + " | " + "비트코인"
                    + " | " + remove.getClosingPrice()
                    + " | " + remove.getMaxPrice()
                    + " | " + remove.getMinPrice()
                    + " | " + remove.getFluctateRate24H()
                    + " | " + remove.getFluctate24H()
                    + " | " + remove.getUnitsTraded()
                    + " | " + remove.getUnitsTraded24H()
                    + " | " + remove.getAccTradeValue()
                    + " | " + remove.getAccTradeValue24H();
            content += " |\n";
            result.append(content);
        }

        return result.toString();
    }

    public String convertCoinMattermostMessageCol(Chunk<? extends CoinEntity> entityList) {
        NumberFormat numberFormat = NumberFormat.getIntegerInstance(Locale.KOREA);

        StringBuilder result = new StringBuilder();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        DateTimeFormatter dtfD = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter dtfT = DateTimeFormatter.ofPattern("HH:mm:ss");

        StringBuilder line = new StringBuilder("| 날짜");
        StringBuilder line0 = new StringBuilder("| :-:");
        StringBuilder line1 = new StringBuilder("| 시각");
//        String line = "| :-:|:--:|:-:|:--:|:--:|:--:|:--:|:--:|:--:|:--:|:--: |\n";
        StringBuilder line2 = new StringBuilder("| 현재가격");
        StringBuilder line3 = new StringBuilder("| 최대가격");
        StringBuilder line4 = new StringBuilder("| 최소가격");
        StringBuilder line5 = new StringBuilder("| 24H 변동률(%)");
        StringBuilder line6 = new StringBuilder("| 24H 변동률");
        StringBuilder line7 = new StringBuilder("| 오늘 거래량");
        StringBuilder line8 = new StringBuilder("| 24H 거래량");
        StringBuilder line9 = new StringBuilder("| 오늘 거래금액");
        StringBuilder line10 = new StringBuilder("| 24H 거래금액");

//        String header = "| 시각 | 제목 | 현재가격 | 최대가격 | 최소가격 | 24H 변동률(%) | 24H 변동률 | 오늘 거래량 | 24H 거래량 | 오늘 거래금액 | 24H 거래금액 |\n";
//        String line = "| :-:|:--:|:-:|:--:|:--:|:--:|:--:|:--:|:--:|:--:|:--: |\n";
//        result.append(header)
//                .append(line);


        Queue<CoinEntity> q = new LinkedList<>(entityList.getItems());
        while (!q.isEmpty()) {
//            String content = "";
            if (q.isEmpty()) {
                break;
            }
            CoinEntity remove = q.remove();
            line.append(" | ").append(dtfD.format(remove.getCreateDate()));
            line0.append("|:-:");
            line1.append(" | ").append(dtfT.format(remove.getCreateDate()));
            line2.append(" | ").append(numberFormat.format(Integer.parseInt(remove.getClosingPrice())));
            line3.append(" | ").append(numberFormat.format(Integer.parseInt(remove.getMaxPrice())));
            line4.append(" | ").append(numberFormat.format(Integer.parseInt(remove.getMinPrice())));
            line5.append(" | ").append(remove.getFluctateRate24H()+"%");
            line6.append(" | ").append(numberFormat.format(Integer.parseInt(remove.getFluctate24H())));
            line7.append(" | ").append(numberFormat.format(Math.round(Double.parseDouble(remove.getUnitsTraded()))));
            line8.append(" | ").append(numberFormat.format(Math.round(Double.parseDouble(remove.getUnitsTraded24H()))));
            line9.append(" | ").append(numberFormat.format(Math.round(Double.parseDouble(remove.getAccTradeValue()))));
            line10.append(" | ").append(numberFormat.format(Math.round(Double.parseDouble(remove.getAccTradeValue24H()))));
//
//            content += "| " + dtf.format(remove.getCreateDate())
//                    + " | " + "비트코인"
//                    + " | " + remove.getClosingPrice()
//                    + " | " + remove.getMaxPrice()
//                    + " | " + remove.getMinPrice()
//                    + " | " + remove.getFluctateRate24H()
//                    + " | " + remove.getFluctate24H()
//                    + " | " + remove.getUnitsTraded()
//                    + " | " + remove.getUnitsTraded24H()
//                    + " | " + remove.getAccTradeValue()
//                    + " | " + remove.getAccTradeValue24H();
//            content += " |\n";
        }
        line.append(" |\n");
        line0.append(" |\n");
        line1.append(" |\n");
        line2.append(" |\n");
        line3.append(" |\n");
        line4.append(" |\n ");
        line5.append(" |\n ");
        line6.append(" |\n ");
        line7.append(" |\n ");
        line8.append(" |\n ");
        line9.append(" |\n ");
        line10.append(" |\n ");

        result.append(line)
                .append(line0)
                .append(line1)
                .append(line2)
                .append(line3)
                .append(line4)
                .append(line5)
                .append(line6)
                .append(line7)
                .append(line8)
                .append(line9)
                .append(line10);

        return result.toString();
    }
}
