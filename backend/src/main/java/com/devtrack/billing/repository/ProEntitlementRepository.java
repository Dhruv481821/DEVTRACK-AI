package com.devtrack.billing.repository;

import com.devtrack.billing.entity.ProEntitlement;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProEntitlementRepository extends JpaRepository<ProEntitlement, UUID> {}
