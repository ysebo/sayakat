package kg.alatoo.sayakat.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("sayakat.routing")
public record RoutingProperties(String provider, String osrmUrl, double osrmDurationFactor,
                                double haversineDetourFactor, double haversineSpeedKmh) {

    public boolean useOsrm() {
        return "osrm".equalsIgnoreCase(provider);
    }
}
