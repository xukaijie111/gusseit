package com.guseeit.repository;

import com.guseeit.domain.AnecdoteData;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnecdoteDataRepository extends JpaRepository<AnecdoteData, Long> {

    List<AnecdoteData> findByAnecdoteName(String anecdoteName);

    Page<AnecdoteData> findByDynastyId(Integer dynastyId, Pageable pageable);
}
