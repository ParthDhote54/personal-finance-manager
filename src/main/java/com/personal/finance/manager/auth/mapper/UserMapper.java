package com.personal.finance.manager.auth.mapper;

import com.personal.finance.manager.auth.dto.UserResponseDTO;
import com.personal.finance.manager.user.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponseDTO toDTO(User user);
}
