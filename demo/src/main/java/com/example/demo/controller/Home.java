package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
@RequestMapping
public class Home {
	
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
	public String userLogin(Model model) {	   
	    return "HomePage"; // looks for 
	}
}
