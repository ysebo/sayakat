package kg.alatoo.sayakat.planner;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("sayakat.planner")
public record PlannerProperties(int defaultCityLodgingPrice, int maxLodgingTransferMin, int transportCostPerKm) {
}
