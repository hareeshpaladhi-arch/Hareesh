package com.ai.classification.repository;

import com.ai.classification.entity.ThirdPref;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ThirdPrefRepository extends JpaRepository<ThirdPref, Long> {

	@Query(value = "SELECT * FROM THIRD_PREF", nativeQuery = true)
    List<Object[]> findAllData();
}
