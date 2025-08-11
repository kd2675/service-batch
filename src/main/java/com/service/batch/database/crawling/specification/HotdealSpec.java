package com.service.batch.database.crawling.specification;

import com.service.batch.database.crawling.entity.HotdealEntity;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class HotdealSpec {
    public static Specification<HotdealEntity> searchWith(final List<String> text) {
        return ((root, query, builder) -> {
            List<Predicate> predicates0 = new ArrayList<>();

            for (String s : text) {
                if (StringUtils.hasText(s)) {
                    predicates0.add(builder.like(root.get("title"), "%" + s + "%"));
                }
            }

            query.orderBy(builder.desc(root.get("id")));

            return builder.and(predicates0.toArray(new Predicate[0]));
        });
    }
}
