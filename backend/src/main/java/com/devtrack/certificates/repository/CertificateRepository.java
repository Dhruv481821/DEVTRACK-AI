package com.devtrack.certificates.repository;

import com.devtrack.certificates.entity.Certificate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CertificateRepository extends JpaRepository<Certificate, UUID> {

  List<Certificate> findByUserIdOrderByIssueDateDesc(UUID userId);
}
