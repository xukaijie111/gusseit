package com.guseeit.repository;

import com.guseeit.domain.GenerationJob;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GenerationJobRepository extends JpaRepository<GenerationJob, String> {

    List<GenerationJob> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
