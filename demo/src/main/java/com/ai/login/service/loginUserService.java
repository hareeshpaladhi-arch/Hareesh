package com.ai.login.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.ai.login.DAO.loginUserDAO;
import com.ai.login.DTO.ApiResponse;
import com.ai.login.DTO.User;

@Service
public class loginUserService {

    @Autowired
    private loginUserDAO userDao;

    @Autowired
    private PasswordEncoder encoder;

    public ApiResponse register(User user) {

        try {

            // 🔍 Basic validation
            if (user.getUsername() == null || user.getUsername().isEmpty()) {
                return new ApiResponse("Username is required", false);
            }

            if (user.getPassword() == null || user.getPassword().length() < 6) {
                return new ApiResponse("Password must be at least 6 characters", false);
            }
            Optional<User> userData=userDao.findByUsername(user.getUsername());

            // 🔍 Check existing user
            if (userData != null && !userData.isEmpty()) {
                return new ApiResponse("User already exists", false);
            }

            // 🔐 Encrypt password
            user.setPassword(encoder.encode(user.getPassword()));
            user.setEmail(user.getEmail());
            String randomId = UUID.randomUUID().toString();
            user.setId(randomId);

            // 💾 Save
            userDao.save(user);

            return new ApiResponse("User registered successfully", true);

        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            // 🔥 Handles duplicate username at DB level
            return new ApiResponse("Username already exists", false);

        } catch (Exception e) {
            return new ApiResponse("Registration failed", false);
        }
    }
}