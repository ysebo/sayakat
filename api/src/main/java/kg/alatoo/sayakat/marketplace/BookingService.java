package kg.alatoo.sayakat.marketplace;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kg.alatoo.sayakat.common.Errors;
import kg.alatoo.sayakat.marketplace.MarketplaceDtos.BookingCreateRequest;
import kg.alatoo.sayakat.marketplace.MarketplaceDtos.BookingResponse;
import kg.alatoo.sayakat.user.UserRepository;

@Service
public class BookingService {

    private final BookingRequestRepository bookings;
    private final ServiceOfferingRepository services;
    private final ProviderRepository providers;
    private final UserRepository users;

    public BookingService(BookingRequestRepository bookings, ServiceOfferingRepository services,
                          ProviderRepository providers, UserRepository users) {
        this.bookings = bookings;
        this.services = services;
        this.providers = providers;
        this.users = users;
    }

    @Transactional
    public BookingResponse create(Long touristId, BookingCreateRequest request) {
        if (request.dateTo().isBefore(request.dateFrom())) {
            throw Errors.badRequest("dateTo must not be before dateFrom");
        }
        ServiceOffering service = services.findById(request.serviceId())
                .filter(ServiceOffering::isActive)
                .orElseThrow(() -> Errors.notFound("Service", request.serviceId()));
        if (service.getCapacity() != null && request.guests() > service.getCapacity()) {
            throw Errors.badRequest("Capacity is " + service.getCapacity());
        }
        BookingRequest b = new BookingRequest();
        b.setTourist(users.getReferenceById(touristId));
        b.setService(service);
        b.setPlanId(request.planId());
        b.setDateFrom(request.dateFrom());
        b.setDateTo(request.dateTo());
        b.setGuests(request.guests());
        b.setMessage(request.message());
        return BookingResponse.of(bookings.saveAndFlush(b));
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> forTourist(Long touristId) {
        return bookings.findByTouristIdOrderByCreatedAtDesc(touristId).stream().map(BookingResponse::of).toList();
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> forProvider(Long providerUserId) {
        Provider provider = providers.findByUserId(providerUserId)
                .orElseThrow(() -> Errors.forbidden("No provider profile"));
        return bookings.findByServiceProviderIdOrderByCreatedAtDesc(provider.getId())
                .stream().map(BookingResponse::of).toList();
    }

    @Transactional
    public BookingResponse cancel(Long touristId, Long bookingId) {
        BookingRequest b = find(bookingId);
        if (!b.getTourist().getId().equals(touristId)) {
            throw Errors.forbidden("Not your booking");
        }
        transition(b, Set.of(BookingStatus.PENDING, BookingStatus.CONFIRMED), BookingStatus.CANCELLED);
        return BookingResponse.of(b);
    }

    @Transactional
    public BookingResponse decide(Long providerUserId, Long bookingId, boolean confirm, String comment) {
        BookingRequest b = find(bookingId);
        if (!b.getService().getProvider().getUser().getId().equals(providerUserId)) {
            throw Errors.forbidden("Not your service");
        }
        transition(b, Set.of(BookingStatus.PENDING), confirm ? BookingStatus.CONFIRMED : BookingStatus.REJECTED);
        b.setProviderComment(comment);
        return BookingResponse.of(b);
    }

    private BookingRequest find(Long id) {
        return bookings.findById(id).orElseThrow(() -> Errors.notFound("Booking", id));
    }

    private static void transition(BookingRequest b, Set<BookingStatus> allowedFrom, BookingStatus to) {
        if (!allowedFrom.contains(b.getStatus())) {
            throw Errors.conflict("Cannot change booking from " + b.getStatus() + " to " + to);
        }
        b.setStatus(to);
    }
}
