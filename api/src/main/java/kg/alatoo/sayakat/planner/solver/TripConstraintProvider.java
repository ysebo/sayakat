package kg.alatoo.sayakat.planner.solver;

import ai.timefold.solver.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintCollectors;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import ai.timefold.solver.core.api.score.stream.uni.UniConstraintStream;

import kg.alatoo.sayakat.planner.domain.Lodging;
import kg.alatoo.sayakat.planner.domain.TripDay;
import kg.alatoo.sayakat.planner.domain.Visit;

public class TripConstraintProvider implements ConstraintProvider {

    static final int STRUCTURAL_PENALTY = 120;
    static final int BUDGET_UNIT_KGS = 100;

    @Override
    public Constraint[] defineConstraints(ConstraintFactory factory) {
        return new Constraint[] {
                openingHours(factory),
                dayLength(factory),
                lodgingReachable(factory),
                budget(factory),
                season(factory),
                visitsPerDay(factory),
                attractionValue(factory),
                travelTime(factory)
        };
    }

    private UniConstraintStream<TripSchedule> schedule(ConstraintFactory factory) {
        return factory.forEach(TripDay.class)
                .groupBy(ConstraintCollectors.toList())
                .map(TripSchedule::of);
    }

    Constraint openingHours(ConstraintFactory factory) {
        return schedule(factory)
                .filter(s -> s.lateMinutes() > 0)
                .penalize(HardMediumSoftScore.ONE_HARD, TripSchedule::lateMinutes)
                .asConstraint("Opening hours");
    }

    Constraint dayLength(ConstraintFactory factory) {
        return schedule(factory)
                .filter(s -> s.overtimeMinutes() > 0)
                .penalize(HardMediumSoftScore.ONE_HARD, TripSchedule::overtimeMinutes)
                .asConstraint("Day length");
    }

    Constraint lodgingReachable(ConstraintFactory factory) {
        return schedule(factory)
                .filter(s -> s.nightsWithoutLodging() > 0)
                .penalize(HardMediumSoftScore.ONE_HARD, s -> s.nightsWithoutLodging() * STRUCTURAL_PENALTY)
                .asConstraint("Lodging reachable");
    }

    Constraint budget(ConstraintFactory factory) {
        return schedule(factory)
                .filter(s -> s.overBudgetKgs() > 0)
                .penalize(HardMediumSoftScore.ONE_HARD,
                        s -> (s.overBudgetKgs() + BUDGET_UNIT_KGS - 1) / BUDGET_UNIT_KGS)
                .asConstraint("Budget");
    }

    Constraint season(ConstraintFactory factory) {
        return factory.forEach(Visit.class)
                .filter(v -> v.isAssigned() && !v.isInSeason(v.getDay().getDate()))
                .penalize(HardMediumSoftScore.ONE_HARD, v -> STRUCTURAL_PENALTY)
                .asConstraint("Season");
    }

    Constraint visitsPerDay(ConstraintFactory factory) {
        return factory.forEach(TripDay.class)
                .filter(d -> d.getVisits().size() > d.getSettings().pace().maxVisitsPerDay())
                .penalize(HardMediumSoftScore.ONE_HARD,
                        d -> (d.getVisits().size() - d.getSettings().pace().maxVisitsPerDay()) * STRUCTURAL_PENALTY)
                .asConstraint("Visits per day");
    }

    Constraint attractionValue(ConstraintFactory factory) {
        return factory.forEach(Visit.class)
                .filter(Visit::isAssigned)
                .reward(HardMediumSoftScore.ONE_MEDIUM, Visit::getValue)
                .asConstraint("Attraction value");
    }

    Constraint travelTime(ConstraintFactory factory) {
        return schedule(factory)
                .penalize(HardMediumSoftScore.ONE_SOFT, TripSchedule::totalTravelMin)
                .asConstraint("Travel time");
    }
}
