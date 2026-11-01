package kg.alatoo.sayakat.routing;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Component
public class OsrmClient {

    private final RoutingProperties properties;
    private RestClient client;

    public OsrmClient(RoutingProperties properties) {
        this.properties = properties;
    }

    private synchronized RestClient client() {
        if (client == null) {
            client = RestClient.builder()
                    .baseUrl(properties.osrmUrl())
                    .requestFactory(new SimpleClientHttpRequestFactory())
                    .build();
        }
        return client;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record TableResponse(String code, Double[][] durations, Double[][] distances) {
    }

    public Double[][][] table(List<GeoPoint> points) {
        String coordinates = points.stream()
                .map(p -> String.format(Locale.ROOT, "%.6f,%.6f", p.lon(), p.lat()))
                .collect(Collectors.joining(";"));
        TableResponse response = client().get()
                .uri("/table/v1/driving/{coords}?annotations=duration,distance", coordinates)
                .retrieve()
                .body(TableResponse.class);
        if (response == null || !"Ok".equals(response.code())) {
            throw new IllegalStateException("OSRM table request failed: " + (response == null ? "empty" : response.code()));
        }
        Double[][] durations = response.durations();
        for (Double[] row : durations) {
            for (int j = 0; j < row.length; j++) {
                if (row[j] != null) {
                    row[j] = row[j] * properties.osrmDurationFactor();
                }
            }
        }
        return new Double[][][] {durations, response.distances()};
    }
}
