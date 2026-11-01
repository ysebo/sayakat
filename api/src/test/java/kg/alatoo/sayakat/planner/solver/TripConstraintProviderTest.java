package kg.alatoo.sayakat.planner.solver;

import static kg.alatoo.sayakat.planner.solver.TripScheduleTest.days;
import static kg.alatoo.sayakat.planner.solver.TripScheduleTest.lake;
import static kg.alatoo.sayakat.planner.solver.TripScheduleTest.museum;
import static kg.alatoo.sayakat.planner.solver.TripScheduleTest.settings;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import ai.timefold.solver.test.api.score.stream.ConstraintVerifier;
import kg.alatoo.sayakat.planner.domain.Pace;
import kg.alatoo.sayakat.planner.domain.TripDay;
import kg.alatoo.sayakat.planner.domain.TripSettings;
import kg.alatoo.sayakat.planner.domain.TripSolution;
import kg.alatoo.sayakat.planner.domain.Visit;

class TripConstraintProviderTest {

    private final ConstraintVerifier<TripConstraintProvider, TripSolution> verifier =
            ConstraintVerifier.build(new TripConstraintProvider(), TripSolution.class, TripDay.class, Visit.class);

    @Test
    void openingHours() {
        List<TripDay> days = days(settings(1, 100_000), List.of(List.of(lake(), museum())));
        verifier.verifyThat(TripConstraintProvider::openingHours)
                .given(days.toArray())
                .penalizesBy(150);
    }

    @Test
    void openingHoursSatisfied() {
        List<TripDay> days = days(settings(1, 100_000), List.of(List.of(museum(), lake())));
        verifier.verifyThat(TripConstraintProvider::openingHours)
                .given(days.toArray())
                .penalizes(0);
    }

    @Test
    void seasonViolatedInWinter() {
        TripSettings winter = new TripSettings(LocalDate.of(2026, 1, 10), 1, Pace.MODERATE, 100_000, Set.of(),
                TripScheduleTest.CITY, 0, 10, TripScheduleTest.matrix());
        Visit lake = lake();
        List<TripDay> days = days(winter, List.of(List.of(lake)));
        verifier.verifyThat(TripConstraintProvider::season)
                .given(days.get(0), lake)
                .penalizesBy(TripConstraintProvider.STRUCTURAL_PENALTY);
    }

    @Test
    void visitsPerDayRespectsPace() {
        TripSettings relaxed = new TripSettings(LocalDate.of(2026, 7, 1), 1, Pace.RELAXED, 100_000, Set.of(),
                TripScheduleTest.CITY, 0, 10, TripScheduleTest.matrix());
        Visit extra = new Visit(9L, "Extra", 1, 30, 0, 1440, 0, 1, 12, 10, TripScheduleTest.CITY);
        List<TripDay> days = days(relaxed, List.of(List.of(museum(), lake(), extra)));
        verifier.verifyThat(TripConstraintProvider::visitsPerDay)
                .given(days.toArray())
                .penalizesBy(TripConstraintProvider.STRUCTURAL_PENALTY);
    }

    @Test
    void budgetPenalisedPerHundredKgs() {
        List<TripDay> days = days(settings(2, 3000), List.of(List.of(museum(), lake()), List.of()));
        verifier.verifyThat(TripConstraintProvider::budget)
                .given(days.toArray())
                .penalizesBy(6);
    }

    @Test
    void rewardsOnlyAssignedVisits() {
        Visit museum = museum();
        Visit lake = lake();
        List<TripDay> days = days(settings(1, 100_000), List.of(List.of(museum)));
        verifier.verifyThat(TripConstraintProvider::attractionValue)
                .given(days.get(0), museum, lake)
                .rewardsWith(museum.getValue());
    }
}
