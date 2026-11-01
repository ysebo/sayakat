package kg.alatoo.sayakat.planner.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import kg.alatoo.sayakat.poi.Poi;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "trip_plan_stop")
@Getter
@Setter
public class TripPlanStop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plan_day_id", nullable = false)
    private TripPlanDay day;

    @Column(nullable = false)
    private int seq;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "poi_id", nullable = false)
    private Poi poi;

    @Column(name = "arrival_min", nullable = false)
    private int arrivalMin;

    @Column(name = "departure_min", nullable = false)
    private int departureMin;
}
