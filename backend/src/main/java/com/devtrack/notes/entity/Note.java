package com.devtrack.notes.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

/**
 * user_id is a plain scalar, not a @ManyToOne to AppUser — notes is a peer module to auth, per the
 * cross-module boundary rule established for `notification` in Phase 0
 * (/docs/07_Backend_Architecture.md §4).
 *
 * <p>content is jsonb, storing Tiptap's native JSON document format (/docs/11_Component_Library.md
 * §3), not flattened HTML/markdown — preserves full rich-text structure for re-editing.
 *
 * <p>@SQLRestriction enforces the soft-delete convention (NFR-REL-01) at the ORM level — every
 * query against this entity automatically excludes soft-deleted rows, so no repository method has
 * to remember to filter deleted_at itself. This is Hibernate 6.3+'s replacement for the
 * deprecated @Where annotation.
 */
@Entity
@Table(name = "note")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
public class Note {

  @Id @GeneratedValue @UuidGenerator private UUID id;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(nullable = false)
  private String title;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(nullable = false)
  private Map<String, Object> content = Map.of();

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
      name = "note_tag",
      joinColumns = @JoinColumn(name = "note_id"),
      inverseJoinColumns = @JoinColumn(name = "tag_id"))
  private Set<Tag> tags = new HashSet<>();

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @Column(name = "deleted_at")
  private Instant deletedAt;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Note other)) return false;
    return id != null && id.equals(other.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
