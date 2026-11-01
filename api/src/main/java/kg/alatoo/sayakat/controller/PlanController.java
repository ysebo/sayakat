package kg.alatoo.sayakat.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import kg.alatoo.sayakat.dto.request.PlanRequest;
import kg.alatoo.sayakat.dto.response.PlanResponse;
import kg.alatoo.sayakat.dto.response.ServiceResponse;
import kg.alatoo.sayakat.entity.enums.ServiceType;
import kg.alatoo.sayakat.security.AuthUser;
import kg.alatoo.sayakat.service.PlanService;

@RestController
@RequestMapping("/api/plans")
@RequiredArgsConstructor
public class PlanController {

    private final PlanService planService;

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public PlanResponse create(@AuthenticationPrincipal AuthUser user, @Valid @RequestBody PlanRequest request) {
        return planService.get(user.id(), planService.create(user.id(), request));
    }

    @GetMapping
    public List<PlanResponse> myPlans(@AuthenticationPrincipal AuthUser user) {
        return planService.findByUser(user.id());
    }

    @GetMapping("/{id}")
    public PlanResponse get(@AuthenticationPrincipal AuthUser user, @PathVariable Long id) {
        return planService.get(user.id(), id);
    }

    @GetMapping("/{id}/days/{dayIndex}/services")
    public List<ServiceResponse> servicesForDay(@AuthenticationPrincipal AuthUser user, @PathVariable Long id,
                                                @PathVariable int dayIndex,
                                                @RequestParam(required = false) ServiceType type,
                                                @RequestParam(defaultValue = "20") double radiusKm) {
        return planService.servicesForDay(user.id(), id, dayIndex, type, radiusKm);
    }
}
