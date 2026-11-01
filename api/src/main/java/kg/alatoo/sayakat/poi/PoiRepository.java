package kg.alatoo.sayakat.poi;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PoiRepository extends JpaRepository<Poi, Long> {

    Optional<Poi> findByExternalId(String externalId);

    List<Poi> findByCategoryOrderByName(PoiCategory category);

    @Query(value = """
            SELECT * FROM poi
            WHERE location && ST_MakeEnvelope(:minLon, :minLat, :maxLon, :maxLat, 4326)
              AND (CAST(:category AS varchar) IS NULL OR category = :category)
            ORDER BY popularity DESC, name
            """, nativeQuery = true)
    List<Poi> findInBox(@Param("minLat") double minLat, @Param("minLon") double minLon,
                        @Param("maxLat") double maxLat, @Param("maxLon") double maxLon,
                        @Param("category") String category);
}
