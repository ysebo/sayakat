package kg.alatoo.sayakat.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import kg.alatoo.sayakat.entity.enums.PriceUnit;
import kg.alatoo.sayakat.entity.enums.ServiceType;

public record ServiceRequest(
        @NotNull ServiceType type,
        @NotBlank String title,
        String description,
        @PositiveOrZero int priceKgs,
        @NotNull PriceUnit priceUnit,
        Long poiId,
        @Min(39) @Max(44) Double lat,
        @Min(69) @Max(81) Double lon,
        @Positive Integer capacity,
        Boolean active) {
}
