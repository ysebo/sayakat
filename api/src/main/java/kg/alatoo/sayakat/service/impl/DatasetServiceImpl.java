package kg.alatoo.sayakat.service.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import kg.alatoo.sayakat.entity.Poi;
import kg.alatoo.sayakat.entity.Provider;
import kg.alatoo.sayakat.entity.ServiceOffering;
import kg.alatoo.sayakat.entity.enums.PoiCategory;
import kg.alatoo.sayakat.entity.enums.PriceUnit;
import kg.alatoo.sayakat.entity.enums.ServiceType;
import kg.alatoo.sayakat.repository.PoiRepository;
import kg.alatoo.sayakat.repository.ServiceOfferingRepository;
import kg.alatoo.sayakat.service.DatasetService;
import kg.alatoo.sayakat.util.Geo;

@Service
@RequiredArgsConstructor
public class DatasetServiceImpl implements DatasetService {

    private final PoiRepository poiRepository;
    private final ServiceOfferingRepository serviceOfferingRepository;

    @Override
    @Transactional
    public int importPois() {
        List<String[]> rows = read("data/pois.csv", 16);
        for (String[] r : rows) {
            Poi poi = poiRepository.findByExternalId(r[0]).orElseGet(Poi::new);
            poi.setExternalId(r[0]);
            poi.setName(r[1]);
            poi.setCategory(PoiCategory.valueOf(r[2]));
            poi.setTags(r[3].isBlank() ? List.of() : Arrays.asList(r[3].split(";")));
            poi.setRegion(r[4]);
            poi.setLocation(Geo.point(Double.parseDouble(r[5]), Double.parseDouble(r[6])));
            poi.setVisitDurationMin(Integer.parseInt(r[7]));
            poi.setCostKgs(Integer.parseInt(r[8]));
            poi.setOpensAt(time(r[9]));
            poi.setClosesAt(time(r[10]));
            poi.setSeasonFrom(Short.parseShort(r[11]));
            poi.setSeasonTo(Short.parseShort(r[12]));
            poi.setPopularity(Short.parseShort(r[13]));
            poi.setSource(r[14]);
            poi.setDescription(r[15]);
            poiRepository.save(poi);
        }
        return rows.size();
    }

    @Override
    @Transactional
    public int importLodgings(Provider owner) {
        List<String[]> rows = read("data/lodgings.csv", 7);
        for (String[] r : rows) {
            ServiceOffering s = serviceOfferingRepository.findByExternalId(r[0]).orElseGet(ServiceOffering::new);
            s.setExternalId(r[0]);
            s.setProvider(owner);
            s.setType(ServiceType.ACCOMMODATION);
            s.setTitle(r[1]);
            s.setLocation(Geo.point(Double.parseDouble(r[2]), Double.parseDouble(r[3])));
            s.setPriceKgs(Integer.parseInt(r[4]));
            s.setPriceUnit(PriceUnit.PER_NIGHT);
            s.setCapacity(Integer.parseInt(r[5]));
            s.setDescription(r[6]);
            serviceOfferingRepository.save(s);
        }
        return rows.size();
    }

    private static LocalTime time(String value) {
        return value.isBlank() ? null : LocalTime.parse(value);
    }

    private static List<String[]> read(String path, int columns) {
        List<String[]> rows = new ArrayList<>();
        forEachLine(path, line -> {
            String[] cells = line.split(",", columns);
            if (cells.length != columns) {
                throw new IllegalArgumentException(path + ": expected " + columns + " columns in: " + line);
            }
            rows.add(Arrays.stream(cells).map(String::trim).toArray(String[]::new));
        });
        return rows;
    }

    private static void forEachLine(String path, Consumer<String> consumer) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new ClassPathResource(path).getInputStream(), StandardCharsets.UTF_8))) {
            reader.readLine();
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.isBlank()) {
                    consumer.accept(line);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
