package com.service.batch.database.pub.repository;

import com.example.batch.database.pub.entity.OrderEntity;
import org.example.database.database.auth.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
    List<OrderEntity> findByUserEntityAndOrderSlctAndCoinSlctAndMarginSlct(@NonNull UserEntity userEntity, @NonNull String orderSlct, @NonNull String coinSlct, @NonNull String marginSlct);
    List<OrderEntity> findByUserEntityAndOrderSlctAndCoinSlct(@NonNull UserEntity userEntity, @NonNull String orderSlct, @NonNull String coinSlct);
    Optional<OrderEntity> findByIdIn(@NonNull Collection<Long> ids);
    Optional<OrderEntity> findByUserEntityInAndCoinSlctInAndMarginSlctIn(@NonNull Collection<UserEntity> userEntities, @NonNull Collection<String> coinSlcts, @NonNull Collection<String> marginSlcts);

}