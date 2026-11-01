package kg.alatoo.sayakat.config.properties;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("sayakat.jwt")
public record JwtProperties(String secret, Duration ttl) {
}
