package kg.alatoo.sayakat.service;

import java.util.List;

import kg.alatoo.sayakat.dto.request.PlanRequest;
import kg.alatoo.sayakat.dto.response.PlanResponse;
import kg.alatoo.sayakat.dto.response.ServiceResponse;
import kg.alatoo.sayakat.entity.enums.ServiceType;

public interface PlanService {

    Long create(Long userId, PlanRequest request);

    PlanResponse get(Long userId, Long planId);

    List<PlanResponse> findByUser(Long userId);

    List<ServiceResponse> servicesForDay(Long userId, Long planId, int dayIndex, ServiceType type, double radiusKm);
}
