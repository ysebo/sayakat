package kg.alatoo.sayakat.planner.greedy;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import kg.alatoo.sayakat.planner.domain.TripDay;
import kg.alatoo.sayakat.planner.domain.TripSolution;
import kg.alatoo.sayakat.planner.domain.Visit;
import kg.alatoo.sayakat.planner.solver.TripSchedule;
import kg.alatoo.sayakat.routing.TravelMatrix;

public final class GreedyPlanner {

    private GreedyPlanner() {
    }

    public static TripSolution solve(TripSolution solution) {
        TravelMatrix matrix = solution.getSettings().matrix();
        int maxPerDay = solution.getSettings().pace().maxVisitsPerDay();
        Set<Visit> used = new HashSet<>();
        int currentLocation = solution.getSettings().startLodging().locationIndex();

        for (TripDay day : solution.getDays()) {
            day.setVisits(new ArrayList<>());
            while (day.getVisits().size() < maxPerDay) {
                Visit best = null;
                double bestRatio = -1;
                for (Visit candidate : solution.getVisits()) {
                    if (used.contains(candidate) || !candidate.isInSeason(day.getDate())) {
                        continue;
                    }
                    int cost = matrix.minutes(currentLocation, candidate.getLocationIndex()) + candidate.getDurationMin();
                    double ratio = candidate.getValue() / (double) Math.max(1, cost);
                    if (ratio <= bestRatio) {
                        continue;
                    }
                    day.getVisits().add(candidate);
                    boolean feasible = TripSchedule.of(solution.getDays()).isTimelineFeasible();
                    day.getVisits().remove(day.getVisits().size() - 1);
                    if (feasible) {
                        best = candidate;
                        bestRatio = ratio;
                    }
                }
                if (best == null) {
                    break;
                }
                day.getVisits().add(best);
                used.add(best);
                currentLocation = best.getLocationIndex();
            }
            TripSchedule schedule = TripSchedule.of(solution.getDays());
            var night = schedule.days().get(day.getIndex()).lodging();
            if (night != null) {
                currentLocation = night.locationIndex();
            }
        }

        for (TripDay day : solution.getDays()) {
            for (Visit visit : day.getVisits()) {
                visit.setDay(day);
            }
        }
        return solution;
    }
}
