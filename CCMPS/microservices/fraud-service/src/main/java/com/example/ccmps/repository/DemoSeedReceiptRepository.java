package com.example.ccmps.repository;

import com.example.ccmps.entity.DemoSeedReceipt;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DemoSeedReceiptRepository extends JpaRepository<DemoSeedReceipt, UUID> {
}
