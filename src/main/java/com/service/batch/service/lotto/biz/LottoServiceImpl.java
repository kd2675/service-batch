package com.service.batch.service.lotto.biz;

import com.service.batch.service.webhook.api.dto.WebhookDTO;
import com.service.batch.utils.MattermostUtil;
import com.service.batch.utils.enums.ChannelEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
@RequiredArgsConstructor
public class LottoServiceImpl implements LottoService {
    private final MattermostUtil mattermostUtil;

    @Override
    public void account() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--no-sandbox");
        options.addArguments("--single-process");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--start-maximized");
//        InternetExplorerOptions options = new InternetExplorerOptions();
//        options.setCapability("ignoreProtectedModeSettings", true);
        WebDriver driver = new ChromeDriver(options);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        try {
            login(driver, wait);

            account(driver, wait);
        } catch (Exception e) {
            log.error("error > {}", e);
        } finally {
            driver.quit();
        }
    }

    @Override
    public void buy() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--no-sandbox");
        options.addArguments("--single-process");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--start-maximized");
//        InternetExplorerOptions options = new InternetExplorerOptions();
//        options.setCapability("ignoreProtectedModeSettings", true);
        WebDriver driver = new ChromeDriver(options);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        try {
            login(driver, wait);


            account(driver, wait);


            buy(driver, wait);
        } catch (Exception e) {
            log.error("error > {}", e);
        } finally {
            driver.quit();
        }
    }

    @Override
    public void check() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--no-sandbox");
        options.addArguments("--single-process");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--start-maximized");
        
        WebDriver driver = new ChromeDriver(options);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        try {
            log.info("🔍 [DEBUG] check() 시작");
            
            login(driver, wait);
            log.info("✅ [DEBUG] 로그인 완료");

            List<List<String>> result = result(driver, wait);
            log.info("✅ [DEBUG] 구매 내역 조회 완료: {}개", result.size());

            List<String> lucky = lucky(driver, wait);
            log.info("✅ [DEBUG] 당첨 번호 조회 완료: {}", lucky);

            this.checkWinning(result, lucky);
            log.info("✅ [DEBUG] check() 완료");
            
        } catch (Exception e) {
            log.error("❌ [DEBUG] check() 에러: {}", e.getMessage(), e);
        } finally {
            log.info("⏸️ [DEBUG] 브라우저 종료 대기 (10초)...");
            try {
                Thread.sleep(10000); // 10초 대기
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            driver.quit();
        }
    }

    @Override
    public void checkBob(WebhookDTO webhookDTO) {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--no-sandbox");
        options.addArguments("--single-process");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--start-maximized");

        WebDriver driver = new ChromeDriver(options);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        try {
            log.info("🔍 [DEBUG] check() 시작");

            login(driver, wait);
            log.info("✅ [DEBUG] 로그인 완료");

            List<List<String>> result = result(driver, wait);
            log.info("✅ [DEBUG] 구매 내역 조회 완료: {}개", result.size());

            List<String> lucky = lucky(driver, wait);
            log.info("✅ [DEBUG] 당첨 번호 조회 완료: {}", lucky);

            this.checkWinningWebhook(result, lucky, webhookDTO);
            log.info("✅ [DEBUG] check() 완료");

        } catch (Exception e) {
            log.error("❌ [DEBUG] check() 에러: {}", e.getMessage(), e);
        } finally {
            log.info("⏸️ [DEBUG] 브라우저 종료 대기 (10초)...");
            try {
                Thread.sleep(10000); // 10초 대기
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            driver.quit();
        }
    }



    private void login(WebDriver driver, WebDriverWait wait) {
        driver.get("https://www.dhlottery.co.kr");
        driver.navigate().to("https://www.dhlottery.co.kr/login");

        // 1. 아이디 입력
        WebElement idElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("inpUserId")));
        idElement.sendKeys("kd2675");

        // 2. 비밀번호 입력
        WebElement pwElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("inpUserPswdEncn")));
        pwElement.sendKeys("Whitered2@");

        // 3. 로그인 버튼 클릭
        WebElement loginButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("btnLogin")));
        loginButton.click();

        // 4. 로그인 완료 대기
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void account(WebDriver driver, WebDriverWait wait) {
        driver.get("https://www.dhlottery.co.kr/mypage/home");

        try {
            // 예치금 정보 조회
            // 마이페이지 로드 대기
            Thread.sleep(2000);

            // 총 예치금 추출
            WebElement totalAmtElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("totalAmt")));
            String totalAmtText = totalAmtElement.getText().replace(",", "").replace("원", "").trim();
            int balance = Integer.parseInt(totalAmtText);

            log.info("예치금 잔액: {}", balance);
            mattermostUtil.send("예치금 잔액 : " + balance, "5zqu88zsef83x8kj86igsqe1wa");

            if (1000 * 5 > balance) {
                //잔액부족
                throw new BalanceError();
            }
        } catch (InterruptedException e) {
            log.error("예치금 조회 중 오류 > {}", e);
            throw new RuntimeException(e);
        }
    }

    private void buy(WebDriver driver, WebDriverWait wait) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        
        try {
            // 1️⃣ 메인 페이지 접속
            log.info("🌐 메인 페이지 접속 중...");
            driver.get("https://www.dhlottery.co.kr/main");
            Thread.sleep(5000);  // 페이지 완전 로딩 대기

            // 2️⃣ JavaScript 함수가 로드될 때까지 대기
            log.info("⏳ gmUtil 함수 로딩 대기 중...");
            wait.until(driver1 -> {
                try {
                    Object result = js.executeScript("return typeof gmUtil !== 'undefined' && typeof gmUtil.goGameClsf === 'function';");
                    return Boolean.TRUE.equals(result);
                } catch (Exception e) {
                    return false;
                }
            });
            log.info("✅ gmUtil 함수 로드 완료!");

            // 3️⃣ 직접 JavaScript 함수 호출로 구매 페이지 이동
            log.info("🎯 로또645 구매 페이지로 이동 중...");
            try {
                // 방법 1: gmUtil.goGameClsf 직접 호출
                js.executeScript("gmUtil.goGameClsf('LO40','PRCHS');");
                log.info("✅ JavaScript 함수 호출 성공!");
            } catch (Exception e) {
                log.warn("⚠️ gmUtil 함수 호출 실패, 버튼 클릭 방식 시도: {}", e.getMessage());
                
                // 방법 2: 버튼 클릭 시도
                WebElement buyButton = findAndClickLt645Button(driver, wait, js);
            }

            // 4️⃣ 게임 페이지 로드 대기 (새 창이 열릴 수 있음)
            log.info("🔄 게임 페이지 로드 대기 중...");
            Thread.sleep(3000);
            
            // 새 창이 열렸는지 확인
            Set<String> windowHandles = driver.getWindowHandles();
            log.info("📋 현재 열린 창 개수: {}", windowHandles.size());
            
            if (windowHandles.size() > 1) {
                // 새 창으로 전환
                String mainWindow = driver.getWindowHandle();
                for (String handle : windowHandles) {
                    if (!handle.equals(mainWindow)) {
                        driver.switchTo().window(handle);
                        log.info("🔄 새 창으로 전환됨");
                        break;
                    }
                }
            }
            
            // URL 확인 (TotalGame.jsp 또는 game645 포함 확인)
            WebDriverWait longWait = new WebDriverWait(driver, Duration.ofSeconds(20));
            longWait.until(driver1 -> {
                String currentUrl = driver1.getCurrentUrl();
                boolean isCorrectPage = currentUrl.contains("TotalGame.jsp") || 
                                       currentUrl.contains("game645") ||
                                       currentUrl.contains("LottoId=LO40");
                log.debug("URL 확인 중: {} (조건 충족: {})", currentUrl, isCorrectPage);
                return isCorrectPage;
            });
            
            String finalUrl = driver.getCurrentUrl();
            log.info("✅ 게임 페이지 로드 완료! URL: {}", finalUrl);

            // 5️⃣ 팝업 처리
            handlePopups(driver, wait);

            // 6️⃣ 로또 번호 자동 생성 및 구매
            performLottoPurchase(driver, wait, js);

            log.info("🎉 로또645 구매 프로세스 완료!");
            mattermostUtil.send("✅ 로또645 구매 완료", "5zqu88zsef83x8kj86igsqe1wa");

        } catch (Exception e) {
            log.error("❌ 구매 프로세스 중 오류 발생: {}", e.getMessage(), e);
            log.error("현재 URL: {}", driver.getCurrentUrl());
            
            // 스크린샷 저장 시도 (디버깅용)
            try {
                String pageSource = driver.getPageSource();
                log.debug("페이지 소스 길이: {}", pageSource.length());
            } catch (Exception ex) {
                log.warn("페이지 소스 가져오기 실패");
            }
            
            mattermostUtil.send("❌ 로또645 구매 실패: " + e.getMessage(), "5zqu88zsef83x8kj86igsqe1wa");
            throw new RuntimeException("로또645 구매 프로세스 실패", e);
        }
    }

    /**
     * 로또645 바로구매 버튼 찾기 및 클릭
     */
    private WebElement findAndClickLt645Button(WebDriver driver, WebDriverWait wait, JavascriptExecutor js) throws Exception {
        log.info("🔍 로또645 바로구매 버튼 탐색 시작...");
        
        // 1. Swiper 초기화 대기 (추가 대기 시간)
        Thread.sleep(2000);
        log.info("⏳ Swiper 초기화 대기 완료");
        
        // 2. 페이지 상단으로 스크롤
        js.executeScript("window.scrollTo(0, 0);");
        Thread.sleep(1000);
        
        WebElement buyButton = null;
        
        // 3. 다양한 선택자로 버튼 찾기 시도
        String[] selectors = {
                ".swiper-slide-active .btnBuyLt645",           // 활성 슬라이드의 버튼 (가장 확률 높음)
                ".lt645-inbox .btnBuyLt645",                   // 로또645 영역 내의 버튼
                "button.btnBuyLt645",                          // 일반 버튼
                "#btnMoLtgmPrchs",                             // 모바일 바로구매 버튼
                "//button[contains(@class, 'btnBuyLt645')]",   // XPath로 버튼 클래스 검색
                ".lottery-box .btnBuyLt645"                    // lottery-box 내부 버튼
        };

        for (int i = 0; i < selectors.length; i++) {
            try {
                log.info("🔍 시도 {}/{}: {}", i + 1, selectors.length, selectors[i]);

                WebElement element;
                if (selectors[i].startsWith("//")) {
                    element = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(selectors[i])));
                } else {
                    element = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(selectors[i])));
                }
                
                // 요소가 실제로 표시되고 클릭 가능한지 확인
                if (element.isDisplayed() && element.isEnabled()) {
                    buyButton = element;
                    log.info("✅ 바로구매 버튼 발견! 선택자: {}", selectors[i]);
                    break;
                } else {
                    log.warn("⚠️ 버튼을 찾았으나 표시되지 않거나 비활성화됨");
                }
            } catch (Exception e) {
                log.debug("❌ 선택자 {} 실패: {}", selectors[i], e.getMessage());
                if (i == selectors.length - 1) {
                    log.error("❌ 모든 선택자로 버튼을 찾을 수 없습니다");
                    
                    // 디버깅: 현재 페이지의 버튼들 확인
                    try {
                        List<WebElement> allButtons = driver.findElements(By.tagName("button"));
                        log.info("📋 페이지에서 발견된 버튼 수: {}", allButtons.size());
                        for (int idx = 0; idx < Math.min(allButtons.size(), 10); idx++) {
                            WebElement btn = allButtons.get(idx);
                            log.info("  - 버튼 {}: class='{}', text='{}'", 
                                idx + 1, 
                                btn.getAttribute("class"), 
                                btn.getText().substring(0, Math.min(20, btn.getText().length()))
                            );
                        }
                    } catch (Exception debugEx) {
                        log.error("디버깅 실패: {}", debugEx.getMessage());
                    }
                    
                    throw new RuntimeException("로또645 바로구매 버튼을 찾을 수 없습니다", e);
                }
            }
        }

        if (buyButton == null) {
            throw new RuntimeException("바로구매 버튼이 null입니다");
        }

        // 4. 버튼 클릭 시도
        log.info("📍 바로구매 버튼 클릭 시도...");
        boolean clickSuccess = false;
        
        // 방법 1: JavaScript 스크롤 후 클릭
        try {
            js.executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", buyButton);
            Thread.sleep(1000);
            js.executeScript("arguments[0].click();", buyButton);
            clickSuccess = true;
            log.info("✅ JavaScript 클릭 성공!");
        } catch (Exception e) {
            log.warn("⚠️ JavaScript 클릭 실패: {}", e.getMessage());
        }
        
        // 방법 2: Actions 클래스로 클릭
        if (!clickSuccess) {
            try {
                org.openqa.selenium.interactions.Actions actions = new org.openqa.selenium.interactions.Actions(driver);
                actions.moveToElement(buyButton).click().perform();
                clickSuccess = true;
                log.info("✅ Actions 클릭 성공!");
            } catch (Exception e) {
                log.warn("⚠️ Actions 클릭 실패: {}", e.getMessage());
            }
        }
        
        // 방법 3: 일반 클릭
        if (!clickSuccess) {
            try {
                buyButton.click();
                clickSuccess = true;
                log.info("✅ 일반 클릭 성공!");
            } catch (Exception e) {
                log.error("❌ 모든 클릭 방법 실패: {}", e.getMessage());
                throw new RuntimeException("버튼 클릭 실패", e);
            }
        }

        Thread.sleep(3000);
        log.info("🎯 버튼 클릭 완료, 페이지 전환 대기 중...");
        return buyButton;
    }

    /**
     * 팝업 처리 (경고창, 안내창 등)
     */
    private void handlePopups(WebDriver driver, WebDriverWait wait) {
        try {
            log.info("🔍 팝업 확인 중...");
            JavascriptExecutor js = (JavascriptExecutor) driver;

            // popupLayerAlert 처리
            try {
                WebElement popupAlert = driver.findElement(By.id("popupLayerAlert"));
                if (isElementDisplayed(popupAlert)) {
                    log.warn("🔔 경고 팝업 감지됨. 닫기 시도...");
                    WebElement confirmBtn = popupAlert.findElement(By.cssSelector("input[value='확인']"));
                    js.executeScript("arguments[0].click();", confirmBtn);
                    log.info("✅ 경고 팝업 닫기 완료!");
                    Thread.sleep(1000);
                }
            } catch (NoSuchElementException e) {
                log.info("ℹ️ 경고 팝업 없음");
            }
        } catch (Exception e) {
            log.warn("⚠️ 팝업 처리 중 오류: {}", e.getMessage());
        }
    }

    /**
     * 엘리먼트 표시 여부 확인
     */
    private boolean isElementDisplayed(WebElement element) {
        try {
            return element.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 로또 번호 자동 생성 및 구매 프로세스
     */
    private void performLottoPurchase(WebDriver driver, WebDriverWait wait, JavascriptExecutor js) throws Exception {
        log.info("🎮 로또 구매 프로세스 시작...");
        
        // 현재 URL 확인
        String currentUrl = driver.getCurrentUrl();
        log.info("📍 현재 페이지: {}", currentUrl);
        
        // 페이지 로딩 대기
        Thread.sleep(3000);
        
        try {
            // TotalGame.jsp 페이지인 경우
            if (currentUrl.contains("TotalGame.jsp")) {
                log.info("🎯 TotalGame.jsp 페이지에서 구매 진행");
                performPurchaseOnTotalGamePage(driver, wait, js);
            } else {
                // 기존 game645 페이지인 경우
                log.info("🎯 game645 페이지에서 구매 진행");
                performPurchaseOnGame645Page(driver, wait, js);
            }
            
            log.info("✨ 로또645 구매 완료!");
            
        } catch (Exception e) {
            log.error("❌ 구매 프로세스 실패: {}", e.getMessage());
            
            // 디버깅: 페이지 소스 확인
            try {
                log.info("🔍 페이지 요소 분석 중...");
                
                // input 요소들 찾기
                List<WebElement> inputs = driver.findElements(By.tagName("input"));
                log.info("📋 페이지의 input 요소 수: {}", inputs.size());
                for (int i = 0; i < Math.min(inputs.size(), 20); i++) {
                    WebElement input = inputs.get(i);
                    log.info("  Input {}: id='{}', type='{}', name='{}'", 
                        i+1, 
                        input.getAttribute("id"), 
                        input.getAttribute("type"),
                        input.getAttribute("name")
                    );
                }
                
                // select 요소들 찾기
                List<WebElement> selects = driver.findElements(By.tagName("select"));
                log.info("📋 페이지의 select 요소 수: {}", selects.size());
                for (int i = 0; i < selects.size(); i++) {
                    WebElement select = selects.get(i);
                    log.info("  Select {}: id='{}', name='{}'", 
                        i+1, 
                        select.getAttribute("id"),
                        select.getAttribute("name")
                    );
                }
                
                // button 요소들 찾기
                List<WebElement> buttons = driver.findElements(By.tagName("button"));
                log.info("📋 페이지의 button 요소 수: {}", buttons.size());
                for (int i = 0; i < Math.min(buttons.size(), 20); i++) {
                    WebElement button = buttons.get(i);
                    String text = button.getText();
                    log.info("  Button {}: id='{}', class='{}', text='{}'", 
                        i+1, 
                        button.getAttribute("id"),
                        button.getAttribute("class"),
                        text.length() > 20 ? text.substring(0, 20) : text
                    );
                }
                
            } catch (Exception debugEx) {
                log.error("디버깅 정보 수집 실패: {}", debugEx.getMessage());
            }
            
            throw e;
        }
    }
    
    /**
     * TotalGame.jsp 페이지에서 구매 진행
     */
    private void performPurchaseOnTotalGamePage(WebDriver driver, WebDriverWait wait, JavascriptExecutor js) throws Exception {
        log.info("🎲 TotalGame.jsp 방식으로 구매 진행");
        
        // 페이지 로딩 대기
        Thread.sleep(3000);
        
        // iframe 찾기 및 전환
        log.info("🔍 iframe 찾는 중...");
        try {
            // ifrm_tab iframe으로 전환
            WebElement iframe = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ifrm_tab")));
            driver.switchTo().frame(iframe);
            log.info("✅ iframe (game645.do)으로 전환 완료!");
            Thread.sleep(2000);
        } catch (Exception e) {
            log.error("❌ iframe 전환 실패: {}", e.getMessage());
            throw new RuntimeException("iframe을 찾을 수 없습니다", e);
        }
        
        // 이제 iframe 내부에서 기존 game645 페이지 로직 실행
        log.info("🎮 iframe 내부 game645 페이지에서 구매 진행...");
        performPurchaseOnGame645Page(driver, wait, js);
        
        // iframe에서 나오기
        driver.switchTo().defaultContent();
        log.info("✅ iframe에서 메인 페이지로 복귀");
    }
    
    /**
     * 기존 game645 페이지에서 구매 진행
     */
    private void performPurchaseOnGame645Page(WebDriver driver, WebDriverWait wait, JavascriptExecutor js) throws Exception {
        log.info("🎲 game645 방식으로 구매 진행");
        
        // 1. 자동번호발급 선택 (있는 경우만)
        log.info("1️⃣ 자동번호발급 탭 찾기...");
        try {
            WebElement autoTab = driver.findElement(By.id("num2"));
            if (autoTab.isDisplayed()) {
                js.executeScript("arguments[0].click();", autoTab);
                Thread.sleep(1000);
                log.info("✅ 자동번호발급 탭 클릭 완료!");
            }
        } catch (Exception e) {
            log.info("ℹ️ 자동번호발급 탭 없음 (이미 자동으로 설정되어 있을 수 있음)");
        }

        // 2. 구매 수량 선택 (5장)
        log.info("2️⃣ 구매 수량 선택 중 (5장)...");
        try {
            WebElement quantitySelect = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("amoundApply")));
            Select select = new Select(quantitySelect);
            select.selectByValue("5");
            Thread.sleep(500);
            log.info("✅ 구매 수량 선택 완료!");
        } catch (Exception e) {
            log.warn("⚠️ 수량 선택 실패, 기본값 사용: {}", e.getMessage());
        }

        // 3. 번호 확인 버튼 클릭 (있는 경우만)
        log.info("3️⃣ 번호 확인 버튼 찾기...");
        try {
            WebElement selectNumBtn = driver.findElement(By.id("btnSelectNum"));
            if (selectNumBtn.isDisplayed()) {
                js.executeScript("arguments[0].click();", selectNumBtn);
                Thread.sleep(2000);
                log.info("✅ 번호 확인 완료!");
            }
        } catch (Exception e) {
            log.info("ℹ️ 번호 확인 버튼 없음 (단순화된 구매 프로세스일 수 있음)");
        }

        // 4. 구매하기 버튼 클릭
        log.info("4️⃣ 구매하기 버튼 클릭...");
        try {
            WebElement buyBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("btnBuy")));
            js.executeScript("arguments[0].click();", buyBtn);
            Thread.sleep(2000);
            log.info("✅ 구매하기 클릭 완료!");
        } catch (Exception e) {
            log.error("❌ 구매하기 버튼 클릭 실패: {}", e.getMessage());
            throw new RuntimeException("구매하기 버튼을 찾을 수 없습니다", e);
        }

        // 5. Alert 처리
        log.info("5️⃣ Alert 확인 중...");
        try {
            Alert alert = wait.until(ExpectedConditions.alertIsPresent());
            String alertText = alert.getText();
            log.info("📢 Alert: {}", alertText);
            alert.accept();
            Thread.sleep(1000);
            log.info("✅ Alert 확인 완료!");
        } catch (Exception e) {
            log.info("ℹ️ Alert 없음");
        }

        // 6. 최종 실행 버튼 클릭 (있는 경우만)
        log.info("6️⃣ 최종 실행 버튼 찾기...");
        try {
            WebElement execBtn = driver.findElement(By.id("execBuy"));
            if (execBtn.isDisplayed()) {
                js.executeScript("arguments[0].click();", execBtn);
                Thread.sleep(2000);
                log.info("✅ 최종 실행 완료!");
            }
        } catch (Exception e) {
            log.info("ℹ️ 최종 실행 버튼 없음 (이미 구매가 완료되었을 수 있음)");
        }

        // 7. 최종 확인 팝업 처리 (있는 경우만)
        log.info("7️⃣ 최종 확인 팝업 처리...");
        try {
            WebElement finalConfirmBtn = driver.findElement(
                    By.xpath("//*[@id='popupLayerConfirm']//input[@value='확인']"));
            if (finalConfirmBtn.isDisplayed()) {
                js.executeScript("arguments[0].click();", finalConfirmBtn);
                Thread.sleep(2000);
                log.info("✅ 최종 확인 완료!");
            }
        } catch (Exception e) {
            log.info("ℹ️ 최종 확인 팝업 없음");
        }

        // 8. 추천 팝업 확인 (있는 경우만)
        log.info("8️⃣ 추천 팝업 확인 중...");
        try {
            WebElement recommendPopup = driver.findElement(By.id("recommend720Plus"));
            if (isElementDisplayed(recommendPopup)) {
                log.info("ℹ️ 추천 팝업이 표시 중입니다");
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            log.info("ℹ️ 추천 팝업 없음");
        }

        // 9. 레이어 닫기 (있는 경우만)
        log.info("9️⃣ 페이지 닫기...");
        try {
            WebElement closeLayer = driver.findElement(By.id("closeLayer"));
            if (closeLayer.isDisplayed()) {
                js.executeScript("arguments[0].click();", closeLayer);
                Thread.sleep(1000);
                log.info("✅ 페이지 닫기 완료!");
            }
        } catch (Exception e) {
            log.info("ℹ️ 닫기 버튼 없음");
        }
        
        log.info("✨ game645 구매 프로세스 완료!");
    }

