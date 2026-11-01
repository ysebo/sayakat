package kg.alatoo.sayakat.dto.response;

import kg.alatoo.sayakat.entity.ServiceOffering;
import kg.alatoo.sayakat.entity.enums.PriceUnit;
import kg.alatoo.sayakat.entity.enums.ServiceType;

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
