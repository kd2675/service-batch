package com.service.batch.service.sport.biz;

import com.service.batch.utils.MattermostUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReserveSportImpl implements ReserveSportSVC {
    private final MattermostUtil mattermostUtil;

    private record ChromeRec(WebDriver webDriver, WebDriverWait webDriverWait) {
    }

    @NotNull
    private static ChromeRec chromeRec() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--no-sandbox");
        options.addArguments("--single-process");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--start-maximized");
//        InternetExplorerOptions options = new InternetExplorerOptions();
//        options.setCapability("ignoreProtectedModeSettings", true);
        WebDriver webDriver = new ChromeDriver(options);
        WebDriverWait webDriverWait = new WebDriverWait(webDriver, Duration.ofSeconds(10));
        ChromeRec chromeRec = new ChromeRec(webDriver, webDriverWait);
        return chromeRec;
    }

    @Override
    public void test1(String year, String month, String day, String st) {
        ChromeRec chromeRec = chromeRec();

        try {
            String urlJangSung = "https://www.jangseong.go.kr/home/ok/health/warabel_gym?step=two";
            String url = urlJangSung + "&year=" + year + "&month=" + month + "&day=" + day + "&start_time=" + st;

            장성예약(chromeRec.webDriver(), chromeRec.webDriverWait(), url);
        } catch (Exception e) {
            log.error("error > {}", e);
        } finally {
            chromeRec.webDriver().quit();
        }
    }

    private void 장성예약(WebDriver driver, WebDriverWait wait, String url) {
        driver.get(url);

        try {
            // 페이지 로딩 대기
            Thread.sleep(2000);

            // 1단계: 예약 가능한 첫 번째 코트 선택
            boolean courtSelected = selectFirstAvailableCourt(driver, wait);

            if (!courtSelected) {
                log.warn("선택 가능한 코트가 없습니다.");
                return;
            }

            // 2단계: 라디오 버튼 선택 후 잠시 대기
            Thread.sleep(1000);

            // 3단계: 예약하기 버튼 클릭
            clickReservationButton(driver, wait);

            // 4단계: 예약 결과 확인 (선택사항)
//            handleReservationResult(driver, wait);

        } catch (Exception e) {
            log.error("예약 과정 중 오류 발생: {}", e.getMessage());
//            mattermostUtil.send("장성 테니스코트 예약창 실패", "5zqu88zsef83x8kj86igsqe1wa");
        }
    }

    private static void selectCourt(JavascriptExecutor driver, WebDriverWait wait) {
        // 예약 가능한 radio 버튼들 중에서 하나를 선택하여 클릭
        // 예시: tennis2 (코트2)를 클릭
        WebElement radioButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("input[type='radio'][name='room_code[]'][value='tennis2']")
        ));

        // JavaScript를 사용하여 클릭 (더 안정적)
        driver.executeScript("arguments[0].click();", radioButton);

        log.info("코트2 라디오 버튼 클릭 완료");
    }

    // 특정 코트를 선택하는 헬퍼 메소드
    private void selectAvailableCourt(WebDriver driver, WebDriverWait wait, String courtValue) {
        try {
            WebElement radioButton = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector("input[type='radio'][name='room_code[]'][value='" + courtValue + "']")
            ));

            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", radioButton);
            log.info("{} 라디오 버튼 클릭 완료", courtValue);

        } catch (Exception e) {
            log.error("{} 라디오 버튼 클릭 실패: {}", courtValue, e.getMessage());
        }
    }

    // selectFirstAvailableCourt 메소드도 boolean 반환하도록 수정
    private boolean selectFirstAvailableCourt(WebDriver driver, WebDriverWait wait) {
        try {
            // 예약 가능한 모든 라디오 버튼을 찾기
            List<WebElement> availableRadios = driver.findElements(
                    By.cssSelector("input[type='radio'][name='room_code[]']")
            );

            if (!availableRadios.isEmpty()) {
                // 첫 번째 예약 가능한 코트 선택
                WebElement firstAvailable = availableRadios.get(0);
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", firstAvailable);

                String courtValue = firstAvailable.getAttribute("value");
                log.info("첫 번째 예약 가능한 코트 ({}) 선택 완료", courtValue);
                return true;
            } else {
                log.warn("예약 가능한 코트가 없습니다.");
                return false;
            }

        } catch (Exception e) {
            log.error("예약 가능한 코트 선택 중 오류 발생: {}", e.getMessage());
            return false;
        }
    }


    // 예약하기 버튼을 클릭하는 메소드
    private void clickReservationButton(WebDriver driver, WebDriverWait wait) {
        try {
            // 방법 1: 이미지를 클릭하는 방법
            WebElement reservationImage = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector("img[alt='예약하기']")
            ));

            // 버튼이 화면에 보이도록 스크롤
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", reservationImage);
            Thread.sleep(500);

            // JavaScript로 클릭 (더 안정적)
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", reservationImage);
            log.info("예약하기 버튼 클릭 완료");

            // 확인 창 처리
            handleConfirmationDialog(driver, wait);

        } catch (Exception e) {
            log.error("예약하기 버튼 클릭 실패, JavaScript 함수 직접 호출 시도: {}", e.getMessage());

            try {
                // 방법 2: JavaScript 함수를 직접 호출하는 방법
                ((JavascriptExecutor) driver).executeScript("room_check(document.room_list);");
                log.info("JavaScript 함수 직접 호출로 예약 완료");

                // 확인 창 처리
                handleConfirmationDialog(driver, wait);

            } catch (Exception jsError) {
                log.error("JavaScript 함수 호출도 실패: {}", jsError.getMessage());

                try {
                    // 방법 3: div 컨테이너를 클릭하는 방법
                    WebElement reservationDiv = wait.until(ExpectedConditions.elementToBeClickable(
                            By.cssSelector("div[style*='text-align:center'][style*='cursor:pointer']")
                    ));

                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", reservationDiv);
                    log.info("div 컨테이너 클릭으로 예약 완료");

                    // 확인 창 처리
                    handleConfirmationDialog(driver, wait);

                } catch (Exception divError) {
                    log.error("모든 예약하기 버튼 클릭 방법 실패: {}", divError.getMessage());
                }
            }
        }
    }

    // 확인 창(컨펌 다이얼로그) 처리 메소드
    private void handleConfirmationDialog(WebDriver driver, WebDriverWait wait) {
        try {
            // 잠시 대기 (확인 창이 나타날 시간)
            Thread.sleep(1000);

            // 방법 1: JavaScript alert 처리
            try {
                Alert alert = wait.until(ExpectedConditions.alertIsPresent());
                String alertText = alert.getText();
                log.info("확인 창 메시지: {}", alertText);

                // 확인 버튼 클릭 (OK)
                alert.accept();
                log.info("확인 창 승인 완료");
                return;

            } catch (Exception alertError) {
                log.debug("JavaScript alert이 아님, 다른 방법 시도");
            }

            // 방법 2: 모달 창의 확인 버튼 찾기
            try {
                // 일반적인 확인 버튼 텍스트들
                String[] confirmButtonTexts = {"확인", "OK", "예", "Yes", "승인", "완료"};

                for (String buttonText : confirmButtonTexts) {
                    try {
                        WebElement confirmButton = wait.until(ExpectedConditions.elementToBeClickable(
                                By.xpath("//button[contains(text(), '" + buttonText + "') or contains(@value, '" + buttonText + "')]")
                        ));

                        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", confirmButton);
                        log.info("모달 창 '{}' 버튼 클릭 완료", buttonText);
                        return;

                    } catch (Exception e) {
                        // 해당 텍스트의 버튼이 없으면 다음 시도
                        continue;
                    }
                }

                // input type="button" 확인 버튼 찾기
                try {
                    WebElement inputConfirmButton = wait.until(ExpectedConditions.elementToBeClickable(
                            By.xpath("//input[@type='button' and (contains(@value, '확인') or contains(@value, 'OK') or contains(@value, '예'))]")
                    ));

                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", inputConfirmButton);
                    log.info("input 확인 버튼 클릭 완료");
                    return;

                } catch (Exception inputError) {
                    log.debug("input 확인 버튼 찾기 실패");
                }

            } catch (Exception modalError) {
                log.debug("모달 창 확인 버튼 찾기 실패");
            }

            // 방법 3: CSS 선택자로 일반적인 확인 버튼 찾기
            try {
                List<String> confirmSelectors = Arrays.asList(
                        "button.btn-confirm",
                        "button.confirm",
                        ".modal-footer button",
                        ".dialog-footer button",
                        "button[onclick*='confirm']",
                        "input[onclick*='confirm']"
                );

                for (String selector : confirmSelectors) {
                    try {
                        WebElement confirmButton = wait.until(ExpectedConditions.elementToBeClickable(
                                By.cssSelector(selector)
                        ));

                        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", confirmButton);
                        log.info("CSS 선택자로 확인 버튼 클릭 완료: {}", selector);
                        return;

                    } catch (Exception e) {
                        continue;
                    }
                }

            } catch (Exception cssError) {
                log.debug("CSS 선택자로 확인 버튼 찾기 실패");
            }

            // 방법 4: Enter 키 누르기 (마지막 수단)
            try {
                Actions actions = new Actions(driver);
                actions.sendKeys(Keys.ENTER).perform();
                log.info("Enter 키로 확인 처리 완료");

            } catch (Exception enterError) {
                log.error("모든 확인 창 처리 방법 실패");
            }

        } catch (Exception e) {
            log.error("확인 창 처리 중 오류 발생: {}", e.getMessage());
        }
    }


    private void login(WebDriver driver, WebDriverWait wait) {
        driver.get("https://www.gwangjusportsinfo.org/space/space_view/1");
        driver.navigate().to("https://www.gwangjusportsinfo.org/login");

        WebElement idElement = driver.findElement(By.cssSelector("input[placeholder='아이디']"));
        idElement.sendKeys("kd2675");

        WebElement pwElement = driver.findElement(By.cssSelector("input[placeholder='비밀번호']"));
        pwElement.sendKeys("Whitered2@");

        // 로그인 버튼 클릭
        WebElement loginButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("input[type='submit'][name='btn_submit'][value='로그인']")
        ));
        loginButton.click();

        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void reserve(WebDriver driver, WebDriverWait wait) {
        driver.get("https://www.gwangjusportsinfo.org/reservation/reservation_view/1/4?agree=1");

        // XPath로 날짜 ID 내에 있는 li 중, b 텍스트가 원하는 시간과 일치하는 li 클릭
        String xpath = String.format("//td[@id='%s']//li[.//b[contains(normalize-space(), '%s')]]", "2025-04-28", "주간(13:00~15:00)");

        WebElement timeSlot = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xpath)));
        timeSlot.click();

        // 특정 요소가 나타날 때까지 대기 (예: 'useCnt' 필드가 로드될 때까지)
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("useCnt")));
        // useCnt 입력 필드 찾아서 '4' 입력
        WebElement inputField = driver.findElement(By.name("useCnt"));
        inputField.sendKeys("4");

        // 2. 체크박스 클릭 및 팝업 열기
        WebElement checkbox = driver.findElement(By.id("userAgreement1"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", checkbox);

        // 3. 팝업이 뜨면 '[닫기]' 버튼을 클릭하여 팝업 닫기
        closePopup(driver, wait);

        // 4. 다음 버튼 클릭 (팝업이 닫힌 후)
        WebElement nextButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input.btn_next.btn_confirm")));
        nextButton.click();

        System.out.println("test");
    }

    private static void closePopup(WebDriver driver, WebDriverWait wait) {
        try {
            // 팝업이 새로운 창(Window)으로 열린 경우 처리
            String mainWindow = driver.getWindowHandle(); // 메인 윈도우 저장
            Set<String> allWindows = driver.getWindowHandles();

            for (String windowHandle : allWindows) {
                if (!windowHandle.equals(mainWindow)) {
                    driver.switchTo().window(windowHandle); // 팝업 윈도우로 전환

                    try {
                        WebElement closeButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//span[text()='[닫기]']")));
                        closeButton.click();
                        System.out.println("팝업 닫기 완료");
                    } catch (Exception e) {
                        System.out.println("팝업 닫기 버튼 클릭 실패");
                    }

                    driver.close(); // 팝업 창 닫기
                    driver.switchTo().window(mainWindow); // 다시 메인 윈도우로 전환
                }
            }
        } catch (Exception e) {
            System.out.println("팝업이 없거나 예외 발생: " + e.getMessage());
        }
    }
}