package com.guseeit.repository;

import com.guseeit.domain.Round;
import com.guseeit.domain.RoundStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RoundRepository extends JpaRepository<Round, String> {

    List<Round> findByDynastyOrderByCreatedAtDesc(String dynasty);

    Page<Round> findByDynasty(String dynasty, Pageable pageable);

    Page<Round> findByStatus(RoundStatus status, Pageable pageable);

    Page<Round> findByDynastyAndStatus(String dynasty, RoundStatus status, Pageable pageable);

    Page<Round> findByDynastyInAndStatus(List<String> dynasties, RoundStatus status, Pageable pageable);

    long countByDynasty(String dynasty);

    @Query("SELECT r FROM Round r WHERE r.dynasty IN :dynasties ORDER BY r.createdAt DESC")
    List<Round> findByDynasties(@Param("dynasties") List<String> dynasties);
}
