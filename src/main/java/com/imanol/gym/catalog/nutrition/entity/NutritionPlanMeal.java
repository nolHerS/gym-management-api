package com.imanol.gym.catalog.nutrition.entity;

import com.imanol.gym.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "nutrition_plan_meals", uniqueConstraints = {
        @UniqueConstraint(name = "uk_nutrition_plan_meals_order",
                columnNames = {"nutrition_plan_id", "order_index"}),
        @UniqueConstraint(name = "uk_nutrition_plan_meals_name",
                columnNames = {"nutrition_plan_id", "name"})
})
@Getter
@Setter
@NoArgsConstructor
public class NutritionPlanMeal extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "nutrition_plan_id", nullable = false)
    private NutritionPlan nutritionPlan;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    @Column(columnDefinition = "TEXT")
    private String description;

    @OneToMany(mappedBy = "nutritionPlanMeal", fetch = FetchType.LAZY)
    @OrderBy("orderIndex ASC")
    private List<NutritionPlanFood> foods = new ArrayList<>();
}
