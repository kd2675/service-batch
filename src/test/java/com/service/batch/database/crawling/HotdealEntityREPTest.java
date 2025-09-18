package com.service.batch.database.crawling;

import com.service.batch.database.crawling.entity.HotdealEntity;
import com.service.batch.database.crawling.repository.HotdealEntityREP;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = "spring.profiles.active=test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@EnableJpaRepositories(basePackages = "com.service.batch.database.crawling.repository")
@EntityScan(basePackages = "com.service.batch.database.crawling.entity")
class HotdealEntityREPTest {
    @Autowired
    HotdealEntityREP hotdealEntityREP;

    @Test
    @DisplayName("sendYn=N 인 엔티티 list 조회")
    void findAllBySendYnOrderByIdDesc_works() throws Exception {
        for (int i = 0; i < 3; i++) {
            HotdealEntity build = HotdealEntity.builder()
                    .productId(1000L + i).sendYn("n").title("title-" + i)
                    .site("test").shop("test").img("test.jpg")
                    .price(1000).priceSlct("w").priceStr("1000원").link("test").build();
            setDates(build, LocalDateTime.now(), LocalDateTime.now());
            hotdealEntityREP.save(build);
        }
        List<HotdealEntity> found = hotdealEntityREP.findAllBySendYnOrderByIdDesc("n");
        assertThat(found).isNotEmpty();
        assertThat(found.get(0).getSendYn()).isEqualTo("n");
    }

    private static void setDates(Object target, LocalDateTime create, LocalDateTime update) throws Exception {
        Class<?> clazz = target.getClass().getSuperclass(); // CommonDateEntity
        Field createDate = clazz.getDeclaredField("createDate");
        Field updateDate = clazz.getDeclaredField("updateDate");
        createDate.setAccessible(true);
        updateDate.setAccessible(true);
        createDate.set(target, create);
        updateDate.set(target, update);
    }
}