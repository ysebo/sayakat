package kg.alatoo.sayakat.planner.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TripPlanRepository extends JpaRepository<TripPlan, Long> {

    List<TripPlan> findByUserIdOrderByCreatedAtDesc(Long userId);
}
