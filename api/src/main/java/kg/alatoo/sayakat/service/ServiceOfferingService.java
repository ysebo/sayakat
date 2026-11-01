package kg.alatoo.sayakat.service;

import java.util.List;

import kg.alatoo.sayakat.dto.request.ServiceRequest;
import kg.alatoo.sayakat.dto.response.ServiceResponse;
import kg.alatoo.sayakat.entity.enums.ServiceType;

public interface ServiceOfferingService {

    List<ServiceResponse> findNear(double lat, double lon, double radiusKm, ServiceType type);

    ServiceResponse get(Long id);

    List<ServiceResponse> findByProviderUser(Long userId);

    ServiceResponse create(Long userId, ServiceRequest request);

    ServiceResponse update(Long userId, Long id, ServiceRequest request);

    void deactivate(Long userId, Long id);
}
