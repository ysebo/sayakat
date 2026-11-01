package kg.alatoo.sayakat.user;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import kg.alatoo.sayakat.common.Errors;
import kg.alatoo.sayakat.security.AuthUser;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final AuthService authService;
    private final UserRepository users;

    public AuthController(AuthService authService, UserRepository users) {
        this.authService = authService;
        this.users = users;
    }

    public record RegisterRequest(
            @NotBlank @Email String email,
            @NotBlank @Size(min = 8, max = 100) String password,
            @NotBlank String fullName,
            @NotNull Role role,
            String providerName) {
    }

    public record LoginRequest(@NotBlank String email, @NotBlank String password) {
    }

    public record TokenResponse(String accessToken, long expiresIn, UserResponse user) {
    }

    public record UserResponse(Long id, String email, String fullName, Role role) {
        static UserResponse of(User u) {
            return new UserResponse(u.getId(), u.getEmail(), u.getFullName(), u.getRole());
        }
    }

    @PostMapping("/auth/register")
    @ResponseStatus(HttpStatus.CREATED)
    public TokenResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/auth/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal AuthUser principal) {
        return users.findById(principal.id()).map(UserResponse::of)
                .orElseThrow(() -> Errors.notFound("User", principal.id()));
    }
}
