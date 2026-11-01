package kg.alatoo.sayakat.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import kg.alatoo.sayakat.entity.BookingRequest;

public interface BookingRequestRepository extends JpaRepository<BookingRequest, Long> {

    List<BookingRequest> findByTouristIdOrderByCreatedAtDesc(Long touristId);

    List<BookingRequest> findByServiceProviderIdOrderByCreatedAtDesc(Long providerId);
}
