package io.github.alexisTrejo11.construction.company.modules.user.shared.domain;

import io.github.alexisTrejo11.construction.company.shared.persistence.AbstractJpaEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "invitations")
@Getter
@Setter
@NoArgsConstructor
public class Invitation extends AbstractJpaEntity {
  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false, unique = true)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "created_by_user_id")
  private User createdBy;

  @Column(name = "token_hash", nullable = false, unique = true, length = 255)
  private String tokenHash;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private InvitationStatus status = InvitationStatus.PENDING;

  @Column(name = "expires_at", nullable = false)
  private Instant expiresAt;


  @Column(name = "accepted_at")
  private Instant acceptedAt;
}
