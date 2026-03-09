package com.service.batch.cron.reader;

import com.service.batch.database.crawling.dto.HotdealDTO;
import com.service.batch.database.crawling.entity.HotdealAlimEntity;
import com.service.batch.database.crawling.entity.HotdealEntity;
import com.service.batch.database.crawling.repository.HotdealAlimEntityREP;
import com.service.batch.database.crawling.repository.HotdealEntityREP;
import com.service.batch.utils.MattermostUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HotdealReaderTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private HotdealEntityREP hotdealEntityREP;

    @Mock
    private HotdealAlimEntityREP hotdealAlimEntityREP;

    @Mock
    private MattermostUtil mattermostUtil;

    private HotdealReader hotdealReader;

    @BeforeEach
    void setUp() {
        hotdealReader = new HotdealReader(restTemplate, hotdealEntityREP, hotdealAlimEntityREP, mattermostUtil);
    }

    @Test
    @DisplayName("기존 데이터가 없을 때 첫 페이지 데이터를 가져오는 비즈니스 로직 테스트")
    void findHotdeal_WithNoExistingData_ShouldFetchFirstPageData() throws Exception {
        // Given - 기존 데이터 없음
        when(hotdealEntityREP.findTop1ByOrderByProductIdDesc()).thenReturn(Arrays.asList());
        when(restTemplate.getForObject(any(), eq(byte[].class)))
            .thenReturn(toUtf8Bytes(createMockInitialHtmlWithProducts("300", false, "100", "200", "300")));
        when(hotdealAlimEntityREP.findBySendYn("n")).thenReturn(Arrays.asList());

        // When
        ListItemReader<HotdealDTO> reader = hotdealReader.findHotdeal();

        // Then - 첫 페이지 데이터를 가져와야 함
        assertThat(reader).isNotNull();
        
        // 첫 번째 아이템 읽기
        HotdealDTO firstItem = reader.read();
        assertThat(firstItem).isNotNull();
        assertThat(firstItem.getProductId()).isEqualTo(100L);
        assertThat(firstItem.getSite()).isEqualTo("Test Site");
        assertThat(firstItem.getShop()).isEqualTo("Test Shop");
        assertThat(firstItem.getSiteIconUrl()).isEqualTo("https://cdn.algumon.com/site-icon/test.png");
        assertThat(firstItem.getDeliveryInfo()).isEqualTo("무료");
        assertThat(firstItem.getPerPriceText()).isEqualTo("");
        assertThat(firstItem.getOriginalLikes()).isEqualTo(0);
        assertThat(firstItem.getOriginalDisLikes()).isEqualTo(0);
        assertThat(firstItem.getOriginalComments()).isEqualTo(0);
        assertThat(firstItem.getCreatedAt()).isEqualTo("2026-03-09T12:10:18.121");
        assertThat(firstItem.getAuthorNickname()).isEqualTo("tester");
        assertThat(firstItem.getEnded()).isFalse();
        assertThat(firstItem.getIsRead()).isFalse();
        assertThat(firstItem.getIsNewWindowOpen()).isTrue();
        assertThat(firstItem.getNowClickCount()).isEqualTo(1);
        
        // RestTemplate이 첫 페이지만 호출되었는지 확인
        verify(restTemplate, times(1)).getForObject(any(), eq(byte[].class));
    }

    @Test
    @DisplayName("기존 데이터가 있을 때 새로운 데이터만 필터링하는 비즈니스 로직 테스트")
    void findHotdeal_WithExistingData_ShouldFilterOnlyNewData() throws Exception {
        // Given - 기존 데이터의 최대 productId가 150
        HotdealEntity existingEntity = createMockHotdealEntity(150L);
        when(hotdealEntityREP.findTop1ByOrderByProductIdDesc())
            .thenReturn(Arrays.asList(existingEntity));
        
        // 첫 번째 페이지: productId 100(기존보다 작음), 200(새로운 데이터)
        // 두 번째 페이지: productId 300, 400 (모두 새로운 데이터)  
        // 세 번째 페이지: productId 140, 160 (140은 기존보다 작음, 160은 새로운 데이터) -> limit2=true이므로 계속
        // 네 번째 페이지: productId 130, 140 (모두 기존보다 작음) -> limit2=false이므로 중단
        when(restTemplate.getForObject(any(), eq(byte[].class)))
            .thenReturn(toUtf8Bytes(createMockInitialHtmlWithProducts("200", true, "100", "200")))          // 페이지 0 (HTML)
            .thenReturn(toUtf8Bytes(createMockListV2JsonWithProducts("400", true, "300", "400")))           // 페이지 1 (JSON)
            .thenReturn(toUtf8Bytes(createMockListV2JsonWithProducts("160", true, "140", "160")))           // 페이지 2 (JSON)
            .thenReturn(toUtf8Bytes(createMockListV2JsonWithProducts("140", true, "130", "140")));          // 페이지 3 (JSON)
        
        when(hotdealAlimEntityREP.findBySendYn("n")).thenReturn(Arrays.asList());

        // When
        ListItemReader<HotdealDTO> reader = hotdealReader.findHotdeal();

        // Then - productId > 150인 데이터만 포함되어야 함
        HotdealDTO item1 = reader.read();
        HotdealDTO item2 = reader.read();
        HotdealDTO item3 = reader.read();
        HotdealDTO item4 = reader.read();
        HotdealDTO item5 = reader.read(); // 더 이상 데이터가 없어야 함

        assertThat(item1).isNotNull();
        assertThat(item1.getProductId()).isEqualTo(200L); // 첫 번째 페이지에서 150보다 큰 값
        
        assertThat(item2).isNotNull();
        assertThat(item2.getProductId()).isEqualTo(300L); // 두 번째 페이지에서 150보다 큰 값
        
        assertThat(item3).isNotNull();
        assertThat(item3.getProductId()).isEqualTo(400L); // 두 번째 페이지에서 150보다 큰 값
        
        assertThat(item4).isNotNull();
        assertThat(item4.getProductId()).isEqualTo(160L); // 세 번째 페이지에서 150보다 큰 값
        
        assertThat(item5).isNull(); // 네 번째 페이지는 새 데이터가 없어서(limit2=false) 중단
        
        // RestTemplate이 4번 호출되었는지 확인 (4페이지까지 가져온 후 중단)
        verify(restTemplate, times(4)).getForObject(any(), eq(byte[].class));
    }

    @Test
    @DisplayName("세 번째 페이지에서 새 데이터가 없을 때 중단하는 비즈니스 로직 테스트")
    void findHotdeal_WhenThirdPageHasNoNewData_ShouldStop() throws Exception {
        // Given - 기존 데이터의 최대 productId가 150
        HotdealEntity existingEntity = createMockHotdealEntity(150L);
        when(hotdealEntityREP.findTop1ByOrderByProductIdDesc())
            .thenReturn(Arrays.asList(existingEntity));
        
        // 첫 번째 페이지: productId 100, 200 (200은 새로운 데이터)
        // 두 번째 페이지: productId 300, 400 (모두 새로운 데이터)
        // 세 번째 페이지: productId 120, 140 (모두 기존보다 작음) -> limit2=false로 중단
        when(restTemplate.getForObject(any(), eq(byte[].class)))
            .thenReturn(toUtf8Bytes(createMockInitialHtmlWithProducts("200", true, "100", "200")))          // 페이지 0 (HTML)
            .thenReturn(toUtf8Bytes(createMockListV2JsonWithProducts("400", true, "300", "400")))           // 페이지 1 (JSON)
            .thenReturn(toUtf8Bytes(createMockListV2JsonWithProducts("140", true, "120", "140")));          // 페이지 2 (JSON) - 새 데이터 없음
        
        when(hotdealAlimEntityREP.findBySendYn("n")).thenReturn(Arrays.asList());

        // When
        ListItemReader<HotdealDTO> reader = hotdealReader.findHotdeal();

        // Then
        HotdealDTO item1 = reader.read();
        HotdealDTO item2 = reader.read();
        HotdealDTO item3 = reader.read();
        HotdealDTO item4 = reader.read(); // 더 이상 데이터가 없어야 함

        assertThat(item1).isNotNull();
        assertThat(item1.getProductId()).isEqualTo(200L);
        
        assertThat(item2).isNotNull();
        assertThat(item2.getProductId()).isEqualTo(300L);
        
        assertThat(item3).isNotNull();
        assertThat(item3.getProductId()).isEqualTo(400L);
        
        assertThat(item4).isNull(); // 세 번째 페이지에서 새 데이터 없어서 중단
        
        // RestTemplate이 3번 호출되었는지 확인 (3페이지에서 중단)
        verify(restTemplate, times(3)).getForObject(any(), eq(byte[].class));
    }

    @Test
    @DisplayName("기존 데이터와 동일한 productId가 발견될 때 중단하는 비즈니스 로직 테스트")
    void findHotdeal_WhenFindExistingProductId_ShouldStopFetching() throws Exception {
        // Given - 기존 데이터의 productId가 200
        HotdealEntity existingEntity = createMockHotdealEntity(200L);
        when(hotdealEntityREP.findTop1ByOrderByProductIdDesc())
            .thenReturn(Arrays.asList(existingEntity));
        
        // 첫 번째 페이지에 기존 productId 200이 포함됨 (limit1 = true가 되어 중단)
        when(restTemplate.getForObject(any(), eq(byte[].class)))
            .thenReturn(toUtf8Bytes(createMockInitialHtmlWithProducts("400", true, "200", "300", "400")));
        
        when(hotdealAlimEntityREP.findBySendYn("n")).thenReturn(Arrays.asList());

        // When
        ListItemReader<HotdealDTO> reader = hotdealReader.findHotdeal();

        // Then - productId > 200인 데이터만 포함되고, limit1=true로 인해 루프 중단
        HotdealDTO item1 = reader.read();
        HotdealDTO item2 = reader.read();
        HotdealDTO item3 = reader.read(); // 더 이상 데이터가 없어야 함

        assertThat(item1).isNotNull();
        assertThat(item1.getProductId()).isEqualTo(300L);
        
        assertThat(item2).isNotNull();
        assertThat(item2.getProductId()).isEqualTo(400L);
        
        assertThat(item3).isNull(); // 더 이상 데이터 없음
        
        // limit1=true로 인해 첫 번째 페이지만 호출되고 중단
        verify(restTemplate, times(1)).getForObject(any(), eq(byte[].class));
    }

    @Test
    @DisplayName("새로운 데이터가 없는 페이지를 만날 때 중단하는 비즈니스 로직 테스트")
    void findHotdeal_WhenNoNewDataInPage_ShouldStop() throws Exception {
        // Given - 기존 데이터의 productId가 500
        HotdealEntity existingEntity = createMockHotdealEntity(500L);
        when(hotdealEntityREP.findTop1ByOrderByProductIdDesc())
            .thenReturn(Arrays.asList(existingEntity));
        
        // 첫 번째 페이지: 600, 700 (모두 새로운 데이터)
        // 두 번째 페이지: 300, 400 (모두 기존 데이터보다 작음 - limit2=false가 되어 중단)
        when(restTemplate.getForObject(any(), eq(byte[].class)))
            .thenReturn(toUtf8Bytes(createMockInitialHtmlWithProducts("700", true, "600", "700")))          // 페이지 0 (HTML)
            .thenReturn(toUtf8Bytes(createMockListV2JsonWithProducts("400", true, "300", "400")));          // 페이지 1 (JSON)
        
        when(hotdealAlimEntityREP.findBySendYn("n")).thenReturn(Arrays.asList());

        // When
        ListItemReader<HotdealDTO> reader = hotdealReader.findHotdeal();

        // Then - limit2=false로 인해 루프 중단
        HotdealDTO item1 = reader.read();
        HotdealDTO item2 = reader.read();
        HotdealDTO item3 = reader.read(); // 더 이상 데이터가 없어야 함

        assertThat(item1).isNotNull();
        assertThat(item1.getProductId()).isEqualTo(600L);
        
        assertThat(item2).isNotNull();
        assertThat(item2.getProductId()).isEqualTo(700L);
        
        assertThat(item3).isNull(); // 더 이상 데이터 없음
        
        // limit2=false로 인해 두 번째 페이지에서 중단
        verify(restTemplate, times(2)).getForObject(any(), eq(byte[].class));
    }

    @Test
    @DisplayName("새로운 데이터가 계속 있을 때 최대 5페이지까지 가져오는 비즈니스 로직 테스트")
    void findHotdeal_WithContinuousNewData_ShouldFetchUpTo5Pages() throws Exception {
        // Given - 기존 데이터의 productId가 100
        HotdealEntity existingEntity = createMockHotdealEntity(100L);
        when(hotdealEntityREP.findTop1ByOrderByProductIdDesc())
            .thenReturn(Arrays.asList(existingEntity));
            
        // 각 페이지마다 기존 데이터보다 큰 productId만 반환 (5페이지 모두 새로운 데이터)
        when(restTemplate.getForObject(any(), eq(byte[].class)))
            .thenReturn(toUtf8Bytes(createMockInitialHtmlWithProducts("300", true, "200", "300")))          // 페이지 0 (HTML)
            .thenReturn(toUtf8Bytes(createMockListV2JsonWithProducts("500", true, "400", "500")))           // 페이지 1 (JSON)
            .thenReturn(toUtf8Bytes(createMockListV2JsonWithProducts("700", true, "600", "700")))           // 페이지 2 (JSON)
            .thenReturn(toUtf8Bytes(createMockListV2JsonWithProducts("900", true, "800", "900")))           // 페이지 3 (JSON)
            .thenReturn(toUtf8Bytes(createMockListV2JsonWithProducts("1100", true, "1000", "1100")))        // 페이지 4 (JSON)
            .thenReturn(toUtf8Bytes(createMockListV2JsonWithProducts("1300", true, "1200", "1300")));       // 페이지 5 (호출되지 않아야 함)
            
        when(hotdealAlimEntityREP.findBySendYn("n")).thenReturn(Arrays.asList());

        // When
        ListItemReader<HotdealDTO> reader = hotdealReader.findHotdeal();

        // Then - 최대 5페이지의 데이터가 포함되어야 함
        int itemCount = 0;
        HotdealDTO item;
        while ((item = reader.read()) != null) {
            itemCount++;
            assertThat(item.getProductId()).isGreaterThan(100L); // 모두 기존 데이터보다 커야 함
        }
        
        assertThat(itemCount).isEqualTo(10); // 5페이지 * 2개씩 = 10개
        
        // RestTemplate이 정확히 5번 호출되었는지 확인
        verify(restTemplate, times(5)).getForObject(any(), eq(byte[].class));
    }

    @Test
    @DisplayName("HTML 파싱 에러 발생 시 빈 리스트를 반환하는 비즈니스 로직 테스트")
    void findHotdeal_WithHtmlParsingError_ShouldReturnEmptyResult() throws Exception {
        // Given
        when(hotdealEntityREP.findTop1ByOrderByProductIdDesc()).thenReturn(Arrays.asList());
        when(restTemplate.getForObject(any(), eq(byte[].class)))
            .thenReturn(toUtf8Bytes("Invalid HTML content")); // 파싱할 수 없는 HTML
        when(hotdealAlimEntityREP.findBySendYn("n")).thenReturn(Arrays.asList());

        // When
        ListItemReader<HotdealDTO> reader = hotdealReader.findHotdeal();

        // Then - 파싱 에러로 인해 빈 결과가 반환되어야 함
        HotdealDTO item = reader.read();
        assertThat(item).isNull();
    }

    @Test
    @DisplayName("가격 파싱 로직 테스트 - 원화와 달러 처리")
    void findHotdeal_ShouldParseKoreanWonAndDollarPricesCorrectly() throws Exception {
        // Given
        when(hotdealEntityREP.findTop1ByOrderByProductIdDesc()).thenReturn(Arrays.asList());
        when(restTemplate.getForObject(any(), eq(byte[].class)))
            .thenReturn(toUtf8Bytes(createMockInitialHtmlWithCustomPrices(
                    "200",
                    false,
                    new String[][]{
                            {"100", "10,000원"},
                            {"200", "$50.99"}
                    }
            )));
        when(hotdealAlimEntityREP.findBySendYn("n")).thenReturn(Arrays.asList());

        // When
        ListItemReader<HotdealDTO> reader = hotdealReader.findHotdeal();

        // Then
        HotdealDTO wonItem = reader.read(); // 원화 상품
        HotdealDTO dollarItem = reader.read(); // 달러 상품

        assertThat(wonItem).isNotNull();
        assertThat(wonItem.getPriceSlct()).isEqualTo("w"); // 원화
        assertThat(wonItem.getPrice()).isEqualTo(10000);

        assertThat(dollarItem).isNotNull();
        assertThat(dollarItem.getPriceSlct()).isEqualTo("d"); // 달러
        assertThat(dollarItem.getPrice()).isEqualTo(50);
    }

    private HotdealEntity createMockHotdealEntity(Long productId) {
        HotdealEntity mockHotdeal = HotdealEntity.builder()
                .id(1L)
                .productId(productId)
                .title("Mock Hotdeal")
                .build();
        return mockHotdeal;
    }

    private String createMockInitialHtmlWithProducts(String endCursor, boolean hasNext, String... productIds) {
        String[][] productPricePairs = new String[productIds.length][2];
        for (int i = 0; i < productIds.length; i++) {
            productPricePairs[i][0] = productIds[i];
            productPricePairs[i][1] = "10,000원";
        }
        return createMockInitialHtmlWithCustomPrices(endCursor, hasNext, productPricePairs);
    }

    private String createMockInitialHtmlWithCustomPrices(String endCursor, boolean hasNext, String[][] productPricePairs) {
        StringBuilder contentsBuilder = new StringBuilder();
        for (int i = 0; i < productPricePairs.length; i++) {
            String productId = productPricePairs[i][0];
            String price = productPricePairs[i][1];
            if (i > 0) {
                contentsBuilder.append(",");
            }
            contentsBuilder.append(createJsDealLiteral(productId, price));
        }

        String encodedSignature = Base64.getEncoder().encodeToString("test-h_111".getBytes(StandardCharsets.UTF_8));

        return "<html><body><script>" +
                "kit.start(app, element, {data:[{},{data:{deals:{contents:[" + contentsBuilder +
                "],endCursor:\"" + endCursor + "\",hasNext:" + hasNext + "},s:\"" + encodedSignature + "\"}}]});" +
                "</script></body></html>";
    }

    private String createMockListV2JsonWithProducts(String endCursor, boolean hasNext, String... productIds) {
        StringBuilder contentsBuilder = new StringBuilder();
        for (int i = 0; i < productIds.length; i++) {
            if (i > 0) {
                contentsBuilder.append(",");
            }
            contentsBuilder.append(createJsonDealLiteral(productIds[i], "10,000원"));
        }

        return "{"
                + "\"contents\":[" + contentsBuilder + "],"
                + "\"endCursor\":\"" + endCursor + "\","
                + "\"hasNext\":" + hasNext + ","
                + "\"h\":\"test-h\","
                + "\"t\":\"111\""
                + "}";
    }

    private String createJsDealLiteral(String productId, String price) {
        return "{"
                + "id:" + productId + ","
                + "siteName:\"Test Site\","
                + "siteIconUrl:\"https://cdn.algumon.com/site-icon/test.png\","
                + "storeName:\"Test Shop\","
                + "rankNum:null,"
                + "title:\"Product " + productId + "\","
                + "thumbnailUrl:\"https://cdn.algumon.com/image-v2/deal/" + productId + ".png?d=200x200\","
                + "price:\"" + price + "\","
                + "deliveryInfo:\"무료\","
                + "perPriceText:\"\","
                + "originalUrl:\"https://www.algumon.com/l/d/" + productId + "\","
                + "originalLikes:0,"
                + "originalDisLikes:0,"
                + "originalComments:0,"
                + "createdAt:\"2026-03-09T12:10:18.121\","
                + "boughtAt:null,"
                + "userWant:false,"
                + "userBought:false,"
                + "wantCount:0,"
                + "boughtCount:0,"
                + "commentCount:0,"
                + "authorNickname:\"tester\","
                + "legacyEditUrl:\"/channel/unknown/" + productId + "/modify\","
                + "ended:false,"
                + "blockNewComments:false,"
                + "exchangeRate:null,"
                + "isRead:false,"
                + "isNewWindowOpen:true,"
                + "nowClickCount:1"
                + "}";
    }

    private String createJsonDealLiteral(String productId, String price) {
        return "{"
                + "\"id\":" + productId + ","
                + "\"siteName\":\"Test Site\","
                + "\"siteIconUrl\":\"https://cdn.algumon.com/site-icon/test.png\","
                + "\"storeName\":\"Test Shop\","
                + "\"rankNum\":null,"
                + "\"title\":\"Product " + productId + "\","
                + "\"thumbnailUrl\":\"https://cdn.algumon.com/image-v2/deal/" + productId + ".png?d=200x200\","
                + "\"price\":\"" + price + "\","
                + "\"deliveryInfo\":\"무료\","
                + "\"perPriceText\":\"\","
                + "\"originalUrl\":\"https://www.algumon.com/l/d/" + productId + "\","
                + "\"originalLikes\":0,"
                + "\"originalDisLikes\":0,"
                + "\"originalComments\":0,"
                + "\"createdAt\":\"2026-03-09T12:10:18.121\","
                + "\"boughtAt\":null,"
                + "\"userWant\":false,"
                + "\"userBought\":false,"
                + "\"wantCount\":0,"
                + "\"boughtCount\":0,"
                + "\"commentCount\":0,"
                + "\"authorNickname\":\"tester\","
                + "\"legacyEditUrl\":\"/channel/unknown/" + productId + "/modify\","
                + "\"ended\":false,"
                + "\"blockNewComments\":false,"
                + "\"exchangeRate\":null,"
                + "\"isRead\":false,"
                + "\"isNewWindowOpen\":true,"
                + "\"nowClickCount\":1"
                + "}";
    }

    private byte[] toUtf8Bytes(String value) {
        return value.getBytes(StandardCharsets.UTF_8);
    }
}
