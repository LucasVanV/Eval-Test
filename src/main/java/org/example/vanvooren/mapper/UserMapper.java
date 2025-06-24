package org.example.vanvooren.mapper;

import org.example.vanvooren.dto.UserDTO;
import org.example.vanvooren.model.User;

public class UserMapper {
    public static UserDTO toDTO(User user) {
        if (user == null) {
            return null;
        }
        return UserDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }
}
