package com.ai.login.DTO;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

public class LoginRequest {
	@NotBlank(message = "Username cannot be empty")
    private String username;
	@NotBlank(message = "Password cannot be empty")
	
	@Pattern(
	        regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@#$%^&+=!]).{8,20}$",
	        message = "Password must be 8-20 characters, include uppercase, lowercase, number and special character"
	    )
    private String password;
	
	
	@NotBlank(message = "Email cannot be empty")
	@Pattern(
	    regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$",
	    message = "Invalid email format"
	)
	private String email;
	
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}

    public String getUsername() { return username; }
    public String getPassword() { return password; }

    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
}
