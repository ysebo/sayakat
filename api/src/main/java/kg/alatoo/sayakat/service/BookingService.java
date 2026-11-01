package kg.alatoo.sayakat.service;

import java.util.List;

import kg.alatoo.sayakat.dto.request.BookingCreateRequest;
import kg.alatoo.sayakat.dto.response.BookingResponse;

public interface BookingService {

    BookingResponse create(Long touristId, BookingCreateRequest request);

    List<BookingResponse> findByTourist(Long touristId);

    List<BookingResponse> findByProviderUser(Long providerUserId);

    BookingResponse cancel(Long touristId, Long bookingId);

    BookingResponse decide(Long providerUserId, Long bookingId, boolean confirm, String comment);
}
