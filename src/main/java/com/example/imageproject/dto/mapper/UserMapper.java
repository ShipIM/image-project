package com.example.imageproject.dto.mapper;

import com.example.imageproject.dto.user.AuthenticateUserRequest;
import com.example.imageproject.dto.user.AuthenticateUserResponse;
import com.example.imageproject.model.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User mapToUser(AuthenticateUserRequest dto);

    AuthenticateUserResponse mapToResponse(User user, String access);

}
