package com.ai.login.controller;

import java.util.Map;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.web.bind.annotation.*;

import com.ai.login.DTO.*;
import com.ai.login.service.loginUserService;
import com.ai.login.util.JwtUtil;

@RestController
@RequestMapping("/auth")
public class loginUserController {

    @Autowired
    private loginUserService userService;

    @Autowired
    private AuthenticationManager authManager;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/checkUserLogin")
    public ResponseEntity<?> login(@RequestBody LoginRequest request,
                                  HttpServletResponse response) {

        try {
            authManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.getUsername(),
                    request.getPassword()
                )
            );

            String token = jwtUtil.generateToken(request.getUsername());

            // 🔥 FIX: Proper cookie with SameSite
            String cookie = "token=" + token +
                    "; Path=/" +
                    "; Max-Age=3600" +
                    "; HttpOnly" +
                    "; SameSite=Lax";   // ✅ important

            response.setHeader("Set-Cookie", cookie);

            return ResponseEntity.ok(Map.of(
                "message", "Login successful"
            ));

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body(
                new ApiResponse("Invalid username or password", false)
            );
        }
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@RequestBody User user) {

        ApiResponse response = userService.register(user);

        return response.isSuccess()
                ? ResponseEntity.ok(response)
                : ResponseEntity.badRequest().body(response);
    }
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {

        // 🔥 delete cookie
        String cookie = "token=; Path=/; Max-Age=0; HttpOnly; SameSite=Lax";
        response.setHeader("Set-Cookie", cookie);

        return ResponseEntity.ok().build();
    }
}