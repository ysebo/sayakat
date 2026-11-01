package kg.alatoo.sayakat.entity;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import kg.alatoo.sayakat.entity.enums.PlanAlgorithm;
import kg.alatoo.sayakat.entity.enums.PlanStatus;
import kg.alatoo.sayakat.planner.domain.Pace;

@Entity
@Table(name = "trip_plan")
@Getter
@Setter
public class TripPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlanStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlanAlgorithm algorithm;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private int days;

    @Column(name = "budget_kgs", nullable = false)
    private int budgetKgs;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Pace pace;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(columnDefinition = "text[]", nullable = false)
    private List<String> interests = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "start_poi_id", nullable = false)
    private Poi startPoi;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "end_poi_id", nullable = false)
    private Poi endPoi;

    private String score;
    private Boolean feasible;

    @Column(name = "total_cost_kgs")
    private Integer totalCostKgs;

    @Column(name = "total_travel_min")
    private Integer totalTravelMin;

    @Column(name = "total_value")
    private Integer totalValue;

    private String error;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "solved_at")
    private Instant solvedAt;

    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("dayIndex")
    private List<TripPlanDay> itinerary = new ArrayList<>();
}
