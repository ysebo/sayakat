package kg.alatoo.sayakat.security;

import kg.alatoo.sayakat.user.Role;

public record AuthUser(Long id, String email, Role role) {
}
