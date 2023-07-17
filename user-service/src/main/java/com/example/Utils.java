package com.example;

import com.example.dto.CreateUserRequest;
import com.example.dto.GetUserResponse;
import com.example.model.User;

public class Utils {

    public static User convertUserCreateRequest(CreateUserRequest request) {
        return User.builder()
                .name(request.getName())
                .age(request.getAge())
                .email(request.getEmail())
                .phone(request.getPhone())
                .build();
    }

    public static GetUserResponse convertToUserResponse(User user) {
        return GetUserResponse.builder()
                .name(user.getName())
                .age(user.getAge())
                .email(user.getEmail())
                .phone(user.getPhone())
                .createdOn(user.getCreatedOn())
                .updatedOn(user.getUpdatedOn())
                .build();
    }
}
