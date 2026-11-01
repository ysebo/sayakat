package kg.alatoo.sayakat.planner;

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
import kg.alatoo.sayakat.marketplace.MarketplaceDtos.ServiceResponse;
import kg.alatoo.sayakat.marketplace.ServiceType;
import kg.alatoo.sayakat.planner.PlanDtos.PlanRequest;
import kg.alatoo.sayakat.planner.PlanDtos.PlanResponse;
import kg.alatoo.sayakat.security.AuthUser;

@RestController
@RequestMapping("/api/plans")
public class PlanController {

    private final PlanService planService;

    public PlanController(PlanService planService) {
        this.planService = planService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public PlanResponse create(@AuthenticationPrincipal AuthUser user, @Valid @RequestBody PlanRequest request) {
        return planService.get(user.id(), planService.create(user.id(), request));
    }

    @GetMapping
    public List<PlanResponse> mine(@AuthenticationPrincipal AuthUser user) {
        return planService.mine(user.id());
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
