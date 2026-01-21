package com.service.batch.service.lotto.biz;

import com.service.batch.utils.MattermostUtil;
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
//        InternetExplorerOptions options = new InternetExplorerOptions();
//        options.setCapability("ignoreProtectedModeSettings", true);
        WebDriver driver = new ChromeDriver(options);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        try {
            login(driver, wait);

            List<List<String>> result = result(driver, wait);

//            result2(webDriver, webDriverWait);

            List<String> lucky = lucky(driver, wait);

            this.checkWinning(result, lucky);
        } catch (Exception e) {
            log.error("error > {}", e);
        } finally {
            driver.quit();
        }
    }

    private void login(WebDriver driver, WebDriverWait wait) {
        driver.get("https://www.dhlottery.co.kr");
        driver.navigate().to("https://www.dhlottery.co.kr/login");

        WebElement idElement = driver.findElement(By.cssSelector("input[placeholder='아이디']"));
        idElement.sendKeys("kd2675");

        WebElement pwElement = driver.findElement(By.cssSelector("input[placeholder='비밀번호']"));
        pwElement.sendKeys("Whitered2@");

        WebElement loginButton = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//form[@name='jform']//*[text()='로그인']")));
        loginButton.sendKeys(Keys.ENTER);

        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void account(WebDriver driver, WebDriverWait wait) {
        driver.get("https://dhlottery.co.kr/common.do?method=main");

        WebElement infoElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("ul.information")));
        String moneyInfoText = infoElement.getText();

        String[] moneyInfoArray = moneyInfoText.split("\n");
        String userName = moneyInfoArray[0];
        int moneyInfo = Integer.parseInt(moneyInfoArray[2].replace(",", "").replace("원", ""));

        //정보전달
        mattermostUtil.send("예치금 잔액 : " + moneyInfo, "5zqu88zsef83x8kj86igsqe1wa");

        if (1000 * 5 > moneyInfo) {
            //잔액부족
            throw new BalanceError();
        }
    }

    private void buy(WebDriver driver, WebDriverWait wait) {
        // 페이지 이동
        driver.get("https://ol.dhlottery.co.kr/olotto/game/game645.do");

        try {
            boolean isPopupPresent = !driver.findElements(By.id("popupLayerAlert")).isEmpty();

            if (!isPopupPresent) {
                List<WebElement> iframes = driver.findElements(By.tagName("iframe"));
                for (WebElement iframe : iframes) {
                    driver.switchTo().frame(iframe);
                    isPopupPresent = !driver.findElements(By.id("popupLayerAlert")).isEmpty();
                    if (isPopupPresent) {
                        log.warn("\uD83D\uDCCC iframe 내 팝업 발견: {}", iframe.getAttribute("id"));
                        driver.switchTo().defaultContent(); // iframe에서 기본 컨텍스트로 돌아옴
                        break;
                    }
                    driver.switchTo().defaultContent();
                }
            }

            if (isPopupPresent) {
                log.warn("\uD83D\uDCCC 팝업 감지됨. 닫기 시도...");

                WebElement popupLayer = driver.findElement(By.id("popupLayerAlert"));
                String displayStyle = popupLayer.getCssValue("display");

                if (!"none".equals(displayStyle)) {
                    WebElement confirmButton = driver.findElement(By.cssSelector("#popupLayerAlert .button.lrg.confirm[value='확인']"));
                    wait.until(ExpectedConditions.elementToBeClickable(confirmButton));
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", confirmButton);
                    log.warn("✅ 팝업 닫기 성공!");
                } else {
                    log.warn("ℹ️ 팝업이 표시되지 않음 (display: none)");
                }

                driver.switchTo().defaultContent();
                Thread.sleep(1000); // 팝업 닫힌 후 1초 대기
            } else {
                log.warn("❌ 팝업이 감지되지 않음.");
            }
        } catch (Exception e) {
            log.error("⚠️ 팝업 처리 실패: {}", e.getMessage());
        }


        try {
            // Step 1: Select the "자동번호발급" tab (second tab)
//            WebElement selectedTab = driver.findElement(By.id("selectedTab"));
//            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].value = '1';", selectedTab);

            WebElement num2 = driver.findElement(By.id("num2"));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", num2);

            // Wait for the tab content to load (optional)
            Thread.sleep(1000);

            // Step 2: Choose the number of papers (5장)
//            Select selectCount = new Select(driver.findElement(By.tagName("select")));
//            int COUNT = 1; // 구매 개수
//            selectCount.selectByValue(String.valueOf(COUNT));
//            WebElement amountDropdown = driver.findElement(By.id("amoundApply"));
//            amountDropdown.sendKeys("5"); // Choose 5 as the amount
            // "적용수량" 드롭다운 요소 찾기
            WebElement amountDropdown = driver.findElement(By.id("amoundApply"));

            // Select 객체를 사용하여 "5" 선택
            Select select = new Select(amountDropdown);
            select.selectByValue("5"); // value 값이 "5"인 옵션 선택

            // Step 3: Click the "확인" button to complete the purchase
            WebElement confirmButton = driver.findElement(By.id("btnSelectNum"));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", confirmButton);

            // Optionally, wait for a few seconds to confirm the purchase (for demonstration)
            Thread.sleep(2000);

            WebElement btnBuy = driver.findElement(By.id("btnBuy"));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btnBuy);

            Thread.sleep(2000);

            WebElement execBuy = driver.findElement(By.id("execBuy"));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", execBuy);

            Thread.sleep(2000);

//            System.out.println(driver.getPageSource());

            WebElement confirmButton2 = driver.findElement(By.xpath("//*[@id='popupLayerConfirm']//input[@value='확인']"));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", confirmButton2);


//            WebElement confirmButton3 = driver.findElement(By.cssSelector(".button.lrg.confirm[value='확인']"));
//            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", confirmButton3);

            WebElement popupLayer = driver.findElement(By.id("recommend720Plus"));
            String displayStyle = popupLayer.getCssValue("display");

            if (!"none".equals(displayStyle)) {
                log.warn("이미 구매 완료");

                return;
            }

            Thread.sleep(2000);

            WebElement closeLayer = driver.findElement(By.id("closeLayer"));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", closeLayer);

            // You can also add additional logic to verify if the purchase is successful
            // For example, check for a success message or confirmation.
            mattermostUtil.send("구매 완료", "5zqu88zsef83x8kj86igsqe1wa");
        } catch (InterruptedException e) {
            log.error("error > {}", e);
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
////        WebElement buyBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[text()='구매하기']")));
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
    }

    private List<List<String>> result(WebDriver driver, WebDriverWait wait) {
        List<List<String>> result = new ArrayList<>();
        driver.get("https://dhlottery.co.kr/common.do?method=main");

//        String searchStartDate = LocalDate.now().minusDays(7).format(DateTimeFormatter.BASIC_ISO_DATE);
//        String searchEndDate = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        // 저번 주 기간 계산 (일요일 ~ 토요일)
        LocalDate lastMonday = LocalDate.now().minusWeeks(1).with(DayOfWeek.SUNDAY);
        LocalDate lastSunday = lastMonday.plusDays(6);

        String searchStartDate = lastMonday.format(DateTimeFormatter.BASIC_ISO_DATE);
        String searchEndDate = lastSunday.format(DateTimeFormatter.BASIC_ISO_DATE);
        String postUrl = "https://dhlottery.co.kr/myPage.do?method=lottoBuyList";

        driver.get(postUrl + "&searchStartDate=" + searchStartDate
                + "&searchEndDate=" + searchEndDate
                + "&winGrade=2");

        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("tbody > tr:first-child > td:nth-child(4) > a"))
            );
        } catch (Exception e) {
            log.error("error > {}", e);
            throw new ResultError();
        }

        WebElement link = driver.findElement(
                By.cssSelector("tbody > tr:first-child > td:nth-child(4) > a")
        );
        String href = link.getAttribute("href");

        Matcher matcher = Pattern.compile("\\d+").matcher(href);
        String[] nums = matcher.results()
                .map(mr -> mr.group())
                .toArray(String[]::new);

        // 4. 상세 페이지 이동
        String detailUrl = String.format(
                "https://dhlottery.co.kr/myPage.do?method=lotto645Detail&orderNo=%s&barcode=%s&issueNo=%s",
                nums[0], nums[1], nums[2]
        );
        driver.get(detailUrl);

        // 5. 당첨 번호 추출
        StringBuilder resultMsg = new StringBuilder("이번주 나의 행운의 번호는?!\n");
        List<WebElement> numbers = wait.until(
                ExpectedConditions.presenceOfAllElementsLocatedBy(
                        By.cssSelector("div.selected li")
                )
        );

        for (WebElement num : numbers) {
            resultMsg.append(num.getText().replace("\n", ", ")).append("\n");
        }

        for (WebElement num : numbers) {
            List<String> list = new ArrayList<>();

            for (String s : num.getText().split("\n")) {
                if (!s.contains("자동")) {
                    list.add(s);
                }
            }

            result.add(list);
        }

        return result;
    }

    private void result2(WebDriver driver, WebDriverWait wait) {
        try {
            driver.get("https://dhlottery.co.kr/myPage.do?method=lottoBuyListView");

            try {
                wait.until(ExpectedConditions.presenceOfElementLocated(
                        By.cssSelector("tbody > tr:nth-child(1) > td:nth-child(4) > a"))
                );
            } catch (Exception e) {
                log.error("error > {}", e);
                throw new ResultError();
            }

            WebElement firstResultLink = wait.until(
                    ExpectedConditions.elementToBeClickable(By.cssSelector("tbody > tr:nth-child(1) > td:nth-child(4) > a"))
            );

            String href = firstResultLink.getAttribute("href");

            Pattern pattern = Pattern.compile("\\d+");
            Matcher matcher = pattern.matcher(href);
            String[] detailInfo = new String[3];
            int i = 0;
            while (matcher.find() && i < 3) {
                detailInfo[i++] = matcher.group();
            }

            if (detailInfo[0] != null && detailInfo[1] != null && detailInfo[2] != null) {
                String detailUrl = String.format(
                        "https://dhlottery.co.kr/myPage.do?method=lotto645Detail&orderNo=%s&barcode=%s&issueNo=%s",
                        detailInfo[0], detailInfo[1], detailInfo[2]
                );
                driver.get(detailUrl);

                List<WebElement> results = driver.findElements(By.cssSelector("div.selected li"));
                StringBuilder resultMsg = new StringBuilder("이번주 나의 행운의 번호는?!\n");

                for (WebElement result : results) {
                    resultMsg.append(result.getText().replace("\n", ", ")).append("\n");
                }

//                System.out.println(resultMsg);
            }
        } catch (Exception e) {
            log.error("error > {}", e);
        }
    }

    public List<String> lucky(WebDriver driver, WebDriverWait wait) {
        driver.get("https://dhlottery.co.kr/common.do?method=main");
        int retryCount = 0;
        WebElement resultInfo = null;

        while (retryCount < 3) {
            try {
                resultInfo = driver.findElement(By.cssSelector("#article div.content"));
                if (resultInfo != null) {
                    break;
                }
            } catch (Exception e) {
                retryCount++;
            }
        }

        if (resultInfo != null) {
            String resultText = resultInfo.getText().split("이전")[0].replace("\n", " ");

            // 당첨번호 추출
            String numberText = resultText.split("당첨결과")[1].split("1등")[0]
                    .replace("보너스번호 ", "")
                    .replace("(", "")
                    .replace(")", "")
                    .trim()
                    .replace(" ", ",");
            List<String> luckyNumbers = Arrays.asList(numberText.split(","));

            if (luckyNumbers.size() >= 9) {
                List<String> result = new ArrayList<>();

                String luckyDate = luckyNumbers.get(0);
                List<String> mainNumbers = luckyNumbers.subList(2, 8);
                String bonus = luckyNumbers.get(8);

//                System.out.println("추첨 날짜: " + luckyDate);
//                System.out.println("당첨 번호: " + mainNumbers);
//                System.out.println("보너스 번호: " + bonus);

                mattermostUtil.send(resultText, "5zqu88zsef83x8kj86igsqe1wa");

                result.addAll(mainNumbers);
                result.add(bonus);

                return result;
            } else {
                mattermostUtil.send("당첨 번호를 올바르게 가져오지 못했습니다.", "5zqu88zsef83x8kj86igsqe1wa");
//                System.out.println("당첨 번호를 올바르게 가져오지 못했습니다.");
            }
        } else {
            mattermostUtil.send("로또 당첨 정보를 가져오지 못했습니다.", "5zqu88zsef83x8kj86igsqe1wa");
//            System.out.println("로또 당첨 정보를 가져오지 못했습니다.");
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

        mattermostUtil.send(stringBuilder.toString(), "5zqu88zsef83x8kj86igsqe1wa");
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
