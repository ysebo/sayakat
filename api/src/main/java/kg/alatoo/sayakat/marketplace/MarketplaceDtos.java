package kg.alatoo.sayakat.marketplace;

import java.time.Instant;
import java.time.LocalDate;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public final class MarketplaceDtos {

    private MarketplaceDtos() {
    }

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

    public record ServiceResponse(Long id, Long providerId, String providerName, ServiceType type, String title,
                                  String description, int priceKgs, PriceUnit priceUnit, Long poiId,
                                  Double lat, Double lon, Integer capacity, boolean active) {

        public static ServiceResponse of(ServiceOffering s) {
            Double lat = s.getLocation() != null ? s.getLocation().getY()
                    : s.getPoi() != null ? s.getPoi().lat() : null;
            Double lon = s.getLocation() != null ? s.getLocation().getX()
                    : s.getPoi() != null ? s.getPoi().lon() : null;
            return new ServiceResponse(s.getId(), s.getProvider().getId(), s.getProvider().getDisplayName(),
                    s.getType(), s.getTitle(), s.getDescription(), s.getPriceKgs(), s.getPriceUnit(),
                    s.getPoi() == null ? null : s.getPoi().getId(), lat, lon, s.getCapacity(), s.isActive());
        }
    }

    public record BookingCreateRequest(
            @NotNull Long serviceId,
            Long planId,
            @NotNull LocalDate dateFrom,
            @NotNull LocalDate dateTo,
            @Positive int guests,
            String message) {
    }

    public record BookingDecision(String comment) {
    }

    public record BookingResponse(Long id, Long serviceId, String serviceTitle, ServiceType serviceType,
                                  String providerName, Long touristId, String touristName, Long planId,
                                  LocalDate dateFrom, LocalDate dateTo, int guests, String message,
                                  BookingStatus status, String providerComment, Instant createdAt) {

        public static BookingResponse of(BookingRequest b) {
            ServiceOffering s = b.getService();
            return new BookingResponse(b.getId(), s.getId(), s.getTitle(), s.getType(),
                    s.getProvider().getDisplayName(), b.getTourist().getId(), b.getTourist().getFullName(),
                    b.getPlanId(), b.getDateFrom(), b.getDateTo(), b.getGuests(), b.getMessage(), b.getStatus(),
                    b.getProviderComment(), b.getCreatedAt());
        }
    }
}
