package kg.alatoo.sayakat.service;

import java.util.List;

import kg.alatoo.sayakat.dto.response.MatrixRebuildResponse;
import kg.alatoo.sayakat.planner.routing.RoutingNode;
import kg.alatoo.sayakat.planner.routing.TravelMatrix;

public interface TravelMatrixService {

    List<RoutingNode> allNodes();

    MatrixRebuildResponse rebuild();

    TravelMatrix matrixFor(List<RoutingNode> nodes);
}
