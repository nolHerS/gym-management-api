package com.imanol.gym.catalog.nutrition.entity;

import com.imanol.gym.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.BatchSize;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@BatchSize(size = 50)
@Table(name = "foods", uniqueConstraints = @UniqueConstraint(
        name = "uk_foods_name", columnNames = "name"))
@Getter
@Setter
@NoArgsConstructor
public class Food extends BaseEntity {
    @Column(nullable = false, length = 150)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = true, precision = 10, scale = 2)
    private BigDecimal calories;

    @Column(nullable = true, precision = 10, scale = 2)
    private BigDecimal protein;

    @Column(nullable = true, precision = 10, scale = 2)
    private BigDecimal carbohydrates;

    @Column(nullable = true, precision = 10, scale = 2)
    private BigDecimal fats;

    @Column(name = "serving_size", precision = 10, scale = 2)
    private BigDecimal servingSize;

    @Column(name = "serving_unit", length = 30)
    private String servingUnit;

    @Column(nullable = false)
    private Boolean active = true;
}
