package com.service.batch.service.lotto.biz;

import com.service.batch.utils.MattermostUtil;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LottoServiceImplTest {

    @Test
    void extractsWinningNumbersFromHighestRoundOnly() {
        MattermostUtil mattermostUtil = mock(MattermostUtil.class);
        LottoServiceImpl service = new LottoServiceImpl(mattermostUtil);
        WebDriver driver = mock(WebDriver.class);
        WebElement olderRound = round("1235");
        WebElement latestRound = round("1236");
        WebElement roundLabel = textElement("1236회");
        List<WebElement> balls = List.of(
                textElement("12"), textElement("18"), textElement("21"), textElement("29"),
                textElement("34"), textElement("38"), textElement(""), textElement("10")
        );

        when(driver.findElements(any(By.class))).thenReturn(List.of(olderRound, latestRound));
        when(latestRound.findElement(any(By.class))).thenReturn(roundLabel);
        when(latestRound.findElements(any(By.class))).thenReturn(balls);

        assertThat(service.extractWinningNumbers(driver))
                .containsExactly("12", "18", "21", "29", "34", "38", "10");
    }

    @Test
    void rejectsInvalidLatestRoundNumbers() {
        MattermostUtil mattermostUtil = mock(MattermostUtil.class);
        LottoServiceImpl service = new LottoServiceImpl(mattermostUtil);
        WebDriver driver = mock(WebDriver.class);
        WebElement latestRound = round("1236");
        WebElement roundLabel = textElement("1236회");
        List<WebElement> balls = List.of(
                textElement("12"), textElement("18"), textElement("21"), textElement("29"),
                textElement("34"), textElement("38"), textElement("38")
        );

        when(driver.findElements(any(By.class))).thenReturn(List.of(latestRound));
        when(latestRound.findElement(any(By.class))).thenReturn(roundLabel);
        when(latestRound.findElements(any(By.class))).thenReturn(balls);

        assertThatThrownBy(() -> service.extractWinningNumbers(driver))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("7개를 정확히");
    }

    @Test
    void extractsWinningRoundFromHighestRoundOnly() {
        LottoServiceImpl service = new LottoServiceImpl(mock(MattermostUtil.class));
        WebDriver driver = mock(WebDriver.class);
        List<WebElement> rounds = List.of(round("1235"), round("1237"), round("1236"));
        when(driver.findElements(any(By.class))).thenReturn(rounds);

        assertThat(service.extractWinningRound(driver)).isEqualTo(1237);
    }

    @Test
    void findsPurchaseRowForExactWinningRound() {
        LottoServiceImpl service = new LottoServiceImpl(mock(MattermostUtil.class));
        WebElement nextRound = purchaseRow("로또6/45", "1237");
        WebElement winningRound = purchaseRow("로또6/45", "1236");

        assertThat(service.findPurchaseRowForRound(List.of(nextRound, winningRound), 1236))
                .isSameAs(winningRound);
    }

    @Test
    void accountFailureIsPropagatedAndDriverIsClosed() {
        MattermostUtil mattermostUtil = mock(MattermostUtil.class);
        WebDriver driver = mock(WebDriver.class);
        WebDriver.Navigation navigation = mock(WebDriver.Navigation.class);
        LottoServiceImpl service = new LottoServiceImpl(mattermostUtil) {
            @Override
            WebDriver createWebDriver() {
                return driver;
            }
        };
        ReflectionTestUtils.setField(service, "lottoUsername", "user");
        ReflectionTestUtils.setField(service, "lottoPassword", "password");
        when(driver.navigate()).thenReturn(navigation);
        org.mockito.Mockito.doThrow(new WebDriverException("navigation failed"))
                .when(navigation).to("https://www.dhlottery.co.kr/login");

        assertThatThrownBy(service::account)
                .isInstanceOf(WebDriverException.class)
                .hasMessageContaining("navigation failed");
        verify(driver).quit();
    }

    private static WebElement round(String number) {
        WebElement element = mock(WebElement.class);
        when(element.getAttribute("data-ltepsd")).thenReturn(number);
        return element;
    }

    private static WebElement textElement(String text) {
        WebElement element = mock(WebElement.class);
        when(element.getText()).thenReturn(text);
        return element;
    }

    private static WebElement purchaseRow(String product, String round) {
        WebElement row = mock(WebElement.class);
        WebElement productElement = textElement(product);
        WebElement roundElement = textElement(round);
        when(row.findElement(any(By.class))).thenAnswer(invocation ->
                invocation.getArgument(0).toString().contains(".col-name") ? productElement : roundElement);
        return row;
    }
}
