package kg.alatoo.sayakat.planner;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Set;

import org.junit.jupiter.api.Test;

import ai.timefold.solver.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;
import ai.timefold.solver.core.api.solver.SolutionManager;
import ai.timefold.solver.core.api.solver.SolverFactory;
import kg.alatoo.sayakat.planner.domain.Pace;
import kg.alatoo.sayakat.planner.domain.TripSolution;
import kg.alatoo.sayakat.planner.greedy.GreedyPlanner;
import kg.alatoo.sayakat.planner.solver.TripSchedule;

class PlannerSmokeTest {

    private static final LocalDate JULY = LocalDate.of(2026, 7, 6);

    @Test
    void greedyProducesFeasibleTrip() {
        TripSolution solution = GreedyPlanner.solve(
                SyntheticInstances.generate(40, 4, Pace.MODERATE, 60_000, Set.of("LAKE", "HIKING"), JULY, 1));
        SolutionManager<TripSolution, HardMediumSoftScore> manager =
                SolutionManager.create(PlannerSolverFactory.create(PlannerSolverFactory.Mode.CONSTRUCTION_ONLY, 1));

        HardMediumSoftScore score = manager.update(solution);

        assertThat(score.isFeasible()).as(score.toString()).isTrue();
        assertThat(TripSchedule.of(solution.getDays()).totalValue()).isPositive();
    }

    @Test
    void timefoldFindsFeasibleTripAtLeastAsValuableAsGreedy() {
        TripSolution problem = SyntheticInstances.generate(40, 4, Pace.MODERATE, 60_000, Set.of("LAKE", "HIKING"), JULY, 1);
        SolverFactory<TripSolution> factory = PlannerSolverFactory.create(PlannerSolverFactory.Mode.FULL, 5);

        TripSolution solved = factory.buildSolver().solve(problem);
        TripSolution greedy = GreedyPlanner.solve(
                SyntheticInstances.generate(40, 4, Pace.MODERATE, 60_000, Set.of("LAKE", "HIKING"), JULY, 1));
        SolutionManager<TripSolution, HardMediumSoftScore> manager = SolutionManager.create(factory);
        HardMediumSoftScore greedyScore = manager.update(greedy);

        assertThat(solved.getScore().isFeasible()).as(solved.getScore().toString()).isTrue();
        assertThat(solved.getScore().mediumScore()).isGreaterThanOrEqualTo(greedyScore.mediumScore());
    }
}
