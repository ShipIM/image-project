package com.example.imageapi.dto.mapper;

import com.example.imageapi.dto.rest.user.AuthenticateUserRequest;
import com.example.imageapi.dto.rest.user.AuthenticateUserResponse;
import com.example.imageapi.model.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User mapToUser(AuthenticateUserRequest dto);

    AuthenticateUserResponse mapToResponse(User user, String access);

}
