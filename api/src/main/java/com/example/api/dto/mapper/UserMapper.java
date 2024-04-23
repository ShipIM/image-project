package com.example.api.dto.mapper;

import com.example.api.dto.rest.user.AuthenticateUserRequest;
import com.example.api.dto.rest.user.AuthenticateUserResponse;
import com.example.api.model.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User mapToUser(AuthenticateUserRequest dto);

    AuthenticateUserResponse mapToResponse(User user, String access);

}
