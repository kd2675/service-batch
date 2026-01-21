package com.service.batch.service.stock.biz;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.service.batch.service.stock.vo.StockHistoryDTO;
import com.service.batch.service.stock.vo.StockPriceDTO;
import com.service.batch.service.stock.vo.StockSearchDTO;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String YAHOO_QUOTE_URL = "https://query1.finance.yahoo.com/v8/finance/chart/";
    private static final String YAHOO_SEARCH_URL = "https://query2.finance.yahoo.com/v1/finance/search";

    // 캐시 관련 필드 추가
    private final Map<String, List<StockSearchDTO>> searchCache = new ConcurrentHashMap<>();
    private final Map<String, LocalDateTime> searchCacheTimestamps = new ConcurrentHashMap<>();
    private final Map<String, StockPriceDTO> priceCache = new ConcurrentHashMap<>();
    private final Map<String, LocalDateTime> priceCacheTimestamps = new ConcurrentHashMap<>();
    private final Map<String, List<StockHistoryDTO>> historyCache = new ConcurrentHashMap<>();
    private final Map<String, LocalDateTime> historyCacheTimestamps = new ConcurrentHashMap<>();

    private static final Duration SEARCH_CACHE_DURATION = Duration.ofMinutes(30); // 검색 결과는 30분 캐시
    private static final Duration PRICE_CACHE_DURATION = Duration.ofMinutes(5);
    private static final Duration HISTORY_CACHE_DURATION = Duration.ofHours(1);


    // 캐시 정리 메소드 업데이트
    @PostConstruct
    public void initCacheCleanup() {
        // 1시간마다 만료된 캐시 정리
        ScheduledExecutorService cacheCleanupExecutor = Executors.newScheduledThreadPool(1);
        cacheCleanupExecutor.scheduleAtFixedRate(this::cleanupExpiredCache, 1, 1, TimeUnit.HOURS);
    }

    private void cleanupExpiredCache() {
        LocalDateTime now = LocalDateTime.now();

        // 가격 캐시 정리
        priceCacheTimestamps.entrySet().removeIf(entry -> {
            if (entry.getValue().isBefore(now.minus(PRICE_CACHE_DURATION.multipliedBy(2)))) {
                priceCache.remove(entry.getKey());
                return true;
            }
            return false;
        });

        // 히스토리 캐시 정리
        historyCacheTimestamps.entrySet().removeIf(entry -> {
            if (entry.getValue().isBefore(now.minus(HISTORY_CACHE_DURATION.multipliedBy(2)))) {
                historyCache.remove(entry.getKey());
                return true;
            }
            return false;
        });

        // 검색 캐시 정리
        searchCacheTimestamps.entrySet().removeIf(entry -> {
            if (entry.getValue().isBefore(now.minus(SEARCH_CACHE_DURATION.multipliedBy(2)))) {
                searchCache.remove(entry.getKey());
                return true;
            }
            return false;
        });

        log.info("Cache cleanup completed. Price cache: {}, History cache: {}, Search cache: {}",
                priceCache.size(), historyCache.size(), searchCache.size());
    }

    // 캐시 통계 조회 메소드 (디버깅용)
    public void logCacheStats() {
        log.info("=== Cache Statistics ===");
        log.info("Search Cache: {} entries", searchCache.size());
        log.info("Price Cache: {} entries", priceCache.size());
        log.info("History Cache: {} entries", historyCache.size());
    }

    // 나머지 메소드들...
    public StockPriceDTO getStockPrice(String symbol) {
        // 이전에 수정한 코드와 동일
        // 캐시 확인
        LocalDateTime cacheTime = priceCacheTimestamps.get(symbol);
        if (cacheTime != null && cacheTime.isAfter(LocalDateTime.now().minus(PRICE_CACHE_DURATION))) {
            log.info("Returning cached price data for symbol: {}", symbol);
            return priceCache.get(symbol);
        }

        try {
            String url = YAHOO_QUOTE_URL + symbol;

            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
            headers.set("Accept", "application/json");
            headers.set("Accept-Language", "en-US,en;q=0.9");
            HttpEntity<String> entity = new HttpEntity<>(headers);

            Thread.sleep(2000);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode result = root.path("chart").path("result").get(0);
            JsonNode meta = result.path("meta");

            String currency = meta.path("currency").asText();
            BigDecimal currentPrice = BigDecimal.valueOf(meta.path("regularMarketPrice").asDouble())
                    .setScale(2, RoundingMode.HALF_UP);
            BigDecimal previousClose = BigDecimal.valueOf(meta.path("previousClose").asDouble())
                    .setScale(2, RoundingMode.HALF_UP);

            BigDecimal change = currentPrice.subtract(previousClose);
            BigDecimal changePercent = change.divide(previousClose, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));

            StockPriceDTO stockPriceDTO = StockPriceDTO.builder()
                    .symbol(symbol.toUpperCase())
                    .price(currentPrice)
                    .change(change)
                    .changePercent(changePercent)
                    .currency(currency)
                    .marketCap(meta.path("marketCap").asLong())
                    .volume(meta.path("regularMarketVolume").asLong())
                    .timestamp(LocalDateTime.now())
                    .build();

            priceCache.put(symbol, stockPriceDTO);
            priceCacheTimestamps.put(symbol, LocalDateTime.now());

            return stockPriceDTO;

        } catch (HttpClientErrorException.TooManyRequests e) {
            log.warn("Rate limit exceeded for symbol: {}. Returning cached data if available.", symbol);
            return priceCache.get(symbol);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Thread interrupted", e);
        } catch (Exception e) {
            log.error("Error fetching stock price for symbol: {}", symbol, e);
            StockPriceDTO cachedData = priceCache.get(symbol);
            if (cachedData != null) {
                log.info("Returning cached data due to API error for symbol: {}", symbol);
                return cachedData;
            }
            throw new RuntimeException("Failed to fetch stock price: " + e.getMessage());
        }
    }



    // getMultipleStockPrices도 개선
    public List<StockPriceDTO> getMultipleStockPrices(List<String> symbols) {
        List<StockPriceDTO> results = new ArrayList<>();
        for (String symbol : symbols) {
            try {
                // 각 요청 간 지연 시간 추가
                if (!results.isEmpty()) {
                    Thread.sleep(2000); // 2초 대기
                }

                StockPriceDTO price = getStockPrice(symbol);
                if (price != null) {
                    results.add(price);
                }
            } catch (Exception e) {
                log.warn("Failed to fetch price for symbol: {}", symbol, e);
            }
        }
        return results;
    }


    public List<StockHistoryDTO> getStockHistory(String symbol, String period) {
        String cacheKey = symbol + "_" + period;

        // 캐시 확인
        LocalDateTime cacheTime = historyCacheTimestamps.get(cacheKey);
        if (cacheTime != null && cacheTime.isAfter(LocalDateTime.now().minus(HISTORY_CACHE_DURATION))) {
            log.info("Returning cached history data for symbol: {} period: {}", symbol, period);
            return historyCache.get(cacheKey);
        }

        try {
            String url = YAHOO_QUOTE_URL + symbol + "?range=" + period + "&interval=1d";

            // User-Agent 헤더 추가
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
            headers.set("Accept", "application/json");
            headers.set("Accept-Language", "en-US,en;q=0.9");
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // 요청 간 지연 시간 추가 (3초)
            Thread.sleep(3000);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode result = root.path("chart").path("result").get(0);

            JsonNode timestamps = result.path("timestamp");
            JsonNode indicators = result.path("indicators").path("quote").get(0);
            JsonNode opens = indicators.path("open");
            JsonNode highs = indicators.path("high");
            JsonNode lows = indicators.path("low");
            JsonNode closes = indicators.path("close");
            JsonNode volumes = indicators.path("volume");

            List<StockHistoryDTO> history = new ArrayList<>();
            for (int i = 0; i < timestamps.size(); i++) {
                if (!opens.get(i).isNull()) {
                    StockHistoryDTO dto = StockHistoryDTO.builder()
                            .timestamp(timestamps.get(i).asLong())
                            .open(BigDecimal.valueOf(opens.get(i).asDouble()))
                            .high(BigDecimal.valueOf(highs.get(i).asDouble()))
                            .low(BigDecimal.valueOf(lows.get(i).asDouble()))
                            .close(BigDecimal.valueOf(closes.get(i).asDouble()))
                            .volume(volumes.get(i).asLong())
                            .build();
                    history.add(dto);
                }
            }

            // 캐시에 저장
            historyCache.put(cacheKey, history);
            historyCacheTimestamps.put(cacheKey, LocalDateTime.now());

            return history;

        } catch (HttpClientErrorException.TooManyRequests e) {
            log.warn("Rate limit exceeded for symbol: {} period: {}. Returning cached data if available.", symbol, period);
            List<StockHistoryDTO> cachedData = historyCache.get(cacheKey);
            return cachedData != null ? cachedData : new ArrayList<>();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Thread interrupted", e);
        } catch (Exception e) {
            log.error("Error fetching stock history for symbol: {} period: {}", symbol, period, e);
            // 에러 시에도 캐시된 데이터가 있으면 반환
            List<StockHistoryDTO> cachedData = historyCache.get(cacheKey);
            if (cachedData != null && !cachedData.isEmpty()) {
                log.info("Returning cached data due to API error for symbol: {} period: {}", symbol, period);
                return cachedData;
            }
            throw new RuntimeException("Failed to fetch stock history: " + e.getMessage());
        }
    }


    // 새로 추가: 주식 검색 기능 (캐시 적용)
    public List<StockSearchDTO> searchStocks(String query) {
        String cacheKey = query.toLowerCase().trim(); // 대소문자 구분 없이 캐시

        // 캐시 확인
        LocalDateTime cacheTime = searchCacheTimestamps.get(cacheKey);
        if (cacheTime != null && cacheTime.isAfter(LocalDateTime.now().minus(SEARCH_CACHE_DURATION))) {
            log.info("Returning cached search data for query: {}", query);
            return searchCache.get(cacheKey);
        }

        try {
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String url = YAHOO_SEARCH_URL + "?q=" + encodedQuery + "&quotesCount=20&newsCount=0";

            // User-Agent 헤더 추가
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
            headers.set("Accept", "application/json");
            headers.set("Accept-Language", "en-US,en;q=0.9");
            headers.set("Referer", "https://finance.yahoo.com/");
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // 요청 간 지연 시간 추가 (3초)
            Thread.sleep(3000);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode quotes = root.path("quotes");

            List<StockSearchDTO> results = new ArrayList<>();

            for (JsonNode quote : quotes) {
                // 주식만 필터링 (ETF, 뮤추얼펀드, 크립토 등 제외 가능)
                String quoteType = quote.path("quoteType").asText();
                if ("EQUITY".equals(quoteType) || "ETF".equals(quoteType)) {
                    StockSearchDTO dto = StockSearchDTO.builder()
                            .symbol(quote.path("symbol").asText())
                            .shortName(quote.path("shortname").asText())
                            .longName(quote.path("longname").asText())
                            .market(quote.path("market").asText())
                            .exchange(quote.path("exchange").asText())
                            .quoteType(quoteType)
                            .typeDisp(quote.path("typeDisp").asText())
                            .build();
                    results.add(dto);
                }
            }

            // 캐시에 저장
            searchCache.put(cacheKey, results);
            searchCacheTimestamps.put(cacheKey, LocalDateTime.now());

            log.info("Search completed for query: {} - {} results found", query, results.size());

            return results;

        } catch (HttpClientErrorException.TooManyRequests e) {
            log.warn("Rate limit exceeded for query: {}. Returning cached data if available.", query);
            List<StockSearchDTO> cachedData = searchCache.get(cacheKey);
            return cachedData != null ? cachedData : new ArrayList<>();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Thread interrupted", e);
        } catch (Exception e) {
            log.error("Error searching stocks with query: {}", query, e);
            // 에러 시에도 캐시된 데이터가 있으면 반환
            List<StockSearchDTO> cachedData = searchCache.get(cacheKey);
            if (cachedData != null && !cachedData.isEmpty()) {
                log.info("Returning cached data due to API error for query: {}", query);
                return cachedData;
            }
            // 빈 리스트 반환 (예외 던지지 않음)
            return new ArrayList<>();
        }
    }


    // 인기 주식 목록 제공
    public List<StockSearchDTO> getPopularStocks() {
        List<String> popularSymbols = List.of(
                "AAPL", "MSFT", "GOOGL", "AMZN", "TSLA",
                "META", "NVDA", "NFLX", "DIS", "BABA"
        );

        List<StockSearchDTO> results = new ArrayList<>();
        for (String symbol : popularSymbols) {
            try {
                // 간단한 정보만 제공
                results.add(StockSearchDTO.builder()
                        .symbol(symbol)
                        .shortName(getCompanyName(symbol))
                        .quoteType("EQUITY")
                        .build());
            } catch (Exception e) {
                log.warn("Failed to get info for popular stock: {}", symbol);
            }
        }
        return results;
    }

    private String getCompanyName(String symbol) {
        switch (symbol) {
            case "AAPL": return "Apple Inc.";
            case "MSFT": return "Microsoft Corporation";
            case "GOOGL": return "Alphabet Inc.";
            case "AMZN": return "Amazon.com Inc.";
            case "TSLA": return "Tesla Inc.";
            case "META": return "Meta Platforms Inc.";
            case "NVDA": return "NVIDIA Corporation";
            case "NFLX": return "Netflix Inc.";
            case "DIS": return "The Walt Disney Company";
            case "BABA": return "Alibaba Group Holding";
            default: return symbol;
        }
    }
}