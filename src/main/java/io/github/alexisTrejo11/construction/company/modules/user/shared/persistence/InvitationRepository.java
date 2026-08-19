package io.github.alexisTrejo11.construction.company.modules.user.shared.persistence;

import io.github.alexisTrejo11.construction.company.modules.user.shared.domain.Invitation;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvitationRepository extends JpaRepository<Invitation, Long> {
  Optional<Invitation> findByTokenHash(String tokenHash);
}
