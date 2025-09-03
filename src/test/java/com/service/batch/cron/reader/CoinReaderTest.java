package com.service.batch.cron.reader;

import com.service.batch.cron.step.CoinStep;
import com.service.batch.database.crawling.entity.CoinEntity;
import com.service.batch.database.crawling.repository.CoinREP;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.support.ListItemReader;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CoinReaderTest {

    @Mock
    private CoinREP coinREP;

    @Mock
    private EntityManagerFactory entityManagerFactory;

    private CoinReader coinReader;

    @BeforeEach
    void setUp() {
        coinReader = new CoinReader(coinREP);
    }

    @Test
    @DisplayName("jpaPagingItemReader가 정상적으로 생성되는지 테스트")
    void jpaPagingItemReader_ShouldBeCreatedSuccessfully() {
        // When
        JpaPagingItemReader<CoinEntity> reader = coinReader.jpaPagingItemReader(entityManagerFactory);

        // Then
        assertThat(reader).isNotNull();
        assertThat(reader.getName()).isEqualTo("jpaPagingItemReader");
        assertThat(reader.getPageSize()).isEqualTo(CoinStep.PAGE_SIZE);
    }

    @Test
    @DisplayName("jpaPagingItemReader가 DelJpaPagingItemReader 타입으로 생성되는지 테스트")
    void jpaPagingItemReader_ShouldBeDelJpaPagingItemReaderType() {
        // When
        JpaPagingItemReader<CoinEntity> reader = coinReader.jpaPagingItemReader(entityManagerFactory);

        // Then
        assertThat(reader).isNotNull();
        assertThat(reader.getClass().getSimpleName()).contains("DelJpaPagingItemReader");
    }

    @Test
    @DisplayName("findTop10ByOrderByIdDesc ListItemReader가 정상적으로 생성되는지 테스트")
    void findTop10ByOrderByIdDesc_ShouldBeCreatedSuccessfully() throws Exception {
        // Given
        List<CoinEntity> mockCoins = createMockCoinEntities(10);
        when(coinREP.findTop10ByOrderByIdDesc()).thenReturn(mockCoins);

        // When
        ListItemReader<CoinEntity> reader = coinReader.findTop10ByOrderByIdDesc(entityManagerFactory);

        // Then
        assertThat(reader).isNotNull();
        CoinEntity firstItem = reader.read();
        assertThat(firstItem).isNotNull();
        
        // Repository 메서드가 호출되었는지 검증
        verify(coinREP, times(1)).findTop10ByOrderByIdDesc();
    }

    @Test
    @DisplayName("findTop1ByOrderByIdDesc ListItemReader가 정상적으로 생성되는지 테스트")
    void findTop1ByOrderByIdDesc_ShouldBeCreatedSuccessfully() throws Exception {
        // Given
        List<CoinEntity> mockCoins = createMockCoinEntities(1);
        when(coinREP.findTop1ByOrderByIdDesc()).thenReturn(mockCoins);

        // When
        ListItemReader<CoinEntity> reader = coinReader.findTop1ByOrderByIdDesc(entityManagerFactory);

        // Then
        assertThat(reader).isNotNull();
        CoinEntity firstItem = reader.read();
        assertThat(firstItem).isNotNull();
        
        // Repository 메서드가 호출되었는지 검증
        verify(coinREP, times(1)).findTop1ByOrderByIdDesc();
    }

    @Test
    @DisplayName("findTop10ByOrderByIdDesc에서 모든 아이템을 순서대로 읽을 수 있는지 테스트")
    void findTop10ByOrderByIdDesc_ShouldReadAllItemsInOrder() throws Exception {
        // Given
        List<CoinEntity> mockCoins = createMockCoinEntities(3);
        when(coinREP.findTop10ByOrderByIdDesc()).thenReturn(mockCoins);

        // When
        ListItemReader<CoinEntity> reader = coinReader.findTop10ByOrderByIdDesc(entityManagerFactory);

        // Then
        CoinEntity item1 = reader.read();
        CoinEntity item2 = reader.read();
        CoinEntity item3 = reader.read();
        CoinEntity item4 = reader.read(); // 4번째는 null이어야 함

        assertThat(item1).isNotNull();
        assertThat(item2).isNotNull();
        assertThat(item3).isNotNull();
        assertThat(item4).isNull();
    }

    @Test
    @DisplayName("상수 값들이 올바르게 정의되어 있는지 테스트")
    void constants_ShouldBeDefinedCorrectly() {
        // Then
        assertThat(CoinReader.FIND_COIN_ENTITY_BEFORE_CREATE_DATE).isEqualTo("findCoinEntityBeforeCreateDate");
        assertThat(CoinReader.FIND_TOP_10_BY_ORDER_BY_ID_DESC).isEqualTo("findTop10ByOrderByIdDesc");
        assertThat(CoinReader.FIND_TOP_1_BY_ORDER_BY_ID_DESC).isEqualTo("findTop1ByOrderByIdDesc");
    }

    @Test
    @DisplayName("jpaPagingItemReader에 null EntityManagerFactory 전달 시 정상 동작 테스트")
    void jpaPagingItemReader_WithNullEntityManagerFactory_ShouldCreateReader() {
        // When
        JpaPagingItemReader<CoinEntity> reader = coinReader.jpaPagingItemReader(null);

        // Then
        // Reader 자체는 생성되지만 실제 사용 시 문제가 발생할 수 있음
        assertThat(reader).isNotNull();
    }

    @Test
    @DisplayName("findTop10ByOrderByIdDesc에서 빈 리스트 반환 시 정상 동작 테스트")
    void findTop10ByOrderByIdDesc_WithEmptyList_ShouldHandleGracefully() throws Exception {
        // Given
        when(coinREP.findTop10ByOrderByIdDesc()).thenReturn(Arrays.asList());

        // When
        ListItemReader<CoinEntity> reader = coinReader.findTop10ByOrderByIdDesc(entityManagerFactory);

        // Then
        assertThat(reader).isNotNull();
        assertThat(reader.read()).isNull(); // 빈 리스트의 경우 null 반환
    }

    @Test
    @DisplayName("findTop1ByOrderByIdDesc에서 빈 리스트 반환 시 정상 동작 테스트")
    void findTop1ByOrderByIdDesc_WithEmptyList_ShouldHandleGracefully() throws Exception {
        // Given
        when(coinREP.findTop1ByOrderByIdDesc()).thenReturn(Arrays.asList());

        // When
        ListItemReader<CoinEntity> reader = coinReader.findTop1ByOrderByIdDesc(entityManagerFactory);

        // Then
        assertThat(reader).isNotNull();
        assertThat(reader.read()).isNull(); // 빈 리스트의 경우 null 반환
    }

    @Test
    @DisplayName("여러 번 호출 시 서로 다른 인스턴스가 생성되는지 테스트")
    void readers_ShouldCreateDifferentInstances() {
        // Given
        List<CoinEntity> mockCoins = createMockCoinEntities(5);
        when(coinREP.findTop10ByOrderByIdDesc()).thenReturn(mockCoins);
        when(coinREP.findTop1ByOrderByIdDesc()).thenReturn(mockCoins);

        // When
        JpaPagingItemReader<CoinEntity> jpaPagingReader1 = coinReader.jpaPagingItemReader(entityManagerFactory);
        JpaPagingItemReader<CoinEntity> jpaPagingReader2 = coinReader.jpaPagingItemReader(entityManagerFactory);
        
        ListItemReader<CoinEntity> listReader1 = coinReader.findTop10ByOrderByIdDesc(entityManagerFactory);
        ListItemReader<CoinEntity> listReader2 = coinReader.findTop1ByOrderByIdDesc(entityManagerFactory);

        // Then
        assertThat(jpaPagingReader1).isNotSameAs(jpaPagingReader2);
        assertThat(listReader1).isNotSameAs(listReader2);
    }

    @Test
    @DisplayName("Repository 메서드 호출 횟수 검증 테스트")
    void repositoryMethods_ShouldBeCalledCorrectTimes() {
        // Given
        List<CoinEntity> mockCoins = createMockCoinEntities(5);
        when(coinREP.findTop10ByOrderByIdDesc()).thenReturn(mockCoins);
        when(coinREP.findTop1ByOrderByIdDesc()).thenReturn(mockCoins);

        // When
        coinReader.findTop10ByOrderByIdDesc(entityManagerFactory);
        coinReader.findTop10ByOrderByIdDesc(entityManagerFactory);
        coinReader.findTop1ByOrderByIdDesc(entityManagerFactory);

        // Then
        verify(coinREP, times(2)).findTop10ByOrderByIdDesc();
        verify(coinREP, times(1)).findTop1ByOrderByIdDesc();
    }

    @Test
    @DisplayName("Reader들이 null이 아닌 유효한 객체를 반환하는지 테스트")
    void allReaderMethods_ShouldReturnNonNullReaders() {
        // Given
        when(coinREP.findTop10ByOrderByIdDesc()).thenReturn(createMockCoinEntities(1));
        when(coinREP.findTop1ByOrderByIdDesc()).thenReturn(createMockCoinEntities(1));

        // When
        JpaPagingItemReader<CoinEntity> jpaPagingReader = coinReader.jpaPagingItemReader(entityManagerFactory);
        ListItemReader<CoinEntity> top10Reader = coinReader.findTop10ByOrderByIdDesc(entityManagerFactory);
        ListItemReader<CoinEntity> top1Reader = coinReader.findTop1ByOrderByIdDesc(entityManagerFactory);

        // Then
        assertThat(jpaPagingReader).isNotNull();
        assertThat(top10Reader).isNotNull();
        assertThat(top1Reader).isNotNull();
    }

    @Test
    @DisplayName("CoinReader 생성자가 정상적으로 동작하는지 테스트")
    void constructor_ShouldWorkProperly() {
        // When
        CoinReader reader = new CoinReader(coinREP);

        // Then
        assertThat(reader).isNotNull();
        
        // Reader가 정상적으로 동작하는지 확인
        when(coinREP.findTop1ByOrderByIdDesc()).thenReturn(createMockCoinEntities(1));
        ListItemReader<CoinEntity> itemReader = reader.findTop1ByOrderByIdDesc(entityManagerFactory);
        assertThat(itemReader).isNotNull();
    }

    private List<CoinEntity> createMockCoinEntities(int count) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(i -> {
                    CoinEntity entity = new CoinEntity();
                    // 실제 CoinEntity 구조에 따라 필드 설정이 필요할 수 있음
                    return entity;
                })
                .toList();
    }
}