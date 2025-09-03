package com.service.batch.database.batch.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.database.common.jpa.CommonDateEntity;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@Entity
@Table(name = "RESET_POINT")
public class ResetPointEntity extends CommonDateEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "point_id", nullable = false)
    private int pointId;

    @ColumnDefault("")
    @Column(name = "point_explain", nullable = true, length = 255, updatable = false)
    private String pointExplain;

    @ColumnDefault("'n'")
    @Column(name = "reset_yn", nullable = false, length = 1)
    private String resetYn;

    public void setResetY() {
        this.resetYn = "y";
    }
}