//        // 자동번호발급 버튼 찾기
//        WebElement autoNumberBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[contains(text(),'자동번호발급')]")));
//        autoNumberBtn.click();
//
//        // 구매 개수 선택
//        Select selectCount = new Select(driver.findElement(By.tagName("select")));
//        int COUNT = 1; // 구매 개수
//        selectCount.selectByValue(String.valueOf(COUNT));
//
//        // 확인 버튼 클릭 (스크롤 후 강제 클릭)
//        WebElement confirmBtn = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//input[@type='button' and contains(@value, '확인')]")));
//        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", confirmBtn);
//        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", confirmBtn);
//
//        // 구매하기 클릭
//        WebElement buyBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//input[@type='button' and contains(@value, '구매하기')]")));
//        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", buyBtn);

    // 최종 확인 버튼 클릭 (다른 요소 방해 체크)
//        WebElement finalConfirmBtn = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//input[@type='button' and contains(@value, '확인')]")));
//        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", finalConfirmBtn);
//        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", finalConfirmBtn);
//
//
//        // 레이어 닫기
//        WebElement closeLayerBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[name='closeLayer']")));
//        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", closeLayerBtn);


//        driver.get("https://ol.dhlottery.co.kr/olotto/game/game645.do");
//
//        JavascriptExecutor js = (JavascriptExecutor) driver;
//
//        try {
//            WebElement finalConfirmOrCancelBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//input[@type='button' and contains(@value, '확인')]")));
//            js.executeScript("arguments[0].click();", finalConfirmOrCancelBtn);
///*            WebElement alertButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[@value='확인']")));
//            js.executeScript("arguments[0].click();", alertButton);*/
//        } catch (Exception e) {
//            log.error("비정상적인 방법 팝업 없음");
//        }

