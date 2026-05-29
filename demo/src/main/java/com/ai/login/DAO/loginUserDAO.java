package com.ai.login.DAO;
import com.ai.login.DTO.User;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface loginUserDAO extends JpaRepository<User, Long>{
	Optional<User> findByUsername(String username);

    User findByEmail(String email);

    User findByUsernameAndPassword(String username, String password);

}
