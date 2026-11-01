package kg.alatoo.sayakat.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import kg.alatoo.sayakat.entity.TripPlan;

public interface TripPlanRepository extends JpaRepository<TripPlan, Long> {

    List<TripPlan> findByUserIdOrderByCreatedAtDesc(Long userId);
}
