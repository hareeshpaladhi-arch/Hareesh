package com.ai.login.controller;

import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;


import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.ai.login.DTO.ApiResponse;
import com.ai.login.DTO.LoginRequest;
import com.ai.login.DTO.User;


import com.ai.login.service.loginUserService;
import com.ai.login.util.JwtUtil;

import io.jsonwebtoken.lang.Arrays;


@RestController
@RequestMapping("/auth")
public class LoginUserController {

	@Autowired
	private loginUserService userService;

	@Autowired
	private AuthenticationManager authManager;

	@Autowired
	private JwtUtil jwtUtil;


	// ✅ LOGIN
	@PostMapping("/checkUserLogin")
	public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpServletResponse response) {
		

		try {
			authManager.authenticate(
					new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

			String token = jwtUtil.generateToken(request.getUsername(),request.getEmail());

			String cookie = "token=" + token + "; Path=/" + "; Max-Age=3600" + "; HttpOnly" + "; SameSite=Lax";

			response.setHeader("Set-Cookie", cookie);
			Map<String, String> responseMap = new HashMap<>();
			responseMap.put("message", "Login successful");
			return ResponseEntity.ok(responseMap);

		} catch (BadCredentialsException e) {
			return ResponseEntity.status(401).body(new ApiResponse("Invalid username or password", false));
		}
	}

	// ✅ REGISTER
	@PostMapping("/register")
	public ResponseEntity<ApiResponse> register(@RequestBody User user) {

		ApiResponse response = userService.register(user);

		return response.isSuccess() ? ResponseEntity.ok(response) : ResponseEntity.badRequest().body(response);
	}

	// ✅ LOGOUT
	@PostMapping("/logout")
	public ResponseEntity<?> logout(HttpServletResponse response) {

		String cookie = "token=; Path=/; Max-Age=0; HttpOnly; SameSite=Lax";
		response.setHeader("Set-Cookie", cookie);

		return ResponseEntity.ok().build();
	}

	

}