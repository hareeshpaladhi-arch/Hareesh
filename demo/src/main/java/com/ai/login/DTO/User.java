package com.ai.login.DTO;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Entity
@Table(name = "USER_DETAILS")
public class User {

	@Id
	@Column(name = "ID")
	private String id;

    public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	@Column(name = "USER_NAME")   // DB column
    private String username;      // Java field (must match repository)

	@Column(name = "EMAIL")
	@NotBlank(message = "Email cannot be empty")
	@Pattern(
	    regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$",
	    message = "Invalid email format"
	)
	private String email;

    @Column(name = "PASSWORD")
    private String password;

    // Getters
    public String getUsername() { return username; }
    public String getPassword() { return password; }

    // Setters
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
	public String getId() {
		return id;
	}
	public void setId(String randomId) {
		this.id = randomId;
	}
    
}