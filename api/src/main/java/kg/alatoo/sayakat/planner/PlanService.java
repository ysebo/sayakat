package kg.alatoo.sayakat.planner;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ai.timefold.solver.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;
import ai.timefold.solver.core.api.solver.SolutionManager;
import ai.timefold.solver.core.api.solver.SolverManager;
import kg.alatoo.sayakat.common.Errors;
import kg.alatoo.sayakat.common.Geo;
import kg.alatoo.sayakat.marketplace.MarketplaceDtos.ServiceResponse;
import kg.alatoo.sayakat.marketplace.ServiceOffering;
import kg.alatoo.sayakat.marketplace.ServiceOfferingRepository;
import kg.alatoo.sayakat.marketplace.ServiceType;
import kg.alatoo.sayakat.planner.PlanDtos.PlanRequest;
import kg.alatoo.sayakat.planner.PlanDtos.PlanResponse;
import kg.alatoo.sayakat.planner.domain.Lodging;
import kg.alatoo.sayakat.planner.domain.TripSolution;
import kg.alatoo.sayakat.planner.greedy.GreedyPlanner;
import kg.alatoo.sayakat.planner.persistence.TripPlan;
import kg.alatoo.sayakat.planner.persistence.TripPlanDay;
import kg.alatoo.sayakat.planner.persistence.TripPlanRepository;
import kg.alatoo.sayakat.planner.solver.TripProblemFactory;
import kg.alatoo.sayakat.planner.solver.TripProblemFactory.CandidateLodging;
import kg.alatoo.sayakat.planner.solver.TripProblemFactory.CandidatePoi;
import kg.alatoo.sayakat.planner.solver.TripProblemFactory.TripRequest;
import kg.alatoo.sayakat.poi.Poi;
import kg.alatoo.sayakat.poi.PoiRepository;
import kg.alatoo.sayakat.routing.GeoPoint;
import kg.alatoo.sayakat.routing.TravelMatrix;
import kg.alatoo.sayakat.routing.TravelMatrixService;
import kg.alatoo.sayakat.routing.TravelMatrixService.Node;
import kg.alatoo.sayakat.user.UserRepository;

@Service
public class PlanService {

    private static final Logger log = LoggerFactory.getLogger(PlanService.class);

    private final TripPlanRepository plans;
    private final PoiRepository pois;
    private final ServiceOfferingRepository services;
    private final UserRepository users;
    private final TravelMatrixService matrixService;
    private final SolverManager<TripSolution, Long> solverManager;
    private final SolutionManager<TripSolution, HardMediumSoftScore> solutionManager;
    private final PlanResultWriter writer;
    private final PlannerProperties properties;

    public PlanService(TripPlanRepository plans, PoiRepository pois, ServiceOfferingRepository services,
                       UserRepository users, TravelMatrixService matrixService,
                       SolverManager<TripSolution, Long> solverManager,
                       SolutionManager<TripSolution, HardMediumSoftScore> solutionManager,
                       PlanResultWriter writer, PlannerProperties properties) {
        this.plans = plans;
        this.pois = pois;
        this.services = services;
        this.users = users;
        this.matrixService = matrixService;
        this.solverManager = solverManager;
        this.solutionManager = solutionManager;
        this.writer = writer;
        this.properties = properties;
    }

    public Long create(Long userId, PlanRequest request) {
        TripPlan plan = createPlanRow(userId, request);
        TripSolution problem = buildProblem(plan);
        Long planId = plan.getId();

        if (plan.getAlgorithm() == TripPlan.Algorithm.GREEDY) {
            TripSolution solved = GreedyPlanner.solve(problem);
            writer.saveSolution(planId, solved, solutionManager.update(solved));
        } else {
            solverManager.solveBuilder()
                    .withProblemId(planId)
                    .withProblem(problem)
                    .withFinalBestSolutionConsumer(solution -> writer.saveSolution(planId, solution, solution.getScore()))
                    .withExceptionHandler((id, error) -> {
                        log.error("Solving plan {} failed", id, error);
                        writer.saveFailure(id, error);
                    })
                    .run();
        }
        return planId;
    }

    @Transactional(readOnly = true)
    public PlanResponse get(Long userId, Long planId) {
        return PlanResponse.of(owned(userId, planId));
    }

    @Transactional(readOnly = true)
    public List<PlanResponse> mine(Long userId) {
        return plans.findByUserIdOrderByCreatedAtDesc(userId).stream().map(PlanResponse::of).toList();
    }

    @Transactional(readOnly = true)
    public List<ServiceResponse> servicesForDay(Long userId, Long planId, int dayIndex, ServiceType type,
                                                double radiusKm) {
        TripPlan plan = owned(userId, planId);
        TripPlanDay day = plan.getItinerary().stream().filter(d -> d.getDayIndex() == dayIndex).findFirst()
                .orElseThrow(() -> Errors.notFound("Day", dayIndex));
        List<GeoPoint> anchors = new ArrayList<>();
        if (day.getLodgingService() != null && day.getLodgingService().getLocation() != null) {
            anchors.add(Geo.toGeoPoint(day.getLodgingService().getLocation()));
        }
        day.getStops().stream().map(s -> Geo.toGeoPoint(s.getPoi().getLocation())).forEach(anchors::add);
        if (anchors.isEmpty()) {
            anchors.add(Geo.toGeoPoint(plan.getStartPoi().getLocation()));
        }
        return mergeNear(anchors, type, radiusKm);
    }

