package kg.alatoo.sayakat.marketplace;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
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
import kg.alatoo.sayakat.marketplace.MarketplaceDtos.BookingCreateRequest;
import kg.alatoo.sayakat.marketplace.MarketplaceDtos.BookingDecision;
import kg.alatoo.sayakat.marketplace.MarketplaceDtos.BookingResponse;
import kg.alatoo.sayakat.marketplace.MarketplaceDtos.ServiceRequest;
import kg.alatoo.sayakat.marketplace.MarketplaceDtos.ServiceResponse;
import kg.alatoo.sayakat.security.AuthUser;

@RestController
public class MarketplaceController {

    private final ServiceOfferingService serviceOfferings;
    private final BookingService bookings;

    public MarketplaceController(ServiceOfferingService serviceOfferings, BookingService bookings) {
        this.serviceOfferings = serviceOfferings;
        this.bookings = bookings;
    }

    @GetMapping("/api/services")
    public List<ServiceResponse> near(@RequestParam double lat, @RequestParam double lon,
                                      @RequestParam(defaultValue = "30") double radiusKm,
                                      @RequestParam(required = false) ServiceType type) {
        return serviceOfferings.near(lat, lon, radiusKm, type);
    }

    @GetMapping("/api/services/{id}")
    public ServiceResponse get(@PathVariable Long id) {
        return serviceOfferings.get(id);
    }

    @PostMapping("/api/bookings")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('TOURIST')")
    public BookingResponse book(@AuthenticationPrincipal AuthUser user, @Valid @RequestBody BookingCreateRequest request) {
        return bookings.create(user.id(), request);
    }

    @GetMapping("/api/bookings")
    @PreAuthorize("hasRole('TOURIST')")
    public List<BookingResponse> myBookings(@AuthenticationPrincipal AuthUser user) {
        return bookings.forTourist(user.id());
    }

    @PostMapping("/api/bookings/{id}/cancel")
    @PreAuthorize("hasRole('TOURIST')")
    public BookingResponse cancel(@AuthenticationPrincipal AuthUser user, @PathVariable Long id) {
        return bookings.cancel(user.id(), id);
    }

    @GetMapping("/api/provider/services")
    public List<ServiceResponse> myServices(@AuthenticationPrincipal AuthUser user) {
        return serviceOfferings.mine(user.id());
    }

    @PostMapping("/api/provider/services")
    @ResponseStatus(HttpStatus.CREATED)
    public ServiceResponse create(@AuthenticationPrincipal AuthUser user, @Valid @RequestBody ServiceRequest request) {
        return serviceOfferings.create(user.id(), request);
    }

    @PutMapping("/api/provider/services/{id}")
    public ServiceResponse update(@AuthenticationPrincipal AuthUser user, @PathVariable Long id,
                                  @Valid @RequestBody ServiceRequest request) {
        return serviceOfferings.update(user.id(), id, request);
    }

    @DeleteMapping("/api/provider/services/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(@AuthenticationPrincipal AuthUser user, @PathVariable Long id) {
        serviceOfferings.deactivate(user.id(), id);
    }

    @GetMapping("/api/provider/bookings")
    public List<BookingResponse> incoming(@AuthenticationPrincipal AuthUser user) {
        return bookings.forProvider(user.id());
    }

    @PostMapping("/api/provider/bookings/{id}/confirm")
    public BookingResponse confirm(@AuthenticationPrincipal AuthUser user, @PathVariable Long id,
                                   @RequestBody(required = false) BookingDecision decision) {
        return bookings.decide(user.id(), id, true, decision == null ? null : decision.comment());
    }

    @PostMapping("/api/provider/bookings/{id}/reject")
    public BookingResponse reject(@AuthenticationPrincipal AuthUser user, @PathVariable Long id,
                                  @RequestBody(required = false) BookingDecision decision) {
        return bookings.decide(user.id(), id, false, decision == null ? null : decision.comment());
    }
}
