package kg.alatoo.sayakat.dto.response;

public record TokenResponse(String accessToken, long expiresIn, UserResponse user) {
}
