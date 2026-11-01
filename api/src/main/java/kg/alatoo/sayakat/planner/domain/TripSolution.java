package kg.alatoo.sayakat.planner.domain;

import java.util.List;

import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.PlanningScore;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.solution.ProblemFactProperty;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;

@PlanningSolution
public class TripSolution {

    @ProblemFactProperty
    private TripSettings settings;

    @PlanningEntityCollectionProperty
    private List<TripDay> days;

    @PlanningEntityCollectionProperty
    @ValueRangeProvider
    private List<Visit> visits;

    @PlanningScore
    private HardMediumSoftScore score;

    public TripSolution() {
    }

    public TripSolution(TripSettings settings, List<TripDay> days, List<Visit> visits) {
        this.settings = settings;
        this.days = days;
        this.visits = visits;
    }

    public TripSettings getSettings() {
        return settings;
    }

    public List<TripDay> getDays() {
        return days;
    }

    public List<Visit> getVisits() {
        return visits;
    }

    public HardMediumSoftScore getScore() {
        return score;
    }

    public void setScore(HardMediumSoftScore score) {
        this.score = score;
    }
}
