package com.service.batch.api.lotto;

import com.service.batch.service.lotto.biz.LottoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 로또 자동화 테스트 API Controller
 * 
 * 테스트 엔드포인트:
 * - POST /lotto/api/account - 예치금 조회
 * - POST /lotto/api/buy - 로또 구매
 * - POST /lotto/api/check - 당첨 확인
 */
@Slf4j
@RestController
@RequestMapping("/lotto/api")
@RequiredArgsConstructor
public class LottoController {
    
    private final LottoService lottoService;
    
    /**
     * 예치금 잔액 조회 API
     * 마이페이지에서 예치금 정보를 조회합니다
     * 
     * @return ResponseEntity 성공/실패 메시지
     */
    @PostMapping("/account")
    public ResponseEntity<?> checkAccount() {
        try {
            log.info("🔍 예치금 조회 시작...");
            lottoService.account();
            return ResponseEntity.ok()
                    .body(new ApiResponse("SUCCESS", "예치금 조회가 완료되었습니다"));
        } catch (Exception e) {
            log.error("❌ 예치금 조회 실패", e);
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse("FAILED", "예치금 조회 중 오류 발생: " + e.getMessage()));
        }
    }
    
    /**
     * 로또 구매 API
     * 메인 페이지에서 로또645 바로구매 버튼 클릭 후 자동 구매
     * 
     * @return ResponseEntity 성공/실패 메시지
     */
    @PostMapping("/buy")
    public ResponseEntity<?> buyLottery() {
        try {
            log.info("🎫 로또 구매 시작...");
            lottoService.buy();
            return ResponseEntity.ok()
                    .body(new ApiResponse("SUCCESS", "로또 구매가 완료되었습니다"));
        } catch (Exception e) {
            log.error("❌ 로또 구매 실패", e);
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse("FAILED", "로또 구매 중 오류 발생: " + e.getMessage()));
        }
    }
    
    /**
     * 당첨 확인 API
     * 구매 내역 조회 및 당첨 번호와 비교
     * 
     * @return ResponseEntity 성공/실패 메시지
     */
    @PostMapping("/check")
    public ResponseEntity<?> checkWinning() {
        try {
            log.info("🎊 당첨 확인 시작...");
            lottoService.check();
            return ResponseEntity.ok()
                    .body(new ApiResponse("SUCCESS", "당첨 확인이 완료되었습니다"));
        } catch (Exception e) {
            log.error("❌ 당첨 확인 실패", e);
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse("FAILED", "당첨 확인 중 오류 발생: " + e.getMessage()));
        }
    }
    
    /**
     * 헬스 체크 API
     * Lotto API 서버 상태 확인
     * 
     * @return ResponseEntity 서버 상태
     */
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok()
                .body(new ApiResponse("ACTIVE", "🚀 Lotto API Server is running"));
    }
    
    /**
     * API 응답 VO
     */
    static class ApiResponse {
        public String status;
        public String message;
        public long timestamp;
        
        public ApiResponse(String status, String message) {
            this.status = status;
            this.message = message;
            this.timestamp = System.currentTimeMillis();
        }
        
        public String getStatus() {
            return status;
        }
        
        public String getMessage() {
            return message;
        }
        
        public long getTimestamp() {
            return timestamp;
        }
    }
}
