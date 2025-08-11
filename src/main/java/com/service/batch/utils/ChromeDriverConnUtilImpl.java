package com.service.batch.utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Service;

@Service
public class ChromeDriverConnUtilImpl implements ChromeDriverConnUtil {
    public Document conn(String url) {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("headless");
        WebDriver driver = new ChromeDriver(options);

        driver.get(url);
        Document doc = Jsoup.parse(driver.getPageSource());
        driver.quit();

        return doc;
    }

    public Document conn(String url, ChromeOptions chromeOptions) {
        WebDriver driver = new ChromeDriver(chromeOptions);

        driver.get(url);
        Document doc = Jsoup.parse(driver.getPageSource());
        driver.quit();

        return doc;
    }
}
