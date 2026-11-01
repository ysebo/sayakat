package kg.alatoo.sayakat.planner.domain;

import java.time.LocalDate;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import ai.timefold.solver.core.api.domain.variable.InverseRelationShadowVariable;

@PlanningEntity
public class Visit {

    @PlanningId
    private Long id;
    private String name;
    private int locationIndex;
    private int durationMin;
    private int opensMin;
    private int closesMin;
    private int costKgs;
    private int seasonFrom;
    private int seasonTo;
    private int value;
    private Lodging nearestLodging;

    @InverseRelationShadowVariable(sourceVariableName = "visits")
    private TripDay day;

    public Visit() {
    }

    public Visit(Long id, String name, int locationIndex, int durationMin, int opensMin, int closesMin,
                 int costKgs, int seasonFrom, int seasonTo, int value, Lodging nearestLodging) {
        this.id = id;
        this.name = name;
        this.locationIndex = locationIndex;
        this.durationMin = durationMin;
        this.opensMin = opensMin;
        this.closesMin = closesMin;
        this.costKgs = costKgs;
        this.seasonFrom = seasonFrom;
        this.seasonTo = seasonTo;
        this.value = value;
        this.nearestLodging = nearestLodging;
    }

    public boolean isInSeason(LocalDate date) {
        int month = date.getMonthValue();
        return seasonFrom <= seasonTo
                ? month >= seasonFrom && month <= seasonTo
                : month >= seasonFrom || month <= seasonTo;
    }

    public boolean isAssigned() {
        return day != null;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getLocationIndex() {
        return locationIndex;
    }

    public int getDurationMin() {
        return durationMin;
    }

    public int getOpensMin() {
        return opensMin;
    }

    public int getClosesMin() {
        return closesMin;
    }

    public int getCostKgs() {
        return costKgs;
    }

    public int getSeasonFrom() {
        return seasonFrom;
    }

    public int getSeasonTo() {
        return seasonTo;
    }

    public int getValue() {
        return value;
    }

    public Lodging getNearestLodging() {
        return nearestLodging;
    }

    public TripDay getDay() {
        return day;
    }

    public void setDay(TripDay day) {
        this.day = day;
    }

    @Override
    public String toString() {
        return name;
    }
}
