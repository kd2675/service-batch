package com.service.batch.database.pub.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.database.common.jpa.CommonDateEntity;
import org.example.database.database.auth.entity.UserEntity;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "order_entity")
public class OrderEntity extends CommonDateEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    //b, s
    @Column(name = "order_slct", nullable = false, length = 2)
    private String orderSlct;

    //BTC
    @Column(name = "coin_slct", nullable = false, length = 10)
    private String coinSlct;

    //l, s
    @Column(name = "margin_slct", nullable = false, length = 1)
    private String marginSlct;

    @Max(500)
    @Min(1)
    @Column(name = "margin", nullable = false)
    private Integer margin;

    @Column(name = "price", nullable = false)
    private Double price;

    @Column(name = "cnt", nullable = false)
    private Double cnt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity userEntity;

}