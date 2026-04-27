package com.ai.login.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ai.login.DAO.loginUserDAO;
import com.ai.login.DTO.User;

@Service
public class loginUserService {

    @Autowired
    private loginUserDAO userDao;

    public String checkUserLogin(String userName, String password) {

        User user = userDao.findByUsername(userName);

        if (user == null) {
            return "User not found";
        }

        // ✅ FIX HERE
        if (!user.getPassword().equals(password)) {
            return "Invalid password";
        }

        return "Login successful";
    }
}