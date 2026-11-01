package kg.alatoo.sayakat.marketplace;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRequestRepository extends JpaRepository<BookingRequest, Long> {

    List<BookingRequest> findByTouristIdOrderByCreatedAtDesc(Long touristId);

    List<BookingRequest> findByServiceProviderIdOrderByCreatedAtDesc(Long providerId);
}
