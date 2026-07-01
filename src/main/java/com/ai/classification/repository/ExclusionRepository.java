package com.ai.classification.repository;

import com.ai.classification.entity.Exclusion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExclusionRepository extends JpaRepository<Exclusion, Long> {
}
