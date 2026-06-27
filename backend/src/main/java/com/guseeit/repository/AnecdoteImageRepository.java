package com.guseeit.repository;

import com.guseeit.domain.AnecdoteImage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnecdoteImageRepository extends JpaRepository<AnecdoteImage, Long> {

    Page<AnecdoteImage> findByDynastyId(Integer dynastyId, Pageable pageable);

    Page<AnecdoteImage> findByDynastyIdIn(List<Integer> dynastyIds, Pageable pageable);
}
