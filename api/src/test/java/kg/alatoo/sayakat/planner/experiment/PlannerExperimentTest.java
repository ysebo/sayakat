package kg.alatoo.sayakat.planner.experiment;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.function.Supplier;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import ai.timefold.solver.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;
import ai.timefold.solver.core.api.solver.SolutionManager;
import ai.timefold.solver.core.api.solver.Solver;
import ai.timefold.solver.core.api.solver.SolverFactory;

import kg.alatoo.sayakat.planner.PlannerSolverFactory.Mode;
import kg.alatoo.sayakat.planner.PlannerSolverFactory;
import kg.alatoo.sayakat.planner.SyntheticInstances;
import kg.alatoo.sayakat.planner.domain.Pace;
import kg.alatoo.sayakat.planner.domain.TripSolution;
import kg.alatoo.sayakat.planner.domain.Visit;
import kg.alatoo.sayakat.planner.greedy.GreedyPlanner;
import kg.alatoo.sayakat.planner.solver.TripSchedule;

@Tag("experiment")
class PlannerExperimentTest {

    private static final LocalDate START = LocalDate.of(2026, 7, 6);
    private static final Set<String> INTERESTS = Set.of("LAKE", "HIKING", "NATURE");

    @Test
    void run() throws IOException {
        int[] sizes = intList(System.getProperty("exp.sizes", "20,50,100,200"));
        int seeds = Integer.parseInt(System.getProperty("exp.seeds", "5"));
        int[] limits = intList(System.getProperty("exp.limits", "10,30"));

        Path dir = Path.of("target", "experiments");
        Files.createDirectories(dir);
        try (PrintWriter results = new PrintWriter(Files.newBufferedWriter(dir.resolve("results.csv")));
             PrintWriter convergence = new PrintWriter(Files.newBufferedWriter(dir.resolve("convergence.csv")))) {
            results.println("algorithm,pois,days,seed,hard,medium,soft,feasible,visited,value,travel_min,cost_kgs,"
                    + "late_min,overtime_min,no_lodging,over_budget_kgs,time_ms");
            convergence.println("algorithm,pois,seed,time_ms,hard,medium,soft");

            PlannerSolverFactory.create(Mode.FULL, 3).buildSolver().solve(
                    SyntheticInstances.generate(30, 3, Pace.MODERATE, 45_000, INTERESTS, START, 999));

            for (int size : sizes) {
                int days = size <= 20 ? 3 : size <= 50 ? 5 : 7;
                int budget = 15_000 * days;
                for (int seed = 1; seed <= seeds; seed++) {
                    final int s = seed;
                    Supplier<TripSolution> instance = () ->
                            SyntheticInstances.generate(size, days, Pace.MODERATE, budget, INTERESTS, START, s);

                    long t0 = System.nanoTime();
                    TripSolution greedy = GreedyPlanner.solve(instance.get());
                    long greedyMs = (System.nanoTime() - t0) / 1_000_000;
                    write(results, "GREEDY", size, days, seed, greedy, score(greedy), greedyMs);

                    runTimefold(results, null, "TF_CH", Mode.CONSTRUCTION_ONLY, 60, instance, size, days, seed);

                    Supplier<TripSolution> warmStart = () -> GreedyPlanner.solve(instance.get());
                    for (int limit : limits) {
                        runTimefold(results, convergence, "TF_LS_" + limit + "s", Mode.FULL, limit, instance,
                                size, days, seed);
                        runTimefold(results, convergence, "HYBRID_" + limit + "s", Mode.FULL, limit, warmStart,
                                size, days, seed);
                    }
                    results.flush();
                    convergence.flush();
                    System.out.printf("done: pois=%d seed=%d%n", size, seed);
                }
            }
        }
        System.out.println("Results written to " + dir.toAbsolutePath());
    }

    private static void runTimefold(PrintWriter results, PrintWriter convergence, String name, Mode mode, int limit,
                                    Supplier<TripSolution> instance, int size, int days, int seed) {
        SolverFactory<TripSolution> factory = PlannerSolverFactory.create(mode, limit);
        Solver<TripSolution> solver = factory.buildSolver();
        long start = System.nanoTime();
        if (convergence != null) {
            solver.addEventListener(event -> {
                HardMediumSoftScore sc = event.getNewBestSolution().getScore();
                convergence.printf(Locale.ROOT, "%s,%d,%d,%d,%d,%d,%d%n", name, size, seed,
                        (System.nanoTime() - start) / 1_000_000, sc.hardScore(), sc.mediumScore(), sc.softScore());
            });
        }
        TripSolution solved = solver.solve(instance.get());
        long ms = (System.nanoTime() - start) / 1_000_000;
        write(results, name, size, days, seed, solved, solved.getScore(), ms);
    }

    private static HardMediumSoftScore score(TripSolution solution) {
        SolutionManager<TripSolution, HardMediumSoftScore> manager =
                SolutionManager.create(PlannerSolverFactory.create(Mode.CONSTRUCTION_ONLY, 1));
        return manager.update(solution);
    }

    private static void write(PrintWriter out, String algorithm, int size, int days, int seed,
                              TripSolution solution, HardMediumSoftScore score, long ms) {
        TripSchedule schedule = TripSchedule.of(solution.getDays());
        long visited = solution.getVisits().stream().filter(Visit::isAssigned).count();
        out.printf(Locale.ROOT, "%s,%d,%d,%d,%d,%d,%d,%b,%d,%d,%d,%d,%d,%d,%d,%d,%d%n",
                algorithm, size, days, seed, score.hardScore(), score.mediumScore(), score.softScore(),
                score.isFeasible(), visited, schedule.totalValue(), schedule.totalTravelMin(), schedule.totalCostKgs(),
                schedule.lateMinutes(), schedule.overtimeMinutes(), schedule.nightsWithoutLodging(),
                schedule.overBudgetKgs(), ms);
    }

    private static int[] intList(String csv) {
        return Arrays.stream(csv.split(",")).map(String::trim).mapToInt(Integer::parseInt).toArray();
    }
}
