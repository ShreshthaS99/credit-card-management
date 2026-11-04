package com.example.ccmps.repository;
import com.example.ccmps.entity.RewardEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RewardEntryRepository
        extends JpaRepository<RewardEntry, UUID> {

    List<RewardEntry>
    findByAccountIdOrderByCreatedAtDesc(UUID accountId);
}