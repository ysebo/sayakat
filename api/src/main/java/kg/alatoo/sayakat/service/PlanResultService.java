package kg.alatoo.sayakat.service;

import ai.timefold.solver.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;

import kg.alatoo.sayakat.planner.domain.TripSolution;

public interface PlanResultService {

    void saveSolution(Long planId, TripSolution solution, HardMediumSoftScore score);

    void saveFailure(Long planId, Throwable error);
}
