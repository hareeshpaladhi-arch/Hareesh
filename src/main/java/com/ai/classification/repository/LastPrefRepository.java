package com.ai.classification.repository;

import com.ai.classification.entity.LastPref;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface LastPrefRepository extends JpaRepository<LastPref, Long> {
	@Query(value = "SELECT * FROM LAST_PREF", nativeQuery = true)
    List<Object[]> findAllData();
}
