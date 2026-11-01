package kg.alatoo.sayakat.planner;

import java.time.Duration;

import ai.timefold.solver.core.api.solver.SolverFactory;
import ai.timefold.solver.core.config.constructionheuristic.ConstructionHeuristicPhaseConfig;
import ai.timefold.solver.core.config.localsearch.LocalSearchPhaseConfig;
import ai.timefold.solver.core.config.solver.SolverConfig;
import ai.timefold.solver.core.config.solver.termination.TerminationConfig;

import kg.alatoo.sayakat.planner.PlannerSolverFactory.Mode;
import kg.alatoo.sayakat.planner.domain.TripDay;
import kg.alatoo.sayakat.planner.domain.TripSolution;
import kg.alatoo.sayakat.planner.domain.Visit;
import kg.alatoo.sayakat.planner.solver.TripConstraintProvider;

public final class PlannerSolverFactory {

    public enum Mode {
        CONSTRUCTION_ONLY,
        FULL
    }

    private PlannerSolverFactory() {
    }

    public static SolverConfig config(Mode mode, long spentLimitSeconds) {
        SolverConfig config = new SolverConfig()
                .withSolutionClass(TripSolution.class)
                .withEntityClasses(TripDay.class, Visit.class)
                .withConstraintProviderClass(TripConstraintProvider.class)
                .withTerminationConfig(new TerminationConfig().withSpentLimit(Duration.ofSeconds(spentLimitSeconds)));
        if (mode == Mode.CONSTRUCTION_ONLY) {
            config.withPhases(new ConstructionHeuristicPhaseConfig());
        } else {
            config.withPhases(new ConstructionHeuristicPhaseConfig(), new LocalSearchPhaseConfig());
        }
        return config;
    }

    public static SolverFactory<TripSolution> create(Mode mode, long spentLimitSeconds) {
        return SolverFactory.create(config(mode, spentLimitSeconds));
    }
}
