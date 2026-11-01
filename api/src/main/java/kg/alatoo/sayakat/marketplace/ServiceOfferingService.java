package kg.alatoo.sayakat.marketplace;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kg.alatoo.sayakat.common.Errors;
import kg.alatoo.sayakat.common.Geo;
import kg.alatoo.sayakat.marketplace.MarketplaceDtos.ServiceRequest;
import kg.alatoo.sayakat.marketplace.MarketplaceDtos.ServiceResponse;
import kg.alatoo.sayakat.poi.PoiRepository;

@Service
public class ServiceOfferingService {

    private final ServiceOfferingRepository services;
    private final ProviderRepository providers;
    private final PoiRepository pois;

    public ServiceOfferingService(ServiceOfferingRepository services, ProviderRepository providers,
                                  PoiRepository pois) {
        this.services = services;
        this.providers = providers;
        this.pois = pois;
    }

    @Transactional(readOnly = true)
    public List<ServiceResponse> near(double lat, double lon, double radiusKm, ServiceType type) {
        return services.findNear(lat, lon, radiusKm * 1000, type == null ? null : type.name())
                .stream().map(ServiceResponse::of).toList();
    }

    @Transactional(readOnly = true)
    public ServiceResponse get(Long id) {
        return ServiceResponse.of(services.findById(id).orElseThrow(() -> Errors.notFound("Service", id)));
    }

    @Transactional(readOnly = true)
    public List<ServiceResponse> mine(Long userId) {
        return services.findByProviderIdOrderByCreatedAtDesc(provider(userId).getId())
                .stream().map(ServiceResponse::of).toList();
    }

    @Transactional
    public ServiceResponse create(Long userId, ServiceRequest request) {
        ServiceOffering s = new ServiceOffering();
        s.setProvider(provider(userId));
        apply(s, request);
        return ServiceResponse.of(services.save(s));
    }

    @Transactional
    public ServiceResponse update(Long userId, Long id, ServiceRequest request) {
        ServiceOffering s = owned(userId, id);
        apply(s, request);
        return ServiceResponse.of(s);
    }

    @Transactional
    public void deactivate(Long userId, Long id) {
        owned(userId, id).setActive(false);
    }

    Provider provider(Long userId) {
        return providers.findByUserId(userId).orElseThrow(() -> Errors.forbidden("No provider profile"));
    }

    private ServiceOffering owned(Long userId, Long id) {
        ServiceOffering s = services.findById(id).orElseThrow(() -> Errors.notFound("Service", id));
        if (!s.getProvider().getUser().getId().equals(userId)) {
            throw Errors.forbidden("Not your service");
        }
        return s;
    }

    private void apply(ServiceOffering s, ServiceRequest r) {
        if (r.type() == ServiceType.ACCOMMODATION && (r.lat() == null || r.lon() == null)) {
            throw Errors.badRequest("Accommodation must have a location");
        }
        s.setType(r.type());
        s.setTitle(r.title());
        s.setDescription(r.description());
        s.setPriceKgs(r.priceKgs());
        s.setPriceUnit(r.priceUnit());
        s.setPoi(r.poiId() == null ? null
                : pois.findById(r.poiId()).orElseThrow(() -> Errors.notFound("POI", r.poiId())));
        s.setLocation(r.lat() == null || r.lon() == null ? null : Geo.point(r.lat(), r.lon()));
        s.setCapacity(r.capacity());
        if (r.active() != null) {
            s.setActive(r.active());
        }
    }
}
