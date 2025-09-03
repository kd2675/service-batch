package com.service.batch.database.crawling.repository;

import com.service.batch.database.crawling.entity.HotdealEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(properties = "spring.profiles.active=test")
@Import(JpaAuditingTestConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@EnableJpaRepositories(basePackages = "com.service.batch.database.crawling.repository")
@EntityScan(basePackages = "com.service.batch.database.crawling.entity")
class HotdealEntityREPTest {
    @Autowired
    HotdealEntityREP hotdealEntityREP;

    @Test
    @DisplayName("sendYn=N 인 엔티티 list 조회")
    void findAllBySendYnOrderByIdDesc_works() {
        for (int i = 0; i < 3; i++) {
            hotdealEntityREP.save(HotdealEntity.builder()
                    .productId(1000L + i).sendYn("n").title("title-" + i)
                    .site("test").shop("test").img("test.jpg")
                    .price(1000).priceSlct("w").priceStr("1000원").link("test").build());
        }
        List<HotdealEntity> found = hotdealEntityREP.findAllBySendYnOrderByIdDesc("n");
        assertThat(found).isNotEmpty();
        assertThat(found.get(0).getSendYn()).isEqualTo("n");
    }
}