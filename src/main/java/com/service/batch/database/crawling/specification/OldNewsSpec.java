package com.service.batch.database.crawling.specification;

import com.service.batch.database.crawling.entity.OldNewsEntity;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class OldNewsSpec {
    public static Specification<OldNewsEntity> searchWith(final List<String> text) {
        return ((root, query, builder) -> {
            List<Predicate> predicates0 = new ArrayList<>();
            List<Predicate> predicates1 = new ArrayList<>();

            for (String s : text) {
                if (StringUtils.hasText(s)) {
                    predicates0.add(builder.like(root.get("title"), "%" + s + "%"));
                }
            }

            for (String s : text) {
                if (StringUtils.hasText(s)) {
                    predicates1.add(builder.like(root.get("content"), "%" + s + "%"));
                }
            }

            query.orderBy(builder.desc(root.get("id")));

            return builder.or(builder.and(predicates0.toArray(new Predicate[0])), builder.and(predicates1.toArray(new Predicate[0])));
        });
    }
}
