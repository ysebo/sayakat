package kg.alatoo.sayakat.dto.response;

import kg.alatoo.sayakat.entity.User;
import kg.alatoo.sayakat.entity.enums.Role;

public record UserResponse(Long id, String email, String fullName, Role role) {

    public static UserResponse of(User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getFullName(), user.getRole());
    }
}
