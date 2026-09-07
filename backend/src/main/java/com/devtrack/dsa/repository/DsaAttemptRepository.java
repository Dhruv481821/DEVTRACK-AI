package com.devtrack.dsa.repository;

import com.devtrack.dsa.entity.DsaAttempt;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DsaAttemptRepository extends JpaRepository<DsaAttempt, UUID> {

  List<DsaAttempt> findByDsaProblemId(UUID dsaProblemId);
}
