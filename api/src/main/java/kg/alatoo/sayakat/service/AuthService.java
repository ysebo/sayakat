package kg.alatoo.sayakat.service;

import kg.alatoo.sayakat.dto.request.LoginRequest;
import kg.alatoo.sayakat.dto.request.RegisterRequest;
import kg.alatoo.sayakat.dto.response.TokenResponse;
import kg.alatoo.sayakat.dto.response.UserResponse;
import kg.alatoo.sayakat.entity.User;
import kg.alatoo.sayakat.entity.enums.Role;

public interface AuthService {

    TokenResponse register(RegisterRequest request);

    TokenResponse login(LoginRequest request);

    UserResponse me(Long userId);

    User createUser(String email, String rawPassword, String fullName, Role role);
}
