package com.ai.login.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.ai.login.DTO.*;
import com.ai.login.service.loginUserService;

@RestController
public class loginUserController {

    @Autowired
    private loginUserService UserService;

    @PostMapping("/checkUserLogin")
    public String checkUserLogin(@RequestBody LoginRequest request) {

        return UserService.checkUserLogin(
                request.getUsername(),
                request.getPassword()
        );
    }
}