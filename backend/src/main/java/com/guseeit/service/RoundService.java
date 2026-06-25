package com.guseeit.service;

import com.guseeit.domain.Round;
import com.guseeit.domain.RoundStatus;
import com.guseeit.repository.RoundRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class RoundService {

    private final RoundRepository roundRepository;

    public RoundService(RoundRepository roundRepository) {
        this.roundRepository = roundRepository;
    }

    public Page<Round> list(String dynasty, String status, int limit, int offset) {
        int size = Math.min(Math.max(limit, 1), 200);
        int page = offset / size;
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        if (dynasty != null && !dynasty.trim().isEmpty() && status != null && !status.trim().isEmpty()) {
            return roundRepository.findByDynastyAndStatus(dynasty, RoundStatus.valueOf(status), pageable);
        }
        if (dynasty != null && !dynasty.trim().isEmpty()) {
            return roundRepository.findByDynasty(dynasty, pageable);
        }
        if (status != null && !status.trim().isEmpty()) {
            return roundRepository.findByStatus(RoundStatus.valueOf(status), pageable);
        }
        return roundRepository.findAll(pageable);
    }

    public long count(String dynasty) {
        if (dynasty != null && !dynasty.trim().isEmpty()) {
            return roundRepository.countByDynasty(dynasty);
        }
        return roundRepository.count();
    }
}
