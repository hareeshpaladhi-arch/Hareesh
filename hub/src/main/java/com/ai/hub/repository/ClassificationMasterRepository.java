package com.ai.hub.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ai.hub.dto.ClassificationMaster;



@Repository
public interface ClassificationMasterRepository
        extends JpaRepository<ClassificationMaster, String> {

    Optional<ClassificationMaster> findByClassNameIgnoreCase(String className);

    Optional<ClassificationMaster> findByShortdescriptionIgnoreCase(String shortdescription);

    Optional<ClassificationMaster> findByLongdescriptionIgnoreCase(String longdescription);

}
