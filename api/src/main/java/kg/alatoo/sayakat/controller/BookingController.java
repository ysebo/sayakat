package kg.alatoo.sayakat.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import kg.alatoo.sayakat.dto.request.BookingCreateRequest;
import kg.alatoo.sayakat.dto.request.BookingDecisionRequest;
import kg.alatoo.sayakat.dto.response.BookingResponse;
import kg.alatoo.sayakat.security.AuthUser;
import kg.alatoo.sayakat.service.BookingService;

@RestController
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping("/api/bookings")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('TOURIST')")
    public BookingResponse create(@AuthenticationPrincipal AuthUser user,
                                  @Valid @RequestBody BookingCreateRequest request) {
        return bookingService.create(user.id(), request);
    }

    @GetMapping("/api/bookings")
    @PreAuthorize("hasRole('TOURIST')")
    public List<BookingResponse> myBookings(@AuthenticationPrincipal AuthUser user) {
        return bookingService.findByTourist(user.id());
    }

    @PostMapping("/api/bookings/{id}/cancel")
    @PreAuthorize("hasRole('TOURIST')")
    public BookingResponse cancel(@AuthenticationPrincipal AuthUser user, @PathVariable Long id) {
        return bookingService.cancel(user.id(), id);
    }

    @GetMapping("/api/provider/bookings")
    public List<BookingResponse> incoming(@AuthenticationPrincipal AuthUser user) {
        return bookingService.findByProviderUser(user.id());
    }

    @PostMapping("/api/provider/bookings/{id}/confirm")
    public BookingResponse confirm(@AuthenticationPrincipal AuthUser user, @PathVariable Long id,
                                   @RequestBody(required = false) BookingDecisionRequest decision) {
        return bookingService.decide(user.id(), id, true, decision == null ? null : decision.comment());
    }

    @PostMapping("/api/provider/bookings/{id}/reject")
    public BookingResponse reject(@AuthenticationPrincipal AuthUser user, @PathVariable Long id,
                                  @RequestBody(required = false) BookingDecisionRequest decision) {
        return bookingService.decide(user.id(), id, false, decision == null ? null : decision.comment());
    }
}
