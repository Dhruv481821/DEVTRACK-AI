package com.devtrack.github.repository;

import com.devtrack.github.entity.RepoSnapshot;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RepoSnapshotRepository extends JpaRepository<RepoSnapshot, UUID> {

  List<RepoSnapshot> findByGithubConnectionId(UUID githubConnectionId);
}
