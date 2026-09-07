package com.devtrack.dsa.repository;

import com.devtrack.dsa.entity.DsaProblem;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DsaProblemRepository extends JpaRepository<DsaProblem, UUID> {

  List<DsaProblem> findByUserIdOrderByCreatedAtDesc(UUID userId);
}