    private List<ServiceResponse> mergeNear(List<GeoPoint> points, ServiceType type, double radiusKm) {
        Map<Long, ServiceOffering> unique = new HashMap<>();
        for (var p : points) {
            for (ServiceOffering s : services.findNear(p.lat(), p.lon(), radiusKm * 1000, type == null ? null : type.name())) {
                unique.putIfAbsent(s.getId(), s);
            }
        }
        return unique.values().stream().map(ServiceResponse::of).toList();
    }

    private TripPlan createPlanRow(Long userId, PlanRequest request) {
        Poi start = pois.findById(request.startPoiId()).orElseThrow(() -> Errors.notFound("POI", request.startPoiId()));
        Long endId = request.endPoiId() == null ? request.startPoiId() : request.endPoiId();
        Poi end = pois.findById(endId).orElseThrow(() -> Errors.notFound("POI", endId));

        TripPlan plan = new TripPlan();
        plan.setUser(users.getReferenceById(userId));
        plan.setStatus(TripPlan.Status.SOLVING);
        plan.setAlgorithm(request.algorithm() == null ? TripPlan.Algorithm.TIMEFOLD : request.algorithm());
        plan.setStartDate(request.startDate());
        plan.setDays(request.days());
        plan.setBudgetKgs(request.budgetKgs());
        plan.setPace(request.pace());
        plan.setInterests(request.interests() == null ? List.of()
                : request.interests().stream().map(Enum::name).sorted().toList());
        plan.setStartPoi(start);
        plan.setEndPoi(end);
        return plans.save(plan);
    }

    private TripSolution buildProblem(TripPlan plan) {
        Long startId = plan.getStartPoi().getId();
        Long endId = plan.getEndPoi().getId();
        List<Poi> candidates = pois.findAll().stream()
                .filter(p -> !p.getId().equals(startId) && !p.getId().equals(endId))
                .toList();
        List<ServiceOffering> lodgings = services.findByTypeAndActiveTrue(ServiceType.ACCOMMODATION).stream()
                .filter(s -> s.getLocation() != null)
                .toList();

        List<Node> nodes = new ArrayList<>();
        candidates.forEach(p -> nodes.add(new Node("poi:" + p.getId(), Geo.toGeoPoint(p.getLocation()))));
        lodgings.forEach(s -> nodes.add(new Node("svc:" + s.getId(), Geo.toGeoPoint(s.getLocation()))));
        nodes.add(new Node("poi:" + startId, Geo.toGeoPoint(plan.getStartPoi().getLocation())));
        int startIndex = nodes.size() - 1;
        int endIndex = startIndex;
        if (!endId.equals(startId)) {
            nodes.add(new Node("poi:" + endId, Geo.toGeoPoint(plan.getEndPoi().getLocation())));
            endIndex = nodes.size() - 1;
        }
        TravelMatrix matrix = matrixService.matrixFor(nodes);

        List<CandidatePoi> candidatePois = new ArrayList<>(candidates.size());
        for (int i = 0; i < candidates.size(); i++) {
            Poi p = candidates.get(i);
            candidatePois.add(new CandidatePoi(p.getId(), p.getName(), p.getCategory().name(), p.getTags(), i,
                    p.getVisitDurationMin(), minutes(p.getOpensAt(), 0), minutes(p.getClosesAt(),
                    TripProblemFactory.MINUTES_PER_DAY), p.getCostKgs(), p.getSeasonFrom(), p.getSeasonTo(),
                    p.getPopularity()));
        }
        List<CandidateLodging> candidateLodgings = new ArrayList<>(lodgings.size());
        for (int i = 0; i < lodgings.size(); i++) {
            ServiceOffering s = lodgings.get(i);
            candidateLodgings.add(new CandidateLodging(s.getId(), s.getTitle(), candidates.size() + i, s.getPriceKgs()));
        }

        Set<String> interests = plan.getInterests().stream().collect(Collectors.toUnmodifiableSet());
        TripRequest request = new TripRequest(plan.getStartDate(), plan.getDays(), plan.getPace(), plan.getBudgetKgs(),
                interests);
        Lodging startLodging = Lodging.of(null, "Hotel in " + plan.getStartPoi().getName(), startIndex,
                properties.defaultCityLodgingPrice());
        return TripProblemFactory.build(request, candidatePois, candidateLodgings, startLodging, endIndex,
                properties.transportCostPerKm(), properties.maxLodgingTransferMin(), matrix);
    }

    private TripPlan owned(Long userId, Long planId) {
        TripPlan plan = plans.findById(planId).orElseThrow(() -> Errors.notFound("Plan", planId));
        if (!plan.getUser().getId().equals(userId)) {
            throw Errors.notFound("Plan", planId);
        }
        return plan;
    }

    private static int minutes(LocalTime time, int fallback) {
        return time == null ? fallback : time.getHour() * 60 + time.getMinute();
    }
}
