package com.example.NewProject.Repository;


import com.example.NewProject.Entity.DictModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface DictRepo extends JpaRepository<DictModel, String> {
    Optional<DictModel> findByNounmodifier(String nounmodifier);

    List<DictModel> findBynoun(String noun);

    @Query("SELECT d.noun FROM DictModel d WHERE d.noun IS NOT NULL")
    List<String> findAllNouns();

    @Query("SELECT d.modifier FROM DictModel d WHERE d.modifier IS NOT NULL")
    List<String> findAllModifier();

    @Query("""
        SELECT d.nounmodifier
        FROM DictModel d
        WHERE UPPER(d.noun) = UPPER(:noun)
        AND UPPER(d.modifier) = UPPER(:modifier)
        """)
    String findNounModifier(String noun, String modifier);
}