package com.ai.login.DAO;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ai.login.DTO.OtpEntity;

public interface OtpRepository extends JpaRepository<OtpEntity, Long> {

    Optional<OtpEntity> findTopByEmailOrderByIdDesc(String email);

}
