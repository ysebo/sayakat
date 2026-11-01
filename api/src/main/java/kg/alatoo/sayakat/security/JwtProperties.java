package kg.alatoo.sayakat.security;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("sayakat.jwt")
public record JwtProperties(String secret, Duration ttl) {
}
