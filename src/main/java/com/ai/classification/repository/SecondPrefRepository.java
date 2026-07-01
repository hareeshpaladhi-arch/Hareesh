package com.ai.classification.repository;

import com.ai.classification.entity.SecondPref;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SecondPrefRepository extends JpaRepository<SecondPref, Long> {
	 @Query(value = "SELECT * FROM SECOND_PREF", nativeQuery = true)
	    List<Object[]> findAllData();
}