//        try {
//            WebElement alertButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@value='확인']")));
//            alertButton.click();
//        } catch (Exception e) {
//            log.error("비정상적인 방법 팝업 없음");
//        }

//        WebElement autoNumberBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[text()='자동번호발급']")));
//        js.executeScript("arguments[0].click();", autoNumberBtn);
//
//        Select selectCount = new Select(driver.findElement(By.tagName("select")));
//        int COUNT = 5; //
//        selectCount.selectByValue(String.valueOf(COUNT));
//
//        WebElement finalConfirmOrCancelBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//input[@type='button' and contains(@value, '확인')]")));
//        js.executeScript("arguments[0].click();", finalConfirmOrCancelBtn);
//

    /// /        WebElement buyBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[text()='구매하기']")));
//        WebElement buyBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//input[@type='button' and contains(@value, '구매하기')]")));
//        js.executeScript("arguments[0].click();", buyBtn);
//
//        try {
//            Thread.sleep(2000);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
//
//        WebElement finalConfirmOrCancelBtn1 = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//input[@type='button' and contains(@value, '확인') or contains(@value, '취소')]")));
//        js.executeScript("arguments[0].click();", finalConfirmOrCancelBtn1);
//
//        WebElement closeLayerBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//input[@name='closeLayer']")));
//        js.executeScript("arguments[0].click();", closeLayerBtn);

