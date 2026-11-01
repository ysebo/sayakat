package kg.alatoo.sayakat.planner.persistence;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import kg.alatoo.sayakat.marketplace.ServiceOffering;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "trip_plan_day")
@Getter
@Setter
public class TripPlanDay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plan_id", nullable = false)
    private TripPlan plan;

    @Column(name = "day_index", nullable = false)
    private int dayIndex;

    @Column(nullable = false)
    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lodging_service_id")
    private ServiceOffering lodgingService;

    @Column(name = "lodging_name")
    private String lodgingName;

    @Column(name = "travel_min", nullable = false)
    private int travelMin;

    @Column(name = "cost_kgs", nullable = false)
    private int costKgs;

    @Column(name = "end_min", nullable = false)
    private int endMin;

    @OneToMany(mappedBy = "day", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("seq")
    private List<TripPlanStop> stops = new ArrayList<>();
}
