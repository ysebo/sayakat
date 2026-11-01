package kg.alatoo.sayakat.user;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import kg.alatoo.sayakat.common.Errors;
import kg.alatoo.sayakat.marketplace.Provider;
import kg.alatoo.sayakat.marketplace.ProviderRepository;
import kg.alatoo.sayakat.security.JwtService;
import kg.alatoo.sayakat.user.AuthController.LoginRequest;
import kg.alatoo.sayakat.user.AuthController.RegisterRequest;
import kg.alatoo.sayakat.user.AuthController.TokenResponse;
import kg.alatoo.sayakat.user.AuthController.UserResponse;

@Service
public class AuthService {

    private final UserRepository users;
    private final ProviderRepository providers;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository users, ProviderRepository providers, PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.users = users;
        this.providers = providers;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public TokenResponse register(RegisterRequest request) {
        if (request.role() == Role.ADMIN) {
            throw Errors.badRequest("Admin accounts cannot be self-registered");
        }
        if (users.existsByEmailIgnoreCase(request.email())) {
            throw Errors.conflict("Email already registered");
        }
        User user = createUser(request.email(), request.password(), request.fullName(), request.role());
        if (request.role() == Role.PROVIDER) {
            Provider provider = new Provider();
            provider.setUser(user);
            provider.setDisplayName(StringUtils.hasText(request.providerName())
                    ? request.providerName() : request.fullName());
            providers.save(provider);
        }
        return token(user);
    }

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        User user = users.findByEmailIgnoreCase(request.email())
                .filter(u -> passwordEncoder.matches(request.password(), u.getPasswordHash()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));
        return token(user);
    }

    @Transactional
    public User createUser(String email, String rawPassword, String fullName, Role role) {
        User user = new User();
        user.setEmail(email.toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setFullName(fullName);
        user.setRole(role);
        return users.save(user);
    }

    private TokenResponse token(User user) {
        return new TokenResponse(jwtService.issue(user), jwtService.ttlSeconds(), UserResponse.of(user));
    }
}
