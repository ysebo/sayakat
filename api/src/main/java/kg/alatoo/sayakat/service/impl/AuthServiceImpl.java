package kg.alatoo.sayakat.service.impl;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;

import kg.alatoo.sayakat.dto.request.LoginRequest;
import kg.alatoo.sayakat.dto.request.RegisterRequest;
import kg.alatoo.sayakat.dto.response.TokenResponse;
import kg.alatoo.sayakat.dto.response.UserResponse;
import kg.alatoo.sayakat.entity.Provider;
import kg.alatoo.sayakat.entity.User;
import kg.alatoo.sayakat.entity.enums.Role;
import kg.alatoo.sayakat.exception.Errors;
import kg.alatoo.sayakat.repository.ProviderRepository;
import kg.alatoo.sayakat.repository.UserRepository;
import kg.alatoo.sayakat.security.JwtService;
import kg.alatoo.sayakat.service.AuthService;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final ProviderRepository providerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    @Transactional
    public TokenResponse register(RegisterRequest request) {
        if (request.role() == Role.ADMIN) {
            throw Errors.badRequest("Admin accounts cannot be self-registered");
        }
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw Errors.conflict("Email already registered");
        }
        User user = createUser(request.email(), request.password(), request.fullName(), request.role());
        if (request.role() == Role.PROVIDER) {
            Provider provider = new Provider();
            provider.setUser(user);
            provider.setDisplayName(StringUtils.hasText(request.providerName())
                    ? request.providerName() : request.fullName());
            providerRepository.save(provider);
        }
        return token(user);
    }

    @Override
    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.email())
                .filter(u -> passwordEncoder.matches(request.password(), u.getPasswordHash()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));
        return token(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse me(Long userId) {
        return userRepository.findById(userId).map(UserResponse::of)
                .orElseThrow(() -> Errors.notFound("User", userId));
    }

    @Override
    @Transactional
    public User createUser(String email, String rawPassword, String fullName, Role role) {
        User user = new User();
        user.setEmail(email.toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setFullName(fullName);
        user.setRole(role);
        return userRepository.save(user);
    }

    private TokenResponse token(User user) {
        return new TokenResponse(jwtService.issue(user), jwtService.ttlSeconds(), UserResponse.of(user));
    }
}