//        mattermostUtil.send("구매 완료", "5zqu88zsef83x8kj86igsqe1wa");
    private List<List<String>> result(WebDriver driver, WebDriverWait wait) {
        List<List<String>> result = new ArrayList<>();
        JavascriptExecutor js = (JavascriptExecutor) driver;
        
        log.info("🔍 구매 내역 조회 시작...");
        
        try {
            // 1. 구매 내역 페이지로 이동
            driver.get("https://www.dhlottery.co.kr/mypage/mylotteryledger");
            Thread.sleep(3000);
            log.info("✅ 구매 내역 페이지 로드 완료");
            
            // 2. 최근 1주일 조회 (JavaScript로 클릭)
            WebElement weekButton = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//button[contains(@class, 'btChgDt') and contains(text(), '최근 1주일')]")));
            js.executeScript("arguments[0].scrollIntoView({block: 'center'});", weekButton);
            Thread.sleep(500);
            js.executeScript("arguments[0].click();", weekButton);
            Thread.sleep(1000);
            log.info("✅ 조회 기간: 최근 1주일");
            
            // 3. 검색 버튼 클릭 (JavaScript로 클릭)
            WebElement searchButton = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("btnSrch")));
            js.executeScript("arguments[0].click();", searchButton);
            Thread.sleep(2000);
            log.info("✅ 검색 완료");
            
            // 4. 구매 내역에서 바코드 클릭 (티켓 팝업 열기)
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".whl-body .barcd")));
            WebElement barcodeElement = driver.findElement(By.cssSelector(".whl-body .barcd"));
            js.executeScript("arguments[0].click();", barcodeElement);
            Thread.sleep(3000);
            log.info("✅ 티켓 팝업 열기 완료");
            
            // 5. 팝업에서 번호 추출
            wait.until(ExpectedConditions.presenceOfElementLocated(By.id("Lotto645TicketP")));
            List<WebElement> ticketRows = driver.findElements(
                By.cssSelector("#Lotto645TicketP .ticket-num-wrap"));
            
            log.info("✅ 번호 추출 시작, 게임 수: {}", ticketRows.size());
            
            for (WebElement row : ticketRows) {
                List<String> numbers = new ArrayList<>();
                List<WebElement> numElements = row.findElements(By.cssSelector(".ticket-num-in"));
                
                for (WebElement numElement : numElements) {
                    String num = numElement.getText().trim();
                    if (!num.isEmpty()) {
                        numbers.add(num);
                    }
                }
                
                if (numbers.size() == 6) {
                    result.add(numbers);
                    log.info("📋 추출된 번호: {}", numbers);
                }
            }
            
            // 6. 팝업 닫기
            WebElement closeButton = driver.findElement(
                By.cssSelector("#Lotto645TicketP .btn-pop-close"));
            js.executeScript("arguments[0].click();", closeButton);
            Thread.sleep(1000);
            
            log.info("✅ 번호 추출 완료: {}개 게임", result.size());
            
            if (result.isEmpty()) {
                throw new ResultError();
            }
            
        } catch (ResultError e) {
            throw e;
        } catch (Exception e) {
            log.error("❌ 구매 내역 조회 실패: {}", e.getMessage(), e);
            throw new ResultError();
        }

        return result;
    }

    public List<String> lucky(WebDriver driver, WebDriverWait wait) {
        driver.get("https://dhlottery.co.kr/common.do?method=main");
        List<String> result = new ArrayList<>();

        try {
            // 1. 회차 정보 가져오기 (예: 1100회)
            WebElement roundElem = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".lt645-round")));
            String round = roundElem.getText();

            // 2. 당첨 번호 6개 추출 (.lt-ball 클래스를 가진 요소들 중 보너스 전까지)
            // JS 코드 구조상 .lt645-list 안에 .lt-ball들이 나열되어 있습니다.
            List<WebElement> ballElements = driver.findElements(By.cssSelector(".lt645-list .lt-ball:not(.plus)"));

            List<String> allNumbers = new ArrayList<>();
            for (WebElement ball : ballElements) {
                String num = ball.getText().trim();
                if (!num.isEmpty()) {
                    allNumbers.add(num);
                }
            }

            // 데이터 검증 (일반번호 6개 + 보너스 1개 = 총 7개여야 함)
            if (allNumbers.size() >= 7) {
                List<String> mainNumbers = allNumbers.subList(0, 6); // 1~6번째
                String bonus = allNumbers.get(6); // 7번째 (보너스)

                // 결과 리스트 생성
                result.addAll(mainNumbers);
                result.add(bonus);

                // 메신저 전송용 텍스트 구성
                String message = String.format("[%s] 당첨번호: %s + 보너스: %s",
                        round, String.join(", ", mainNumbers), bonus);
//                mattermostUtil.send(message, "5zqu88zsef83x8kj86igsqe1wa");

                return result;
            } else {
                mattermostUtil.send("당첨 번호 개수가 올바르지 않습니다. (찾은 개수: " + allNumbers.size() + ")", "5zqu88zsef83x8kj86igsqe1wa");
            }

        } catch (Exception e) {
            mattermostUtil.send("로또 정보를 가져오는 중 오류 발생: " + e.getMessage(), "5zqu88zsef83x8kj86igsqe1wa");
            e.printStackTrace();
        }

        return Collections.emptyList();
    }

    public void checkWinning(List<List<String>> myNumbers, List<String> luckyNumbers) {
        if (luckyNumbers.isEmpty()) {
            log.error("당첨 번호를 가져오지 못했습니다.");
            return;
        }

        // 3. 보너스 번호 분리
        String bonusNumber = luckyNumbers.get(luckyNumbers.size() - 1);
        Set<String> mainWinningNumbers = new HashSet<>(luckyNumbers.subList(0, 6));

        StringBuilder stringBuilder = new StringBuilder();

        for (List<String> myTicket : myNumbers) {
            int matchCount = 0;
            boolean bonusMatched = false;

            for (String num : myTicket) {
                if (mainWinningNumbers.contains(num)) {
                    matchCount++;
                } else if (num.equals(bonusNumber)) {
                    bonusMatched = true;
                }
            }

            // 4. 당첨 여부 판단
            String result;
            switch (matchCount) {
                case 6:
                    result = "🎉 1등 당첨! 축하합니다!";
                    break;
                case 5:
                    result = bonusMatched ? "🥈 2등 당첨! (보너스 번호 일치)" : "🥉 3등 당첨!";
                    break;
                case 4:
                    result = "🏅 4등 당첨!";
                    break;
                case 3:
                    result = "🎖 5등 당첨!";
                    break;
                default:
                    result = "❌ 낙첨";
                    break;
            }

            stringBuilder.append("내 로또 번호: " + myTicket + " -> 결과: " + result + "\n");
        }

        mattermostUtil.send(stringBuilder.toString(),"5zqu88zsef83x8kj86igsqe1wa");
    }

    public void checkWinningWebhook(List<List<String>> myNumbers, List<String> luckyNumbers, WebhookDTO webhookDTO) {
        if (luckyNumbers.isEmpty()) {
            log.error("당첨 번호를 가져오지 못했습니다.");
            return;
        }

        // 3. 보너스 번호 분리
        String bonusNumber = luckyNumbers.get(luckyNumbers.size() - 1);
        Set<String> mainWinningNumbers = new HashSet<>(luckyNumbers.subList(0, 6));

        StringBuilder stringBuilder = new StringBuilder();

        for (List<String> myTicket : myNumbers) {
            int matchCount = 0;
            boolean bonusMatched = false;

            for (String num : myTicket) {
                if (mainWinningNumbers.contains(num)) {
                    matchCount++;
                } else if (num.equals(bonusNumber)) {
                    bonusMatched = true;
                }
            }

            // 4. 당첨 여부 판단
            String result;
            switch (matchCount) {
                case 6:
                    result = "🎉 1등 당첨! 축하합니다!";
                    break;
                case 5:
                    result = bonusMatched ? "🥈 2등 당첨! (보너스 번호 일치)" : "🥉 3등 당첨!";
                    break;
                case 4:
                    result = "🏅 4등 당첨!";
                    break;
                case 3:
                    result = "🎖 5등 당첨!";
                    break;
                default:
                    result = "❌ 낙첨";
                    break;
            }

            stringBuilder.append("내 로또 번호: " + myTicket + " -> 결과: " + result + "\n");
        }

        mattermostUtil.sendWebhookChannel(stringBuilder.toString(), webhookDTO);
    }

    public class BalanceError extends RuntimeException {
        public BalanceError() {
            super("예치금 잔액이 부족합니다.");
            mattermostUtil.send("예치금 잔액이 부족합니다.", "5zqu88zsef83x8kj86igsqe1wa");
        }
    }

    public class ResultError extends RuntimeException {
        public ResultError() {
            super("구매목록이 존재하지 않습니다.");
            mattermostUtil.send("구매목록이 존재하지 않습니다.", "5zqu88zsef83x8kj86igsqe1wa");
        }
    }
}
