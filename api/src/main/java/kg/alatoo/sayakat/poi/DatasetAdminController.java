package kg.alatoo.sayakat.poi;

import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DatasetAdminController {

    private final DatasetImporter importer;

    public DatasetAdminController(DatasetImporter importer) {
        this.importer = importer;
    }

    @PostMapping("/api/admin/dataset/pois/import")
    public Map<String, Integer> importPois() {
        return Map.of("imported", importer.importPois());
    }
}
