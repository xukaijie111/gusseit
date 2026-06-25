package com.guseeit.repository;

import com.guseeit.domain.UserHistory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserHistoryRepository extends JpaRepository<UserHistory, Long> {
    List<UserHistory> findByUserIdOrderByAnsweredAtDesc(Long userId, Pageable pageable);

    int countByUserId(Long userId);

    @Query("SELECT h.roundId FROM UserHistory h WHERE h.userId = :userId")
    List<String> findRoundIdsByUserId(@Param("userId") Long userId);
}
