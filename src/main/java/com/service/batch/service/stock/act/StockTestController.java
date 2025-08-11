package com.service.batch.service.stock.act;


import com.service.batch.service.stock.biz.StockService;
import com.service.batch.service.stock.vo.StockHistoryDTO;
import com.service.batch.service.stock.vo.StockPriceDTO;
import com.service.batch.service.stock.vo.StockSearchDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stock")
@RequiredArgsConstructor
@Slf4j
public class StockTestController {

    private final StockService stockService;

    /**
     * 주식 검색 테스트
     * GET /api/stock/search?q=Apple
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchStocks(@RequestParam("q") String query) {
        long startTime = System.currentTimeMillis();

        try {
            List<StockSearchDTO> results = stockService.searchStocks(query);
            long endTime = System.currentTimeMillis();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("query", query);
            response.put("results", results);
            response.put("count", results.size());
            response.put("responseTime", (endTime - startTime) + "ms");
            response.put("timestamp", LocalDateTime.now());

            log.info("Stock search completed - Query: '{}', Results: {}, Time: {}ms",
                    query, results.size(), (endTime - startTime));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            long endTime = System.currentTimeMillis();

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("query", query);
            response.put("error", e.getMessage());
            response.put("responseTime", (endTime - startTime) + "ms");
            response.put("timestamp", LocalDateTime.now());

            log.error("Stock search failed - Query: '{}', Error: {}", query, e.getMessage());

            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 주식 가격 조회 테스트
     * GET /api/stock/price/AAPL
     */
    @GetMapping("/price/{symbol}")
    public ResponseEntity<Map<String, Object>> getStockPrice(@PathVariable String symbol) {
        long startTime = System.currentTimeMillis();

        try {
            StockPriceDTO result = stockService.getStockPrice(symbol);
            long endTime = System.currentTimeMillis();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("symbol", symbol);
            response.put("data", result);
            response.put("responseTime", (endTime - startTime) + "ms");
            response.put("timestamp", LocalDateTime.now());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            long endTime = System.currentTimeMillis();

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("symbol", symbol);
            response.put("error", e.getMessage());
            response.put("responseTime", (endTime - startTime) + "ms");
            response.put("timestamp", LocalDateTime.now());

            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 주식 히스토리 조회 테스트
     * GET /api/stock/history/AAPL?period=1mo
     */
    @GetMapping("/history/{symbol}")
    public ResponseEntity<Map<String, Object>> getStockHistory(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "1mo") String period) {
        long startTime = System.currentTimeMillis();

        try {
            List<StockHistoryDTO> results = stockService.getStockHistory(symbol, period);
            long endTime = System.currentTimeMillis();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("symbol", symbol);
            response.put("period", period);
            response.put("data", results);
            response.put("count", results.size());
            response.put("responseTime", (endTime - startTime) + "ms");
            response.put("timestamp", LocalDateTime.now());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            long endTime = System.currentTimeMillis();

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("symbol", symbol);
            response.put("period", period);
            response.put("error", e.getMessage());
            response.put("responseTime", (endTime - startTime) + "ms");
            response.put("timestamp", LocalDateTime.now());

            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 인기 주식 목록 조회
     * GET /api/stock/popular
     */
    @GetMapping("/popular")
    public ResponseEntity<Map<String, Object>> getPopularStocks() {
        long startTime = System.currentTimeMillis();

        try {
            List<StockSearchDTO> results = stockService.getPopularStocks();
            long endTime = System.currentTimeMillis();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", results);
            response.put("count", results.size());
            response.put("responseTime", (endTime - startTime) + "ms");
            response.put("timestamp", LocalDateTime.now());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            long endTime = System.currentTimeMillis();

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("responseTime", (endTime - startTime) + "ms");
            response.put("timestamp", LocalDateTime.now());

            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 캐시 상태 조회 (디버깅용)
     * GET /api/stock/cache/stats
     */
    @GetMapping("/cache/stats")
    public ResponseEntity<Map<String, Object>> getCacheStats() {
        try {
            stockService.logCacheStats();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Cache stats logged - check server logs");
            response.put("timestamp", LocalDateTime.now());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("timestamp", LocalDateTime.now());

            return ResponseEntity.status(500).body(response);
        }
    }
}