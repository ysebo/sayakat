package kg.alatoo.sayakat.service.impl;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import kg.alatoo.sayakat.dto.request.BookingCreateRequest;
import kg.alatoo.sayakat.dto.response.BookingResponse;
import kg.alatoo.sayakat.entity.BookingRequest;
import kg.alatoo.sayakat.entity.Provider;
import kg.alatoo.sayakat.entity.ServiceOffering;
import kg.alatoo.sayakat.entity.enums.BookingStatus;
import kg.alatoo.sayakat.exception.Errors;
import kg.alatoo.sayakat.repository.BookingRequestRepository;
import kg.alatoo.sayakat.repository.ProviderRepository;
import kg.alatoo.sayakat.repository.ServiceOfferingRepository;
import kg.alatoo.sayakat.repository.UserRepository;
import kg.alatoo.sayakat.service.BookingService;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRequestRepository bookingRepository;
    private final ServiceOfferingRepository serviceOfferingRepository;
    private final ProviderRepository providerRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public BookingResponse create(Long touristId, BookingCreateRequest request) {
        if (request.dateTo().isBefore(request.dateFrom())) {
            throw Errors.badRequest("dateTo must not be before dateFrom");
        }
        ServiceOffering service = serviceOfferingRepository.findById(request.serviceId())
                .filter(ServiceOffering::isActive)
                .orElseThrow(() -> Errors.notFound("Service", request.serviceId()));
        if (service.getCapacity() != null && request.guests() > service.getCapacity()) {
            throw Errors.badRequest("Capacity is " + service.getCapacity());
        }
        BookingRequest booking = new BookingRequest();
        booking.setTourist(userRepository.getReferenceById(touristId));
        booking.setService(service);
        booking.setPlanId(request.planId());
        booking.setDateFrom(request.dateFrom());
        booking.setDateTo(request.dateTo());
        booking.setGuests(request.guests());
        booking.setMessage(request.message());
        return BookingResponse.of(bookingRepository.saveAndFlush(booking));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> findByTourist(Long touristId) {
        return bookingRepository.findByTouristIdOrderByCreatedAtDesc(touristId).stream()
                .map(BookingResponse::of).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> findByProviderUser(Long providerUserId) {
        Provider provider = providerRepository.findByUserId(providerUserId)
                .orElseThrow(() -> Errors.forbidden("No provider profile"));
        return bookingRepository.findByServiceProviderIdOrderByCreatedAtDesc(provider.getId())
                .stream().map(BookingResponse::of).toList();
    }

    @Override
    @Transactional
    public BookingResponse cancel(Long touristId, Long bookingId) {
        BookingRequest booking = find(bookingId);
        if (!booking.getTourist().getId().equals(touristId)) {
            throw Errors.forbidden("Not your booking");
        }
        transition(booking, Set.of(BookingStatus.PENDING, BookingStatus.CONFIRMED), BookingStatus.CANCELLED);
        return BookingResponse.of(booking);
    }

    @Override
    @Transactional
    public BookingResponse decide(Long providerUserId, Long bookingId, boolean confirm, String comment) {
        BookingRequest booking = find(bookingId);
        if (!booking.getService().getProvider().getUser().getId().equals(providerUserId)) {
            throw Errors.forbidden("Not your service");
        }
        transition(booking, Set.of(BookingStatus.PENDING), confirm ? BookingStatus.CONFIRMED : BookingStatus.REJECTED);
        booking.setProviderComment(comment);
        return BookingResponse.of(booking);
    }

    private BookingRequest find(Long id) {
        return bookingRepository.findById(id).orElseThrow(() -> Errors.notFound("Booking", id));
    }

    private static void transition(BookingRequest booking, Set<BookingStatus> allowedFrom, BookingStatus to) {
        if (!allowedFrom.contains(booking.getStatus())) {
            throw Errors.conflict("Cannot change booking from " + booking.getStatus() + " to " + to);
        }
        booking.setStatus(to);
    }
}
