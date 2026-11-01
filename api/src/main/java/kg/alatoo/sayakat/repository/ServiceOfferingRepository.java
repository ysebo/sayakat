package kg.alatoo.sayakat.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import kg.alatoo.sayakat.entity.ServiceOffering;
import kg.alatoo.sayakat.entity.enums.ServiceType;

public interface ServiceOfferingRepository extends JpaRepository<ServiceOffering, Long> {

    List<ServiceOffering> findByProviderIdOrderByCreatedAtDesc(Long providerId);

    List<ServiceOffering> findByTypeAndActiveTrue(ServiceType type);

    Optional<ServiceOffering> findByExternalId(String externalId);

    @Query(value = """
            SELECT s.* FROM service_offering s
            LEFT JOIN poi p ON p.id = s.poi_id
            WHERE s.active
              AND (CAST(:type AS varchar) IS NULL OR s.type = :type)
              AND ST_DWithin(COALESCE(s.location, p.location)::geography,
                             ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)::geography, :radiusM)
            ORDER BY ST_Distance(COALESCE(s.location, p.location)::geography,
                                 ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)::geography)
            """, nativeQuery = true)
    List<ServiceOffering> findNear(@Param("lat") double lat, @Param("lon") double lon,
                                   @Param("radiusM") double radiusM, @Param("type") String type);
}
