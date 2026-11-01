package kg.alatoo.sayakat.security;

import kg.alatoo.sayakat.entity.enums.Role;

public record AuthUser(Long id, String email, Role role) {
}
