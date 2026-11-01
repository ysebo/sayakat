package kg.alatoo.sayakat.routing;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import kg.alatoo.sayakat.routing.TravelMatrixService.RebuildResult;

@RestController
public class MatrixAdminController {

    private final TravelMatrixService matrixService;

    public MatrixAdminController(TravelMatrixService matrixService) {
        this.matrixService = matrixService;
    }

    @PostMapping("/api/admin/matrix/rebuild")
    public RebuildResult rebuild() {
        return matrixService.rebuild();
    }
}
