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
                            i + 1,
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
                            i + 1,
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
                            i + 1,
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

            // 페이지 구조 분석 (디버깅용)
            analyzePageStructure(driver);

            // 2️⃣ 복권상품 필터 설정: 로또6/45만 선택
            log.info("🎫 복권상품 필터 설정 중... (로또6/45)");
            try {
                // 복권상품 필터링 버튼/탭 찾기 - "로또6/45" 텍스트 포함
                // 2️⃣ 복권상품 필터 설정: 로또6/45만 선택
                log.info("🎫 복권상품 필터 설정 중... (로또6/45)");
                try {
                    // select 드롭다운에서 로또6/45 (LO40) 선택
                    WebElement ltGdsSelect = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ltGdsSelect")));
                    Select lotteryProductSelect = new Select(ltGdsSelect);
                    lotteryProductSelect.selectByValue("LO40"); // 로또6/45 코드값

                    log.info("✅ 로또6/45 필터 선택 완료");
                } catch (Exception e) {
                    log.warn("⚠️ 복권상품 필터 설정 실패: {}", e.getMessage());
                }
            } catch (Exception e) {
                log.warn("⚠️ 복권상품 필터 설정 실패: {}", e.getMessage());
            }

            // 3. 최근 1주일 조회 (JavaScript로 클릭)
            log.info("📅 조회 기간 설정 중...");
            try {
                WebElement weekButton = wait.until(ExpectedConditions.presenceOfElementLocated(
                        By.xpath("//button[contains(@class, 'btChgDt') and contains(text(), '최근 1주일')]")));
                js.executeScript("arguments[0].scrollIntoView({block: 'center'});", weekButton);
                Thread.sleep(500);
                js.executeScript("arguments[0].click();", weekButton);
                Thread.sleep(1000);
                log.info("✅ 조회 기간: 최근 1주일");
            } catch (Exception e) {
                log.warn("⚠️ 조회 기간 설정 실패 (기본값 사용): {}", e.getMessage());
            }

            // 3. 검색 버튼 클릭 (JavaScript로 클릭)
            log.info("🔎 검색 중...");
            try {
                WebElement searchButton = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("btnSrch")));
                js.executeScript("arguments[0].click();", searchButton);
                Thread.sleep(3000);
                log.info("✅ 검색 완료");
            } catch (Exception e) {
                log.warn("⚠️ 검색 버튼 클릭 실패: {}", e.getMessage());
            }

            // 4. 구매 내역 데이터 확인
            log.info("📋 구매 내역 테이블 확인...");
            try {
                List<WebElement> purchaseRows = driver.findElements(By.cssSelector(".whl-body tr, .lotto-ledger-item"));
                log.info("📊 구매 내역 행 발견: {}개", purchaseRows.size());

                if (!purchaseRows.isEmpty()) {
                    log.info("✅ 구매 내역이 존재합니다");
                }
            } catch (Exception e) {
                log.warn("⚠️ 구매 내역 확인 실패: {}", e.getMessage());
            }

            // 5. 바코드 요소 찾기 및 클릭 (재시도 로직)
            log.info("🎫 바코드 클릭 시도 중...");
            WebElement barcodeElement = findBarcodeElement(driver, wait, js);

            if (barcodeElement == null) {
                log.error("❌ 바코드 요소를 찾을 수 없습니다");
                // 폴백: 번호가 이미 테이블에 표시되어 있을 가능성
                log.info("📋 폴백: 페이지의 모든 숫자 추출 시도...");
                result = tryExtractFromVisiblePage(driver);

                if (!result.isEmpty()) {
                    log.info("✅ 폴백 성공: {}개 티켓 추출", result.size());
                    return result;
                }

            }

            // 바코드 클릭 전에 스크린샷 로그
            log.info("📸 바코드 클릭 전 페이지 상태 확인...");
            try {
                List<WebElement> allNumbers = driver.findElements(By.cssSelector("[class*='num'], span[class*='ball']"));
                log.info("  페이지의 숫자 요소: {}개", allNumbers.size());
            } catch (Exception ignored) {
            }

            // 바코드 클릭
            log.info("🖱️ 바코드 클릭 실행...");
            try {
                js.executeScript("arguments[0].scrollIntoView({block: 'center'});", barcodeElement);
                Thread.sleep(500);
                js.executeScript("arguments[0].click();", barcodeElement);
                Thread.sleep(4000);
                log.info("✅ 바코드 클릭 완료");
            } catch (Exception e) {
                log.warn("⚠️ 바코드 클릭 실패: {}", e.getMessage());
                // 클릭 실패해도 계속 진행
            }

            // 6. 팝업에서 번호 추출 (여러 방식 시도)
            log.info("🎟️ 티켓 팝업에서 번호 추출 중...");
            result = extractTicketNumbers(driver, wait, js);

            // 7. 팝업 닫기
            log.info("📍 팝업 닫기 시도...");
            closeTicketPopup(driver, js);

            log.info("✅ 번호 추출 완료: {}개 게임", result.size());

            if (result.isEmpty()) {
                log.warn("⚠️ 추출된 번호가 없습니다. 페이지 소스 분석...");
                result = tryExtractFromPageSource(driver);
            }

            if (result.isEmpty()) {
            }

        } catch (ResultError e) {
            throw e;
        } catch (Exception e) {
            log.error("❌ 구매 내역 조회 실패: {}", e.getMessage(), e);
            mattermostUtil.send("❌ 구매 내역 조회 실패: " + e.getMessage(), "5zqu88zsef83x8kj86igsqe1wa");
        }

        return result;
    }

    /**
     * 페이지에 표시된 숫자에서 직접 추출 (폴백)
     * 로또6/45만 필터링
     */
    private List<List<String>> tryExtractFromVisiblePage(WebDriver driver) {
        List<List<String>> result = new ArrayList<>();

        try {
            log.info("🔍 페이지에 표시된 로또6/45 번호 추출 시도...");

            // 1. 전체 페이지 텍스트에서 로또6/45 섹션 찾기
            List<WebElement> allRows = driver.findElements(By.cssSelector("tr"));
            log.info("📋 전체 테이블 행 수: {}", allRows.size());

            for (WebElement row : allRows) {
                try {
                    String rowText = row.getText();

                    // 로또6/45 복권명이 포함된 행인지 확인
                    if (rowText.contains("로또6/45") || rowText.contains("로또645") || rowText.contains("6/45")) {
                        log.info("✅ 로또6/45 행 발견");
                        log.debug("  행 내용: {}", rowText.substring(0, Math.min(200, rowText.length())));

                        // 이 행에서 모든 숫자 추출
                        List<String> rowNumbers = new ArrayList<>();
                        String[] parts = rowText.split("[^0-9]+");

                        for (String part : parts) {
                            if (!part.isEmpty()) {
                                int num = Integer.parseInt(part);
                                // 로또 번호는 1~45 범위
                                if (num >= 1 && num <= 45) {
                                    rowNumbers.add(part);
                                    log.debug("  추출된 번호: {}", part);
                                }
                            }
                        }

                        log.info("✅ 로또6/45에서 총 {}개의 숫자 추출", rowNumbers.size());

                        // 6개씩 그룹화
                        if (rowNumbers.size() >= 6) {
                            for (int i = 0; i + 5 < rowNumbers.size(); i += 6) {
                                List<String> ticket = new ArrayList<>();
                                for (int j = 0; j < 6; j++) {
                                    ticket.add(rowNumbers.get(i + j));
                                }
                                result.add(ticket);
                                log.info("📋 추출된 로또 티켓: {}", ticket);
                            }
                        }
                    }
                } catch (Exception e) {
                    log.debug("❌ 행 처리 실패: {}", e.getMessage());
                }
            }

            log.info("✅ 페이지 직접 추출 완료: {}개 티켓", result.size());
        } catch (Exception e) {
            log.error("❌ 페이지 직접 추출 실패: {}", e.getMessage());
        }

        return result;
    }

    /**
     * 페이지 소스에서 추출 (최종 폴백)
     * 로또6/45만 필터링
     */
    private List<List<String>> tryExtractFromPageSource(WebDriver driver) {
        List<List<String>> result = new ArrayList<>();

        try {
            log.info("🔍 페이지 소스에서 로또6/45 번호 추출 시도...");
            String pageSource = driver.getPageSource();

            // 1. 로또6/45 섹션 찾기
            int lottoIndex = pageSource.indexOf("로또6/45");
            if (lottoIndex == -1) {
                lottoIndex = pageSource.indexOf("로또645");
            }
            if (lottoIndex == -1) {
                lottoIndex = pageSource.indexOf("6/45");
            }

            if (lottoIndex == -1) {
                log.warn("⚠️ 페이지 소스에서 로또6/45를 찾을 수 없습니다");
                return result;
            }

            log.info("✅ 로또6/45 섹션 발견 위치: {}", lottoIndex);

            // 2. 로또6/45 섹션의 번호 추출
            // 섹션 범위: 로또6/45부터 다음 복권명까지 (또는 다음 1000자)
            int startIndex = Math.max(0, lottoIndex - 200);
            int endIndex = Math.min(pageSource.length(), lottoIndex + 1000);
            String section = pageSource.substring(startIndex, endIndex);

            log.debug("추출 섹션: {}", section.substring(0, Math.min(200, section.length())));

            // 1~45 범위의 연속된 6개 숫자 찾기
            Pattern pattern = Pattern.compile("(\\d+)[^0-9]+(\\d+)[^0-9]+(\\d+)[^0-9]+" +
                    "(\\d+)[^0-9]+(\\d+)[^0-9]+(\\d+)");
            Matcher matcher = pattern.matcher(section);

            while (matcher.find()) {
                List<String> ticket = new ArrayList<>();
                boolean valid = true;

                for (int i = 1; i <= 6; i++) {
                    String numStr = matcher.group(i);
                    int num = Integer.parseInt(numStr);

                    // 로또 번호 범위 확인
                    if (num < 1 || num > 45) {
                        valid = false;
                        break;
                    }
                    ticket.add(numStr);
                }

                if (valid && !result.contains(ticket)) {
                    result.add(ticket);
                    log.info("📋 추출된 로또 티켓: {}", ticket);
                }
            }

            log.info("✅ 페이지 소스 추출 완료: {}개 티켓", result.size());
        } catch (Exception e) {
            log.error("❌ 페이지 소스 추출 실패: {}", e.getMessage());
        }

        return result;
    }

    /**
     * 바코드 요소 찾기 (재시도 로직)
     */
    private WebElement findBarcodeElement(WebDriver driver, WebDriverWait wait, JavascriptExecutor js) {
        String[] barcodeSelectors = {
                ".whl-body .barcd",           // 기존 선택자
                "a[title*='바코드']",          // 바코드 버튼
                ".whl-list .barcd",           // 다른 경로
                "[onclick*='popupBarcode']",  // JavaScript 이벤트 기반
                ".lotto-barcode",             // 대체 클래스명
                "button.barcode-btn"          // 버튼 형식
        };

        for (int i = 0; i < barcodeSelectors.length; i++) {
            try {
                log.info("🔍 바코드 선택자 시도 {}/{}: {}", i + 1, barcodeSelectors.length, barcodeSelectors[i]);

                WebElement element;
                if (barcodeSelectors[i].startsWith("[")) {
                    element = wait.until(ExpectedConditions.presenceOfElementLocated(
                            By.cssSelector(barcodeSelectors[i])));
                } else {
                    element = wait.until(ExpectedConditions.presenceOfElementLocated(
                            By.cssSelector(barcodeSelectors[i])));
                }

                if (element.isDisplayed() || isElementInViewport(driver, element)) {
                    log.info("✅ 바코드 요소 발견: {}", barcodeSelectors[i]);
                    return element;
                }
            } catch (Exception e) {
                log.debug("❌ 선택자 {} 실패", barcodeSelectors[i]);
            }
        }

        log.warn("⚠️ 모든 바코드 선택자로 요소를 찾을 수 없습니다");
        return null;
    }

    /**
     * 티켓 번호 추출 (여러 방식 시도)
     */
    private List<List<String>> extractTicketNumbers(WebDriver driver, WebDriverWait wait, JavascriptExecutor js)
            throws InterruptedException {
        List<List<String>> result = new ArrayList<>();

        // 방법 1: 기존 팝업 ID로 추출
        try {
            log.info("📋 방법 1: Lotto645TicketP ID로 시도...");
            if (waitForPopup(driver, wait, "Lotto645TicketP", 5)) {
                result = extractNumbersFromPopup(driver, "Lotto645TicketP");
                if (!result.isEmpty()) {
                    log.info("✅ 방법 1 성공: {}개 번호 추출", result.size());
                    return result;
                }
            }
        } catch (Exception e) {
            log.debug("❌ 방법 1 실패: {}", e.getMessage());
        }

        // 방법 2: 팝업 레이어 ID로 추출
        try {
            log.info("📋 방법 2: popupLayer 또는 레이어 팝업으로 시도...");
            String[] popupIds = {
                    "popupLayerTicket",
                    "popupTicket",
                    "layerTicket",
                    "ticketPopup",
                    "popupLottoTicket"
            };

            for (String popupId : popupIds) {
                if (waitForPopup(driver, wait, popupId, 3)) {
                    result = extractNumbersFromPopup(driver, popupId);
                    if (!result.isEmpty()) {
                        log.info("✅ 방법 2 성공 ({}): {}개 번호 추출", popupId, result.size());
                        return result;
                    }
                }
            }
        } catch (Exception e) {
            log.debug("❌ 방법 2 실패: {}", e.getMessage());
        }

        // 방법 3: CSS 선택자로 모든 티켓 번호 찾기
        try {
            log.info("📋 방법 3: CSS 선택자로 모든 티켓 번호 찾기...");
            String[] ticketSelectors = {
                    ".ticket-num-wrap .ticket-num-in",
                    ".lotto-number",
                    "[class*='ticket'][class*='number']",
                    ".whl-body .num"
            };

            for (String selector : ticketSelectors) {
                try {
                    List<WebElement> numberElements = driver.findElements(By.cssSelector(selector));
                    if (!numberElements.isEmpty()) {
                        result = parseTicketNumbersFromElements(numberElements);
                        if (!result.isEmpty()) {
                            log.info("✅ 방법 3 성공 ({}): {}개 번호 추출", selector, result.size());
                            return result;
                        }
                    }
                } catch (Exception ignored) {
                    log.debug("선택자 {} 실패", selector);
                }
            }
        } catch (Exception e) {
            log.debug("❌ 방법 3 실패: {}", e.getMessage());
        }

        // 방법 4: 페이지 소스 분석
        try {
            log.info("📋 방법 4: 페이지 소스 분석으로 번호 추출...");
            String pageSource = driver.getPageSource();
            result = parseTicketNumbersFromPageSource(pageSource);
            if (!result.isEmpty()) {
                log.info("✅ 방법 4 성공: {}개 번호 추출", result.size());
                return result;
            }
        } catch (Exception e) {
            log.debug("❌ 방법 4 실패: {}", e.getMessage());
        }

        log.error("❌ 모든 방법으로 번호 추출 실패");
        return result;
    }

    /**
     * 팝업이 나타날 때까지 대기
     */
    private boolean waitForPopup(WebDriver driver, WebDriverWait wait, String popupId, long timeoutSeconds) {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds))
                    .until(ExpectedConditions.presenceOfElementLocated(By.id(popupId)));
            log.info("✅ 팝업 나타남: {}", popupId);
            return true;
        } catch (Exception e) {
            log.debug("⏳ 팝업 대기 시간 초과: {}", popupId);
            return false;
        }
    }

    /**
     * 팝업에서 번호 추출
     */
    private List<List<String>> extractNumbersFromPopup(WebDriver driver, String popupId) {
        List<List<String>> result = new ArrayList<>();

        try {
            WebElement popup = driver.findElement(By.id(popupId));

            // 팝업이 표시될 때까지 대기
            if (!isElementDisplayed(popup)) {
                log.warn("⚠️ 팝업이 표시되지 않음: {}", popupId);
                return result;
            }

            log.info("🔍 팝업 내 티켓 행 탐색 중...");

            // 여러 선택자로 티켓 행 찾기
            String[] rowSelectors = {
                    ".ticket-num-wrap",
                    ".lotto-ticket-row",
                    ".ticket-row",
                    "[class*='ticket-row']"
            };

            List<WebElement> ticketRows = null;
            String foundSelector = null;
            for (String selector : rowSelectors) {
                try {
                    ticketRows = popup.findElements(By.cssSelector(selector));
                    if (!ticketRows.isEmpty()) {
                        foundSelector = selector;
                        log.info("✅ 티켓 행 발견 ({}): {}개", selector, ticketRows.size());
                        break;
                    }
                } catch (Exception ignored) {
                }
            }

            if (ticketRows == null || ticketRows.isEmpty()) {
                log.warn("⚠️ 티켓 행을 찾을 수 없습니다. 모든 숫자 요소로 시도...");
                // 폴백: 모든 숫자 요소 찾기
                List<WebElement> allNumbers = popup.findElements(By.cssSelector("[class*='num'], span, div"));
                return parseTicketNumbersFromElements(allNumbers);
            }

            // 각 티켓 행에서 번호 추출
            log.info("🎫 각 티켓 행에서 번호 추출 중...");
            int ticketIndex = 1;
            for (WebElement row : ticketRows) {
                List<String> numbers = new ArrayList<>();

                // 여러 선택자로 번호 요소 찾기
                String[] numSelectors = {
                        ".ticket-num-in",
                        ".number",
                        "[class*='num'][class*='in']",
                        "span[class*='num']",
                        "span",
                        "div"
                };

                List<WebElement> numElements = null;
                String numSelector = null;
                for (String selector : numSelectors) {
                    try {
                        numElements = row.findElements(By.cssSelector(selector));
                        if (!numElements.isEmpty()) {
                            numSelector = selector;
                            break;
                        }
                    } catch (Exception ignored) {
                    }
                }

                if (numElements != null && !numElements.isEmpty()) {
                    log.debug("  티켓 {}: {} 선택자로 {}개 요소 발견",
                            ticketIndex, numSelector, numElements.size());

                    for (WebElement numElement : numElements) {
                        String num = numElement.getText().trim();
                        if (!num.isEmpty() && num.matches("\\d+")) {
                            numbers.add(num);
                        }
                    }
                }

                if (numbers.size() == 6) {
                    result.add(numbers);
                    log.info("📋 티켓 {}: {}", ticketIndex, numbers);
                    ticketIndex++;
                } else if (!numbers.isEmpty()) {
                    log.warn("⚠️ 티켓 행에서 {}개의 번호만 추출됨 (6개 필요): {}", numbers.size(), numbers);
                }
            }

            log.info("✅ 팝업에서 총 {}개의 티켓 추출 완료", result.size());

        } catch (Exception e) {
            log.error("❌ 팝업에서 번호 추출 실패 ({}): {}", popupId, e.getMessage());
        }

        return result;
    }

    /**
     * 엘리먼트 목록에서 티켓 번호 파싱
     */
    private List<List<String>> parseTicketNumbersFromElements(List<WebElement> numberElements) {
        List<List<String>> result = new ArrayList<>();

        log.info("🔍 번호 엘리먼트 파싱 시작: 총 {}개 요소", numberElements.size());

        // 모든 번호를 문자열로 변환
        List<String> allNumbers = new ArrayList<>();
        for (WebElement element : numberElements) {
            String num = element.getText().trim();
            if (num.matches("\\d+")) {
                allNumbers.add(num);
                log.debug("  추출된 번호: {}", num);
            }
        }

        log.info("✅ 총 {}개의 번호 추출", allNumbers.size());

        // 6개씩 그룹화
        for (int i = 0; i + 5 < allNumbers.size(); i += 6) {
            List<String> ticket = new ArrayList<>();
            for (int j = 0; j < 6; j++) {
                ticket.add(allNumbers.get(i + j));
            }
            result.add(ticket);
            log.info("📋 티켓 {}: {}", result.size(), ticket);
        }

        log.info("✅ 총 {}개의 티켓 구성", result.size());
        return result;
    }

    /**
     * 페이지 소스에서 로또 번호 추출 (정규식)
     */
    private List<List<String>> parseTicketNumbersFromPageSource(String pageSource) {
        List<List<String>> result = new ArrayList<>();

        // 로또 번호 패턴: 6개의 숫자가 연속으로 나오는 패턴
        Pattern pattern = Pattern.compile(">(\\d{1,2})</[^>]*>\\s*>(\\d{1,2})</[^>]*>\\s*>" +
                "(\\d{1,2})</[^>]*>\\s*>(\\d{1,2})</[^>]*>\\s*>" +
                "(\\d{1,2})</[^>]*>\\s*>(\\d{1,2})<");
        Matcher matcher = pattern.matcher(pageSource);

        while (matcher.find()) {
            List<String> numbers = new ArrayList<>();
            for (int i = 1; i <= 6; i++) {
                numbers.add(matcher.group(i));
            }

            // 중복 제거
            if (!result.contains(numbers)) {
                result.add(numbers);
                log.info("📋 페이지 소스에서 추출된 번호: {}", numbers);
            }
        }

        return result;
    }

    /**
     * 티켓 팝업 닫기
     */
    private void closeTicketPopup(WebDriver driver, JavascriptExecutor js) {
        String[] closeSelectors = {
                "#Lotto645TicketP .btn-pop-close",
                ".popup-layer .close-btn",
                "button.close",
                "[class*='close'][class*='btn']",
                "input[value='닫기']",
                "input[value='확인']"
        };

        for (String selector : closeSelectors) {
            try {
                List<WebElement> closeButtons = driver.findElements(By.cssSelector(selector));
                if (!closeButtons.isEmpty()) {
                    WebElement closeBtn = closeButtons.get(0);
                    if (isElementDisplayed(closeBtn)) {
                        js.executeScript("arguments[0].click();", closeBtn);
                        Thread.sleep(500);
                        log.info("✅ 팝업 닫기 완료");
                        return;
                    }
                }
            } catch (Exception ignored) {
            }
        }

        log.warn("⚠️ 팝업 닫기 버튼을 찾을 수 없습니다");
    }

    /**
     * 엘리먼트가 뷰포트에 있는지 확인
     */
    private boolean isElementInViewport(WebDriver driver, WebElement element) {
        try {
            return (Boolean) ((JavascriptExecutor) driver).executeScript(
                    "var rect = arguments[0].getBoundingClientRect();" +
                            "return (rect.top <= window.innerHeight && rect.bottom >= 0);",
                    element
            );
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 페이지 구조 분석 (디버깅용)
     */
    private void analyzePageStructure(WebDriver driver) {
        try {
            log.info("🔍 페이지 구조 분석 시작...");

            // 바코드 관련 요소 찾기
            List<WebElement> barcodes = driver.findElements(By.cssSelector("[class*='barcd'], [class*='barcode']"));
            log.info("📋 바코드 관련 요소: {}개", barcodes.size());

            // 티켓 관련 요소 찾기
            List<WebElement> tickets = driver.findElements(By.cssSelector("[class*='ticket']"));
            log.info("📋 티켓 관련 요소: {}개", tickets.size());

            // ID로 시작하는 주요 요소 찾기
            List<WebElement> popups = driver.findElements(By.cssSelector("[id*='Lotto'], [id*='popup']"));
            log.info("📋 팝업 관련 요소: {}개", popups.size());
            for (int i = 0; i < Math.min(popups.size(), 5); i++) {
                log.info("  - {}", popups.get(i).getAttribute("id"));
            }

        } catch (Exception e) {
            log.warn("⚠️ 페이지 구조 분석 실패: {}", e.getMessage());
        }
    }

    public List<String> lucky(WebDriver driver, WebDriverWait wait) {
        driver.get("https://dhlottery.co.kr/common.do?method=main");
        List<String> result = new ArrayList<>();

        try {
            log.info("🔍 로또 당첨 번호 조회 시작...");
            Thread.sleep(3000); // 페이지 로딩 대기

            // 1. 회차 정보 가져오기 (예: 1100회)
            WebElement roundElem = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".lt645-round")));
            String round = roundElem.getText();
            log.info("📊 회차: {}", round);

            // 2. 당첨 번호 추출 (여러 방식 시도)
            List<String> allNumbers = extractWinningNumbers(driver, wait);

            if (allNumbers.isEmpty()) {
                log.error("❌ 당첨 번호를 추출할 수 없습니다");
                mattermostUtil.send("❌ 당첨 번호 추출 실패", "5zqu88zsef83x8kj86igsqe1wa");
                return Collections.emptyList();
            }

            log.info("✅ 추출된 당첨 번호: {}개", allNumbers.size());

            // 데이터 검증 (일반번호 6개 + 보너스 1개 = 총 7개여야 함)
            if (allNumbers.size() >= 7) {
                List<String> mainNumbers = new ArrayList<>(allNumbers.subList(0, 6)); // 1~6번째
                String bonus = allNumbers.get(6); // 7번째 (보너스)

                // 결과 리스트 생성
                result.addAll(mainNumbers);
                result.add(bonus);

                // 메신저 전송용 텍스트 구성
//                String message = String.format("[%s] 당첨번호: %s + 보너스: %s",
//                        round, String.join(", ", mainNumbers), bonus);
//                log.info("🎯 {}", message);
//                mattermostUtil.send(message, "5zqu88zsef83x8kj86igsqe1wa");

                return result;
            } else {
                log.error("❌ 당첨 번호 개수가 올바르지 않습니다. (찾은 개수: {})", allNumbers.size());
                mattermostUtil.send("❌ 당첨 번호 개수가 올바르지 않습니다. (찾은 개수: " + allNumbers.size() + ")", "5zqu88zsef83x8kj86igsqe1wa");
            }

        } catch (Exception e) {
            log.error("❌ 로또 정보 조회 중 오류 발생: {}", e.getMessage(), e);
            mattermostUtil.send("❌ 로또 정보를 가져오는 중 오류 발생: " + e.getMessage(), "5zqu88zsef83x8kj86igsqe1wa");
        }

        return Collections.emptyList();
    }

    /**
     * 로또 당첨 번호 추출 (여러 방식 시도)
     */
    private List<String> extractWinningNumbers(WebDriver driver, WebDriverWait wait) throws InterruptedException {
        // 방법 1: CSS 선택자로 공 요소 찾기 (.lt-ball)
        try {
            log.info("📋 방법 1: .lt-ball CSS 선택자로 시도...");
            List<WebElement> ballElements = driver.findElements(By.cssSelector(".lt645-list .lt-ball"));
            List<String> numbers = extractNumbersFromBalls(ballElements);

            if (!numbers.isEmpty()) {
                log.info("✅ 방법 1 성공: {}개 번호 추출", numbers.size());
                logNumbers("추출된 번호", numbers);
                return numbers;
            }
        } catch (Exception e) {
            log.debug("❌ 방법 1 실패: {}", e.getMessage());
        }

        // 방법 2: 다양한 공 요소 선택자
        try {
            log.info("📋 방법 2: 다양한 공 요소 선택자로 시도...");
            String[] ballSelectors = {
                    ".lotto-ball",
                    ".ball",
                    "[class*='ball']",
                    ".lt645 .num",
                    ".winning-number"
            };

            for (String selector : ballSelectors) {
                try {
                    List<WebElement> ballElements = driver.findElements(By.cssSelector(selector));
                    if (!ballElements.isEmpty()) {
                        List<String> numbers = extractNumbersFromBalls(ballElements);
                        if (!numbers.isEmpty()) {
                            log.info("✅ 방법 2 성공 ({}): {}개 번호 추출", selector, numbers.size());
                            logNumbers("추출된 번호", numbers);
                            return numbers;
                        }
                    }
                } catch (Exception ignored) {
                }
            }
        } catch (Exception e) {
            log.debug("❌ 방법 2 실패: {}", e.getMessage());
        }

        // 방법 3: span 요소에서 숫자 추출
        try {
            log.info("📋 방법 3: span 요소에서 숫자 추출...");
            List<WebElement> spanElements = driver.findElements(By.cssSelector(".lt645-list span, .winning-numbers span"));
            List<String> numbers = extractNumbersFromElements(spanElements);

            if (numbers.size() >= 7) {
                log.info("✅ 방법 3 성공: {}개 번호 추출", numbers.size());
                logNumbers("추출된 번호", numbers);
                return numbers;
            }
        } catch (Exception e) {
            log.debug("❌ 방법 3 실패: {}", e.getMessage());
        }

        // 방법 4: 페이지 소스 분석
        try {
            log.info("📋 방법 4: 페이지 소스 정규식으로 추출...");
            String pageSource = driver.getPageSource();
            List<String> numbers = extractNumbersFromPageSourceRegex(pageSource);

            if (!numbers.isEmpty()) {
                log.info("✅ 방법 4 성공: {}개 번호 추출", numbers.size());
                logNumbers("추출된 번호", numbers);
                return numbers;
            }
        } catch (Exception e) {
            log.debug("❌ 방법 4 실패: {}", e.getMessage());
        }

        // 방법 5: 페이지 구조 분석 후 상세 로깅
        try {
            log.info("📋 방법 5: 페이지 구조 분석...");
            analyzeWinningNumbersStructure(driver);
        } catch (Exception e) {
            log.debug("❌ 방법 5 실패: {}", e.getMessage());
        }

        log.error("❌ 모든 방법으로 번호 추출 실패");
        return new ArrayList<>();
    }

    /**
     * 공 요소(.lt-ball)에서 번호 추출
     */
    private List<String> extractNumbersFromBalls(List<WebElement> ballElements) {
        List<String> numbers = new ArrayList<>();

        for (WebElement ball : ballElements) {
            try {
                String num = ball.getText().trim();
                if (!num.isEmpty() && num.matches("\\d+")) {
                    numbers.add(num);
                    log.debug("  공에서 번호 추출: {}", num);
                }
            } catch (Exception ignored) {
            }
        }

        return numbers;
    }

    /**
     * 엘리먼트들에서 숫자 추출
     */
    private List<String> extractNumbersFromElements(List<WebElement> elements) {
        List<String> numbers = new ArrayList<>();

        for (WebElement element : elements) {
            try {
                String num = element.getText().trim();
                if (!num.isEmpty() && num.matches("\\d+")) {
                    numbers.add(num);
                    log.debug("  엘리먼트에서 번호 추출: {}", num);
                }
            } catch (Exception ignored) {
            }
        }

        return numbers;
    }

    /**
     * 페이지 소스에서 정규식으로 번호 추출
     */
    private List<String> extractNumbersFromPageSourceRegex(String pageSource) {
        List<String> numbers = new ArrayList<>();

        // 로또 당첨 번호는 보통 span 또는 div 내에 있으며, 1~45 범위의 숫자
        // 패턴: >숫자< 형태로 7개가 연속으로 나옴
        Pattern pattern = Pattern.compile("(?:<[^>]*>)?\\s*(\\d{1,2})\\s*(?:</[^>]*>)?");
        String cleanedSource = pageSource.replaceAll("<[^>]+>", " ");
        Matcher matcher = pattern.matcher(cleanedSource);

        // 1~45 범위의 숫자를 찾기
        Pattern numberPattern = Pattern.compile("\\b([1-9]|[1-3]\\d|4[0-5])\\b");
        Matcher numberMatcher = numberPattern.matcher(cleanedSource);

        Set<String> foundNumbers = new LinkedHashSet<>();

        // 페이지 소스에서 "당첨번호" 또는 유사한 텍스트 주변의 숫자를 찾기
        Pattern winningPattern = Pattern.compile(
                "(?:당첨번호|winning.*number|lotto.*number)[^\\d]*([1-9]|[1-3]\\d|4[0-5])[^\\d]*" +
                        "([1-9]|[1-3]\\d|4[0-5])[^\\d]*([1-9]|[1-3]\\d|4[0-5])[^\\d]*" +
                        "([1-9]|[1-3]\\d|4[0-5])[^\\d]*([1-9]|[1-3]\\d|4[0-5])[^\\d]*" +
                        "([1-9]|[1-3]\\d|4[0-5])[^\\d]*([1-9]|[1-3]\\d|4[0-5])",
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL
        );

        Matcher winningMatcher = winningPattern.matcher(cleanedSource);
        if (winningMatcher.find()) {
            for (int i = 1; i <= 7; i++) {
                String num = winningMatcher.group(i);
                if (num != null && !num.isEmpty()) {
                    foundNumbers.add(num);
                    log.debug("  정규식에서 번호 추출: {}", num);
                }
            }
        }

        numbers.addAll(foundNumbers);
        return numbers;
    }

    /**
     * 당첨 번호 페이지 구조 분석
     */
    private void analyzeWinningNumbersStructure(WebDriver driver) {
        try {
            log.info("🔍 페이지 구조 분석 시작...");

            // 주요 컨테이너 찾기
            List<WebElement> mainContainers = driver.findElements(By.cssSelector("[class*='lt645'], [class*='winning'], [class*='number']"));
            log.info("📋 주요 컨테이너 찾음: {}개", mainContainers.size());

            for (int i = 0; i < Math.min(mainContainers.size(), 10); i++) {
                WebElement container = mainContainers.get(i);
                String className = container.getAttribute("class");
                String id = container.getAttribute("id");
                String text = container.getText().substring(0, Math.min(100, container.getText().length()));
                log.info("  [{}] class='{}', id='{}', text='{}'", i, className, id, text);
            }

            // 숫자를 포함하는 모든 span/div 찾기
            List<WebElement> numElements = driver.findElements(By.cssSelector("span, div"));
            int numCount = 0;
            for (WebElement elem : numElements) {
                String text = elem.getText().trim();
                if (text.matches("\\d+") && Integer.parseInt(text) <= 45) {
                    log.debug("  숫자 요소: {} (class: '{}')", text, elem.getAttribute("class"));
                    numCount++;
                    if (numCount >= 15) break; // 처음 15개만
                }
            }

        } catch (Exception e) {
            log.warn("⚠️ 페이지 구조 분석 실패: {}", e.getMessage());
        }
    }

    /**
     * 번호 목록 로깅
     */
    private void logNumbers(String label, List<String> numbers) {
        log.info("{}: {}", label, numbers);
        for (int i = 0; i < numbers.size(); i++) {
            log.debug("  [{}] = {}", i + 1, numbers.get(i));
        }
    }

    public void checkWinning(List<List<String>> myNumbers, List<String> luckyNumbers) {
        if (luckyNumbers.isEmpty()) {
            log.error("당첨 번호를 가져오지 못했습니다.");
            mattermostUtil.send("❌ 당첨 번호를 가져오지 못했습니다.", "5zqu88zsef83x8kj86igsqe1wa");
            return;
        }

        log.info("🎰 당첨 검사 시작");
        log.info("📊 내 티켓 수: {}", myNumbers.size());
        log.info("🎯 당첨 번호: {}", luckyNumbers);

        // 3. 보너스 번호 분리
        String bonusNumber = luckyNumbers.get(luckyNumbers.size() - 1);
        Set<String> mainWinningNumbers = new HashSet<>(luckyNumbers.subList(0, 6));

        log.info("📌 주당첨 번호: {}", mainWinningNumbers);
        log.info("💎 보너스 번호: {}", bonusNumber);

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("=== 로또 당첨 검사 결과 ===\n");
        stringBuilder.append(String.format("주당첨: %s, 보너스: %s\n\n", mainWinningNumbers, bonusNumber));

        for (int ticketIdx = 0; ticketIdx < myNumbers.size(); ticketIdx++) {
            List<String> myTicket = myNumbers.get(ticketIdx);
            int matchCount = 0;
            boolean bonusMatched = false;
            List<String> matchedNumbers = new ArrayList<>();

            for (String num : myTicket) {
                if (mainWinningNumbers.contains(num)) {
                    matchCount++;
                    matchedNumbers.add(num);
                } else if (num.equals(bonusNumber)) {
                    bonusMatched = true;
                    matchedNumbers.add(num + "(보너스)");
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

            String ticketResult = String.format("[티켓 %d] %s -> 일치: %d개 %s -> %s\n",
                    ticketIdx + 1, myTicket, matchCount, matchedNumbers, result);
            stringBuilder.append(ticketResult);
            log.info(ticketResult.trim());
        }

        mattermostUtil.send(stringBuilder.toString(), "5zqu88zsef83x8kj86igsqe1wa");
    }

    public void checkWinningWebhook(List<List<String>> myNumbers, List<String> luckyNumbers, WebhookDTO webhookDTO) {
        if (luckyNumbers.isEmpty()) {
            log.error("당첨 번호를 가져오지 못했습니다.");
            mattermostUtil.sendWebhookChannel("❌ 당첨 번호를 가져오지 못했습니다.", webhookDTO);
            return;
        }

        log.info("🎰 당첨 검사 시작 (Webhook)");
        log.info("📊 내 티켓 수: {}", myNumbers.size());
        log.info("🎯 당첨 번호: {}", luckyNumbers);

        // 3. 보너스 번호 분리
        String bonusNumber = luckyNumbers.get(luckyNumbers.size() - 1);
        Set<String> mainWinningNumbers = new HashSet<>(luckyNumbers.subList(0, 6));

        log.info("📌 주당첨 번호: {}", mainWinningNumbers);
        log.info("💎 보너스 번호: {}", bonusNumber);

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("=== 로또 당첨 검사 결과 ===\n");
        stringBuilder.append(String.format("주당첨: %s, 보너스: %s\n\n", mainWinningNumbers, bonusNumber));

        for (int ticketIdx = 0; ticketIdx < myNumbers.size(); ticketIdx++) {
            List<String> myTicket = myNumbers.get(ticketIdx);
            int matchCount = 0;
            boolean bonusMatched = false;
            List<String> matchedNumbers = new ArrayList<>();

            for (String num : myTicket) {
                if (mainWinningNumbers.contains(num)) {
                    matchCount++;
                    matchedNumbers.add(num);
                } else if (num.equals(bonusNumber)) {
                    bonusMatched = true;
                    matchedNumbers.add(num + "(보너스)");
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

            String ticketResult = String.format("[티켓 %d] %s -> 일치: %d개 %s -> %s\n",
                    ticketIdx + 1, myTicket, matchCount, matchedNumbers, result);
            stringBuilder.append(ticketResult);
            log.info(ticketResult.trim());
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
