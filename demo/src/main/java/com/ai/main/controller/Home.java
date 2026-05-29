package com.ai.main.controller;

import java.util.List;
import java.util.Optional;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ai.login.DTO.User;
import com.ai.login.service.loginUserService;
import com.ai.login.util.JwtUtil;


@Controller
@RequestMapping
public class Home {
	@Autowired
	JwtUtil jwtUtil;
	@Autowired
	loginUserService loginService;
	@GetMapping("/")
	public String home(Model model) {
	    model.addAttribute("message", "Welcome!");
	    return "Home"; // looks for index.html / index.jsp
	}
	@GetMapping("/login")
	public String login(Model model) {
		System.out.print("Login");
	    return "LoginBasic"; // looks for 
	}
	@GetMapping("/userLogin")
	public String userLogin(Model model,HttpServletRequest request) {
		String token = null;
	    if (request.getCookies() != null) {
	        for (Cookie cookie : request.getCookies()) {
	            if (cookie.getName().equals("token")) {
	                token = cookie.getValue();
	                break;
	            }
	        }
	    }
	    if (token != null) {
	        String username = jwtUtil.extractUsername(token);
	        Optional<User> loginlist=loginService.getUserDetails(username);
	        model.addAttribute("userName", username);
	        if (loginlist.isPresent()) {
	            model.addAttribute("email", loginlist.get().getEmail());
	        };
	    }
	    return "HomePage"; // looks for 
	}
}
