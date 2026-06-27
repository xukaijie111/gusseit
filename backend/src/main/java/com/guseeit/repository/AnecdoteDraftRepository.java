package com.guseeit.repository;

import com.guseeit.domain.AnecdoteDraft;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnecdoteDraftRepository extends JpaRepository<AnecdoteDraft, Long> {

    Page<AnecdoteDraft> findByDynastyId(Integer dynastyId, Pageable pageable);
}
