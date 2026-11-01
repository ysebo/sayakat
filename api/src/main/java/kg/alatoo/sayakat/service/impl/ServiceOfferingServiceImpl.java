package kg.alatoo.sayakat.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import kg.alatoo.sayakat.dto.request.ServiceRequest;
import kg.alatoo.sayakat.dto.response.ServiceResponse;
import kg.alatoo.sayakat.entity.Provider;
import kg.alatoo.sayakat.entity.ServiceOffering;
import kg.alatoo.sayakat.entity.enums.ServiceType;
import kg.alatoo.sayakat.exception.Errors;
import kg.alatoo.sayakat.repository.PoiRepository;
import kg.alatoo.sayakat.repository.ProviderRepository;
import kg.alatoo.sayakat.repository.ServiceOfferingRepository;
import kg.alatoo.sayakat.service.ServiceOfferingService;
import kg.alatoo.sayakat.util.Geo;

@Service
@RequiredArgsConstructor
public class ServiceOfferingServiceImpl implements ServiceOfferingService {

    private final ServiceOfferingRepository serviceOfferingRepository;
    private final ProviderRepository providerRepository;
    private final PoiRepository poiRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ServiceResponse> findNear(double lat, double lon, double radiusKm, ServiceType type) {
        return serviceOfferingRepository.findNear(lat, lon, radiusKm * 1000, type == null ? null : type.name())
                .stream().map(ServiceResponse::of).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceResponse get(Long id) {
        return ServiceResponse.of(serviceOfferingRepository.findById(id)
                .orElseThrow(() -> Errors.notFound("Service", id)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceResponse> findByProviderUser(Long userId) {
        return serviceOfferingRepository.findByProviderIdOrderByCreatedAtDesc(provider(userId).getId())
                .stream().map(ServiceResponse::of).toList();
    }

    @Override
    @Transactional
    public ServiceResponse create(Long userId, ServiceRequest request) {
        ServiceOffering service = new ServiceOffering();
        service.setProvider(provider(userId));
        apply(service, request);
        return ServiceResponse.of(serviceOfferingRepository.save(service));
    }

    @Override
    @Transactional
    public ServiceResponse update(Long userId, Long id, ServiceRequest request) {
        ServiceOffering service = owned(userId, id);
        apply(service, request);
        return ServiceResponse.of(service);
    }

    @Override
    @Transactional
    public void deactivate(Long userId, Long id) {
        owned(userId, id).setActive(false);
    }

    private Provider provider(Long userId) {
        return providerRepository.findByUserId(userId).orElseThrow(() -> Errors.forbidden("No provider profile"));
    }

    private ServiceOffering owned(Long userId, Long id) {
        ServiceOffering service = serviceOfferingRepository.findById(id)
                .orElseThrow(() -> Errors.notFound("Service", id));
        if (!service.getProvider().getUser().getId().equals(userId)) {
            throw Errors.forbidden("Not your service");
        }
        return service;
    }

    private void apply(ServiceOffering service, ServiceRequest r) {
        if (r.type() == ServiceType.ACCOMMODATION && (r.lat() == null || r.lon() == null)) {
            throw Errors.badRequest("Accommodation must have a location");
        }
        service.setType(r.type());
        service.setTitle(r.title());
        service.setDescription(r.description());
        service.setPriceKgs(r.priceKgs());
        service.setPriceUnit(r.priceUnit());
        service.setPoi(r.poiId() == null ? null
                : poiRepository.findById(r.poiId()).orElseThrow(() -> Errors.notFound("POI", r.poiId())));
        service.setLocation(r.lat() == null || r.lon() == null ? null : Geo.point(r.lat(), r.lon()));
        service.setCapacity(r.capacity());
        if (r.active() != null) {
            service.setActive(r.active());
        }
    }
}
