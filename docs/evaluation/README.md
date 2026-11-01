# Planner evaluation

## Goal

Measure how well the route planner satisfies constraints, how much value it collects for the tourist and how
long it needs, and compare it with a simple baseline.

## Algorithms

| Name | Description |
|---|---|
| Greedy | Fills days in order; repeatedly appends the place with the best value / (travel + visit time) ratio that keeps the whole trip feasible. |
| Timefold CH | Timefold construction heuristic only. |
| Timefold LS *N*s | Construction heuristic followed by local search (Late Acceptance) with an *N*-second limit. |
| Hybrid *N*s | The greedy route is used as the starting solution for Timefold; local search runs for *N* seconds. This is what the application uses. |

All algorithms are scored with the same `TripConstraintProvider`, so scores are directly comparable.

## Instances

`SyntheticInstances` generates reproducible problems: POIs are clustered around five real regions
(Chuy, Issyk-Kul, Naryn, Osh, Talas), with a realistic mix of categories, opening hours and seasonal
availability; about a quarter of the POI count is added as lodgings. Travel times use the haversine estimate
(×1.4 detour, 50 km/h). Sizes: 20, 50, 100 and 200 POIs (3, 5, 7 and 7 days), five seeds each,
a July trip with the Balanced pace and a budget of 15 000 KGS per day.

## Metrics

- **Feasibility** — hard score is 0 (no opening-hour, day-length, season, pace, lodging or budget violations).
- **Value** — medium score, the sum of personalised values of visited places.
- **Driving time** — soft score.
- **Solve time** and **time to best** — when the final best solution was found.

## Findings

Full numbers and charts: [results.md](results.md).

1. **Constraint satisfaction.** All 120 runs (6 algorithms × 4 sizes × 5 seeds) are feasible: no hard
   constraint is violated by any algorithm.
2. **Local search pays off.** Timefold with local search collects 17–44 % more value than the greedy baseline
   on 20, 50 and 200 POIs.
3. **Plain Timefold is not robust.** On 100 POIs it beats greedy in only 2 of 5 seeds (+3 % on average): local
   search stalls in a local optimum after about 2.5 s and extra time does not help (10 s and 30 s give the same
   result).
4. **The construction heuristic alone is weak.** Timefold CH is 10–49 % worse than greedy — it inserts places
   without looking at the value / time ratio.
5. **Hybrid fixes it.** Starting local search from the greedy route beats greedy in 20 of 20 runs
   (+21–50 %, +25.7 % on 100 POIs) and reaches its best solution sooner (8.2 s vs 11.8 s on 200 POIs).
   It is therefore used in the application.
6. **Time limit.** For up to 100 POIs the best solution is found within 4 s; only 200-POI instances benefit
   from more than 10 s. The production limit of 20 s with 5 s unimproved termination is a reasonable trade-off.

## Threats to validity

- Instances are synthetic and use straight-line travel estimates; real road times (OSRM) change absolute
  numbers but not the comparison, since all algorithms share the same matrix.
- Five seeds per size is enough to show the trend, not for statistical significance tests.
- Timefold default settings (Late Acceptance, default move selectors) were used without tuning.

## Reproduce

```bash
cd api
mvn -Pexperiments test -Dexp.sizes=20,50,100,200 -Dexp.seeds=5 -Dexp.limits=10,30
node ../docs/evaluation/build-report.mjs
```

The generated tables and charts are in [results.md](results.md).
