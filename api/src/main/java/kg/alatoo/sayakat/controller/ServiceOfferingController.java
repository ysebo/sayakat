package kg.alatoo.sayakat.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import kg.alatoo.sayakat.dto.request.ServiceRequest;
import kg.alatoo.sayakat.dto.response.ServiceResponse;
import kg.alatoo.sayakat.entity.enums.ServiceType;
import kg.alatoo.sayakat.security.AuthUser;
import kg.alatoo.sayakat.service.ServiceOfferingService;

@RestController
@RequiredArgsConstructor
public class ServiceOfferingController {

    private final ServiceOfferingService serviceOfferingService;

    @GetMapping("/api/services")
    public List<ServiceResponse> findNear(@RequestParam double lat, @RequestParam double lon,
                                          @RequestParam(defaultValue = "30") double radiusKm,
                                          @RequestParam(required = false) ServiceType type) {
        return serviceOfferingService.findNear(lat, lon, radiusKm, type);
    }

    @GetMapping("/api/services/{id}")
    public ServiceResponse get(@PathVariable Long id) {
        return serviceOfferingService.get(id);
    }

    @GetMapping("/api/provider/services")
    public List<ServiceResponse> myServices(@AuthenticationPrincipal AuthUser user) {
        return serviceOfferingService.findByProviderUser(user.id());
    }

    @PostMapping("/api/provider/services")
    @ResponseStatus(HttpStatus.CREATED)
    public ServiceResponse create(@AuthenticationPrincipal AuthUser user, @Valid @RequestBody ServiceRequest request) {
        return serviceOfferingService.create(user.id(), request);
    }

    @PutMapping("/api/provider/services/{id}")
    public ServiceResponse update(@AuthenticationPrincipal AuthUser user, @PathVariable Long id,
                                  @Valid @RequestBody ServiceRequest request) {
        return serviceOfferingService.update(user.id(), id, request);
    }

    @DeleteMapping("/api/provider/services/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(@AuthenticationPrincipal AuthUser user, @PathVariable Long id) {
        serviceOfferingService.deactivate(user.id(), id);
    }
}
