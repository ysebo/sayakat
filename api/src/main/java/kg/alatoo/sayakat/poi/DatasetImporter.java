package kg.alatoo.sayakat.poi;

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

import kg.alatoo.sayakat.common.Geo;
import kg.alatoo.sayakat.marketplace.PriceUnit;
import kg.alatoo.sayakat.marketplace.Provider;
import kg.alatoo.sayakat.marketplace.ServiceOffering;
import kg.alatoo.sayakat.marketplace.ServiceOfferingRepository;
import kg.alatoo.sayakat.marketplace.ServiceType;

@Service
public class DatasetImporter {

    private final PoiRepository pois;
    private final ServiceOfferingRepository services;

    public DatasetImporter(PoiRepository pois, ServiceOfferingRepository services) {
        this.pois = pois;
        this.services = services;
    }

    @Transactional
    public int importPois() {
        List<String[]> rows = read("data/pois.csv", 16);
        for (String[] r : rows) {
            Poi poi = pois.findByExternalId(r[0]).orElseGet(Poi::new);
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
            pois.save(poi);
        }
        return rows.size();
    }

    @Transactional
    public int importLodgings(Provider owner) {
        List<String[]> rows = read("data/lodgings.csv", 7);
        for (String[] r : rows) {
            ServiceOffering s = services.findByExternalId(r[0]).orElseGet(ServiceOffering::new);
            s.setExternalId(r[0]);
            s.setProvider(owner);
            s.setType(ServiceType.ACCOMMODATION);
            s.setTitle(r[1]);
            s.setLocation(Geo.point(Double.parseDouble(r[2]), Double.parseDouble(r[3])));
            s.setPriceKgs(Integer.parseInt(r[4]));
            s.setPriceUnit(PriceUnit.PER_NIGHT);
            s.setCapacity(Integer.parseInt(r[5]));
            s.setDescription(r[6]);
            services.save(s);
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
