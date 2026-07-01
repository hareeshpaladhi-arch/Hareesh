package com.ai.classification.repository;

import com.ai.classification.entity.FirstPref;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface FirstPrefRepository extends JpaRepository<FirstPref, Long> {
	 @Query(value = "SELECT * FROM first_pref", nativeQuery = true)
	    List<Object[]> findAllData();
}
