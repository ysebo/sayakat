package kg.alatoo.sayakat.planner.domain;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import ai.timefold.solver.core.api.domain.variable.PlanningListVariable;

@PlanningEntity
public class TripDay {

    @PlanningId
    private Integer index;
    private LocalDate date;
    private TripSettings settings;

    @PlanningListVariable(allowsUnassignedValues = true)
    private List<Visit> visits = new ArrayList<>();

    public TripDay() {
    }

    public TripDay(int index, LocalDate date, TripSettings settings) {
        this.index = index;
        this.date = date;
        this.settings = settings;
    }

    public boolean isLast() {
        return index == settings.dayCount() - 1;
    }

    public Integer getIndex() {
        return index;
    }

    public LocalDate getDate() {
        return date;
    }

    public TripSettings getSettings() {
        return settings;
    }

    public List<Visit> getVisits() {
        return visits;
    }

    public void setVisits(List<Visit> visits) {
        this.visits = visits;
    }

    @Override
    public String toString() {
        return "Day " + (index + 1) + " " + visits;
    }
}
