package kg.alatoo.sayakat.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record BookingCreateRequest(
        @NotNull Long serviceId,
        Long planId,
        @NotNull LocalDate dateFrom,
        @NotNull LocalDate dateTo,
        @Positive int guests,
        String message) {
}
