package kg.alatoo.sayakat.dto.response;

import java.time.Instant;
import java.time.LocalDate;

import kg.alatoo.sayakat.entity.BookingRequest;
import kg.alatoo.sayakat.entity.ServiceOffering;
import kg.alatoo.sayakat.entity.enums.BookingStatus;
import kg.alatoo.sayakat.entity.enums.ServiceType;

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
