package kg.alatoo.sayakat.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import kg.alatoo.sayakat.dto.response.MatrixRebuildResponse;
import kg.alatoo.sayakat.service.DatasetService;
import kg.alatoo.sayakat.service.TravelMatrixService;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final DatasetService datasetService;
    private final TravelMatrixService travelMatrixService;

    @PostMapping("/dataset/pois/import")
    public Map<String, Integer> importPois() {
        return Map.of("imported", datasetService.importPois());
    }

    @PostMapping("/matrix/rebuild")
    public MatrixRebuildResponse rebuildMatrix() {
        return travelMatrixService.rebuild();
    }
}
