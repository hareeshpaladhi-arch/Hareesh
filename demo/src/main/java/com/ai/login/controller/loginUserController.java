package com.ai.login.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.web.bind.annotation.*;

import com.ai.login.DTO.*;
import com.ai.login.service.loginUserService;
import com.ai.login.util.JwtUtil;

@RestController
@RequestMapping("/auth")
@CrossOrigin
public class loginUserController {

    @Autowired
    private loginUserService userService;

    @Autowired
    private AuthenticationManager authManager;

    @Autowired
    private JwtUtil jwtUtil;

    // 🔐 LOGIN API
    @PostMapping("/checkUserLogin")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {

        try {
            authManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
            );

            // ✅ Generate JWT
            String token = jwtUtil.generateToken(request.getUsername());

            return ResponseEntity.ok(Map.of(
                    "message", "Login successful",
                    "token", token
            ));

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body(
                    new ApiResponse("Invalid username or password", false)
            );
        }
    }

    // 🔐 REGISTER API
    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@RequestBody User user) {

        ApiResponse response = userService.register(user);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}