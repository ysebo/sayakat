package kg.alatoo.sayakat.poi;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
public class PoiController {

    private final PoiService service;

    public PoiController(PoiService service) {
        this.service = service;
    }

    @GetMapping("/api/pois")
    public List<PoiDto> list(@RequestParam(required = false) PoiCategory category,
                             @RequestParam(required = false) Double minLat,
                             @RequestParam(required = false) Double minLon,
                             @RequestParam(required = false) Double maxLat,
                             @RequestParam(required = false) Double maxLon) {
        if (minLat != null && minLon != null && maxLat != null && maxLon != null) {
            return service.inBox(minLat, minLon, maxLat, maxLon, category);
        }
        return service.list(category);
    }

    @GetMapping("/api/pois/{id}")
    public PoiDto get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping("/api/admin/pois")
    @ResponseStatus(HttpStatus.CREATED)
    public PoiDto create(@Valid @RequestBody PoiDto dto) {
        return service.create(dto);
    }

    @PutMapping("/api/admin/pois/{id}")
    public PoiDto update(@PathVariable Long id, @Valid @RequestBody PoiDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/api/admin/pois/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
