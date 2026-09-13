package com.devtrack.github.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

/**
 * @ManyToOne to GithubConnection is intra-module — same pattern as StudyTask -> StudyPlan.
 * A cached sync snapshot, not live data (FR-GH-02).
 */
@Entity
@Table(name = "repo_snapshot")
@Getter
@Setter
@NoArgsConstructor
public class RepoSnapshot {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "github_connection_id", nullable = false)
    private GithubConnection githubConnection;

    @Column(name = "repo_name", nullable = false)
    private String repoName;

    @Column(nullable = false)
    private int stars = 0;

    @Column(name = "primary_language")
    private String primaryLanguage;

    @Column(name = "commits_last_90_days", nullable = false)
    private int commitsLast90Days = 0;

    @Column(name = "synced_at", nullable = false)
    private Instant syncedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RepoSnapshot other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}