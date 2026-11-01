package kg.alatoo.sayakat.dto.request;

import java.time.LocalDate;
import java.util.Set;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import kg.alatoo.sayakat.entity.enums.PlanAlgorithm;
import kg.alatoo.sayakat.entity.enums.PoiCategory;
import kg.alatoo.sayakat.planner.domain.Pace;

public record PlanRequest(
        @NotNull LocalDate startDate,
        @Min(1) @Max(21) int days,
        @Positive int budgetKgs,
        @NotNull Pace pace,
        Set<PoiCategory> interests,
        @NotNull Long startPoiId,
        Long endPoiId,
        PlanAlgorithm algorithm) {
}
