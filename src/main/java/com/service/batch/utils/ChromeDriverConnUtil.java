package com.service.batch.utils;

import org.jsoup.nodes.Document;
import org.openqa.selenium.chrome.ChromeOptions;

public interface ChromeDriverConnUtil {
    Document conn(String url);
    Document conn(String url, ChromeOptions chromeOptions);
}
