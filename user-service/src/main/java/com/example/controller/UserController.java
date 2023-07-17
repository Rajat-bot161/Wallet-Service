package com.example.controller;

import com.example.Utils;
import com.example.dto.CreateUserRequest;
import com.example.dto.GetUserResponse;
import com.example.model.User;
import com.example.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
public class UserController {

    @Autowired
    UserService userService;

    @PostMapping("/user")
    public void createUser(@RequestBody @Valid CreateUserRequest request) throws JsonProcessingException {
             userService.createUser(Utils.convertUserCreateRequest(request));
    }

    @GetMapping("/user/{userId}")
    public GetUserResponse getUser(@PathVariable("userId") int userId) throws Exception {
        User user = userService.get(userId);
        return Utils.convertToUserResponse(user);
    }

    @GetMapping("/user/phone/{phone}")
    public GetUserResponse getUserByPhone(@PathVariable("phone") String phone) throws Exception {
        User user = userService.getByPhone(phone);
        return Utils.convertToUserResponse(user);
    }
}
