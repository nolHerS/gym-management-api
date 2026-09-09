package com.imanol.gym.catalog.nutrition.entity;

import com.imanol.gym.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "nutrition_plan_foods", uniqueConstraints = {
        @UniqueConstraint(name = "uk_nutrition_plan_foods_meal_food",
                columnNames = {"nutrition_plan_meal_id", "food_id"}),
        @UniqueConstraint(name = "uk_nutrition_plan_foods_meal_order",
                columnNames = {"nutrition_plan_meal_id", "order_index"})
})
@Getter
@Setter
@NoArgsConstructor
public class NutritionPlanFood extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "nutrition_plan_meal_id", nullable = false)
    private NutritionPlanMeal nutritionPlanMeal;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "food_id", nullable = false)
    private Food food;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal quantity;

    @Column(length = 30)
    private String unit;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;
}
