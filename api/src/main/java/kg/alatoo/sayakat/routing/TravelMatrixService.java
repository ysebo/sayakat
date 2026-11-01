package kg.alatoo.sayakat.routing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TravelMatrixService {

    private static final Logger log = LoggerFactory.getLogger(TravelMatrixService.class);

    public record Node(String key, GeoPoint point) {
    }

    public record RebuildResult(int nodes, int legs, String provider, long millis) {
    }

    private final JdbcTemplate jdbc;
    private final OsrmClient osrm;
    private final RoutingProperties properties;

    public TravelMatrixService(JdbcTemplate jdbc, OsrmClient osrm, RoutingProperties properties) {
        this.jdbc = jdbc;
        this.osrm = osrm;
        this.properties = properties;
    }

    public List<Node> allNodes() {
        List<Node> nodes = new ArrayList<>();
        jdbc.query("SELECT id, ST_Y(location) lat, ST_X(location) lon FROM poi", rs -> {
            nodes.add(new Node("poi:" + rs.getLong("id"), new GeoPoint(rs.getDouble("lat"), rs.getDouble("lon"))));
        });
        jdbc.query("""
                SELECT id, ST_Y(location) lat, ST_X(location) lon FROM service_offering
                WHERE location IS NOT NULL AND type = 'ACCOMMODATION' AND active
                """, rs -> {
            nodes.add(new Node("svc:" + rs.getLong("id"), new GeoPoint(rs.getDouble("lat"), rs.getDouble("lon"))));
        });
        return nodes;
    }

    @Transactional
    public RebuildResult rebuild() {
        long start = System.currentTimeMillis();
        List<Node> nodes = allNodes();
        List<GeoPoint> points = nodes.stream().map(Node::point).toList();
        TravelMatrix fallback = HaversineMatrixBuilder.build(points, properties.haversineDetourFactor(),
                properties.haversineSpeedKmh());

        int n = nodes.size();
        int[][] minutes = new int[n][n];
        int[][] meters = new int[n][n];
        String[][] source = new String[n][n];
        Double[][][] table = properties.useOsrm() && n > 1 ? osrm.table(points) : null;
        int fallbacks = 0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (table != null && table[0][i][j] != null && table[1][i][j] != null) {
                    minutes[i][j] = (int) Math.ceil(table[0][i][j] / 60.0);
                    meters[i][j] = (int) Math.round(table[1][i][j]);
                    source[i][j] = "osrm";
                } else {
                    minutes[i][j] = fallback.minutes(i, j);
                    meters[i][j] = fallback.meters(i, j);
                    source[i][j] = "haversine";
                    if (table != null && i != j) {
                        fallbacks++;
                    }
                }
            }
        }
        if (fallbacks > 0) {
            log.warn("OSRM could not route {} pairs, used haversine estimate for them", fallbacks);
        }

        jdbc.update("DELETE FROM travel_leg");
        List<Object[]> rows = new ArrayList<>(n * n);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                rows.add(new Object[] {nodes.get(i).key(), nodes.get(j).key(), minutes[i][j] * 60, meters[i][j],
                        source[i][j]});
            }
        }
        jdbc.batchUpdate("INSERT INTO travel_leg (from_key, to_key, duration_s, distance_m, provider) VALUES (?,?,?,?,?)",
                rows);
        long ms = System.currentTimeMillis() - start;
        String provider = table != null ? "osrm" : "haversine";
        log.info("Travel matrix rebuilt: {} nodes, {} legs via {} in {} ms", n, rows.size(), provider, ms);
        return new RebuildResult(n, rows.size(), provider, ms);
    }

    @Transactional(readOnly = true)
    public TravelMatrix matrixFor(List<Node> nodes) {
        int n = nodes.size();
        Map<String, Integer> index = new HashMap<>();
        for (int i = 0; i < n; i++) {
            index.put(nodes.get(i).key(), i);
        }
        TravelMatrix fallback = HaversineMatrixBuilder.build(nodes.stream().map(Node::point).toList(),
                properties.haversineDetourFactor(), properties.haversineSpeedKmh());
        int[][] minutes = new int[n][n];
        int[][] meters = new int[n][n];
        boolean[][] known = new boolean[n][n];

        String[] keys = index.keySet().toArray(String[]::new);
        jdbc.query(con -> {
            var ps = con.prepareStatement(
                    "SELECT from_key, to_key, duration_s, distance_m FROM travel_leg WHERE from_key = ANY(?) AND to_key = ANY(?)");
            var array = con.createArrayOf("varchar", keys);
            ps.setArray(1, array);
            ps.setArray(2, array);
            return ps;
        }, rs -> {
            int i = index.get(rs.getString(1));
            int j = index.get(rs.getString(2));
            minutes[i][j] = (int) Math.ceil(rs.getInt(3) / 60.0);
            meters[i][j] = rs.getInt(4);
            known[i][j] = true;
        });

        int missing = 0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (!known[i][j] && i != j) {
                    minutes[i][j] = fallback.minutes(i, j);
                    meters[i][j] = fallback.meters(i, j);
                    missing++;
                }
            }
        }
        if (missing > 0) {
            log.debug("{} matrix pairs not in travel_leg, estimated with haversine", missing);
        }
        return new TravelMatrix(minutes, meters);
    }
}
